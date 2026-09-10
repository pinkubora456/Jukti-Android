package com.example.ui.components

import com.example.data.local.PlanEntity

object PlanDisplayHelper {

    /**
     * Formats prices cleanly, avoiding double rupee signs, trailing punctuation, or missing symbols.
     * Examples: "999" -> "₹999", "₹999" -> "₹999", "Rs. 999" -> "₹999", "0" -> "₹0"
     */
    fun formatPrice(rawPrice: String?): String {
        if (rawPrice.isNullOrBlank()) return ""
        val trimmed = rawPrice.trim()
        val digitsAndDots = trimmed.replace(Regex("^[₹RsINR\\s.]+"), "").trim()
        return if (digitsAndDots.isNotEmpty()) "₹$digitsAndDots" else trimmed
    }

    /**
     * Formats discount cleanly, avoiding "50% OFF% Off", duplicate '%' or trailing fragments.
     * Examples: "50% OFF" -> "50% OFF", "50%" -> "50% OFF", "50" -> "50% OFF", "0" -> ""
     */
    fun formatDiscount(rawDiscount: String?): String {
        if (rawDiscount.isNullOrBlank()) return ""
        val clean = rawDiscount
            .replace(Regex("(?i)off"), "")
            .replace("%", "")
            .trim()
        return if (clean.isNotEmpty() && clean != "0" && clean != "0.0") "$clean% OFF" else ""
    }

    /**
     * Extracts benefits / features into a clean list of non-empty strings,
     * regardless of whether they were stored with pipe (|), newline (\n), or comma (,) separator.
     */
    fun parseFeatures(features: String?): List<String> {
        if (features.isNullOrBlank()) return emptyList()
        val rawItems = when {
            features.contains("|") -> features.split("|")
            features.contains("\n") -> features.split("\n")
            features.contains(",") -> features.split(",")
            else -> listOf(features)
        }
        return rawItems
            .map { it.trim().trimStart('•', '-', '*', ' ') }
            .filter { it.isNotBlank() }
    }

    /**
     * Formats the displayed validity text clearly.
     * Examples: "1 Year", "7 Days", "Lifetime Access"
     */
    fun formatValidity(plan: PlanEntity): String {
        val label = when {
            plan.isLifetime || plan.validityType.equals("LIFETIME", ignoreCase = true) -> "Lifetime Access"
            plan.validityLabel.isNotBlank() -> plan.validityLabel
            plan.planValidity.isNotBlank() -> plan.planValidity
            plan.offerValidity.isNotBlank() && !plan.offerValidity.contains("off", ignoreCase = true) -> plan.offerValidity
            else -> "1 Month"
        }
        return if (label.equals("Lifetime", ignoreCase = true)) "Lifetime Access" else label
    }

    /**
     * Checks if a plan is a paid plan (not ₹0 or Free Plan).
     */
    fun isPaidPlan(plan: PlanEntity): Boolean {
        if (plan.planName.contains("Free", ignoreCase = true)) return false
        val digits = plan.finalPrice.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
        return digits > 0
    }

    /**
     * Identifies whether a plan is one of the legacy hard-coded or dummy plans.
     * Only plans created by admin/owner should be preserved and displayed.
     */
    fun isDummyOrHardcodedPlan(plan: PlanEntity): Boolean {
        return isDummyOrHardcodedPlan(plan.id, plan.planName, plan.googlePlayProductId)
    }

    fun isDummyOrHardcodedPlan(id: Long, planName: String?, googlePlayProductId: String? = null): Boolean {
        if (id in 1L..3L) return true
        val name = (planName ?: "").trim()
        if (name.equals("Jukti Complete Premium", ignoreCase = true)) return true
        if (name.equals("Jukti Starter Plan", ignoreCase = true)) return true
        if (name.equals("Free Plan", ignoreCase = true) && id <= 100L) return true
        val prodId = (googlePlayProductId ?: "").trim()
        if ((prodId == "premium_1_year" || prodId == "starter_7_day") && id <= 100L) return true
        return false
    }
}
