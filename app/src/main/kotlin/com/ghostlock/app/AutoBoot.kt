package com.ghostlock.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock

/**
 * 开机触发：只做一件事——把无界面的 AutoRunService 拉起来。
 * 起不来就用 AlarmManager 15 秒后重试一次，不碰界面、不需要悬浮窗权限。
 */
class AutoBoot : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != "android.intent.action.QUICKBOOT_POWERON") return
        if (!AutoRunSwitch.isEnabled(context)) return

        val svc = Intent(context, AutoRunService::class.java)
        val started = runCatching {
            context.startForegroundService(svc)
            true
        }.getOrDefault(false)
        if (started) return

        val pi = PendingIntent.getForegroundService(
            context, 1001, svc,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        runCatching {
            context.getSystemService(AlarmManager::class.java)
                .set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 15_000, pi)
        }
    }
}
