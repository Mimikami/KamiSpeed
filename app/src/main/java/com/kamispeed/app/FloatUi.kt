package com.kamispeed.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import com.kamispeed.app.databinding.PanelSpeedBinding
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 悬浮 UI 状态机：
 *   - panel：完整变速面板（滑杆 / 开关 / ± / ✕）
 *   - ball ：半圆小球，吸附屏幕左/右边缘，可拖动，点一下展开回面板
 *   - KamiSpeed 自己的页面在前台时自动隐藏；游戏在前台时才显示
 *
 * 显示位置跟随**游戏所在的 Display**：
 * MuMu/多屏设备会给每个 task 分配独立虚拟屏幕，如果悬浮窗还加在默认屏幕上，
 * 就会出现「面板页面看得到、游戏里没有」的情况。所以这里在游戏 Activity
 * 获得窗口焦点时记录它的 Display，用 createDisplayContext 把悬浮窗加到那块屏幕上。
 */
object FloatUi {

    var enabled = false
        private set

    /** 当前处于「小球」形态还是「面板」形态 */
    private var collapsed = true

    /** 用户当前聚焦的任务是不是 KamiSpeed 自己的页面 / 游戏 */
    private var focusedOwn = false
    private var focusedGuest = false

    /** 游戏窗口当前是否持有焦点（回桌面/切走时置 false，用于隐藏悬浮窗） */
    private var guestWindowFocused = false

    /** 游戏所在的 display context（悬浮窗要挂到它上面） */
    @Volatile
    private var gameDisplayContext: Context? = null

    /** 当前聚焦的游戏包名（位置按应用分别记忆） */
    private var currentGuestPackage: String? = null

    private var panel: FloatPanel? = null
    private var ball: FloatBall? = null

    private var appContext: Context? = null

    private val pollHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val pollRunnable = object : Runnable {
        override fun run() {
            if (enabled) {
                queryFocused()
                pollHandler.postDelayed(this, 800)
            }
        }
    }

    fun attach(context: Context) {
        val app = context.applicationContext
        appContext = app
        if (panel == null) panel = FloatPanel(app)
        if (ball == null) ball = FloatBall(app)
    }

    fun setEnabled(on: Boolean) {
        enabled = on
        pollHandler.removeCallbacks(pollRunnable)
        if (on) {
            queryFocused()
            pollHandler.postDelayed(pollRunnable, 800)
        }
        refresh()
    }

    /** 面板 ✕ → 收成小球 */
    fun collapseToBall() {
        collapsed = true
        refresh()
    }

    /** 展开后尚未锚定（等待包名/位置就绪） */
    private var pendingAnchor = false

    /** 小球被点 → 展开面板（面板锚定在小球附近） */
    fun expandToPanel() {
        collapsed = false
        pendingAnchor = true
        refresh()
    }

    /** 游戏窗口焦点变化（由容器 ContainerLifecycle 转发）→ 记下 display 并立即查一次 */
    fun onGuestWindowFocus(activity: Activity, hasFocus: Boolean) {
        // 容器的焦点钩子对所有 Activity 生效，这里忽略自己的页面（MainActivity 等），
        // 只认游戏窗口 —— 否则 MainActivity 先聚焦会把包名/display 记成宿主自己
        if (activity.javaClass.name.startsWith("com.kamispeed.app")) return
        guestWindowFocused = hasFocus
        if (hasFocus) {
            // 记下游戏包名（位置按应用分别记忆）
            currentGuestPackage = try {
                activity.applicationInfo.packageName
            } catch (t: Throwable) {
                null
            }
            try {
                val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    activity.display
                } else {
                    @Suppress("DEPRECATION") activity.windowManager.defaultDisplay
                }
                if (display != null) {
                    gameDisplayContext = activity.createDisplayContext(display)
                }
            } catch (t: Throwable) {
                // 保持旧值
            }
        }
        if (enabled) queryFocused()
    }

    /**
     * 用「最前台的自己的任务」判断用户在看谁。
     *
     * MuMu/多屏下每个 display 的窗口各自持有焦点，单看某个窗口的 focus 判断不了
     * 用户在看哪个。我们的两个任务（MainActivity 页 / 游戏 task）都属于自己，
     * `ActivityManager.getRunningTasks(1)` 返回的第一条就是用户当前正在看的那个；
     * 若拿不到再退回反射调 ActivityTaskManager.getFocusedRootTaskInfo()。
     */
    @Suppress("DEPRECATION")
    private fun queryFocused() {
        val ctx = appContext ?: return
        var top: android.content.ComponentName? = null

        try {
            val am = ctx.getSystemService(android.app.ActivityManager::class.java)
            top = am?.getRunningTasks(2)?.firstOrNull { it.topActivity != null }?.topActivity
        } catch (t: Throwable) {
            android.util.Log.w("SM-FloatUi", "getRunningTasks failed", t)
        }

        if (top == null) {
            top = try {
                val atmClass = Class.forName("android.app.ActivityTaskManager")
                val getService = atmClass.getDeclaredMethod("getService")
                getService.isAccessible = true
                val atm = getService.invoke(null)
                val getFocused = atmClass.getDeclaredMethod("getFocusedRootTaskInfo")
                getFocused.isAccessible = true
                val info = if (java.lang.reflect.Modifier.isStatic(getFocused.modifiers)) {
                    getFocused.invoke(null)
                } else {
                    getFocused.invoke(atm)
                }
                info?.javaClass?.getField("topActivity")?.get(info) as? android.content.ComponentName
            } catch (t: Throwable) {
                android.util.Log.w("SM-FloatUi", "focusedRootTaskInfo failed", t)
                null
            }
        }

        if (top == null) {
            focusedOwn = false
            focusedGuest = false
            refresh()
            return
        }

        focusedOwn = top.className == "com.kamispeed.app.MainActivity"
        focusedGuest = top.packageName == ctx.packageName &&
            top.className.startsWith("com.kamispeed.container.StubActivity")
        refresh()
    }

    fun syncSpeed(speed: Float, on: Boolean) {
        panel?.sync(speed, on)
        ball?.sync(speed, on)
    }

    fun destroy() {
        pollHandler.removeCallbacks(pollRunnable)
        panel?.hide()
        ball?.hide()
        panel = null
        ball = null
    }

    /** 只在「已启用 + 游戏窗口有焦点 + 不是自己的页面」时显示 */
    private fun refresh() {
        val show = enabled && guestWindowFocused && !focusedOwn
        val ctx = if (show) gameDisplayContext else null
        android.util.Log.i(
            "SM-FloatUi",
            "refresh show=$show enabled=$enabled guest=$focusedGuest own=$focusedOwn " +
                "collapsed=$collapsed displayCtx=${ctx != null}"
        )
        // 展开面板时锚定到悬浮球位置；包名尚未就绪则等待，就绪后锚定一次
        if (show && !collapsed && pendingAnchor) {
            val anchor = ball?.anchorForPanel(currentGuestPackage)
            if (anchor != null || currentGuestPackage != null) {
                pendingAnchor = false
                anchor?.let { (ax, ay) -> panel?.setAnchor(ax, ay) }
            }
        }
        panel?.let { if (show && !collapsed) it.showOn(ctx) else it.hide() }
        ball?.let { if (show && collapsed) it.showOn(ctx, currentGuestPackage) else it.hide() }
    }
}

/* ------------------------------------------------------------------ */
/*  完整面板                                                          */
/* ------------------------------------------------------------------ */

@SuppressLint("ClickableViewAccessibility", "InflateParams")
class FloatPanel(private val context: Context) {

    private val themed = ContextThemeWrapper(context, R.style.Theme_KamiSpeed)
    private var wm: WindowManager? = null
    private var binding: PanelSpeedBinding? = null
    private var params: WindowManager.LayoutParams? = null

    private var downX = 0f
    private var downY = 0f
    private var startX = 0
    private var startY = 0

    /** 面板锚点（展开时贴小球附近）；null 用默认位置 */
    private var anchorX: Int? = null
    private var anchorY: Int? = null

    /** displayContext 为 null 时退回应用默认屏幕 */
    fun showOn(displayContext: Context?) {
        val target = displayContext ?: context
        val newWm = target.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (binding != null && wm === newWm) return
        // 换了屏幕 / 之前挂在别处：先摘掉再加
        if (binding != null) hide()

        val b = PanelSpeedBinding.inflate(LayoutInflater.from(themed))
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = anchorX ?: 24
            y = anchorY ?: 320
        }

        b.sbPanel.max = ((SpeedState.MAX - SpeedState.MIN) * 10).roundToInt()
        b.sbPanel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                SpeedState.setSpeed(SpeedState.MIN + progress / 10f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        b.swPanel.setOnCheckedChangeListener { _, checked -> SpeedState.setEnabled(checked) }
        b.btnMinus.setOnClickListener {
            SpeedState.setSpeed(((SpeedState.speed - 0.5f) * 10).roundToInt() / 10f)
        }
        b.btnPlus.setOnClickListener {
            SpeedState.setSpeed(((SpeedState.speed + 0.5f) * 10).roundToInt() / 10f)
        }

        // ✕ 不再是直接消失，而是收成半圆小球
        b.btnPanelClose.setOnClickListener { FloatUi.collapseToBall() }
        b.tvPanelTitle.setOnTouchListener { _, event -> onDrag(event, b) }

        try {
            newWm.addView(b.root, lp)
            wm = newWm
            binding = b
            params = lp
            // 测量面板实际尺寸，clamp 到屏幕内（锚定小球时避免面板跑出屏幕）
            val sw = target.resources.displayMetrics.widthPixels
            val sh = target.resources.displayMetrics.heightPixels
            b.root.measure(
                android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED),
                android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED),
            )
            val pw = b.root.measuredWidth
            val ph = b.root.measuredHeight
            lp.x = lp.x.coerceIn(0, (sw - pw).coerceAtLeast(0))
            lp.y = lp.y.coerceIn(0, (sh - ph).coerceAtLeast(0))
            newWm.updateViewLayout(b.root, lp)
            sync(SpeedState.speed, SpeedState.enabled)
        } catch (t: Throwable) {
            android.util.Log.w("SM-FloatUi", "panel addView failed", t)
            binding = null
            params = null
        }
    }

    /** 设置展开时的锚点（小球位置）；若面板已显示则立即重定位 */
    fun setAnchor(x: Int, y: Int) {
        anchorX = x
        anchorY = y
        val b = binding ?: return
        val m = wm ?: return
        val lp = params ?: return
        val sw = b.root.context.resources.displayMetrics.widthPixels
        val sh = b.root.context.resources.displayMetrics.heightPixels
        b.root.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED),
            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED),
        )
        val pw = b.root.measuredWidth
        val ph = b.root.measuredHeight
        lp.x = x.coerceIn(0, (sw - pw).coerceAtLeast(0))
        lp.y = y.coerceIn(0, (sh - ph).coerceAtLeast(0))
        try {
            m.updateViewLayout(b.root, lp)
        } catch (_: Throwable) {
        }
    }

    fun hide() {
        val b = binding ?: return
        val manager = wm ?: return
        try {
            manager.removeView(b.root)
        } catch (_: Throwable) {
        }
        binding = null
        params = null
        wm = null
    }

    fun sync(speed: Float, on: Boolean) {
        val b = binding ?: return
        b.tvPanelSpeed.text = String.format("%.1fx", speed)
        b.sbPanel.progress = ((speed - SpeedState.MIN) * 10).roundToInt()
        if (b.swPanel.isChecked != on) b.swPanel.isChecked = on
        b.root.alpha = if (on) 1f else 0.55f
    }

    private fun onDrag(event: MotionEvent, b: PanelSpeedBinding): Boolean {
        val lp = params ?: return false
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                downY = event.rawY
                startX = lp.x
                startY = lp.y
                true
            }
            MotionEvent.ACTION_MOVE -> {
                lp.x = startX + (event.rawX - downX).roundToInt()
                lp.y = startY + (event.rawY - downY).roundToInt()
                try {
                    wm?.updateViewLayout(b.root, lp)
                } catch (_: Throwable) {
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                if (abs(event.rawX - downX) < 8 && abs(event.rawY - downY) < 8) {
                    b.root.performClick()
                }
                true
            }
            else -> false
        }
    }

    private fun overlayType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
}

/* ------------------------------------------------------------------ */
/*  半圆小球：吸附边缘 / 可拖动 / 点击展开                              */
/* ------------------------------------------------------------------ */

@SuppressLint("ClickableViewAccessibility")
class FloatBall(private val context: Context) {

    private var wm: WindowManager? = null
    private val density = context.resources.displayMetrics.density
    private val size = (54 * density).roundToInt()

    private val baseTextSize = 11f
    private val minTextSize = 7f

    private var view: TextView? = null
    private var params: WindowManager.LayoutParams? = null

    private var downX = 0f
    private var downY = 0f
    private var startX = 0
    private var startY = 0
    private var moved = false

    /** 当前是否处于「半圆吸附」形态（吸附时文字要放进露出的半圆里） */
    private var snapped = false

    /** 记住上次吸附位置（跨收起/展开保留） */
    private var lastX: Int? = null
    private var lastY: Int? = null

    /** 当前游戏包名（位置按应用分别持久化） */
    private var guestPackage: String? = null

    private fun posPrefs() = context.getSharedPreferences("kamispeed_float_pos", Context.MODE_PRIVATE)

    /** 从持久化读该应用的「边 + y」（x 由屏幕宽度换算） */
    private fun loadPos(pkg: String): Pair<String, Int>? {
        val p = posPrefs()
        val side = p.getString("$pkg.side", null) ?: return null
        val y = p.getInt("$pkg.y", -1)
        return if (y < 0) null else Pair(side, y)
    }

    private fun savePos(pkg: String, x: Int, y: Int, sw: Int) {
        val centerX = x + size / 2
        val side = if (centerX < sw / 2) "L" else "R"
        posPrefs().edit()
            .putString("$pkg.side", side)
            .putInt("$pkg.y", y)
            .apply()
    }

    fun showOn(displayContext: Context?, guestPackage: String?) {
        val target = displayContext ?: context
        val newWm = target.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (view != null && wm === newWm) return
        if (view != null) hide()
        this.guestPackage = guestPackage

        val sw = target.resources.displayMetrics.widthPixels
        val sh = target.resources.displayMetrics.heightPixels
        // 位置优先级：内存 lastX/lastY → 按包名持久化 → 默认
        val saved = if (lastX == null && guestPackage != null) loadPos(guestPackage) else null
        val initX = lastX
            ?: saved?.let { (side, _) -> if (side == "R") sw - size / 2 else -size / 2 }
            ?: -size / 2
        val initY = (lastY ?: saved?.second ?: (sh * 2 / 5)).coerceIn(0, (sh - size).coerceAtLeast(0))

        val tv = TextView(target)
        tv.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0xE6171C24.toInt())
            setStroke((1.5f * density).roundToInt(), 0x66FFFFFF)
        }
        tv.setTextColor(Color.WHITE)
        tv.textSize = baseTextSize
        tv.setTypeface(Typeface.DEFAULT_BOLD)
        tv.gravity = Gravity.CENTER
        tv.alpha = 0.92f

        val lp = WindowManager.LayoutParams(
            size,
            size,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initX
            y = initY
        }

        tv.setOnTouchListener { v, event -> onTouch(v, event) }

        try {
            newWm.addView(tv, lp)
            wm = newWm
            view = tv
            params = lp
            sync(SpeedState.speed, SpeedState.enabled)
            applySnappedStyle(tv, target)
        } catch (t: Throwable) {
            android.util.Log.w("SM-FloatUi", "ball addView failed", t)
            view = null
            params = null
        }
    }

    fun hide() {
        val v = view ?: return
        val manager = wm ?: return
        try {
            manager.removeView(v)
        } catch (_: Throwable) {
        }
        view = null
        params = null
        wm = null
    }

    fun sync(speed: Float, on: Boolean) {
        val v = view ?: return
        v.text = "⚡\n" + String.format("%.1fx", speed)
        v.alpha = if (on) 0.95f else 0.45f
        // 倍率位数变化（如 5.0x → 12.5x）时重新按文字宽度调整字号
        if (snapped) applySnappedStyle(v, v.context)
    }

    private fun onTouch(v: android.view.View, event: MotionEvent): Boolean {
        val lp = params ?: return false
        val tv = v as TextView
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                downY = event.rawY
                startX = lp.x
                startY = lp.y
                moved = false
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (event.rawX - downX).roundToInt()
                val dy = (event.rawY - downY).roundToInt()
                if (abs(dx) > 8 || abs(dy) > 8) moved = true
                if (moved) {
                    val sw = tv.context.resources.displayMetrics.widthPixels
                    val sh = tv.context.resources.displayMetrics.heightPixels
                    // 拖动过程中完整显示，文字居中
                    styleFull(tv)
                    lp.x = (startX + dx).coerceIn(-size / 2, sw - size / 2)
                    lp.y = (startY + dy).coerceIn(0, sh - size)
                    try {
                        wm?.updateViewLayout(tv, lp)
                    } catch (_: Throwable) {
                    }
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                if (!moved) {
                    // 轻点 = 展开面板
                    FloatUi.expandToPanel()
                    return true
                }
                snapToEdge(tv, lp)
                true
            }
            else -> false
        }
    }

    /** 松手后吸附到最近的左/右边缘（露出一半，像个半圆） */
    private fun snapToEdge(tv: TextView, lp: WindowManager.LayoutParams) {
        val sw = tv.context.resources.displayMetrics.widthPixels
        val center = lp.x + size / 2
        lp.x = if (center < sw / 2) -size / 2 else sw - size / 2
        applySnappedStyle(tv, tv.context)
        // 记住吸附后的位置（内存 + 按包名持久化）
        lastX = lp.x
        lastY = lp.y
        guestPackage?.let { pkg -> savePos(pkg, lp.x, lp.y, sw) }
        try {
            wm?.updateViewLayout(tv, lp)
        } catch (_: Throwable) {
        }
    }

    /** 小球位置作为面板锚点（展开面板贴小球附近；小球未显示时用记忆/持久化位置） */
    fun anchorForPanel(guestPackage: String?): Pair<Int, Int>? {
        val pos = resolvePosition(guestPackage) ?: return null
        val x = pos.first
        val y = pos.second
        val ctx = view?.context ?: context
        val sw = ctx.resources.displayMetrics.widthPixels
        val d = ctx.resources.displayMetrics.density
        val centerX = x + size / 2
        // 面板顶部与小球基本对齐、略往上偏（面板比小球高，让小球落在面板中段）
        val ay = (y - (50 * d).roundToInt()).coerceAtLeast(0)
        return if (centerX < sw / 2) {
            Pair(0, ay)                 // 小球在左：面板贴左边缘
        } else {
            Pair(sw, ay)                // 小球在右：面板贴右边缘（showOn 里 clamp 贴边）
        }
    }

    /** 解析小球当前位置：活视图 → 按包名持久化 → 内存记忆 */
    private fun resolvePosition(guestPackage: String?): Pair<Int, Int>? {
        val lp = params
        if (lp != null) return Pair(lp.x, lp.y)
        if (guestPackage != null) {
            loadPos(guestPackage)?.let { (side, y) ->
                val sw = context.resources.displayMetrics.widthPixels
                return Pair(if (side == "R") sw - size / 2 else -size / 2, y)
            }
        }
        if (lastX != null && lastY != null) return Pair(lastX!!, lastY!!)
        return null
    }

    /**
     * 吸附状态：文字完整放进露出来的那半圆里。
     *
     * 小球直径保持 54dp 不变（正圆/半圆形状不被文字宽度破坏），
     * 改为**字号自适应**：从基准字号往下缩，直到最宽的一行文字能完整
     * 放进露出的半圆 —— 「10.0x / 12.5x / 20.0x」这类多位数也不会被切掉。
     */
    private fun applySnappedStyle(tv: TextView, ctx: Context) {
        val lp = params ?: return
        val sw = ctx.resources.displayMetrics.widthPixels
        val center = lp.x + lp.width / 2
        val inset = (6 * density).roundToInt()
        val avail = (size / 2 - inset).coerceAtLeast((12 * density).roundToInt())

        // 自适应字号：最宽一行文字能放进可见半圆
        var ts = baseTextSize
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, ts)
        val lines = tv.text.toString().split("\n")
        var maxW = lines.maxOfOrNull { tv.paint.measureText(it) } ?: 0f
        while (maxW > avail && ts > minTextSize) {
            ts -= 0.5f
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, ts)
            maxW = lines.maxOfOrNull { tv.paint.measureText(it) } ?: 0f
        }

        // 宽度锁定为标准圆
        lp.width = size
        lp.height = size

        if (center < sw / 2) {
            tv.gravity = Gravity.CENTER_VERTICAL or Gravity.END
            tv.setPadding(0, 0, inset, 0)
            lp.x = -size / 2
        } else {
            tv.gravity = Gravity.CENTER_VERTICAL or Gravity.START
            tv.setPadding(inset, 0, 0, 0)
            lp.x = sw - size / 2
        }
        snapped = true
        try {
            wm?.updateViewLayout(tv, lp)
        } catch (_: Throwable) {
        }
    }

    private fun styleFull(tv: TextView) {
        tv.gravity = Gravity.CENTER
        tv.setPadding(0, 0, 0, 0)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, baseTextSize)
        params?.let {
            it.width = size
            it.height = size
        }
        snapped = false
    }

    private fun overlayType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
}
