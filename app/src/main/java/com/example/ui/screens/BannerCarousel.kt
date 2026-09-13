package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Check

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AutoShiftingBannerCarousel(
    banners: List<com.example.data.local.BannerEntity>,
    plans: List<com.example.data.local.PlanEntity> = emptyList(),
    language: com.example.ui.viewmodel.AppLanguage,
    isUserPremium: Boolean = false,
    hasEverPurchasedAnyPlan: Boolean = false,
    onUpgradeClick: (com.example.data.local.PlanEntity) -> Unit,
    onNavigateToPlans: () -> Unit = {},
    onBannerClick: ((com.example.data.local.BannerEntity) -> Unit)? = null
) {
    var selectedPlanForDetails by remember { mutableStateOf<com.example.data.local.PlanEntity?>(null) }
    var isRotationPaused by remember { mutableStateOf(false) }
    val activePlans = remember(plans, hasEverPurchasedAnyPlan) {
        plans.filter { plan ->
            plan.isActive && 
            com.example.ui.components.PlanDisplayHelper.isPaidPlan(plan) && 
            !com.example.ui.components.PlanDisplayHelper.isDummyOrHardcodedPlan(plan) &&
            !(hasEverPurchasedAnyPlan && (plan.finalPrice == "9" || plan.planName.contains("Starter Pass", ignoreCase = true)))
        }.sortedWith(compareBy({ it.displayOrder }, { it.id }))
    }

    val displayBanners = if (banners.isEmpty() && activePlans.isEmpty()) {
        listOf(com.example.data.local.BannerEntity(
            titleEn = "Welcome to Jukti",
            titleAs = "Welcome to Jukti",
            subtitleEn = "Test Your Knowledge • Practice Smarter • Improve Your Score",
            subtitleAs = "Test Your Knowledge • Practice Smarter • Improve Your Score",
            type = "INFORMATION",
            badgeText = "WELCOME",
            actionUrl = "",
            isActive = true
        ))
    } else {
        banners
    }
    
    if (selectedPlanForDetails != null) {
        val plan = selectedPlanForDetails!!
        val cleanFinalPrice = com.example.ui.components.PlanDisplayHelper.formatPrice(plan.finalPrice)
        val cleanOriginalPrice = com.example.ui.components.PlanDisplayHelper.formatPrice(plan.planPrice)
        val cleanDiscount = com.example.ui.components.PlanDisplayHelper.formatDiscount(plan.discount)
        val cleanValidity = com.example.ui.components.PlanDisplayHelper.formatValidity(plan)
        val benefits = com.example.ui.components.PlanDisplayHelper.parseFeatures(plan.features)

        AlertDialog(
            onDismissRequest = { 
                selectedPlanForDetails = null
                isRotationPaused = false
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = plan.planName,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    if (cleanFinalPrice.isNotBlank() || cleanOriginalPrice.isNotBlank() || cleanDiscount.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            if (cleanFinalPrice.isNotBlank()) {
                                Text(
                                    text = cleanFinalPrice,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (cleanOriginalPrice.isNotBlank() && cleanOriginalPrice != cleanFinalPrice) {
                                Text(
                                    text = cleanOriginalPrice,
                                    style = MaterialTheme.typography.titleMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            if (cleanDiscount.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = cleanDiscount,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (cleanValidity.isNotBlank()) {
                        Text(
                            text = "Plan Validity: $cleanValidity",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (plan.offerValidity.isNotBlank() && !plan.offerValidity.equals(cleanValidity, ignoreCase = true)) {
                        Text(
                            text = "Offer Note: ${plan.offerValidity}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    if (plan.examTarget.isNotBlank()) {
                        Text(
                            text = "Target Exam: ${plan.examTarget}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (plan.guidanceEnabled) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "💡 Includes Preparation Guidance & Strategy",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (benefits.isNotEmpty()) {
                        Text(
                            text = "Benefits Included:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        benefits.forEach { b ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = b,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!isUserPremium) {
                                selectedPlanForDetails = null
                                isRotationPaused = false
                                onUpgradeClick(plan)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUserPremium,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUserPremium) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                            contentColor = if (isUserPremium) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(if (isUserPremium) "Active Plan" else "Buy Now")
                    }
                }
            },
            confirmButton = {}
        )
    }
    val totalItems = displayBanners.size + activePlans.size

    val pagerState = rememberPagerState(pageCount = { totalItems })
    
    // Smooth auto-shifting loop without keys that interrupt animation
    LaunchedEffect(pagerState, totalItems) {
        if (totalItems > 1) {
            while (true) {
                delay(3500)
                if (!pagerState.isScrollInProgress && !isRotationPaused) {
                    val nextPage = (pagerState.currentPage + 1) % totalItems
                    try {
                        pagerState.animateScrollToPage(nextPage)
                    } catch (e: Exception) {
                        // Ignore touch interruption
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState, 
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) { page ->
            if (page < activePlans.size) {
                val plan = activePlans[page]
                FeaturedPlanBanner(
                    plan = plan,
                    onBuyClick = { onUpgradeClick(plan) },
                    onMoreInfoClick = {
                        selectedPlanForDetails = plan
                        isRotationPaused = true
                    },
                    isPlanActive = isUserPremium
                )
            } else {
                val bannerIndex = page - activePlans.size
                if (bannerIndex in displayBanners.indices) {
                    InfoBannerContent(
                        banner = displayBanners[bannerIndex],
                        language = language,
                        onUpgradeClick = onNavigateToPlans,
                        onBannerClick = onBannerClick,
                        isUserPremium = isUserPremium
                    )
                }
            }
        }

        if (totalItems > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalItems) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBannerContent(
    banner: com.example.data.local.BannerEntity,
    language: com.example.ui.viewmodel.AppLanguage,
    onUpgradeClick: () -> Unit,
    onBannerClick: ((com.example.data.local.BannerEntity) -> Unit)? = null,
    isUserPremium: Boolean = false,
    hasEverPurchasedAnyPlan: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showDetailsDialog by remember { mutableStateOf(false) }

    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = {
                com.example.ui.components.BilingualText(
                    textEn = banner.titleEn,
                    textAs = banner.titleAs.ifEmpty { banner.titleEn },
                    language = language,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    com.example.ui.components.BilingualText(
                        textEn = banner.subtitleEn,
                        textAs = banner.subtitleAs.ifEmpty { banner.subtitleEn },
                        language = language,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val buttonText = when (banner.actionType) {
                        "Mock Test" -> if (language == com.example.ui.viewmodel.AppLanguage.ASSAMESE) "মক টেষ্ট কৰক" else "Take Mock Test"
                        "Study Notes" -> if (language == com.example.ui.viewmodel.AppLanguage.ASSAMESE) "নোটছ পঢ়ক" else "Read Study Notes"
                        "Link" -> if (language == com.example.ui.viewmodel.AppLanguage.ASSAMESE) "অধিক জানক" else "Learn More"
                        else -> if (language == com.example.ui.viewmodel.AppLanguage.ASSAMESE) "খোলক" else "Open"
                    }

                    Button(
                        onClick = {
                            showDetailsDialog = false
                            if (onBannerClick != null) {
                                onBannerClick(banner)
                            } else {
                                onUpgradeClick()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = buttonText, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        if (banner.imageUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        showDetailsDialog = true
                    }
            ) {
                coil.compose.AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = banner.titleEn,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            val gradientColors = if (isDark) {
                listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.tertiary
                )
            } else {
                listOf(
                    Color(0xFF1E3A8A), // Deep Blue
                    Color(0xFF3730A3)  // Deep Indigo
                )
            }
    
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(colors = gradientColors),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        showDetailsDialog = true
                    }
            ) {
            // Subtle Jaapi pattern in the background
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw a few faint Jaapis
                val jaapiPath = androidx.compose.ui.graphics.Path()
                val color = Color.White.copy(alpha = 0.1f)
                
                // Jaapi 1
                jaapiPath.moveTo(size.width * 0.8f, size.height * 0.2f)
                jaapiPath.lineTo(size.width * 0.95f, size.height * 0.5f)
                jaapiPath.lineTo(size.width * 0.65f, size.height * 0.5f)
                jaapiPath.close()
                drawPath(jaapiPath, color)
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(size.width * 0.65f, size.height * 0.5f),
                    end = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.5f),
                    strokeWidth = 10f
                )
                
                // Jaapi 2
                val jaapiPath2 = androidx.compose.ui.graphics.Path()
                jaapiPath2.moveTo(size.width * 0.1f, size.height * 0.7f)
                jaapiPath2.lineTo(size.width * 0.3f, size.height * 1.0f)
                jaapiPath2.lineTo(size.width * -0.1f, size.height * 1.0f)
                jaapiPath2.close()
                drawPath(jaapiPath2, color)
                
                // Silhouette of Assam map (simplified abstract shape)
                val mapPath = androidx.compose.ui.graphics.Path()
                mapPath.moveTo(size.width * 0.2f, size.height * 0.8f)
                mapPath.quadraticBezierTo(size.width * 0.4f, size.height * 0.9f, size.width * 0.6f, size.height * 0.6f)
                mapPath.quadraticBezierTo(size.width * 0.8f, size.height * 0.5f, size.width * 0.9f, size.height * 0.2f)
                mapPath.quadraticBezierTo(size.width * 0.7f, size.height * 0.3f, size.width * 0.5f, size.height * 0.2f)
                mapPath.quadraticBezierTo(size.width * 0.3f, size.height * 0.3f, size.width * 0.2f, size.height * 0.8f)
                drawPath(mapPath, Color.White.copy(alpha = 0.05f))
                
                // Gamosa border along the bottom
                val dashWidth = 15f
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color.Red.copy(alpha = 0.7f),
                        start = androidx.compose.ui.geometry.Offset(x, size.height - 4f),
                        end = androidx.compose.ui.geometry.Offset(x + dashWidth, size.height - 4f),
                        strokeWidth = 8f
                    )
                    x += dashWidth + 10f
                }
            }
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.05f),
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 40.dp, y = 20.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        com.example.ui.components.BilingualText(
                            textEn = banner.titleEn,
                            textAs = banner.titleAs.ifEmpty { banner.titleEn },
                            language = language,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        com.example.ui.components.BilingualText(
                            textEn = banner.subtitleEn,
                            textAs = banner.subtitleAs.ifEmpty { banner.subtitleEn },
                            language = language,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        if (banner.planPrice.isNotBlank() || banner.discount.isNotBlank() || banner.finalPrice.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
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
                                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
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
                            Spacer(modifier = Modifier.height(4.dp))
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
                    }

                    if (banner.imageUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(64.dp)
                        ) {
                            coil.compose.AsyncImage(
                                model = banner.imageUrl,
                                contentDescription = "Banner Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }
                }

                val buttonText = when (banner.actionType) {
                    "Mock Test" -> if (language == com.example.ui.viewmodel.AppLanguage.ASSAMESE) "মক টেষ্ট কৰক" else "Take Mock Test"
                    "Study Notes" -> if (language == com.example.ui.viewmodel.AppLanguage.ASSAMESE) "নোটছ পঢ়ক" else "Read Study Notes"
                    "Link" -> if (language == com.example.ui.viewmodel.AppLanguage.ASSAMESE) "অধিক জানক" else "Learn More"
                    else -> if (language == com.example.ui.viewmodel.AppLanguage.ASSAMESE) "খোলক" else "Open"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (onBannerClick != null) {
                                onBannerClick(banner)
                            } else {
                                onUpgradeClick()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = buttonText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        }
    }
}
