package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.viewmodel.JuktiViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current

    // Animation states for a smooth entrance
    val imageScale = remember { Animatable(0.92f) }
    val imageAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            imageScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            imageAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(800, easing = EaseInOutCubic)
            )
        }
        
        delay(3000) // Show full-screen splash for 3 seconds
        viewModel.finishSplash()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(com.example.R.drawable.splash_screen_img)
                .crossfade(true)
                .error(com.example.R.drawable.jukti_logo)
                .fallback(com.example.R.drawable.jukti_logo)
                .build(),
            contentDescription = "Full Screen Splash Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = imageScale.value,
                    scaleY = imageScale.value,
                    alpha = imageAlpha.value
                )
        )
    }
}
