package com.example.richtext.core.markdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MarkdownTextParserTest {

    @Test
    fun `extract summary removes markdown syntax`() {
        val markdown = """
            # Title
            这是 **粗体** 文本，含 [link](https://example.com)。
            - [ ] todo item
            ```kotlin
            println("hello")
            ```
        """.trimIndent()

        val summary = MarkdownTextParser.extractPlainSummary(markdown, maxChars = 60)

        assertEquals(true, summary.contains("Title"))
        assertEquals(false, summary.contains("```"))
        assertEquals(false, summary.contains("[link]"))
    }

    @Test
    fun `extract first image path returns first match`() {
        val markdown = """
            text
            ![a](note://asset/123)
            ![b](file:///tmp/abc.png)
        """.trimIndent()

        assertEquals("note://asset/123", MarkdownTextParser.extractFirstImagePath(markdown))
    }

    @Test
    fun `extract first image path returns null when no image`() {
        assertNull(MarkdownTextParser.extractFirstImagePath("plain text"))
    }
}
