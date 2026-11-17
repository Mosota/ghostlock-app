package com.ghostlock.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ghostlock.app.ui.MainActivity

/**
 * Boot trigger: opens MainActivity with the autorun extra so the app runs the
 * same exploit path as the Run button, without a tap.
 */
class AutoBoot : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return
        if (!AutoRunSwitch.isEnabled(context)) return
        /* 先走无界面服务；起不来再回退到带待办标记的界面路径。 */
        val started = runCatching {
            context.startForegroundService(Intent(context, AutoRunService::class.java))
            true
        }.getOrDefault(false)
        if (started) return
        AutoRunSwitch.markPending(context)
        val launch = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("autorun", true)
        }
        runCatching { context.startActivity(launch) }
    }
}
