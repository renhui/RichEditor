package com.example.richtext

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.richtext.core.markdown.MarkdownRenderer
import com.example.richtext.core.ui.theme.RichNoteTheme
import com.example.richtext.navigation.RichNoteNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var markdownRenderer: MarkdownRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RichNoteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RichNoteNavHost(markdownRenderer = markdownRenderer)
                }
            }
        }
    }
}
