package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PlanEntity
import com.example.ui.components.PlanDisplayHelper

@Composable
fun FeaturedPlanBanner(
    plan: PlanEntity,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlanActive: Boolean = false
) {
    var showDetailsDialog by remember { mutableStateOf(false) }

    val cleanFinalPrice = PlanDisplayHelper.formatPrice(plan.finalPrice)
    val cleanOriginalPrice = PlanDisplayHelper.formatPrice(plan.planPrice)
    val cleanDiscount = PlanDisplayHelper.formatDiscount(plan.discount)
    val cleanValidity = PlanDisplayHelper.formatValidity(plan)
    val benefits = PlanDisplayHelper.parseFeatures(plan.features)

    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
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
                            if (!isPlanActive) {
                                showDetailsDialog = false
                                onBuyClick()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isPlanActive,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlanActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                            contentColor = if (isPlanActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(if (isPlanActive) "✅ Active Plan" else "Buy Now", fontWeight = FontWeight.Bold)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        val isDark = androidx.compose.foundation.isSystemInDarkTheme()
        val gradientColors = if (isDark) {
            listOf(
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.primary
            )
        } else {
            listOf(
                Color(0xFF0F766E), // Deep Teal
                Color(0xFF1D4ED8)  // Deep Blue
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (plan.imageUrl.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = plan.imageUrl,
                    contentDescription = plan.planName,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient scrim overlay for perfect contrast & readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.55f),
                                    Color.Black.copy(alpha = 0.90f)
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = Brush.horizontalGradient(colors = gradientColors))
                )
            }

            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.06f),
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 40.dp, y = 20.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = plan.planName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                // Pricing Info Section
                if (cleanFinalPrice.isNotBlank() || cleanOriginalPrice.isNotBlank() || cleanDiscount.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (cleanFinalPrice.isNotBlank()) {
                            Text(
                                text = cleanFinalPrice,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFEB3B)
                            )
                        }
                        if (cleanOriginalPrice.isNotBlank() && cleanOriginalPrice != cleanFinalPrice) {
                            Text(
                                text = cleanOriginalPrice,
                                style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        if (cleanDiscount.isNotBlank()) {
                            Surface(
                                color = Color(0xFFD32F2F),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = cleanDiscount,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Plan & Offer Validity Section
                if (cleanValidity.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFFFFEB3B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Validity: $cleanValidity",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFEB3B)
                        )
                    }
                }

                // Benefits Section
                if (benefits.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    benefits.take(2).forEach { b ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = b,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { showDetailsDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text("More Info", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (!isPlanActive) {
                                onBuyClick()
                            } else {
                                showDetailsDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlanActive) Color.White.copy(alpha = 0.25f) else Color.White,
                            contentColor = if (isPlanActive) Color.White else Color(0xFF1D4ED8)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isPlanActive) "✅ Active" else "Buy Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
