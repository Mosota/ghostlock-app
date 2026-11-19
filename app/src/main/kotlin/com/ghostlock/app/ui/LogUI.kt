package com.ghostlock.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ghostlock.app.R
import java.io.File
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference

private fun readLog(context: android.content.Context, maxLines: Int = 400): List<String> {
    val f = File(context.getExternalFilesDir(null), "autorun.log")
    if (!f.exists()) return listOf("（还没有日志：尚未运行过）")
    return runCatching {
        f.readLines().takeLast(maxLines)
    }.getOrElse { listOf("读取失败: ${it.message}") }
}

@Composable
internal fun LogScreen(actions: GhostlockActions) {
    val context = LocalContext.current
    var lines by remember { mutableStateOf(readLog(context).asReversed()) }
    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = stringResource(R.string.logs_label),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = actions::onCloseLogs) {
                        Icon(imageVector = MiuixIcons.Back, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = pageContentPadding(padding, top = 12.dp, bottom = 24.dp, horizontalMin = 12.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    ArrowPreference(
                        title = stringResource(R.string.logs_refresh),
                        summary = stringResource(R.string.logs_path),
                        onClick = { lines = readLog(context).asReversed() },
                    )
                }
            }
            items(lines) { line ->
                Text(
                    text = line,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 1.dp),
                )
            }
        }
    }
}
