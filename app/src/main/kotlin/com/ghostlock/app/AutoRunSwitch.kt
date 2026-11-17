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

    private const val PENDING = "pendingUntil"
    private const val WINDOW_MS = 120_000L

    /** 开机时打一个 2 分钟内有效的待办标记，由界面消费一次。 */
    fun markPending(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(PENDING, System.currentTimeMillis() + WINDOW_MS).apply()
    }

    /** 消费待办：只有开机窗口内、且只消费一次，才返回 true。 */
    fun consumePending(ctx: Context): Boolean {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val until = prefs.getLong(PENDING, 0L)
        val ok = until > System.currentTimeMillis()
        if (until != 0L) prefs.edit().remove(PENDING).apply()
        return ok
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
