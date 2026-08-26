package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import com.example.data.local.*
import com.example.ui.components.BilingualText
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.components.getLogoIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val aboutConfig by viewModel.aboutConfig.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val plans by viewModel.plans.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val banners by viewModel.banners.collectAsState()
    val examUpdates by viewModel.examUpdates.collectAsState()
    val mockTests by viewModel.accessibleMockTests.collectAsState()
    val studyNotes by viewModel.accessibleStudyNotes.collectAsState()
    val questions by viewModel.accessibleQuestions.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val isUserPremium by viewModel.isUserPremium.collectAsState()

    var showPomodoroDialog by remember { mutableStateOf(false) }

    if (showPomodoroDialog) {
        PomodoroClockDialog(
            language = language,
            onDismiss = { showPomodoroDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.height(48.dp).padding(4.dp)) {
                            val imageModifier = Modifier.fillMaxHeight().widthIn(max = 140.dp)
                            val localLogo = java.io.File(androidx.compose.ui.platform.LocalContext.current.filesDir, "cached_logo.png")
                            if (localLogo.exists() && localLogo.length() > 0) {
                                coil.compose.AsyncImage(
                                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                        .data(localLogo)
                                        .crossfade(true)
                                        .error(androidx.core.content.ContextCompat.getDrawable(androidx.compose.ui.platform.LocalContext.current, com.example.R.drawable.app_logo))
                                        .fallback(androidx.core.content.ContextCompat.getDrawable(androidx.compose.ui.platform.LocalContext.current, com.example.R.drawable.app_logo))
                                        .build(),
                                    contentDescription = "Jukti Logo",
                                    modifier = imageModifier,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            } else if (aboutConfig.logoUrl.isNotBlank() && aboutConfig.logoUrl.startsWith("http")) {
                                coil.compose.AsyncImage(
                                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                        .data(aboutConfig.logoUrl)
                                        .crossfade(true)
                                        .error(androidx.core.content.ContextCompat.getDrawable(androidx.compose.ui.platform.LocalContext.current, com.example.R.drawable.app_logo))
                                        .fallback(androidx.core.content.ContextCompat.getDrawable(androidx.compose.ui.platform.LocalContext.current, com.example.R.drawable.app_logo))
                                        .build(),
                                    contentDescription = "Jukti Logo",
                                    modifier = imageModifier,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            } else {
                                coil.compose.AsyncImage(
                                    model = com.example.R.drawable.app_logo,
                                    contentDescription = "Jukti Logo",
                                    modifier = imageModifier,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            }
                        }
                        Column {
                            Text(
                                text = aboutConfig.appTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (language == com.example.ui.viewmodel.AppLanguage.ASSAMESE) aboutConfig.appSubtitleAs else aboutConfig.appSubtitleEn,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Notification Button
                        IconButton(onClick = { viewModel.navigateTo(Screen.USER_NOTIFICATIONS) }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Profile Icon Button in top right corner
                        IconButton(onClick = { viewModel.navigateTo(Screen.PROFILE) }) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = userProfile?.name?.take(1)?.uppercase() ?: "U",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // User Welcome & Streak Header Card
            UserWelcomeHeader(userProfile = userProfile, language = language, onProfileClick = {
                viewModel.navigateTo(Screen.PROFILE)
            })

            Spacer(modifier = Modifier.height(24.dp))

            val context = androidx.compose.ui.platform.LocalContext.current
            var selectedBannerForDetails by remember { mutableStateOf<com.example.data.local.BannerEntity?>(null) }
            
            if (selectedBannerForDetails != null) {
                val banner = selectedBannerForDetails!!
                AlertDialog(
                    onDismissRequest = { selectedBannerForDetails = null },
                    title = {
                        Column {
                            if (banner.badgeText.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = banner.badgeText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            com.example.ui.components.BilingualText(
                                textEn = banner.titleEn,
                                textAs = banner.titleAs.ifEmpty { banner.titleEn },
                                language = language,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (banner.imageUrl.isNotEmpty()) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                ) {
                                    coil.compose.AsyncImage(
                                        model = banner.imageUrl,
                                        contentDescription = "Banner Image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }
                            com.example.ui.components.BilingualText(
                                textEn = banner.subtitleEn,
                                textAs = banner.subtitleAs.ifEmpty { banner.subtitleEn },
                                language = language,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (banner.offerValidity.isNotBlank()) {
                                Text(
                                    text = "Note: ${banner.offerValidity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    confirmButton = {
                        if (banner.actionType == "Link" && banner.actionUrl.isNotEmpty()) {
                            Button(onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(banner.actionUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Invalid link URL", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                selectedBannerForDetails = null
                            }) {
                                Text("Visit Link")
                            }
                        } else if (banner.type == "PROMOTIONAL" || banner.actionType == "Plan") {
                            Button(onClick = {
                                selectedBannerForDetails = null
                                viewModel.navigateTo(Screen.PREMIUM_PLANS)
                            }) {
                                Text("View Premium Plans")
                            }
                        } else {
                            Button(onClick = { selectedBannerForDetails = null }) {
                                Text("Close")
                            }
                        }
                    },
                    dismissButton = {
                        if ((banner.actionType == "Link" && banner.actionUrl.isNotEmpty()) || banner.type == "PROMOTIONAL" || banner.actionType == "Plan") {
                            TextButton(onClick = { selectedBannerForDetails = null }) {
                                Text("Close")
                            }
                        }
                    }
                )
            }

            // Banners Carousel / Section (Promotional & Admin Info Banners)
            AutoShiftingBannerCarousel(
                banners = banners,
                plans = plans,
                language = language,
                isUserPremium = isUserPremium,
                onUpgradeClick = { viewModel.navigateTo(Screen.PREMIUM_PLANS) },
                onBannerClick = { banner ->
                    when (banner.actionType) {
                        "Mock Test" -> {
                            val mockId = banner.actionUrl.toLongOrNull()
                            if (mockId != null) {
                                val mock = mockTests.find { it.id == mockId }
                                if (mock != null) {
                                    viewModel.selectMockTest(mock)
                                    viewModel.navigateTo(Screen.MOCK_PLAYER)
                                } else {
                                    viewModel.navigateTo(Screen.MOCK_TESTS)
                                }
                            } else {
                                viewModel.navigateTo(Screen.MOCK_TESTS)
                            }
                        }
                        "Study Notes" -> {
                            val noteId = banner.actionUrl.toLongOrNull()
                            if (noteId != null) {
                                val note = studyNotes.find { it.id == noteId }
                                if (note != null) {
                                    viewModel.selectStudyNote(note)
                                    viewModel.navigateTo(Screen.STUDY_NOTE_DETAIL)
                                } else {
                                    viewModel.navigateTo(Screen.STUDY_NOTES)
                                }
                            } else {
                                viewModel.navigateTo(Screen.STUDY_NOTES)
                            }
                        }
                        "Link" -> {
                            if (banner.actionUrl.isNotEmpty()) {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(banner.actionUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    selectedBannerForDetails = banner
                                }
                            } else {
                                selectedBannerForDetails = banner
                            }
                        }
                        "Plan" -> {
                            viewModel.navigateTo(Screen.PREMIUM_PLANS)
                        }
                        else -> {
                            // Information banner or general details -> show Information Banner Details
                            selectedBannerForDetails = banner
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Navigation Grid
            QuickNavGrid(
                language = language,
                isAdminOrOwner = isAdminOrOwner,
                onNavClick = { screen ->
                    if (screen == Screen.MCQ_STUDY) {
                        viewModel.openStudyMcq(fromHome = true)
                    } else {
                        viewModel.navigateTo(screen)
                    }
                },
                onOpenPomodoro = { showPomodoroDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Performance Summary Widget
            PerformanceSummaryCard(
                userProfile = userProfile,
                language = language,
                onViewAnalytics = { viewModel.navigateTo(Screen.MY_ANALYTICS) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun UserWelcomeHeader(
    userProfile: UserProfileEntity?,
    language: AppLanguage,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onProfileClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userProfile?.name?.take(1)?.uppercase() ?: "J",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Column {
                    Text(
                        text = "Hello, ${userProfile?.name ?: "Scholar"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun PromotionalBannersSection(
    banners: List<BannerEntity>,
    language: AppLanguage,
    onUpgradeClick: () -> Unit
) {
    if (banners.isEmpty()) return

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        val banner = banners.first()
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = banner.badgeText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    BilingualText(
                        textEn = banner.titleEn,
                        textAs = banner.titleAs,
                        language = language,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BilingualText(
                        textEn = banner.subtitleEn,
                        textAs = banner.subtitleAs,
                        language = language,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    if (banner.planPrice.isNotBlank() || banner.discount.isNotBlank() || banner.finalPrice.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (banner.finalPrice.isNotBlank()) {
                                Text(
                                    text = "₹${banner.finalPrice}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFEB3B)
                                )
                            }
                            if (banner.planPrice.isNotBlank()) {
                                Text(
                                    text = "₹${banner.planPrice}",
                                    style = MaterialTheme.typography.bodySmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            if (banner.discount.isNotBlank()) {
                                Surface(
                                    color = Color(0xFFD32F2F),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = banner.discount,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (banner.offerValidity.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFFFFEB3B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Validity: ${banner.offerValidity}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFEB3B)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onUpgradeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Upgrade to Pass Pro",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickNavGrid(
    language: AppLanguage,
    isAdminOrOwner: Boolean,
    onNavClick: (Screen) -> Unit,
    onOpenPomodoro: () -> Unit
) {
    val seventhItem = QuickNavItem(
        titleEn = "Focus Timer",
        titleAs = "ফ'কাচ টাইমাৰ",
        icon = Icons.Default.HourglassTop,
        screen = Screen.MENU,
        color = BrandSecondary
    )

    val items = listOf(
        QuickNavItem("Learn", "শিকা", Icons.Default.MenuBook, Screen.MCQ_STUDY, BrandPrimary),
        QuickNavItem("Practice", "অনুশীলন", Icons.Default.Quiz, Screen.PRACTICE, BrandSecondary),
        QuickNavItem("Mock Test", "মক পৰীক্ষা", Icons.Default.Timer, Screen.MOCK_TESTS, BrandAccent),
        QuickNavItem("Study Notes", "অধ্যয়ন নোটছ", Icons.Default.StickyNote2, Screen.STUDY_NOTES, BrandWarning),
        QuickNavItem("Exam Updates", "পৰীক্ষাৰ জাননী", Icons.Default.NotificationsActive, Screen.EXAM_INFO, BrandPrimary),
        QuickNavItem("Leaderboard", "লিডাৰব'ৰ্ড", Icons.Default.Leaderboard, Screen.LEADERBOARD, BrandAccent),
        seventhItem,
        QuickNavItem("All Plans", "সকলো প্লাণ", Icons.Default.WorkspacePremium, Screen.PREMIUM_PLANS, BrandWarning)
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Quick Navigation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.take(4).forEach { item ->
                QuickNavItemCard(
                    item = item,
                    language = language,
                    onClick = {
                        if (item.titleEn == "Focus Timer") {
                            onOpenPomodoro()
                        } else {
                            onNavClick(item.screen)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.drop(4).take(4).forEach { item ->
                QuickNavItemCard(
                    item = item,
                    language = language,
                    onClick = {
                        if (item.titleEn == "Focus Timer") {
                            onOpenPomodoro()
                        } else {
                            onNavClick(item.screen)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

data class QuickNavItem(
    val titleEn: String,
    val titleAs: String,
    val icon: ImageVector,
    val screen: Screen,
    val color: Color
)

@Composable
fun QuickNavItemCard(
    item: QuickNavItem,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        onClick = onClick,
        modifier = modifier.scale(scale),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = item.color.copy(alpha = 0.15f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            BilingualText(
                textEn = item.titleEn,
                textAs = item.titleAs,
                language = language,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DailyQuizCard(language: AppLanguage, onStartQuiz: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Daily Quiz Challenge",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Daily Quick MCQs Challenge - Earn XP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Topics: Assam History & Current Affairs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onStartQuiz,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start Quiz")
            }
        }
    }
}

@Composable
fun PerformanceSummaryCard(
    userProfile: UserProfileEntity?,
    language: AppLanguage,
    onViewAnalytics: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onViewAnalytics() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Performance Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onViewAnalytics) {
                    Text("View Analytics")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val p = userProfile
                val accuracyStr = if (p != null && p.totalSolved > 0) {
                    String.format("%.1f%%", (p.correctCount.toFloat() / p.totalSolved) * 100)
                } else {
                    "0.0%"
                }
                val practiceSpeed = if (p != null && p.totalSolved > 0) {
                    val avgSeconds = (p.totalTimeMinutes * 60) / p.totalSolved
                    "${avgSeconds}s"
                } else {
                    "0s"
                }
Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatItemCard(
                        modifier = Modifier.weight(1f),
                        title = "Questions Solved",
                        value = "${p?.totalSolved ?: 0}",
                        icon = "📝"
                    )
                    StatItemCard(
                        modifier = Modifier.weight(1f),
                        title = "Accuracy",
                        value = accuracyStr,
                        icon = "🎯"
                    )
                }
Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatItemCard(
                        modifier = Modifier.weight(1f),
                        title = "Practice Speed / MCQ",
                        value = practiceSpeed,
                        icon = "⚡"
                    )
                    StatItemCard(
                        modifier = Modifier.weight(1f),
                        title = "Level",
                        value = "Lvl ${p?.level ?: 1}",
                        icon = "📈",
                        isBadge = true
                    )
                }
            }
        }
    }
}

@Composable
fun StatItemCard(modifier: Modifier = Modifier, title: String, value: String, icon: String = "", isBadge: Boolean = false) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isBadge) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (icon.isNotEmpty()) {
                    Text(text = icon, style = MaterialTheme.typography.labelMedium)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    titleEn: String,
    titleAs: String,
    language: AppLanguage,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BilingualText(
            textEn = titleEn,
            textAs = titleAs,
            language = language,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onSeeAllClick) {
            Text("See All")
        }
    }
}

@Composable
fun ExamUpdateCard(
    update: ExamUpdateEntity,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = update.examName,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = update.updateDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            BilingualText(
                textEn = update.titleEn,
                textAs = update.titleAs,
                language = language,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MockTestCard(
    mock: MockTestEntity,
    language: AppLanguage,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "${mock.durationMinutes} Mins • ${mock.totalQuestions} Qs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            BilingualText(
                textEn = mock.titleEn,
                textAs = mock.titleAs,
                language = language,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (mock.isCompleted) {
                        "Re-attempt"
                    } else {
                        "Attempt Mock"
                    }
                )
            }
        }
    }
}

@Composable
fun StudyNoteCard(
    note: StudyNoteEntity,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = note.subject,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            BilingualText(
                textEn = note.titleEn,
                textAs = note.titleAs,
                language = language,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${note.readTimeMinutes} min read",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CurrentAffairsSection(language: AppLanguage, onStartPractice: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Assam & National Current Affairs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Daily updated news capsules and study notes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onStartPractice,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun LeaderboardMiniBanner(
    userProfile: UserProfileEntity?,
    language: AppLanguage,
    onViewLeaderboard: () -> Unit
) {
    val xp = userProfile?.xp ?: 0
    val rankText = if (xp > 0) "Assam State Rank: #14" else "Unranked"
    val subtitleText = if (xp > 0) "You are in Top 5% of scholars in Assam" else "Solve questions to get ranked"

    Card(
        onClick = onViewLeaderboard,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.warning,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = rankText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroClockDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isAssamese = language == AppLanguage.ASSAMESE
    var totalSeconds by remember { mutableStateOf(25 * 60) }
    var initialSeconds by remember { mutableStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var sessionsCompleted by remember { mutableStateOf(1) }
    var isBreakMode by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, totalSeconds) {
        if (isRunning && totalSeconds > 0) {
            kotlinx.coroutines.delay(1000L)
            totalSeconds -= 1
        } else if (isRunning && totalSeconds == 0) {
            isRunning = false
            if (!isBreakMode) {
                sessionsCompleted += 1
                isBreakMode = true
                totalSeconds = 5 * 60
                initialSeconds = 5 * 60
            } else {
                isBreakMode = false
                totalSeconds = 25 * 60
                initialSeconds = 25 * 60
            }
        }
    }

    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timeFormatted = "%02d:%02d".format(minutes, seconds)
    val progress = if (initialSeconds > 0) totalSeconds.toFloat() / initialSeconds.toFloat() else 0f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Focus Timer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isBreakMode) {
                        "☕ Short Rest & Refresh"
                    } else {
                        "🎯 Deep Focus Study Cycle"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isBreakMode) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = !isBreakMode && initialSeconds == 25 * 60,
                        onClick = {
                            isBreakMode = false
                            isRunning = false
                            totalSeconds = 25 * 60
                            initialSeconds = 25 * 60
                        },
                        label = { Text("25m Focus", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isBreakMode && initialSeconds == 5 * 60,
                        onClick = {
                            isBreakMode = true
                            isRunning = false
                            totalSeconds = 5 * 60
                            initialSeconds = 5 * 60
                        },
                        label = { Text("5m Break", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isBreakMode && initialSeconds == 50 * 60,
                        onClick = {
                            isBreakMode = false
                            isRunning = false
                            totalSeconds = 50 * 60
                            initialSeconds = 50 * 60
                        },
                        label = { Text("50m Study", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isRunning) "Pause" else "Start Timer", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            isRunning = false
                            totalSeconds = initialSeconds
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
                    }
                }

                Text(
                    text = "Completed Sessions Today: $sessionsCompleted 🔥",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

