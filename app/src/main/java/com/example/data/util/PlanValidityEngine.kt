package com.example.data.util

import com.example.data.local.EntitlementEntity
import java.text.SimpleDateFormat
import java.util.*

object PlanValidityEngine {

    const val TYPE_DAYS = "DAYS"
    const val TYPE_MONTHS = "MONTHS"
    const val TYPE_YEARS = "YEARS"
    const val TYPE_LIFETIME = "LIFETIME"

    /**
     * Calculates the authoritative expiry timestamp based on actual activation date
     * and structured validity configuration.
     *
     * @param activatedAt The authoritative timestamp when the user purchased or was assigned the plan.
     * @param validityType "DAYS", "MONTHS", "YEARS", or "LIFETIME".
     * @param validityValue Number of days, months, or years.
     * @return Expiry timestamp in milliseconds, or 0L for Lifetime plans.
     */
    fun calculateExpiry(
        activatedAt: Long,
        validityType: String,
        validityValue: Int
    ): Long {
        val type = validityType.trim().uppercase(Locale.ROOT)
        if (type == TYPE_LIFETIME || (validityValue <= 0 && type != TYPE_DAYS)) {
            return 0L
        }

        val baseTime = if (activatedAt > 0L) activatedAt else System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = baseTime

        when (type) {
            TYPE_DAYS -> {
                calendar.add(Calendar.DAY_OF_YEAR, validityValue.coerceAtLeast(1))
            }
            TYPE_MONTHS -> {
                calendar.add(Calendar.MONTH, validityValue.coerceAtLeast(1))
            }
            TYPE_YEARS -> {
                calendar.add(Calendar.YEAR, validityValue.coerceAtLeast(1))
            }
            else -> {
                calendar.add(Calendar.MONTH, 1)
            }
        }

        return calendar.timeInMillis
    }

    fun calculateExpiryTimestamp(
        activationTime: Long,
        validityType: String,
        validityValue: Int,
        isLifetime: Boolean = false
    ): Long {
        if (isLifetime || validityType.uppercase(Locale.ROOT) == TYPE_LIFETIME) return 0L
        return calculateExpiry(activationTime, validityType, validityValue)
    }

    fun inferValidityType(rawVal: String?): String {
        return normalizeValidity(rawVal).first
    }

    fun inferValidityValue(rawVal: String?): Int {
        return normalizeValidity(rawVal).second
    }

    fun formatValidityLabel(validityType: String?, validityValue: Int): String {
        val type = validityType?.trim()?.uppercase(Locale.ROOT) ?: TYPE_MONTHS
        if (type == TYPE_LIFETIME || (validityValue <= 0 && type != TYPE_DAYS)) return "Lifetime"
        return when (type) {
            TYPE_DAYS -> "$validityValue ${if (validityValue == 1) "Day" else "Days"}"
            TYPE_MONTHS -> "$validityValue ${if (validityValue == 1) "Month" else "Months"}"
            TYPE_YEARS -> "$validityValue ${if (validityValue == 1) "Year" else "Years"}"
            else -> "$validityValue $type"
        }
    }

    /**
     * Normalizes legacy free-text validity strings into structured validity format.
     */
    fun normalizeValidity(validityStr: String?): Triple<String, Int, String> {
        if (validityStr.isNullOrBlank()) {
            return Triple(TYPE_MONTHS, 1, "1 Month")
        }
        val clean = validityStr.trim().lowercase(Locale.ROOT)
        return when {
            clean.contains("lifetime") -> Triple(TYPE_LIFETIME, 0, "Lifetime")
            clean == "1 week" || clean == "7 days" || clean == "7 day" -> Triple(TYPE_DAYS, 7, "1 Week")
            clean == "1 month" || clean == "1 months" -> Triple(TYPE_MONTHS, 1, "1 Month")
            clean == "2 months" || clean == "2 month" -> Triple(TYPE_MONTHS, 2, "2 Months")
            clean == "3 months" || clean == "3 month" -> Triple(TYPE_MONTHS, 3, "3 Months")
            clean == "6 months" || clean == "6 month" -> Triple(TYPE_MONTHS, 6, "6 Months")
            clean == "9 months" || clean == "9 month" -> Triple(TYPE_MONTHS, 9, "9 Months")
            clean == "1 year" || clean == "1 years" || clean == "12 months" -> Triple(TYPE_YEARS, 1, "1 Year")
            clean == "2 years" || clean == "2 year" || clean == "24 months" -> Triple(TYPE_YEARS, 2, "2 Years")
            clean.contains("day") -> {
                val num = clean.filter { it.isDigit() }.toIntOrNull()?.coerceAtLeast(1) ?: 30
                Triple(TYPE_DAYS, num, "$num ${if (num == 1) "Day" else "Days"}")
            }
            clean.contains("month") -> {
                val num = clean.filter { it.isDigit() }.toIntOrNull()?.coerceAtLeast(1) ?: 1
                Triple(TYPE_MONTHS, num, "$num ${if (num == 1) "Month" else "Months"}")
            }
            clean.contains("year") -> {
                val num = clean.filter { it.isDigit() }.toIntOrNull()?.coerceAtLeast(1) ?: 1
                Triple(TYPE_YEARS, num, "$num ${if (num == 1) "Year" else "Years"}")
            }
            else -> {
                // Check if it's a parseable date string
                val parsedDate = parseDateStringToMillis(validityStr)
                if (parsedDate > 0L) {
                    val daysDiff = ((parsedDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
                    if (daysDiff > 0) {
                        Triple(TYPE_DAYS, daysDiff, "$daysDiff Days")
                    } else {
                        Triple(TYPE_MONTHS, 1, validityStr)
                    }
                } else {
                    Triple(TYPE_MONTHS, 1, validityStr)
                }
            }
        }
    }

    /**
     * Parses arbitrary date strings safely.
     */
    fun parseDateStringToMillis(dateStr: String): Long {
        val formats = listOf(
            "dd MMM yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "d MMM yyyy",
            "dd-MM-yyyy", "MMM dd, yyyy", "MMMM dd, yyyy"
        )
        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.getDefault())
                sdf.isLenient = false
                val date = sdf.parse(dateStr.trim())
                if (date != null) {
                    val cal = Calendar.getInstance()
                    cal.time = date
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    return cal.timeInMillis
                }
            } catch (_: Exception) {}
        }
        return -1L
    }

    /**
     * Determines whether an entitlement is currently active.
     */
    fun isEntitlementActive(entitlement: EntitlementEntity?, currentTime: Long = System.currentTimeMillis()): Boolean {
        if (entitlement == null) return false
        if (entitlement.status != "ACTIVE" && entitlement.status != "LIFETIME") return false
        if (entitlement.planId.isBlank() && entitlement.planName.isBlank()) return false
        
        // Lifetime never expires
        if (entitlement.isLifetime || entitlement.validityType.uppercase(Locale.ROOT) == TYPE_LIFETIME || entitlement.validUntil <= 0L) {
            return true
        }

        // Time-limited plan
        return entitlement.validUntil > currentTime
    }

    /**
     * Formats remaining time or expiry date for UI display.
     */
    fun formatValidityDisplay(entitlement: EntitlementEntity?, currentTime: Long = System.currentTimeMillis()): String {
        if (entitlement == null) return "Free Plan"
        if (entitlement.isLifetime || entitlement.validityType.uppercase(Locale.ROOT) == TYPE_LIFETIME || entitlement.validUntil <= 0L) {
            return "Lifetime Access"
        }
        if (entitlement.validUntil < currentTime) {
            return "Expired"
        }
        val diffMs = entitlement.validUntil - currentTime
        val daysRemaining = (diffMs / (24 * 60 * 60 * 1000L)).toInt()
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val formattedDate = sdf.format(Date(entitlement.validUntil))
        return if (daysRemaining > 1) {
            "$daysRemaining days left (Expires $formattedDate)"
        } else if (daysRemaining == 1) {
            "1 day left (Expires $formattedDate)"
        } else {
            "Expires today ($formattedDate)"
        }
    }
}
