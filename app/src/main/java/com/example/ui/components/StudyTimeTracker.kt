package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun StudyTimeTracker(
    isActive: Boolean,
    onTimeAccumulated: (Long) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnTimeAccumulated = rememberUpdatedState(onTimeAccumulated)
    
    // Using a class to hold mutable state inside the effect
    val trackerState = remember {
        object {
            var sessionStartTime: Long = 0L
            var accumulatedTime: Long = 0L
            var isTracking: Boolean = false
        }
    }

    DisposableEffect(lifecycleOwner, isActive) {
        trackerState.sessionStartTime = System.currentTimeMillis()
        trackerState.isTracking = isActive

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (trackerState.isTracking) {
                        trackerState.sessionStartTime = System.currentTimeMillis()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    if (trackerState.isTracking && trackerState.sessionStartTime > 0) {
                        val durationMs = System.currentTimeMillis() - trackerState.sessionStartTime
                        trackerState.accumulatedTime += durationMs
                        trackerState.sessionStartTime = 0L
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (trackerState.isTracking && trackerState.sessionStartTime > 0) {
                val durationMs = System.currentTimeMillis() - trackerState.sessionStartTime
                trackerState.accumulatedTime += durationMs
                trackerState.sessionStartTime = 0L
            }
            if (trackerState.accumulatedTime > 0) {
                // Report accumulated time in seconds
                currentOnTimeAccumulated.value(trackerState.accumulatedTime / 1000)
                trackerState.accumulatedTime = 0L
            }
        }
    }
}
