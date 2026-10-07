package com.kamispeed.app

import android.app.Application
import com.kamispeed.container.ContainerLifecycle
import com.kamispeed.container.VirtualCore

class SpeedApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        // 安装容器（隐藏 API 白名单 + ActivityThread 钩子 + AM 代理）并加载变速引擎
        // 真机排查开关：adb shell setprop debug.kamispeed.skipattach 1
        val skip = try {
            val sp = Class.forName("android.os.SystemProperties")
            val m = sp.getMethod("get", String::class.java, String::class.java)
            m.invoke(null, "debug.kamispeed.skipattach", "0").toString() == "1"
        } catch (t: Throwable) {
            false
        }
        if (!skip) {
            VirtualCore.INSTANCE.attach(this)
        } else {
            android.util.Log.w("KamiSpeed", "attach skipped by debug prop")
        }
        SpeedState.init()

        // 悬浮 UI：速度同步 + 游戏窗口焦点（由容器 ContainerLifecycle 转发）
        FloatUi.attach(this)
        SpeedState.addListener { speed, on -> FloatUi.syncSpeed(speed, on) }
        ContainerLifecycle.INSTANCE.addListener(object : ContainerLifecycle.FocusListener {
            override fun onGuestWindowFocus(activity: android.app.Activity, hasFocus: Boolean) {
                FloatUi.onGuestWindowFocus(activity, hasFocus)
            }
        })
    }

    companion object {
        lateinit var instance: SpeedApp
            private set
    }
}
