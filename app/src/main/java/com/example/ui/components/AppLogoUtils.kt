package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getLogoIcon(name: String): ImageVector {
    return when (name.trim()) {
        "Book" -> Icons.Default.MenuBook
        "Library" -> Icons.Default.LocalLibrary
        "Star" -> Icons.Default.Star
        "Sparkles" -> Icons.Default.AutoAwesome
        "Psychology" -> Icons.Default.Psychology
        "Award" -> Icons.Default.WorkspacePremium
        "Trophy" -> Icons.Default.EmojiEvents
        "Balance" -> Icons.Default.AccountBalance
        "Gavel" -> Icons.Default.Gavel
        "Graduation" -> Icons.Default.HistoryEdu
        "Lightbulb" -> Icons.Default.Lightbulb
        else -> Icons.Default.School
    }
}
