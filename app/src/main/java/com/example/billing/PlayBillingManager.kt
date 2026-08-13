package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class VerifiedPurchaseInfo(
    val purchaseToken: String,
    val purchaseId: String,
    val planId: String,
    val planName: String,
    val productId: String,
    val validity: String
)

class PlayBillingManager(private val context: Context) : PurchasesUpdatedListener {

    private val TAG = "PlayBillingManager"
    private val prefs = context.getSharedPreferences("jukti_billing_prefs", Context.MODE_PRIVATE)

    private val _billingStatus = MutableStateFlow<String?>(null)
    val billingStatus: StateFlow<String?> = _billingStatus.asStateFlow()

    private val _pendingVerificationPurchase = MutableStateFlow<Purchase?>(null)
    val pendingVerificationPurchase: StateFlow<Purchase?> = _pendingVerificationPurchase.asStateFlow()

    private val _pendingVerificationInfo = MutableStateFlow<VerifiedPurchaseInfo?>(null)
    val pendingVerificationInfo: StateFlow<VerifiedPurchaseInfo?> = _pendingVerificationInfo.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun startConnection(onReady: (() -> Unit)? = null) {
        if (billingClient.isReady) {
            queryPurchases()
            onReady?.invoke()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Google Play Billing Client setup completed successfully.")
                    queryPurchases()
                    onReady?.invoke()
                } else {
                    val msg = when (billingResult.responseCode) {
                        BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
                        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE ->
                            "Google Play Store billing service is currently unavailable on this device."
                        else -> "Google Play Billing Connection status (${billingResult.responseCode}): ${billingResult.debugMessage}"
                    }
                    Log.w(TAG, msg)
                    _billingStatus.value = msg
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Google Play Billing service disconnected.")
            }
        })
    }

    fun buyPlan(
        activity: Activity,
        planId: String,
        planName: String,
        explicitProductId: String = "",
        planValidity: String = "1 year"
    ) {
        val sanitizedProductId = explicitProductId.trim().ifBlank {
            planName.lowercase(Locale.ROOT)
                .replace(" ", "_")
                .replace("[^a-z0-9_]".toRegex(), "")
                .ifEmpty { "jukti_premium_pass" }
        }

        // Save purchase context to handle Activity/Screen recreation
        prefs.edit()
            .putString("last_plan_id", planId)
            .putString("last_plan_name", planName)
            .putString("last_product_id", sanitizedProductId)
            .putString("last_validity", planValidity)
            .apply()

        if (!billingClient.isReady) {
            startConnection {
                queryAndLaunchBillingFlow(activity, sanitizedProductId)
            }
        } else {
            queryAndLaunchBillingFlow(activity, sanitizedProductId)
        }
    }

    private fun queryAndLaunchBillingFlow(activity: Activity, productId: String) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("jukti_premium_pass")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !productDetailsList.isNullOrEmpty()) {
                val productDetails = productDetailsList[0]

                val productDetailsParamsList = if (productDetails.productType == BillingClient.ProductType.SUBS) {
                    val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    )
                } else {
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                }

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                val launchResult = billingClient.launchBillingFlow(activity, billingFlowParams)
                if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    _billingStatus.value = "Google Play Store returned status: ${launchResult.debugMessage}"
                }
            } else {
                val err = "Google Play Billing: Product '$productId' not found or service unavailable."
                Log.w(TAG, err)
                _billingStatus.value = err
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingStatus.value = "Purchase canceled by user."
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _billingStatus.value = "Plan is already active on this Google Account. Restoring purchases..."
                queryPurchases()
            }
            else -> {
                _billingStatus.value = "Google Play Billing status (${billingResult.responseCode}): ${billingResult.debugMessage}"
            }
        }
    }

    fun queryPurchases() {
        if (!billingClient.isReady) return

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { handlePurchase(it) }
            }
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
            ) { inAppResult, inAppPurchases ->
                if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    inAppPurchases.forEach { handlePurchase(it) }
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val lastPlanId = prefs.getString("last_plan_id", "") ?: ""
        val lastPlanName = prefs.getString("last_plan_name", "") ?: ""
        val lastProductId = prefs.getString("last_product_id", "") ?: ""
        val lastValidity = prefs.getString("last_validity", "1 year") ?: "1 year"

        val purchaseId = purchase.orderId.takeUnless { it.isNullOrBlank() } ?: purchase.purchaseToken
        val productId = purchase.products.firstOrNull() ?: lastProductId

        val info = VerifiedPurchaseInfo(
            purchaseToken = purchase.purchaseToken,
            purchaseId = purchaseId,
            planId = lastPlanId.ifBlank { "play_$productId" },
            planName = lastPlanName.ifBlank { productId.replace("_", " ").capitalize(Locale.ROOT) },
            productId = productId,
            validity = lastValidity
        )

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _pendingVerificationPurchase.value = purchase
                        _pendingVerificationInfo.value = info
                        _billingStatus.value = "Payment successful! Activating your plan..."
                    } else {
                        _billingStatus.value = "Failed to acknowledge Google Play purchase: ${billingResult.debugMessage}"
                    }
                }
            } else {
                _pendingVerificationPurchase.value = purchase
                _pendingVerificationInfo.value = info
                _billingStatus.value = "Google Play Purchase detected. Activating your plan..."
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _billingStatus.value = "Payment is pending completion in Google Play Store."
        }
    }

    fun clearVerificationInfo() {
        _pendingVerificationInfo.value = null
        _pendingVerificationPurchase.value = null
    }

    fun clearStatus() {
        _billingStatus.value = null
    }

    fun destroy() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
