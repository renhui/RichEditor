package com.example.richtext.data.note.repository

object NoteSearchQueryFormatter {
    fun buildFtsMatchQuery(raw: String): String {
        val tokens = raw
            .split(Regex("\\s+"))
            .map { token -> token.trim() }
            .filter { token -> token.isNotBlank() }
        if (tokens.isEmpty()) return raw
        return tokens.joinToString(" AND ") { token -> "\"$token\"*" }
    }
}
