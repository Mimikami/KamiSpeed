package com.kamispeed.app

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build

data class AppEntry(
    val label: String,
    val packageName: String,
    val icon: Drawable,
    val system: Boolean,
)

object AppRepository {

    private const val SELF = "com.kamispeed.app"

    fun load(context: Context): List<AppEntry> {
        val pm = context.packageManager
        val installed: List<ApplicationInfo> = if (Build.VERSION.SDK_INT >= 33) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }

        return installed.asSequence()
            .filter { it.packageName != SELF }
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map {
                AppEntry(
                    label = it.loadLabel(pm).toString(),
                    packageName = it.packageName,
                    icon = it.loadIcon(pm),
                    system = (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    /** 按包名解析单个应用条目（快捷启动列表用；应用被卸载时返回 null）。 */
    fun entryOf(context: Context, packageName: String): AppEntry? = try {
        val pm = context.packageManager
        val info = pm.getApplicationInfo(packageName, 0)
        AppEntry(
            label = info.loadLabel(pm).toString(),
            packageName = info.packageName,
            icon = info.loadIcon(pm),
            system = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
        )
    } catch (t: Throwable) {
        null
    }
}
