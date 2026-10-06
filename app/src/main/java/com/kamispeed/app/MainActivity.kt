package com.kamispeed.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kamispeed.app.databinding.ActivityMainBinding
import com.kamispeed.container.VirtualCore
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AppListAdapter

    private val stateListener: (Float, Boolean) -> Unit = { speed, enabled ->
        binding.tvSpeed.text = String.format("%.1fx", speed)
        binding.sbSpeed.progress = ((speed - SpeedState.MIN) * 10).roundToInt()
        if (binding.swEnable.isChecked != enabled) binding.swEnable.isChecked = enabled
        binding.tvPanelState.text = if (enabled) "变速已开启" else "变速已关闭"
        refreshEngineInfo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpeedControls()
        setupButtons()
        setupAppList()

        requestNotifPermission()
        SpeedState.addListener(stateListener)

        handleLaunchExtras(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleLaunchExtras(intent)
    }

    /** 深链/自动化入口：launch_pkg + 可选 target_activity */
    private fun handleLaunchExtras(intent: Intent?) {
        intent?.getStringExtra(EXTRA_LAUNCH_PKG)?.let { pkg ->
            intent.getStringExtra(EXTRA_TARGET_ACTIVITY)?.let { cls ->
                VirtualCore.INSTANCE.setPreferredActivity(pkg, cls)
            }
            binding.root.post { launchGuest(pkg, pkg) }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshEngineInfo()
        updateFloatButton()
        loadFavorites()
    }

    override fun onDestroy() {
        SpeedState.removeListener(stateListener)
        super.onDestroy()
    }

    /* ------------------------------------------------------------------ */

    private fun setupSpeedControls() {
        binding.sbSpeed.max = ((SpeedState.MAX - SpeedState.MIN) * 10).roundToInt()
        binding.sbSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) SpeedState.setSpeed(SpeedState.MIN + progress / 10f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.swEnable.setOnCheckedChangeListener { _, checked -> SpeedState.setEnabled(checked) }
    }

    private fun setupButtons() {
        // 开关「游戏中悬浮面板」（小球/面板整套）
        binding.btnFloat.setOnClickListener {
            if (!hasOverlayPermission()) {
                requestOverlayPermission()
                return@setOnClickListener
            }
            if (FloatUi.enabled) {
                FloatUi.setEnabled(false)
                Toast.makeText(this, "已关闭游戏中悬浮面板", Toast.LENGTH_SHORT).show()
            } else {
                FloatUi.attach(this)
                FloatUi.setEnabled(true)
                FloatUi.expandToPanel()
                Toast.makeText(this, "已开启，进入游戏后显示悬浮面板", Toast.LENGTH_SHORT).show()
            }
            updateFloatButton()
        }

        binding.btnReset.setOnClickListener {
            SpeedState.reset()
            Toast.makeText(this, "已恢复 1.0x / 关闭变速", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddApps.setOnClickListener {
            startActivity(Intent(this, AddAppActivity::class.java))
        }
    }

    private fun updateFloatButton() {
        binding.btnFloat.text = if (FloatUi.enabled) "关闭悬浮面板" else "悬浮面板"
    }

    private fun setupAppList() {
        adapter = AppListAdapter(
            emptyList(),
            onClick = { entry -> launchGuest(entry) },
            onLongClick = { entry -> showFavoriteMenu(entry) },
        )
        binding.rvApps.layoutManager = LinearLayoutManager(this)
        binding.rvApps.adapter = adapter
        loadFavorites()
    }

    private fun showFavoriteMenu(entry: AppEntry) {
        AlertDialog.Builder(this)
            .setTitle(entry.label)
            .setItems(arrayOf("选择启动 Activity", "从主面板移除")) { _, which ->
                when (which) {
                    0 -> showActivityPicker(entry)
                    1 -> {
                        FavoritesStore.remove(this, entry.packageName)
                        loadFavorites()
                        Toast.makeText(this, "已移除：${entry.label}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 很多国内游戏的 LAUNCHER 是渠道 SDK 的权限闸门页，在容器里容易卡住。
     * 长按可以手动指定真正要启动的 Activity。
     */
    private fun showActivityPicker(entry: AppEntry) {
        val list = VirtualCore.INSTANCE.activitiesOf(entry.packageName)
        if (list.isEmpty()) {
            Toast.makeText(this, "读不到 ${entry.label} 的 Activity 列表", Toast.LENGTH_SHORT).show()
            return
        }
        val current = VirtualCore.INSTANCE.preferredActivity(entry.packageName)
        val labels = ArrayList<String>()
        labels.add("跟随系统默认（LAUNCHER）")
        list.forEach { info ->
            val marks = buildString {
                if (info.isLauncher) append("  [LAUNCHER]")
                if (!info.exported) append("  [未导出]")
                if (info.looksLikeSdkGate) append("  [疑似SDK闸门]")
            }
            labels.add("${info.label}\n${info.className}$marks")
        }
        val checked = if (current == null) 0 else list.indexOfFirst { it.className == current } + 1
        AlertDialog.Builder(this)
            .setTitle("选择启动 Activity：${entry.label}")
            .setSingleChoiceItems(labels.toTypedArray(), checked) { dialog, which ->
                VirtualCore.INSTANCE.setPreferredActivity(
                    entry.packageName,
                    if (which == 0) null else list[which - 1].className,
                )
                dialog.dismiss()
                adapter.notifyDataSetChanged()
                val chosen = if (which == 0) "系统默认" else list[which - 1].className
                Toast.makeText(this, "已设为：$chosen", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /** 主面板下半部分：快捷启动收藏列表（通过「＋ 添加」维护） */
    private fun loadFavorites() {
        val list = FavoritesStore.load(this).mapNotNull { AppRepository.entryOf(this, it) }
        adapter.submit(list)
        binding.tvApps.text = "快捷启动 (${list.size})"
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun launchGuest(entry: AppEntry) = launchGuest(entry.label, entry.packageName)

    private fun launchGuest(label: String, packageName: String) {
        if (!hasOverlayPermission()) {
            requestOverlayPermission()
        }
        SpeedService.send(this, SpeedService.ACTION_SHOW_PANEL)

        val ok = VirtualCore.INSTANCE.launch(
            packageName,
            SpeedState.speed.toDouble(),
            SpeedState.enabled,
        )
        if (ok) {
            Toast.makeText(this, "正在以 ${String.format("%.1fx", SpeedState.speed)} 启动 $label",
                Toast.LENGTH_SHORT).show()
        } else {
            showFailureDialog(label, packageName, VirtualCore.INSTANCE.lastErrorReport)
        }
    }

    /** 启动失败时把真实原因（含堆栈）直接摊在界面上，不必接 adb。 */
    private fun showFailureDialog(label: String, packageName: String, detail: String) {
        val text = detail
        android.util.Log.e("KamiSpeed", "launch $packageName failed:\n$text")
        AlertDialog.Builder(this)
            .setTitle("启动失败：$label")
            .setMessage(text)
            .setPositiveButton("复制") { _, _ ->
                val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                cm.setPrimaryClip(android.content.ClipData.newPlainText("KamiSpeed", text))
                Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    /* ------------------------------------------------------------------ */

    private fun refreshEngineInfo() {
        val ready = VirtualCore.INSTANCE.engineReady
        binding.tvEngine.text = buildString {
            append(if (ready) "引擎：已就绪" else "引擎：未初始化")
            append("  ·  已挂钩时间调用 ")
            append(VirtualCore.INSTANCE.hookedSlots)
            append(" 处")
            append("\n容器：mH=")
            append(if (VirtualCore.INSTANCE.activityThreadHooked) "OK" else "FAIL")
            append("  AM代理=")
            append(if (VirtualCore.INSTANCE.amProxyInstalled) "OK" else "FAIL")
            val err = VirtualCore.INSTANCE.lastError
            if (err != null) {
                append("\n上次失败：")
                append(VirtualCore.INSTANCE.lastErrorStage ?: "?")
                append("（点这里看详情）")
            }
        }
        binding.tvEngine.setOnClickListener {
            val err = VirtualCore.INSTANCE.lastError ?: return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle("最近一次失败详情")
                .setMessage(err)
                .setPositiveButton("复制") { _, _ ->
                    val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("KamiSpeed", err))
                }
                .setNegativeButton("关闭", null)
                .show()
        }
    }

    private fun hasOverlayPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
        )
    }

    private fun requestNotifPermission() {
        if (Build.VERSION.SDK_INT < 33) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) return
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001
        )
    }

    companion object {
        const val EXTRA_LAUNCH_PKG = "launch_pkg"
        const val EXTRA_TARGET_ACTIVITY = "target_activity"
    }
}
