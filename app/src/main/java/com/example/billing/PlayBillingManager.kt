package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayBillingManager(private val context: Context) : PurchasesUpdatedListener {

    private val TAG = "PlayBillingManager"

    private val _billingStatus = MutableStateFlow<String?>(null)
    val billingStatus: StateFlow<String?> = _billingStatus.asStateFlow()

    private val _isPurchaseSuccessful = MutableStateFlow(false)
    val isPurchaseSuccessful: StateFlow<Boolean> = _isPurchaseSuccessful.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun startConnection(onReady: (() -> Unit)? = null) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Google Play Billing Client setup completed successfully.")
                    onReady?.invoke()
                } else {
                    val msg = "Google Play Billing Connection failed (${billingResult.responseCode}): ${billingResult.debugMessage}"
                    Log.e(TAG, msg)
                    _billingStatus.value = msg
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Google Play Billing service disconnected.")
            }
        })
    }

    fun buyPlan(activity: Activity, planId: String, planName: String) {
        val sanitizedProductId = planName.lowercase()
            .replace(" ", "_")
            .replace("[^a-z0-9_]".toRegex(), "")
            .ifEmpty { "jukti_premium_pass" }

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
                    _billingStatus.value = "Google Play Store returned error (${launchResult.responseCode}): ${launchResult.debugMessage}"
                }
            } else {
                val err = "Google Play Billing: Product '$productId' not found in Play Console or Billing unavailable (Code ${billingResult.responseCode}: ${billingResult.debugMessage})"
                Log.e(TAG, err)
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
                } else {
                    _billingStatus.value = "Purchase list is empty."
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingStatus.value = "Purchase canceled by user."
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _isPurchaseSuccessful.value = true
                _billingStatus.value = "Plan is already active on this Google Account."
            }
            else -> {
                _billingStatus.value = "Google Play Billing Error (${billingResult.responseCode}): ${billingResult.debugMessage}"
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _isPurchaseSuccessful.value = true
                        _billingStatus.value = "Payment Successful via Google Play! Premium plan activated."
                    } else {
                        _billingStatus.value = "Failed to acknowledge Google Play purchase: ${billingResult.debugMessage}"
                    }
                }
            } else {
                _isPurchaseSuccessful.value = true
                _billingStatus.value = "Subscription active via Google Play."
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _billingStatus.value = "Payment is pending completion in Google Play Store."
        }
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
