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
import androidx.compose.runtime.Composable

import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import com.example.data.local.PlanEntity

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun FeaturedPlanBanner(
    plan: PlanEntity,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlanActive: Boolean = false
) {
    var showDetailsDialog by remember { mutableStateOf(false) }

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
                    if (plan.planPrice.isNotBlank() || plan.discount.isNotBlank() || plan.finalPrice.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            if (plan.finalPrice.isNotBlank()) {
                                Text(
                                    text = "₹${plan.finalPrice}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (plan.planPrice.isNotBlank()) {
                                Text(
                                    text = "₹${plan.planPrice}",
                                    style = MaterialTheme.typography.titleMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            if (plan.discount.isNotBlank() && plan.discount != "0") {
                                val cleanDisc = plan.discount.replace(Regex("(?i)off"), "").replace("%", "").trim()
                                val discountText = if (cleanDisc.isNotEmpty()) "$cleanDisc % Off" else plan.discount
                                Surface(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = discountText,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (plan.planValidity.isNotBlank()) {
                        Text(
                            text = "Plan Validity: ${plan.planValidity}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (plan.offerValidity.isNotBlank()) {
                        Text(
                            text = "Offer Validity: ${plan.offerValidity}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    Text(
                        text = "Benefits Included:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val benefits = plan.features.split("|").filter { it.isNotBlank() }
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

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "NOT REFUNDABLE • Secure transaction via Google Play Billing",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

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
        if (plan.imageUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showDetailsDialog = true }
            ) {
                coil.compose.AsyncImage(
                    model = plan.imageUrl,
                    contentDescription = plan.planName,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            val gradientColors = if (isDark) {
                listOf(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.primary
                )
            } else {
                listOf(
                    androidx.compose.ui.graphics.Color(0xFF0F766E), // Deep Teal
                    androidx.compose.ui.graphics.Color(0xFF1D4ED8)  // Deep Blue
                )
            }
    
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(colors = gradientColors)
                    )
                    .clickable { showDetailsDialog = true }
            ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f),
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
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                // Pricing Info Section (Plan Price, Discount, Pay Only)
                if (plan.planPrice.isNotBlank() || plan.discount.isNotBlank() || plan.finalPrice.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (plan.finalPrice.isNotBlank()) {
                            Text(
                                text = "₹${plan.finalPrice}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = androidx.compose.ui.graphics.Color(0xFFFFEB3B)
                            )
                        }
                        if (plan.planPrice.isNotBlank()) {
                            Text(
                                text = "₹${plan.planPrice}",
                                style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                            )
                        }
                        if (plan.discount.isNotBlank()) {
                            Surface(
                                color = androidx.compose.ui.graphics.Color(0xFFD32F2F),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (plan.discount.endsWith("%")) "${plan.discount.replace("%", "").trim()}% Off" else "${plan.discount}% Off",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Plan & Offer Validity Section
                if (plan.planValidity.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color(0xFFFFEB3B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Validity: ${plan.planValidity}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color(0xFFFFEB3B)
                        )
                    }
                }

                // Benefits Section
                val benefits = plan.features.split("|").filter { it.isNotBlank() }
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
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = b,
                                style = MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color.White,
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
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Text("More Info", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showDetailsDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlanActive) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.onPrimary,
                            contentColor = if (isPlanActive) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary
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
}
