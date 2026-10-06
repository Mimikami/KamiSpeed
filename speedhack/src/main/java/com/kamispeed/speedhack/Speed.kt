package com.kamispeed.speedhack

import android.util.Log

/**
 * 变速引擎 Kotlin 门面。
 *
 * 必须在**目标游戏所在的进程**中初始化（本方案用虚拟容器把游戏跑在自己的进程里，
 * 所以容器 Application.onCreate 里调一次 [init] 即可对全部被加速的 App 生效）。
 *
 * 典型用法：
 * ```
 * Speed.init()
 * Speed.scale = 5.0        // 1x ~ 20x
 * Speed.enabled = true
 * ```
 */
object Speed {

    private const val TAG = "SpeedHack"

    /** 倍率下限。 */
    const val MIN_SCALE = 1.0
    /** 倍率上限（需求：1~20 倍）。 */
    const val MAX_SCALE = 20.0

    @Volatile
    private var nativeReady = false

    private external fun nativeInit(): Boolean
    private external fun nativeSetScale(scale: Double)
    private external fun nativeGetScale(): Double
    private external fun nativeSetEnabled(on: Boolean)
    private external fun nativeIsEnabled(): Boolean
    private external fun nativeSetScaleIo(on: Boolean)
    private external fun nativeHookCount(): Int
    private external fun nativeRescan()

    /**
     * 加载 native 库并安装 GOT Hook。可重复调用。
     * @return 引擎是否可用
     */
    @Synchronized
    fun init(): Boolean {
        if (nativeReady) return true
        return try {
            System.loadLibrary("speedhack")
            nativeReady = nativeInit()
            Log.i(TAG, "engine init = $nativeReady")
            nativeReady
        } catch (t: Throwable) {
            Log.e(TAG, "loadLibrary failed", t)
            false
        }
    }

    /** 引擎是否已就绪。 */
    val ready: Boolean get() = nativeReady && init()

    /** 变速倍率，取值 1.0 ~ 20.0，超出会被 native 层夹紧。 */
    var scale: Double
        get() = if (ready) nativeGetScale().coerceIn(MIN_SCALE, MAX_SCALE) else 1.0
        set(value) {
            if (!ready) return
            nativeSetScale(value.coerceIn(MIN_SCALE, MAX_SCALE))
        }

    /** 总开关。 */
    var enabled: Boolean
        get() = ready && nativeIsEnabled()
        set(value) {
            if (!ready) return
            nativeSetEnabled(value)
        }

    /**
     * 是否同时缩放 epoll_wait / poll / select 的超时。
     * 默认关闭：开启后节奏更快，但可能影响网络 IO 的响应时延。
     */
    var scaleIoTimeouts: Boolean
        get() = false
        set(value) {
            if (ready) nativeSetScaleIo(value)
        }

    /** 已经被改写的时间函数 GOT 槽位总数，用于自检。 */
    val hookedSlotCount: Int get() = if (ready) nativeHookCount() else 0

    /** 手动重扫模块表（新 dlopen 的 so 会被自动挂上，通常不需要手动调）。 */
    fun rescan() {
        if (ready) nativeRescan()
    }

    /** 一键设置 1~20 之间的倍率。 */
    fun apply(scaleValue: Double, on: Boolean = true) {
        if (!init()) return
        scale = scaleValue
        enabled = on
    }

    /** 还原 1 倍速并关闭。 */
    fun reset() {
        if (!nativeReady) return
        scale = 1.0
        enabled = false
    }
}
