package com.kamispeed.app

import android.content.Context
import com.kamispeed.container.VirtualCore
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 全局变速状态：UI、悬浮面板、Service 共用一份，并负责持久化。
 */
object SpeedState {

    private const val PREF = "kamispeed"
    private const val KEY_SPEED = "speed"
    private const val KEY_ENABLED = "enabled"

    const val MIN = 1.0f
    const val MAX = 20.0f

    private val listeners = CopyOnWriteArrayList<(Float, Boolean) -> Unit>()

    var speed: Float = 1.0f
        private set

    var enabled: Boolean = false
        private set

    fun init() {
        val sp = SpeedApp.instance.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        speed = sp.getFloat(KEY_SPEED, 1.0f).coerceIn(MIN, MAX)
        enabled = sp.getBoolean(KEY_ENABLED, false)
        // 引擎里目前的倍率同步一次
        VirtualCore.INSTANCE.applySpeed(speed.toDouble(), enabled)
    }

    fun setSpeed(value: Float) {
        speed = value.coerceIn(MIN, MAX)
        persist()
        VirtualCore.INSTANCE.applySpeed(speed.toDouble(), enabled)
        notifyListeners()
    }

    fun setEnabled(value: Boolean) {
        enabled = value
        persist()
        VirtualCore.INSTANCE.applySpeed(speed.toDouble(), enabled)
        notifyListeners()
    }

    fun reset() {
        speed = 1.0f
        enabled = false
        persist()
        VirtualCore.INSTANCE.resetSpeed()
        notifyListeners()
    }

    fun addListener(l: (Float, Boolean) -> Unit) {
        listeners.add(l)
        l(speed, enabled)
    }

    fun removeListener(l: (Float, Boolean) -> Unit) {
        listeners.remove(l)
    }

    private fun notifyListeners() {
        listeners.forEach { it(speed, enabled) }
    }

    private fun persist() {
        SpeedApp.instance.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_SPEED, speed)
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }
}
