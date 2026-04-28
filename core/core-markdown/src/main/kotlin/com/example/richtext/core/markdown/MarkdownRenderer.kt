package com.example.richtext.core.markdown

import android.widget.TextView

interface MarkdownRenderer {
    fun render(markdown: String, target: TextView)
    fun extractPlainSummary(markdown: String, maxChars: Int = 120): String
    fun extractFirstImagePath(markdown: String): String?
}
