package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.billing.PlayBillingManager
import com.example.data.local.PlanEntity
import com.example.ui.components.PlanDisplayHelper
import com.example.ui.viewmodel.JuktiViewModel
import kotlinx.coroutines.launch

@Composable
fun PremiumPlansScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val plans by viewModel.plans.collectAsState()
    val userEntitlements by viewModel.userEntitlements.collectAsState()
    val hasEverPurchasedAnyPlan = remember(userEntitlements) {
        userEntitlements.any { !it.planName.equals("Free Plan", ignoreCase = true) }
    }
    val isUserPremium by viewModel.isUserPremium.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    
    var selectedPlan by remember { mutableStateOf<PlanEntity?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    val billingManager = remember { PlayBillingManager(context) }
    val billingStatus by billingManager.billingStatus.collectAsState()
    val pendingVerificationInfo by billingManager.pendingVerificationInfo.collectAsState()

    LaunchedEffect(Unit) {
        billingManager.startConnection()
    }

    DisposableEffect(Unit) {
        onDispose {
            billingManager.destroy()
        }
    }

    LaunchedEffect(billingStatus) {
        billingStatus?.let { statusMsg ->
            Toast.makeText(context, statusMsg, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(pendingVerificationInfo) {
        pendingVerificationInfo?.let { info ->
            viewModel.verifyAndProvisionPurchase(
                purchaseToken = info.purchaseToken,
                purchaseId = info.purchaseId,
                planId = info.planId,
                planName = info.planName,
                validity = info.validity,
                productId = info.productId
            )
            billingManager.clearVerificationInfo()
        }
    }

    LaunchedEffect(plans) {
        if (plans.isNotEmpty() && selectedPlan == null) {
            selectedPlan = plans.firstOrNull { it.isActive && PlanDisplayHelper.isPaidPlan(it) }
        }
    }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Subscription Plans",
                onBackClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.HOME) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner
            PremiumHeaderBanner(isUserPremium = isUserPremium)

            // Filter active plans created by admin/owner, excluding any dummy or hardcoded plans
            val activePlans = remember(plans, hasEverPurchasedAnyPlan) {
                plans.filter { plan ->
                    plan.isActive && !PlanDisplayHelper.isDummyOrHardcodedPlan(plan) &&
                    !(hasEverPurchasedAnyPlan && (plan.finalPrice == "9" || plan.planName.contains("Starter Pass", ignoreCase = true)))
                }.sortedWith(compareBy({ it.displayOrder }, { it.id }))
            }

            if (activePlans.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
                            Text(
                                text = "Checking for plans from server...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "No Subscription Plans Available",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAdminOrOwner) {
                                    "No plans have been created yet. You can create new subscription plans from the Workspace."
                                } else {
                                    "Subscription plans created by the administrator will appear here once published."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isAdminOrOwner) {
                                Button(
                                    onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.CREATE_PLAN) },
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Create Plan")
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    isRefreshing = true
                                    viewModel.refreshDataFromFirebase()
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(1500)
                                        isRefreshing = false
                                    }
                                },
                                enabled = !isRefreshing
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Check for Updates")
                            }
                        }
                    }
                }
            } else {
                val (paidPlans, freePlans) = remember(activePlans) {
                    activePlans.partition { PlanDisplayHelper.isPaidPlan(it) }
                }
                    // Render each active paid plan with full details
                    paidPlans.forEach { plan ->
                        val isSpecificActive = viewModel.isSpecificPlanActive(plan)
                        FullPlanDetailCard(
                            plan = plan,
                            isPlanActive = isSpecificActive,
                            onSubscribeClick = {
                                coroutineScope.launch {
                                    val (canBuy, reasonMsg) = viewModel.validatePurchaseEligibility(plan)
                                    if (!canBuy) {
                                        Toast.makeText(context, reasonMsg, Toast.LENGTH_LONG).show()
                                    } else {
                                        if (activity != null) {
                                            selectedPlan = plan
                                            billingManager.buyPlan(
                                                activity = activity,
                                                planId = plan.id.toString(),
                                                planName = plan.planName,
                                                explicitProductId = plan.googlePlayProductId,
                                                planValidity = plan.planValidity.ifBlank { "1 year" }
                                            )
                                        } else {
                                            Toast.makeText(context, "Activity reference not available for Play Billing.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        )
                    }

                    // Render Free Plan card if configured
                    freePlans.forEach { freePlan ->
                        FreePlanDetailCard(plan = freePlan)
                    }
                }

                // Trust & Security Footer
                TrustBadgeFooter()
            }
        }
    }

@Composable
private fun PremiumHeaderBanner(isUserPremium: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUserPremium) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isUserPremium) Icons.Default.CheckCircle else Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isUserPremium) "Active Premium Member" else "Upgrade Your Preparation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = if (isUserPremium) {
                    "You have full access to premium mock tests, revision notes, PYQs, and tailored guidance strategies."
                } else {
                    "Unlock full-length mock tests, exam-oriented study notes, PYQ strategy, and personalized mentorship."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FullPlanDetailCard(
    plan: PlanEntity,
    isPlanActive: Boolean,
    onSubscribeClick: () -> Unit
) {
    val cleanFinalPrice = PlanDisplayHelper.formatPrice(plan.finalPrice)
    val cleanOriginalPrice = PlanDisplayHelper.formatPrice(plan.planPrice)
    val cleanDiscount = PlanDisplayHelper.formatDiscount(plan.discount)
    val cleanValidity = PlanDisplayHelper.formatValidity(plan)
    val benefits = PlanDisplayHelper.parseFeatures(plan.features)


    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Optional Header Banner Image
            if (plan.imageUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    AsyncImage(
                        model = plan.imageUrl,
                        contentDescription = plan.planName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Tag & Plan Name Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (plan.planBadge.isNotBlank() && !plan.planBadge.equals("None", ignoreCase = true)) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = plan.planBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = plan.planName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (plan.examTarget.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🎯 Target: ${plan.examTarget}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Badges
                    if (isPlanActive) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Pricing Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                            style = MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.LineThrough),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    if (cleanDiscount.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = cleanDiscount,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Validity & Offer Tags Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "⏱ $cleanValidity",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (plan.offerValidity.isNotBlank() && !plan.offerValidity.equals(cleanValidity, ignoreCase = true)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "🔥 ${plan.offerValidity}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Guidance Tag (if enabled)
                if (plan.guidanceEnabled) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Includes Exam Strategy & PYQ Guidance",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Benefits Checklist
                if (benefits.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Included Benefits:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        benefits.forEach { benefit ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = benefit,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // CTA Button
                Button(
                    onClick = onSubscribeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlanActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                        contentColor = if (isPlanActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = !isPlanActive
                ) {
                    if (isPlanActive) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Current Active Plan", fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            text = if (cleanFinalPrice.isNotBlank()) "Subscribe Now • $cleanFinalPrice" else "Subscribe Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FreePlanDetailCard(plan: PlanEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.planName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "FREE TIER",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            val benefits = PlanDisplayHelper.parseFeatures(plan.features)
            if (benefits.isNotEmpty()) {
                benefits.forEach { b ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = b,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "Access to free practice tests and daily questions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TrustBadgeFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Secure Payments via Google Play Billing",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "Instant access activation • Plans valid as specified",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
