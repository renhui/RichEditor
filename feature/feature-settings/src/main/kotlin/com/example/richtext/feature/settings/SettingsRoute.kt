package com.example.richtext.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.richtext.core.datastore.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val exportPreview = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("主题", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val items = listOf(ThemeMode.SYSTEM to "跟随系统", ThemeMode.LIGHT to "浅色", ThemeMode.DARK to "深色")
                items.forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        selected = uiState.themeMode == mode,
                        onClick = { viewModel.setTheme(mode) },
                        shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(index, items.size),
                        label = { Text(label) },
                    )
                }
            }

            Text("字号缩放：${"%.2f".format(uiState.fontScale)}")
            Slider(
                value = uiState.fontScale,
                onValueChange = viewModel::setFontScale,
                valueRange = 0.85f..1.4f,
            )

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("自动保存")
                Switch(
                    checked = uiState.autoSaveEnabled,
                    onCheckedChange = viewModel::setAutoSave,
                )
            }

            Button(onClick = { viewModel.exportPreview { exportPreview.value = it } }) {
                Text("预览导出 Markdown")
            }

            Button(onClick = { viewModel.importSample { exportPreview.value = "已导入便签：$it" } }) {
                Text("导入示例")
            }

            if (exportPreview.value.isNotBlank()) {
                Text(
                    text = exportPreview.value,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
