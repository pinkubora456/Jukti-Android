package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.getLogoIcon
import com.example.ui.viewmodel.JuktiViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: JuktiViewModel) {
    val aboutConfig by viewModel.aboutConfig.collectAsState()
    
    LaunchedEffect(Unit) {
        delay(2000) // Show splash for 2 seconds
        viewModel.finishSplash()
    }
    
    val transition = rememberInfiniteTransition(label = "footprints")
    val footprintAlpha1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fp1"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)), // Very light blue/white
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getLogoIcon(aboutConfig.logoIconName),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = aboutConfig.appTitle,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Jukti Test Your Knowledge",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Gamosa loading bar mock
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val spacing = 20f
                    val dashWidth = 10f
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = Color.Red,
                            start = Offset(x, size.height / 2),
                            end = Offset(x + dashWidth, size.height / 2),
                            strokeWidth = size.height,
                            alpha = footprintAlpha1
                        )
                        x += spacing
                    }
                }
            }
        }
        
        // Gamosa border at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .align(Alignment.BottomCenter)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val diamondWidth = 30f
                var x = 0f
                while (x < size.width + diamondWidth) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(x, size.height / 2)
                        lineTo(x + diamondWidth / 2, 0f)
                        lineTo(x + diamondWidth, size.height / 2)
                        lineTo(x + diamondWidth / 2, size.height)
                        close()
                    }
                    drawPath(path = path, color = Color.Red)
                    x += diamondWidth + 10f
                }
                
                // Top and bottom thin red lines
                drawLine(
                    color = Color.Red,
                    start = Offset(0f, 2f),
                    end = Offset(size.width, 2f),
                    strokeWidth = 4f
                )
                drawLine(
                    color = Color.Red,
                    start = Offset(0f, size.height - 2f),
                    end = Offset(size.width, size.height - 2f),
                    strokeWidth = 4f
                )
            }
        }
    }
}
