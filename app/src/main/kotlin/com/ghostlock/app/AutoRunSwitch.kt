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

/**
 * 开关的远程控制：
 *   adb shell am broadcast -a com.ghostlock.app.AUTORUN_OFF
 *   adb shell am broadcast -a com.ghostlock.app.AUTORUN_ON
 *   adb shell am broadcast -a com.ghostlock.app.AUTORUN --ez enabled false
 */
class AutoRunToggle : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val on = when {
            intent.hasExtra("enabled") -> intent.getBooleanExtra("enabled", true)
            intent.action == ACTION_ON -> true
            intent.action == ACTION_OFF -> false
            else -> !AutoRunSwitch.isEnabled(context)
        }
        AutoRunSwitch.setEnabled(context, on)
    }

    companion object {
        const val ACTION_ON = "com.ghostlock.app.AUTORUN_ON"
        const val ACTION_OFF = "com.ghostlock.app.AUTORUN_OFF"
    }
}
