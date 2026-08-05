package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    onUpgradeClick: () -> Unit,
    onBannerClick: ((com.example.data.local.BannerEntity) -> Unit)? = null
) {

    val activePlansCount = if (isUserPremium) 0 else plans.count { it.isActive }
    val pageCount = banners.size + (if (isUserPremium) 0 else if (activePlansCount > 0) activePlansCount else 1)
    
    if (pageCount == 0) return

    val pagerState = rememberPagerState(pageCount = { pageCount })
    
    LaunchedEffect(pagerState) {
        while(true) {
            delay(3000)
            if (pagerState.pageCount > 0) {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % pagerState.pageCount)
            }
        }
    }
    

        HorizontalPager(
            state = pagerState, 
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) { page ->
            if (isUserPremium) {
                if (page >= 0 && page < banners.size) {
                    InfoBannerContent(
                        banner = banners[page],
                        language = language,
                        onUpgradeClick = onUpgradeClick,
                        onBannerClick = onBannerClick,
                        isUserPremium = isUserPremium
                    )
                }
            } else {
                val activePlans = plans.filter { it.isActive }
                if (activePlans.isNotEmpty()) {
                    if (page < activePlans.size) {
                        val plan = activePlans[page]
                        FeaturedPlanBanner(
                            plan = plan,
                            onBuyClick = onUpgradeClick
                        )
                    } else {
                        val bannerIndex = page - activePlans.size
                        if (bannerIndex >= 0 && bannerIndex < banners.size) {
                            InfoBannerContent(
                                banner = banners[bannerIndex],
                                language = language,
                                onUpgradeClick = onUpgradeClick,
                                onBannerClick = onBannerClick,
                                isUserPremium = isUserPremium
                            )
                        }
                    }
                } else {
                    if (page == 0) {
                        // Fallback empty banner or dummy if no plans
                        FeaturedPlanBanner(
                            plan = com.example.data.local.PlanEntity(
                                planName = "Premium Access",
                                planPrice = "₹499",
                                discount = "0",
                                finalPrice = "499",
                                offerValidity = "",
                                features = "Full Access|All Mocks",
                                isActive = true
                            ),
                            onBuyClick = onUpgradeClick
                        )
                    } else {
                        val bannerIndex = page - 1
                        if (bannerIndex >= 0 && bannerIndex < banners.size) {
                            InfoBannerContent(
                                banner = banners[bannerIndex],
                                language = language,
                                onUpgradeClick = onUpgradeClick,
                                onBannerClick = onBannerClick,
                                isUserPremium = isUserPremium
                            )
                        }
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
    isUserPremium: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
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
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(colors = gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable {
                    if (onBannerClick != null) {
                        onBannerClick(banner)
                    } else {
                        onUpgradeClick()
                    }
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
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                        com.example.ui.components.BilingualText(
                            textEn = banner.titleEn,
                            textAs = banner.titleAs.ifEmpty { banner.titleEn },
                            language = language,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        com.example.ui.components.BilingualText(
                            textEn = banner.subtitleEn,
                            textAs = banner.subtitleAs.ifEmpty { banner.subtitleEn },
                            language = language,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    if (banner.imageUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(72.dp)
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

                Spacer(modifier = Modifier.height(12.dp))
                
                val buttonText = when (banner.actionType) {
                    "Mock Test" -> if (language == com.example.ui.viewmodel.AppLanguage.ASSAMESE) "মক টেষ্ট কৰক" else "Take Mock Test"
                    "Study Notes" -> if (language == com.example.ui.viewmodel.AppLanguage.ASSAMESE) "নোটছ পঢ়ক" else "Read Study Notes"
                    "Link" -> if (language == com.example.ui.viewmodel.AppLanguage.ASSAMESE) "অধিক জানক" else "Learn More"
                    else -> if (language == com.example.ui.viewmodel.AppLanguage.ASSAMESE) "খোলক" else "Open"
                }

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
                    shape = RoundedCornerShape(24.dp),
                    
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
