package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import com.example.ui.theme.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.billing.PlayBillingManager
import com.example.data.local.PlanEntity
import com.example.ui.viewmodel.JuktiViewModel
import kotlinx.coroutines.launch

@Composable
fun PremiumPlansScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val plans by viewModel.plans.collectAsState()
    val isUserPremium by viewModel.isUserPremium.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    
    var selectedPlan by remember { mutableStateOf<PlanEntity?>(null) }
    
    val billingManager = remember { PlayBillingManager(context) }
    val billingStatus by billingManager.billingStatus.collectAsState()
    val pendingVerificationInfo by billingManager.pendingVerificationInfo.collectAsState()
    val entitlement by viewModel.userEntitlement.collectAsState()

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
            selectedPlan = plans.firstOrNull { it.isActive }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        com.example.ui.components.JuktiTopAppBar(
            title = "All Plans",
            onBackClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.HOME) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (plans.isEmpty()) {
                Text("No plans available right now.", style = MaterialTheme.typography.titleMedium)
                return@Column
            }

        val activePlans = plans.filter { it.isActive }

        if (activePlans.isEmpty()) {
            Text("No active plans available right now.", style = MaterialTheme.typography.titleMedium)
            return@Column
        }

        activePlans.forEach { plan ->
            val isSpecificActive = viewModel.isSpecificPlanActive(plan)

            FeaturedPlanBanner(
                plan = plan,
                isPlanActive = isSpecificActive,
                onBuyClick = {
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
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "NOT REFUNDABLE • Secure transaction via Google Play Billing",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
}


