package com.example.util

import java.security.MessageDigest

fun generateDuplicateKey(questionText: String): String {
    val normalized = questionText
        .lowercase()
        .replace(Regex("\\s+"), " ")
        .replace(Regex("\\s*([?!.,;:])\\s*"), "$1")
        .trim()
        
    val bytes = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
