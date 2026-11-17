package com.ghostlock.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** 开机自动执行的独立开关，默认开启。 */
object AutoRunSwitch {
    private const val PREFS = "autorun"
    private const val KEY = "enabled"

    fun isEnabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, true)

    fun setEnabled(ctx: Context, on: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, on).apply()
    }
}
