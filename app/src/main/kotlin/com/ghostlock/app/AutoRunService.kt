package com.ghostlock.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ghostlock.app.domain.model.ShizukuStatus
import com.ghostlock.app.domain.usecase.LoadKernelSnapshotUseCase
import com.ghostlock.app.domain.usecase.RunExploitUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 无界面执行：开机后直接在服务里跑官方那条 runExploit 路径，不依赖 Activity，
 * 所以不需要「显示在其他应用上层」，也不会在桌面上闪界面。
 */
class AutoRunService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!RUNNING.compareAndSet(false, true)) {
            /* 已经在跑：重复点击直接忽略，避免并发跑多份 exploit */
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(1, notification("准备中"))
        scope.launch { runOnce() }
        return START_NOT_STICKY
    }

    private suspend fun runOnce() {
        val logFile = File(getExternalFilesDir(null) ?: filesDir, "autorun.log")
        fun log(msg: String) {
            Log.i(TAG, msg)
            runCatching {
                logFile.appendText(
                    "${SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(Date())}  $msg\n"
                )
            }
        }
        try {
            val app = application as GhostlockApplication
            val repo = app.createRepository()
            val snapshot = LoadKernelSnapshotUseCase(repo)()
            log("device=${snapshot.deviceName} kernel=${snapshot.kernelRelease} supported=${snapshot.kernelSupported}")
            val pair = snapshot.cpuPairs.getOrNull(snapshot.selectedCpuPair) ?: snapshot.cpuPairs.firstOrNull()
            if (!snapshot.kernelSupported || pair == null) {
                log("kernel unsupported or no cpu pair, abort")
                return
            }
            val useShizuku = snapshot.shizukuEnabled && snapshot.shizukuStatus == ShizukuStatus.READY
            log("start exploit pair=$pair shizuku=$useShizuku")
            val code = RunExploitUseCase(repo)(pair, useShizuku) { log(it) }
            log("exit=$code")

            /* 关键诊断：exploit 之后本进程到底是不是 root */
            val selfUid = android.os.Process.myUid()
            log("self uid after exploit = $selfUid")

            fun runRoot(cmd: String): String {
                if (selfUid == 0) {
                    return runCatching {
                        val proc = ProcessBuilder("sh", "-c", cmd).redirectErrorStream(true).start()
                        val out = proc.inputStream.bufferedReader().readText().trim()
                        proc.waitFor(); out
                    }.getOrElse { "error: ${it.message}" }
                }
                val candidates = listOf("su", "/data/adb/ksu/bin/su", "/debug_ramdisk/su", "/system/bin/su")
                for (bin in candidates) {
                    val r = runCatching {
                        val proc = ProcessBuilder(bin, "-c", cmd).redirectErrorStream(true).start()
                        val out = proc.inputStream.bufferedReader().readText().trim()
                        proc.waitFor(); out
                    }.getOrElse { "" }
                    if (r.isNotEmpty()) return "[$bin] $r"
                }
                return "no su usable (tried ${candidates.joinToString()})"
            }

            /* 把 su 暴露到 /debug_ramdisk：与其他开关无关，始终执行 */
            log("su-expose -> " + runRoot(
                "cp /data/adb/ksu/bin/su /debug_ramdisk/su 2>/dev/null; " +
                "chmod 755 /debug_ramdisk/su 2>/dev/null; ls -l /debug_ramdisk/su 2>&1"
            ))

            /* 按独立开关关掉 ColorOS 的权限监控（不影响界面）。 */
            if (PermMonSwitch.isEnabled(this@AutoRunService)) {
                log("permission-monitor -> " + runRoot(
                    "setprop persist.sys.permission.enable false; getprop persist.sys.permission.enable"
                ))
            }

            /* 开机脚本：/sdcard/Download/DSH-Go/boot.sh，逐行以 root 执行 */
            val bootScript = java.io.File(getExternalFilesDir(null) ?: filesDir, "boot.sh")
            if (bootScript.exists() && bootScript.canRead()) {
                log("boot.sh: found ${bootScript.absolutePath}")
                bootScript.readLines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .forEach { line ->
                        log("boot.sh: $line")
                        log("  -> " + runRoot(line))
                    }
            }
        } catch (t: Throwable) {
            log("error: ${t::class.java.simpleName}: ${t.message}")
        } finally {
            RUNNING.set(false)
            stopSelf()
        }
    }

    private fun notification(text: String): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, "AutoRun", NotificationManager.IMPORTANCE_LOW)
            )
        }
        return Notification.Builder(this, CHANNEL)
            .setContentTitle("GhostLock Auto")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .build()
    }

    private companion object {
        const val TAG = "GhostLockAuto"
        const val CHANNEL = "autorun"
        val RUNNING = java.util.concurrent.atomic.AtomicBoolean(false)
    }
}
