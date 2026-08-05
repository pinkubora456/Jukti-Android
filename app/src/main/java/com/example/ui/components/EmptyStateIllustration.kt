package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class EmptyStateType {
    RHINO_BOOK,
    STUDENT_JAAPI,
    NOTEBOOK_GAMOSA
}

@Composable
fun EmptyStateIllustration(
    type: EmptyStateType,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            when (type) {
                EmptyStateType.RHINO_BOOK -> {
                    // Rhino + Book
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).align(Alignment.BottomEnd).offset(x = 10.dp, y = 10.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                EmptyStateType.STUDENT_JAAPI -> {
                    // Student + Jaapi
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).offset(y = 20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    // Draw a simple Jaapi top
                    Canvas(modifier = Modifier.fillMaxSize().offset(y = (-10).dp)) {
                        val jaapiPath = Path().apply {
                            moveTo(size.width / 2, size.height * 0.2f) // Top tip
                            lineTo(size.width * 0.8f, size.height * 0.6f) // Right brim
                            lineTo(size.width * 0.2f, size.height * 0.6f) // Left brim
                            close()
                        }
                        drawPath(path = jaapiPath, color = Color(0xFFE53935))
                        drawLine(
                            color = Color(0xFFFDD835),
                            start = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.6f),
                            end = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.6f),
                            strokeWidth = 8f
                        )
                    }
                }
                EmptyStateType.NOTEBOOK_GAMOSA -> {
                    // Notebook + Gamosa
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(70.dp).offset(x = (-10).dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Canvas(modifier = Modifier.fillMaxSize().offset(x = 30.dp, y = 20.dp)) {
                        drawRect(
                            color = Color.White,
                            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.2f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.2f, size.height * 0.6f)
                        )
                        // Gamosa red borders
                        drawLine(
                            color = Color.Red,
                            start = androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.25f),
                            end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.25f),
                            strokeWidth = 8f
                        )
                        drawLine(
                            color = Color.Red,
                            start = androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.75f),
                            end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.75f),
                            strokeWidth = 8f
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
