package com.ghostlock.app

import android.content.Context

/**
 * 独立开关：开机提权成功后，自动把 ColorOS 的「权限监控」关掉
 * （persist.sys.permission.enable=false）。
 *
 * 注意：这跟「禁用系统优化」不是一回事——只关 ADB 权限监控，
 * 不触发系统 UI 回退。
 */
object PermMonSwitch {
    private const val PREFS = "permmon"
    private const val KEY = "enabled"

    fun isEnabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, true)

    fun setEnabled(ctx: Context, on: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, on).apply()
    }
}
