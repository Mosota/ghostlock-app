package com.ghostlock.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ghostlock.app.AutoRunSwitch
import com.ghostlock.app.PermMonSwitch
import com.ghostlock.app.R
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
internal fun ExtensionScreen(actions: GhostlockActions) {
    val context = LocalContext.current
    var autoRunOn by remember { mutableStateOf(AutoRunSwitch.isEnabled(context)) }
    var permMonOn by remember { mutableStateOf(PermMonSwitch.isEnabled(context)) }
    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = stringResource(R.string.extensions_label),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = actions::onCloseExtension) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = stringResource(R.string.action_back),
                        )
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
                    SwitchPreference(
                        checked = autoRunOn,
                        onCheckedChange = { enabled ->
                            autoRunOn = enabled
                            AutoRunSwitch.setEnabled(context, enabled)
                        },
                        title = stringResource(R.string.autorun_label),
                        summary = stringResource(R.string.autorun_summary),
                    )
                    SwitchPreference(
                        checked = permMonOn,
                        onCheckedChange = { enabled ->
                            permMonOn = enabled
                            PermMonSwitch.setEnabled(context, enabled)
                        },
                        title = stringResource(R.string.permmon_label),
                        summary = stringResource(R.string.permmon_summary),
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    ArrowPreference(
                        title = stringResource(R.string.prepare_root_label),
                        summary = stringResource(R.string.prepare_root_summary),
                        onClick = actions::onPrepareRoot,
                    )
                }
            }
        }
    }
}
