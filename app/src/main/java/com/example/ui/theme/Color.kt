package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Brand Palette - Modern, Premium System
val BrandPrimary = Color(0xFF2563EB)         // Royal Blue
val BrandPrimaryLight = Color(0xFF3B82F6)    // Lighter Blue for dark theme contrast
val BrandPrimaryContainerLight = Color(0xFFDBEAFE) // Blue 100
val BrandOnPrimaryContainerLight = Color(0xFF1E3A8A) // Blue 900
val BrandPrimaryContainerDark = Color(0xFF1E3A8A)  // Blue 900
val BrandOnPrimaryContainerDark = Color(0xFFDBEAFE) // Blue 100

val BrandSecondary = Color(0xFF4F46E5)       // Indigo
val BrandSecondaryContainerLight = Color(0xFFE0E7FF) // Indigo 100
val BrandOnSecondaryContainerLight = Color(0xFF3730A3) // Indigo 800
val BrandSecondaryContainerDark = Color(0xFF312E81)  // Indigo 900
val BrandOnSecondaryContainerDark = Color(0xFFE0E7FF) // Indigo 100

val BrandAccent = Color(0xFF14B8A6)          // Teal (Positive actions & achievements)
val BrandAccentContainerLight = Color(0xFFCCFBF1)   // Teal 100
val BrandOnAccentContainerLight = Color(0xFF115E59)  // Teal 800
val BrandAccentContainerDark = Color(0xFF134E4A)     // Teal 900
val BrandOnAccentContainerDark = Color(0xFFCCFBF1)   // Teal 100

val BrandSuccess = Color(0xFF22C55E)         // Success Green
val BrandSuccessContainerLight = Color(0xFFDCFCE7)  // Green 100
val BrandOnSuccessContainerLight = Color(0xFF15803D) // Green 800
val BrandSuccessContainerDark = Color(0xFF14532D)   // Green 900
val BrandOnSuccessContainerDark = Color(0xFFDCFCE7)  // Green 100

val BrandWarning = Color(0xFFF59E0B)         // Warning Amber
val BrandWarningContainerLight = Color(0xFFFEF3C7)  // Amber 100
val BrandOnWarningContainerLight = Color(0xFFB45309) // Amber 800
val BrandWarningContainerDark = Color(0xFF78350F)   // Amber 900
val BrandOnWarningContainerDark = Color(0xFFFEF3C7)  // Amber 100

val BrandError = Color(0xFFEF4444)           // Error Red
val BrandErrorContainerLight = Color(0xFFFEE2E2)    // Red 100
val BrandOnErrorContainerLight = Color(0xFF991B1B)   // Red 800
val BrandErrorContainerDark = Color(0xFF7F1D1D)     // Red 900
val BrandOnErrorContainerDark = Color(0xFFFEE2E2)    // Red 100

// Light Theme Neutrals
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF64748B)
val LightOutline = Color(0xFF94A3B8)
val LightBorder = Color(0xFFE2E8F0)

// Dark Theme Neutrals
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSurfaceVariant = Color(0xFF334155)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurfaceVariant = Color(0xFFCBD5E1)
val DarkOutline = Color(0xFF64748B)
val DarkBorder = Color(0xFF334155)

// ColorScheme Helpers for Brand Extensions
val androidx.compose.material3.ColorScheme.successContainer: Color
    @androidx.compose.runtime.Composable
    get() = if (androidx.compose.foundation.isSystemInDarkTheme()) BrandSuccessContainerDark else BrandSuccessContainerLight

val androidx.compose.material3.ColorScheme.onSuccessContainer: Color
    @androidx.compose.runtime.Composable
    get() = if (androidx.compose.foundation.isSystemInDarkTheme()) BrandOnSuccessContainerDark else BrandOnSuccessContainerLight

val androidx.compose.material3.ColorScheme.success: Color
    get() = BrandSuccess

val androidx.compose.material3.ColorScheme.warningContainer: Color
    @androidx.compose.runtime.Composable
    get() = if (androidx.compose.foundation.isSystemInDarkTheme()) BrandWarningContainerDark else BrandWarningContainerLight

val androidx.compose.material3.ColorScheme.onWarningContainer: Color
    @androidx.compose.runtime.Composable
    get() = if (androidx.compose.foundation.isSystemInDarkTheme()) BrandOnWarningContainerDark else BrandOnWarningContainerLight

val androidx.compose.material3.ColorScheme.warning: Color
    get() = BrandWarning

val androidx.compose.material3.ColorScheme.accent: Color
    get() = BrandAccent

val androidx.compose.material3.ColorScheme.accentContainer: Color
    @androidx.compose.runtime.Composable
    get() = if (androidx.compose.foundation.isSystemInDarkTheme()) BrandAccentContainerDark else BrandAccentContainerLight

val androidx.compose.material3.ColorScheme.onAccentContainer: Color
    @androidx.compose.runtime.Composable
    get() = if (androidx.compose.foundation.isSystemInDarkTheme()) BrandOnAccentContainerDark else BrandOnAccentContainerLight


