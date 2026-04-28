package com.example.richtext.feature.detail

import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.richtext.core.markdown.MarkdownRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailRoute(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel(),
    markdownRenderer: MarkdownRenderer,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contentColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButtonCompact(text = "编辑", onClick = onEdit)
                },
            )
        },
    ) { inner ->
        AndroidView(
            factory = { context ->
                TextView(context).apply {
                    textSize = 16f
                    setTextColor(contentColor)
                    setLineSpacing(8f, 1.1f)
                }
            },
            update = { textView ->
                val markdown = if (uiState.isLoading) "加载中..." else uiState.markdown
                markdownRenderer.render(markdown, textView)
            },
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun TextButtonCompact(text: String, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        Text(text)
    }
}
