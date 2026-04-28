package com.example.richtext.core.markdown

object MarkdownTextParser {
    fun extractPlainSummary(markdown: String, maxChars: Int = 120): String {
        return markdown
            .replace(Regex("```[\\s\\S]*?```"), " ")
            .replace(Regex("`[^`]*`"), " ")
            .replace(Regex("!\\[[^]]*]\\(([^)]+)\\)"), " ")
            .replace(Regex("\\[[^]]*]\\(([^)]+)\\)"), " ")
            .replace(Regex("[#>*_~\\-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(maxChars)
    }

    fun extractFirstImagePath(markdown: String): String? {
        return Regex("!\\[[^]]*]\\(([^)]+)\\)").find(markdown)?.groupValues?.getOrNull(1)
    }
}
