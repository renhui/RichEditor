package com.example.richtext.data.note.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteSearchQueryFormatterTest {

    @Test
    fun `single keyword should add wildcard`() {
        val query = NoteSearchQueryFormatter.buildFtsMatchQuery("markdown")
        assertEquals("\"markdown\"*", query)
    }

    @Test
    fun `multiple keywords should join with AND`() {
        val query = NoteSearchQueryFormatter.buildFtsMatchQuery("rich editor")
        assertEquals("\"rich\"* AND \"editor\"*", query)
    }

    @Test
    fun `blank input should return original`() {
        val query = NoteSearchQueryFormatter.buildFtsMatchQuery("   ")
        assertEquals("   ", query)
    }
}
