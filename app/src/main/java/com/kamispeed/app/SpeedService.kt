package com.kamispeed.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kamispeed.container.VirtualCore

/**
 * 前台服务：保证游戏过程中进程不被回收，并托管悬浮面板/小球。
 */
class SpeedService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_PANEL -> {
                FloatUi.attach(this)
                FloatUi.setEnabled(true)
                FloatUi.expandToPanel()
                refreshNotification()
            }
            ACTION_HIDE_PANEL -> {
                FloatUi.attach(this)
                FloatUi.collapseToBall()
            }
            ACTION_TOGGLE -> {
                SpeedState.setEnabled(!SpeedState.enabled)
                refreshNotification()
            }
            ACTION_SET_SPEED -> {
                val v = intent.getFloatExtra(EXTRA_SCALE, 1.0f)
                SpeedState.setSpeed(v.coerceIn(SpeedState.MIN, SpeedState.MAX))
                refreshNotification()
            }
            ACTION_SET_ENABLED -> {
                SpeedState.setEnabled(intent.getBooleanExtra(EXTRA_ON, true))
                refreshNotification()
            }
            ACTION_STOP -> {
                FloatUi.setEnabled(false)
                stopSelf()
            }
            else -> Unit
        }
        return START_STICKY
    }

    override fun onDestroy() {
        FloatUi.destroy()
        super.onDestroy()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "变速引擎",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "保持变速引擎在后台运行"
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else PendingIntent.FLAG_UPDATE_CURRENT

        val content = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), flags
        )
        val toggle = PendingIntent.getService(
            this, 1,
            Intent(this, SpeedService::class.java).setAction(ACTION_TOGGLE), flags
        )

        val status = if (SpeedState.enabled)
            String.format("运行中 · %.1fx · 已挂钩 %d 个时间点", SpeedState.speed, VirtualCore.INSTANCE.hookedSlots)
        else "已就绪 · 未开启变速"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("KamiSpeed - 耳神速")
            .setContentText(status)
            .setContentIntent(content)
            .addAction(0, if (SpeedState.enabled) "关闭变速" else "开启变速", toggle)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun refreshNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification())
    }

    companion object {
        private const val CHANNEL_ID = "kamispeed_engine"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_SHOW_PANEL = "com.kamispeed.app.SHOW_PANEL"
        const val ACTION_HIDE_PANEL = "com.kamispeed.app.HIDE_PANEL"
        const val ACTION_TOGGLE = "com.kamispeed.app.TOGGLE"
        const val ACTION_STOP = "com.kamispeed.app.STOP"
        const val ACTION_SET_SPEED = "com.kamispeed.app.SET_SPEED"
        const val ACTION_SET_ENABLED = "com.kamispeed.app.SET_ENABLED"
        const val EXTRA_SCALE = "scale"
        const val EXTRA_ON = "on"

        fun send(context: Context, action: String) {
            val intent = Intent(context, SpeedService::class.java).setAction(action)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (t: Throwable) {
                // 后台启动前台服务受限时忽略；用户在前台操作时会再次触发
            }
        }
    }
}
