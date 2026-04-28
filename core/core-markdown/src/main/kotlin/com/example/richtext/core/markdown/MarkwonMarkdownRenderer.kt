package com.example.richtext.core.markdown

import android.widget.TextView
import io.noties.markwon.Markwon
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkwonMarkdownRenderer @Inject constructor(
    private val markwon: Markwon,
) : MarkdownRenderer {

    override fun render(markdown: String, target: TextView) {
        markwon.setMarkdown(target, markdown.normalizeForPreview())
    }

    override fun extractPlainSummary(markdown: String, maxChars: Int): String {
        return MarkdownTextParser.extractPlainSummary(markdown, maxChars)
    }

    override fun extractFirstImagePath(markdown: String): String? {
        return MarkdownTextParser.extractFirstImagePath(markdown)
    }

    private fun String.normalizeForPreview(): String {
        // Avoid excessive vertical gap before image blocks in preview.
        val leadingNormalized = replaceFirst(Regex("""^(?:[ \t]*\n)+(?!$)(?=!\[[^]]*]\([^)]+\))"""), "")
        return leadingNormalized.replace(
            Regex("""(?:\n[ \t]*){2,}(?=!\[[^]]*]\([^)]+\))"""),
            "\n",
        )
    }
}
