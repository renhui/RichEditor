package com.example.richtext.feature.editor

import android.widget.TextView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.richtext.core.markdown.MarkdownRenderer
import com.example.richtext.feature.editor.model.EditorAction
import com.example.richtext.feature.editor.model.EditorMode
import com.example.richtext.feature.editor.model.SaveState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorRoute(
    onBack: () -> Unit,
    markdownRenderer: MarkdownRenderer,
    viewModel: NoteEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { viewModel.insertImageFromUri(it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            when (uiState.saveState) {
                                SaveState.SAVING -> "编辑中 · 保存中"
                                SaveState.SAVED -> "编辑中 · 已保存"
                                SaveState.ERROR -> "编辑中 · 保存失败"
                                SaveState.IDLE -> "编辑器"
                            },
                        )
                        val indicatorColor by animateColorAsState(
                            targetValue = when (uiState.saveState) {
                                SaveState.SAVING -> Color(0xFF42A5F5)
                                SaveState.SAVED -> Color(0xFF66BB6A)
                                SaveState.ERROR -> Color(0xFFEF5350)
                                SaveState.IDLE -> MaterialTheme.colorScheme.outlineVariant
                            },
                            label = "saveStateIndicator",
                        )
                        Badge(containerColor = indicatorColor)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.switchMode(EditorMode.EDIT) }) { Text("编辑") }
                    TextButton(onClick = { viewModel.switchMode(EditorMode.PREVIEW) }) { Text("预览") }
                    TextButton(onClick = { viewModel.switchMode(EditorMode.SPLIT) }) { Text("分屏") }
                },
            )
        },
        bottomBar = {
            val toolbarScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(toolbarScrollState)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { viewModel.applyAction(EditorAction.HEADER_1) },
                    modifier = Modifier.widthIn(min = 72.dp),
                ) { Text("H1") }
                Button(
                    onClick = { viewModel.applyAction(EditorAction.BOLD) },
                    modifier = Modifier.widthIn(min = 72.dp),
                ) { Text("B") }
                Button(
                    onClick = { viewModel.applyAction(EditorAction.TODO) },
                    modifier = Modifier.widthIn(min = 72.dp),
                ) { Text("TODO") }
                Button(
                    onClick = { viewModel.applyAction(EditorAction.CODE_BLOCK) },
                    modifier = Modifier.widthIn(min = 72.dp),
                ) { Text("代码") }
                Button(
                    onClick = {
                        pickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    modifier = Modifier.widthIn(min = 72.dp),
                ) {
                    Text("图片")
                }
                Button(
                    onClick = viewModel::undo,
                    enabled = uiState.canUndo,
                    modifier = Modifier.widthIn(min = 72.dp),
                ) { Text("撤销") }
                Button(
                    onClick = viewModel::redo,
                    enabled = uiState.canRedo,
                    modifier = Modifier.widthIn(min = 72.dp),
                ) { Text("重做") }
            }
        },
    ) { innerPadding ->
        when (uiState.mode) {
            EditorMode.EDIT -> {
                EditorPane(
                    title = uiState.title,
                    markdown = uiState.markdown,
                    onTitleChange = viewModel::updateTitle,
                    onMarkdownChange = viewModel::updateMarkdown,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )
            }

            EditorMode.PREVIEW -> {
                PreviewPane(
                    markdown = uiState.previewMarkdown,
                    markdownRenderer = markdownRenderer,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )
            }

            EditorMode.SPLIT -> {
                Row(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                ) {
                    EditorPane(
                        title = uiState.title,
                        markdown = uiState.markdown,
                        onTitleChange = viewModel::updateTitle,
                        onMarkdownChange = viewModel::updateMarkdown,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                    )
                    PreviewPane(
                        markdown = uiState.previewMarkdown,
                        markdownRenderer = markdownRenderer,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorPane(
    title: String,
    markdown: String,
    onTitleChange: (String) -> Unit,
    onMarkdownChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("标题") },
            singleLine = true,
        )
        OutlinedTextField(
            value = markdown,
            onValueChange = onMarkdownChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            label = { Text("Markdown") },
            textStyle = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun PreviewPane(
    markdown: String,
    markdownRenderer: MarkdownRenderer,
    modifier: Modifier = Modifier,
) {
    val contentColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val scrollState = rememberScrollState()
    var lastRenderedMarkdown by remember { mutableStateOf<String?>(null) }
    AndroidView(
        factory = { context ->
            TextView(context).apply {
                textSize = 16f
                setTextColor(contentColor)
                setLineSpacing(8f, 1.1f)
            }
        },
        update = { textView ->
            if (lastRenderedMarkdown != markdown) {
                markdownRenderer.render(markdown, textView)
                lastRenderedMarkdown = markdown
            }
        },
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
    )
}
