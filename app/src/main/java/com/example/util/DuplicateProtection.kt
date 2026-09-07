package com.example.util

import java.security.MessageDigest

private val WHITESPACE_REGEX = Regex("\\s+")
private val PUNCTUATION_REGEX = Regex("\\s*([?!.,;:])\\s*")

fun generateDuplicateKey(questionText: String): String {
    if (questionText.isBlank()) return ""
    val normalized = questionText
        .lowercase()
        .replace(WHITESPACE_REGEX, " ")
        .replace(PUNCTUATION_REGEX, "$1")
        .trim()
        
    if (normalized.isEmpty()) return ""
    val bytes = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}
