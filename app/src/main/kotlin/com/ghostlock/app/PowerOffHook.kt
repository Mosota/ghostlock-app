package com.ghostlock.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 关机前把「停用系统优化」恢复成启用状态。
 *
 * 为什么需要：关闭权限监控写的是 persist.sys.permission.enable=false，
 * 这是持久属性，开机早期会被 ColorOS 读到，于是界面回退（表现为
 * 「停用系统优化」被打开）。所以在关机时把它写回 true，让下次开机干净；
 * 本次会话需要 ADB 时，再由 AutoRunService 在提权后写回 false。
 *
 * 已知失效：强杀 / 断电 / 内核崩溃时关机广播发不出去，那次开机会回退。
 */
class PowerOffHook : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_SHUTDOWN &&
            action != "android.intent.action.QUICKBOOT_POWEROFF" &&
            action != "android.intent.action.ACTION_SHUTDOWN"
        ) return
        if (!PermMonSwitch.isEnabled(context)) return
        runCatching {
            ProcessBuilder("su", "-c", "setprop persist.sys.permission.enable true")
                .redirectErrorStream(true).start()
        }
    }
}
