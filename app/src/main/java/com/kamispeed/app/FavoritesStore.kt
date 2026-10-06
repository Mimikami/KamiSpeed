package com.kamispeed.app

import android.content.Context

/**
 * 主面板「快捷启动」收藏列表的持久化。
 * 只存包名，标签/图标在展示时实时解析。
 */
object FavoritesStore {

    private const val PREF = "kamispeed_favorites"
    private const val KEY = "packages"

    fun load(context: Context): List<String> =
        prefs(context).getStringSet(KEY, emptySet())?.toList() ?: emptyList()

    fun contains(context: Context, pkg: String): Boolean =
        prefs(context).getStringSet(KEY, emptySet())?.contains(pkg) == true

    fun add(context: Context, pkg: String): List<String> {
        val set = HashSet(load(context))
        set.add(pkg)
        save(context, set)
        return set.toList()
    }

    fun remove(context: Context, pkg: String): List<String> {
        val set = HashSet(load(context))
        set.remove(pkg)
        save(context, set)
        return set.toList()
    }

    private fun save(context: Context, set: Set<String>) {
        prefs(context).edit().putStringSet(KEY, set).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
}
