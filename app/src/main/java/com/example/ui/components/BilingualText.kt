package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppLanguage

@Composable
fun BilingualText(
    textEn: String,
    textAs: String,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    if (language == AppLanguage.BOTH) {
        Column(modifier = modifier) {
            val hasEn = textEn.isNotBlank()
            val hasAs = textAs.isNotBlank()

            if (hasEn) {
                Text(
                    text = textEn,
                    style = style,
                    color = color,
                    fontWeight = fontWeight,
                    textAlign = textAlign,
                    maxLines = maxLines,
                    overflow = overflow
                )
            }
            if (hasEn && hasAs) {
                Spacer(modifier = Modifier.height(2.dp))
            }
            if (hasAs) {
                Text(
                    text = textAs,
                    style = style.copy(fontSize = if (style.fontSize.isSp) (style.fontSize.value * 0.95f).sp else style.fontSize),
                    color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else color.copy(alpha = 0.92f),
                    fontWeight = fontWeight,
                    textAlign = textAlign,
                    maxLines = maxLines,
                    overflow = overflow
                )
            }
        }
    } else {
        val displayString = when (language) {
            AppLanguage.ASSAMESE -> textAs.ifBlank { textEn }
            AppLanguage.ENGLISH -> textEn.ifBlank { textAs }
            AppLanguage.BOTH -> if (textEn.isNotBlank() && textAs.isNotBlank()) "$textEn\n$textAs" else textEn.ifBlank { textAs }
        }
        Text(
            text = displayString,
            modifier = modifier,
            style = style,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow
        )
    }
}
