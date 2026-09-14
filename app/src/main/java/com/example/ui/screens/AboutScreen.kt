package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.AboutConfigEntity
import com.example.ui.components.JuktiTopAppBar
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: JuktiViewModel) {
    val language = AppLanguage.ENGLISH
    val isAssamese = language == AppLanguage.ASSAMESE
    val aboutConfig by viewModel.aboutConfig.collectAsState()

    var showGamificationInfo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            JuktiTopAppBar(
                title = "About Jukti App",
                onBackClick = { viewModel.navigateTo(Screen.ABOUT_LEGAL) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Logo Display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(90.dp)
                    .padding(10.dp)
            ) {
                val imageModifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 200.dp)
                val localLogo = File(LocalContext.current.filesDir, "cached_logo.png")
                if (localLogo.exists() && localLogo.length() > 0) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(localLogo)
                            .crossfade(true)
                            .error(androidx.core.content.ContextCompat.getDrawable(LocalContext.current, com.example.R.drawable.app_logo))
                            .fallback(androidx.core.content.ContextCompat.getDrawable(LocalContext.current, com.example.R.drawable.app_logo))
                            .build(),
                        contentDescription = "App Logo",
                        modifier = imageModifier,
                        contentScale = ContentScale.Fit
                    )
                } else if (aboutConfig.logoUrl.isNotBlank() && aboutConfig.logoUrl.startsWith("http")) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(aboutConfig.logoUrl)
                            .crossfade(true)
                            .error(androidx.core.content.ContextCompat.getDrawable(LocalContext.current, com.example.R.drawable.app_logo))
                            .fallback(androidx.core.content.ContextCompat.getDrawable(LocalContext.current, com.example.R.drawable.app_logo))
                            .build(),
                        contentDescription = "App Logo",
                        modifier = imageModifier,
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AsyncImage(
                        model = com.example.R.drawable.app_logo,
                        contentDescription = "App Logo",
                        modifier = imageModifier,
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Title & Subtitle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = aboutConfig.appTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = aboutConfig.appSubtitleEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Our Mission
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Our Mission",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = aboutConfig.missionEn,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }

            // Key Platform Features
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Key Platform Features",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val features = listOf(
                        "Subject & Chapter-Wise Practice MCQs in English & Assamese",
                        "Assam History, Geography, Polity, Economy, Culture & Special GK",
                        "Full-Length Timed Mock Tests with Instant Answer Key & Solutions",
                        "State-Wide Leaderboard Ranking, Levels, Badges & XP Rewards",
                        "Daily Quiz Practice Challenges with Detailed Explanation Sheets",
                        "Comprehensive Study Notes & Downloadable PDF Revision Sheets",
                        "Detailed Performance Analytics & Exam Clearance Probability",
                        "Weak Topic AI Tutor & Subject-Wise Accuracy Breakdown",
                        "Bookmarked MCQs & Offline Practice Support",
                        "Real-Time Exam Updates, Notifications & Instant Cloud Sync"
                    )

                    features.forEach { feature ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = feature, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Gamification & Leaderboard Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGamificationInfo = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "How XP, Levels & Leaderboard Work",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Learn about exam clearance probability and analytics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Founder Information Card (Read-only)
            FounderCard(
                aboutConfig = aboutConfig
            )

            // Developer tagline & copyright footer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = aboutConfig.developerTagline,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = aboutConfig.copyrightText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showGamificationInfo) {
        GamificationInfoDialog(
            isAssamese = isAssamese,
            onDismiss = { showGamificationInfo = false }
        )
    }
}

@Composable
fun GamificationInfoDialog(
    isAssamese: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "How XP, Levels & Analytics Work",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // XP & Levels
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "XP & Levels",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You earn XP by completing Mock Tests and Practice Quizzes. Gaining more XP helps you level up and unlock new Badges.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Probability of Clearing Exam
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Exam Clearance Probability (Estimate)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This is an automated in-app estimate calculated from your mock test scores, practice question accuracy, and daily study consistency. It is provided for personal practice tracking only and does NOT guarantee or predict that you will pass, clear, qualify, or fail any examination. Real exam results depend on multiple external factors, and Jukti is not responsible for official examination outcomes.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Leaderboard
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Leaderboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The Leaderboard ranks you against other aspirants across Assam. It uses both your total XP and average mock test scores to determine your position.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

@Composable
fun FounderCard(
    aboutConfig: AboutConfigEntity
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val photoUrl = aboutConfig.founderPhotoUrl
                        if (photoUrl.startsWith("custom_founder:")) {
                            val path = photoUrl.removePrefix("custom_founder:")
                            AsyncImage(
                                model = File(path),
                                contentDescription = "Founder Profile Photo - Pinku Bora",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Founder Profile Photo - Pinku Bora",
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = aboutConfig.founderName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = aboutConfig.founderTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = aboutConfig.founderCredential,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val desc = aboutConfig.founderDescription
            val displayDesc = if (isExpanded || desc.length < 100) {
                desc
            } else {
                desc.take(100) + "..."
            }

            Text(
                text = displayDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            if (desc.length >= 100) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Read Less" else "Read More",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
