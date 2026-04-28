package com.example.richtext.data.note.repository

object NoteSearchQueryFormatter {
    private const val MIN_FTS_TOKEN_LENGTH = 2

    fun buildFtsMatchQuery(raw: String): String {
        val tokens = raw
            .split(Regex("\\s+"))
            .map { token -> token.trim() }
            .filter { token -> token.isNotBlank() }
        if (tokens.isEmpty()) return raw
        return tokens.joinToString(" AND ") { token -> "${escapeFtsToken(token)}*" }
    }

    fun shouldUseLikeFallback(raw: String): Boolean {
        val tokens = raw
            .split(Regex("\\s+"))
            .map { token -> token.trim() }
            .filter { token -> token.isNotBlank() }
        if (tokens.isEmpty()) return false
        return tokens.any { it.length < MIN_FTS_TOKEN_LENGTH }
    }

    fun buildLikePattern(raw: String): String {
        val normalized = raw.trim()
        val escaped = normalized
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
        return "%$escaped%"
    }

    private fun escapeFtsToken(token: String): String {
        return buildString(token.length) {
            token.forEach { ch ->
                if (ch == '"') append("\"\"") else append(ch)
            }
        }
    }
}
