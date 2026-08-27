package com.example.data.util

import com.example.data.local.*
import java.text.SimpleDateFormat
import java.util.*

data class PlanAccessibleContentCounts(
    val mockTestsCount: Int = 0,
    val studyNotesCount: Int = 0,
    val currentAffairsCount: Int = 0,
    val questionsCount: Int = 0,
    val effectivePlanName: String = "Free Plan",
    val isPlanActive: Boolean = false
)

data class EffectiveUserEntitlement(
    val userId: String = "",
    val activePlans: List<EntitlementEntity> = emptyList(),
    val activePlanEntities: List<PlanEntity> = emptyList(),
    val effectivePlanName: String = "Free Plan",
    val effectiveValidityLabel: String = "Lifetime",
    val isPremium: Boolean = false,
    val isLifetime: Boolean = false,
    val combinedBenefits: Set<String> = emptySet(),
    val combinedTargetExams: Set<String> = emptySet(),
    val hasAllExamsAccess: Boolean = false,
    val maxExpiryTime: Long = 0L
)

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
     * Determines whether a single entitlement is currently active.
     */
    fun isEntitlementActive(entitlement: EntitlementEntity?, currentTime: Long = System.currentTimeMillis()): Boolean {
        if (entitlement == null) return false
        if (entitlement.status != "ACTIVE" && entitlement.status != "LIFETIME") return false
        if (entitlement.planId.isBlank() && entitlement.planName.isBlank()) return false
        if (entitlement.planName.equals("Free Plan", ignoreCase = true) || entitlement.planId.equals("free_plan", ignoreCase = true)) {
            return false
        }
        
        // Lifetime never expires
        if (entitlement.isLifetime || entitlement.validityType.uppercase(Locale.ROOT) == TYPE_LIFETIME) {
            return true
        }

        // Time-limited plan must have validUntil > currentTime and validUntil > 0
        return entitlement.validUntil > 0L && entitlement.validUntil > currentTime
    }

    /**
     * Helper to infer exam targets directly from plan names if examTarget attribute is not explicitly populated.
     */
    fun inferExamTargetsFromPlan(planName: String, examTarget: String = ""): List<String> {
        val targets = mutableListOf<String>()
        if (examTarget.isNotBlank()) {
            targets.addAll(examTarget.split(",", "|").map { it.trim() }.filter { it.isNotBlank() })
        }
        val nameLower = planName.lowercase(Locale.ROOT)
        when {
            nameLower.contains("all exam") || nameLower.contains("all-exam") || nameLower.contains("combo") || nameLower.contains("mega") -> {
                targets.add("All Exams")
            }
            nameLower.contains("grade 4") || nameLower.contains("grade iv") || nameLower.contains("class 4") -> {
                targets.add("ADRE Grade 4")
                targets.add("ADRE Grade IV")
                targets.add("Grade 4")
                targets.add("Grade IV")
            }
            nameLower.contains("grade 3") || nameLower.contains("grade iii") || nameLower.contains("class 3") -> {
                targets.add("ADRE Grade 3")
                targets.add("ADRE Grade III")
                targets.add("Grade 3")
                targets.add("Grade III")
            }
            nameLower.contains("driver") -> {
                targets.add("Driver")
                targets.add("ADRE Driver")
            }
            nameLower.contains("police") -> {
                targets.add("Assam Police")
                targets.add("Police")
                targets.add("Constable")
                targets.add("SI")
            }
            nameLower.contains("tet") -> {
                targets.add("Assam TET")
                targets.add("TET")
            }
            nameLower.contains("apsc") -> {
                targets.add("APSC")
            }
            nameLower.contains("forest") -> {
                targets.add("Forest Guard")
                targets.add("Forester")
            }
        }
        return targets.distinct()
    }

    /**
     * Multi-Plan Entitlement Combination Engine:
     * Evaluates all plans belonging to the current user, filters active/valid plans,
     * and generates the combined EffectiveUserEntitlement.
     *
     * If no active paid plans remain, falls back to Basic/Free Lifetime plan.
     */
    fun resolveEffectiveEntitlement(
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity> = emptyList(),
        currentTime: Long = System.currentTimeMillis(),
        isAdminOrOwner: Boolean = false
    ): EffectiveUserEntitlement {
        if (isAdminOrOwner) {
            val ownerBenefits = setOf(
                "All Premium MCQs & Question Bank",
                "Full-Length Mock Tests & Instant Solutions",
                "Chapter-wise Study Notes & Downloadable PDFs",
                "Real-Time Leaderboard & Performance Analytics",
                "Ad-Free Learning Experience",
                "Exam Alerts & Syllabus Updates",
                "Unlimited Practice & Custom Tests",
                "All Exam Target Access (Grade 3, Grade 4, Driver, Police, TET, APSC)",
                "Owner & Admin Full Privileges"
            )
            return EffectiveUserEntitlement(
                userId = entitlements?.firstOrNull()?.userId ?: "",
                activePlans = emptyList(),
                activePlanEntities = plans.filter { it.isActive },
                effectivePlanName = "Pass Pro (Owner/Admin)",
                effectiveValidityLabel = "Lifetime Access",
                isPremium = true,
                isLifetime = true,
                combinedBenefits = ownerBenefits,
                combinedTargetExams = setOf("All Exams"),
                hasAllExamsAccess = true,
                maxExpiryTime = 0L
            )
        }

        val userEnts = entitlements ?: emptyList()
        val activePaidEntitlements = userEnts.filter { isEntitlementActive(it, currentTime) && !it.planName.equals("Free Plan", ignoreCase = true) && !it.planId.equals("free_plan", ignoreCase = true) }

        if (activePaidEntitlements.isEmpty()) {
            return EffectiveUserEntitlement(
                userId = userEnts.firstOrNull()?.userId ?: "",
                activePlans = emptyList(),
                activePlanEntities = emptyList(),
                effectivePlanName = "Free Plan",
                effectiveValidityLabel = "Lifetime",
                isPremium = false,
                isLifetime = true,
                combinedBenefits = setOf("Basic Questions", "Daily Tests", "Syllabus Updates"),
                combinedTargetExams = emptySet(),
                hasAllExamsAccess = false,
                maxExpiryTime = 0L
            )
        }

        val activeMatchingPlanEntities = mutableListOf<PlanEntity>()
        val combinedBenefitsSet = mutableSetOf<String>()
        val combinedTargetsSet = mutableSetOf<String>()
        var hasAllExamsAccess = false
        var isLifetime = false
        var maxExpiry = 0L

        for (ent in activePaidEntitlements) {
            val matchingPlan = plans.find {
                it.id.toString() == ent.planId ||
                it.planName.equals(ent.planName, ignoreCase = true) ||
                it.planName.contains(ent.planName, ignoreCase = true) ||
                ent.planName.contains(it.planName, ignoreCase = true)
            }
            if (matchingPlan != null) {
                activeMatchingPlanEntities.add(matchingPlan)
            }

            if (ent.isLifetime || ent.validityType.uppercase(Locale.ROOT) == TYPE_LIFETIME || matchingPlan?.isLifetime == true) {
                isLifetime = true
            } else if (ent.validUntil > maxExpiry) {
                maxExpiry = ent.validUntil
            }

            // Benefits combination
            if (ent.benefits.isNotBlank()) {
                ent.benefits.split(",", "|").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                    combinedBenefitsSet.add(it)
                }
            }
            matchingPlan?.features?.let {
                it.split(",", "|").map { f -> f.trim() }.filter { f -> f.isNotBlank() }.forEach { f ->
                    combinedBenefitsSet.add(f)
                }
            }
            matchingPlan?.contents?.let {
                it.split(",", "|").map { c -> c.trim() }.filter { c -> c.isNotBlank() }.forEach { c ->
                    combinedBenefitsSet.add(c)
                }
            }

            // Target exams combination
            val targets = inferExamTargetsFromPlan(ent.planName, matchingPlan?.examTarget ?: "")
            if (targets.any { it.equals("All Exams", ignoreCase = true) || it.equals("All", ignoreCase = true) } ||
                (matchingPlan != null && matchingPlan.examTarget.isBlank() && matchingPlan.planName.contains("All", ignoreCase = true))) {
                hasAllExamsAccess = true
            }
            combinedTargetsSet.addAll(targets)
        }

        if (combinedTargetsSet.any { it.equals("All Exams", ignoreCase = true) || it.equals("All", ignoreCase = true) }) {
            hasAllExamsAccess = true
        }

        val distinctPlanNames = activePaidEntitlements.map { it.planName }.distinct()
        val effectivePlanName = when {
            distinctPlanNames.size == 1 -> distinctPlanNames.first()
            distinctPlanNames.size == 2 -> "${distinctPlanNames[0]} + ${distinctPlanNames[1]}"
            else -> "${distinctPlanNames.first()} + ${distinctPlanNames.size - 1} More (${distinctPlanNames.size} Plans)"
        }

        val effectiveValidityLabel = when {
            isLifetime -> "Lifetime Access"
            distinctPlanNames.size == 1 -> formatValidityDisplay(activePaidEntitlements.first(), currentTime)
            maxExpiry > 0L -> {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                "${activePaidEntitlements.size} Active Plans (Valid till ${sdf.format(Date(maxExpiry))})"
            }
            else -> "Active Access"
        }

        return EffectiveUserEntitlement(
            userId = activePaidEntitlements.first().userId,
            activePlans = activePaidEntitlements,
            activePlanEntities = activeMatchingPlanEntities,
            effectivePlanName = effectivePlanName,
            effectiveValidityLabel = effectiveValidityLabel,
            isPremium = true,
            isLifetime = isLifetime,
            combinedBenefits = combinedBenefitsSet,
            combinedTargetExams = combinedTargetsSet,
            hasAllExamsAccess = hasAllExamsAccess,
            maxExpiryTime = if (isLifetime) 0L else maxExpiry
        )
    }

    /**
     * Resolves the effective plan name for a user.
     */
    fun getEffectivePlanName(entitlement: EntitlementEntity?, currentTime: Long = System.currentTimeMillis()): String {
        if (entitlement == null) return "Free Plan"
        if (!isEntitlementActive(entitlement, currentTime)) return "Free Plan"
        if (entitlement.planName.isBlank() || entitlement.planName.equals("Free Plan", ignoreCase = true)) return "Free Plan"
        return entitlement.planName
    }

    fun getEffectivePlanName(entitlements: List<EntitlementEntity>?, plans: List<PlanEntity> = emptyList(), currentTime: Long = System.currentTimeMillis(), isAdminOrOwner: Boolean = false): String {
        return resolveEffectiveEntitlement(entitlements, plans, currentTime, isAdminOrOwner).effectivePlanName
    }

    /**
     * Resolves the effective validity string for a user.
     */
    fun getEffectiveValidityLabel(entitlements: List<EntitlementEntity>?, plans: List<PlanEntity> = emptyList(), currentTime: Long = System.currentTimeMillis(), isAdminOrOwner: Boolean = false): String {
        return resolveEffectiveEntitlement(entitlements, plans, currentTime, isAdminOrOwner).effectiveValidityLabel
    }

    fun getEffectiveValidityLabel(entitlement: EntitlementEntity?, currentTime: Long = System.currentTimeMillis()): String {
        if (entitlement == null) return "Lifetime"
        if (!isEntitlementActive(entitlement, currentTime)) return "Lifetime"
        if (entitlement.planName.equals("Free Plan", ignoreCase = true) || entitlement.isLifetime || entitlement.validityType.uppercase(Locale.ROOT) == TYPE_LIFETIME) {
            return "Lifetime"
        }
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val formattedDate = sdf.format(Date(entitlement.validUntil))
        val label = entitlement.validityLabel
        return if (label.isNotBlank() && !label.equals("Custom", ignoreCase = true)) "$formattedDate ($label)" else formattedDate
    }

    /**
     * Formats remaining time or expiry date for UI display.
     */
    fun formatValidityDisplay(entitlements: List<EntitlementEntity>?, plans: List<PlanEntity> = emptyList(), currentTime: Long = System.currentTimeMillis(), isAdminOrOwner: Boolean = false): String {
        val resolved = resolveEffectiveEntitlement(entitlements, plans, currentTime, isAdminOrOwner)
        if (isAdminOrOwner) return "Lifetime Owner/Admin Access"
        if (!resolved.isPremium) return "Free Lifetime Access"
        if (resolved.effectiveValidityLabel.equals("Lifetime", ignoreCase = true) || resolved.effectiveValidityLabel.contains("Lifetime", ignoreCase = true)) return "Lifetime Premium Access"
        return "Valid until ${resolved.effectiveValidityLabel}"
    }

    fun formatValidityDisplay(entitlement: EntitlementEntity?, currentTime: Long = System.currentTimeMillis()): String {
        if (entitlement == null || !isEntitlementActive(entitlement, currentTime) || entitlement.planName.equals("Free Plan", ignoreCase = true)) {
            return "Free Plan (Lifetime Access)"
        }
        if (entitlement.isLifetime || entitlement.validityType.uppercase(Locale.ROOT) == TYPE_LIFETIME) {
            return "Lifetime Access"
        }
        if (entitlement.validUntil <= 0L || entitlement.validUntil < currentTime) {
            return "Expired (Reverted to Free Plan)"
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

    /**
     * Extracts numerical limit from plan feature or content descriptor strings.
     */
    fun extractNumericalLimit(featureStr: String): Int? {
        val clean = featureStr.trim()
        if (clean.isBlank()) return null
        val parts = clean.split(":")
        val target = if (parts.size > 1) parts[1].trim() else clean
        if (target.equals("All", ignoreCase = true) || target.contains("Unlimited", ignoreCase = true)) {
            return null
        }
        val numberRegex = Regex("""\b(\d+)\b""")
        val match = numberRegex.find(target)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    /**
     * Checks whether an item's exam matches the allowed exam list of an active plan.
     */
    fun matchesExamTarget(itemExamCategory: String?, itemTitleOrTopic: String?, allowedExams: List<String>): Boolean {
        if (allowedExams.isEmpty()) return true
        if (allowedExams.any { it.isBlank() || it.equals("All Exams", ignoreCase = true) || it.equals("All", ignoreCase = true) || it.equals("ALL_EXAMS", ignoreCase = true) }) return true

        val cat = itemExamCategory?.trim().orEmpty()
        val title = itemTitleOrTopic?.trim().orEmpty()
        val combined = "$cat $title".trim().lowercase(Locale.ROOT)

        return allowedExams.any { rawAllowed ->
            val allowed = rawAllowed.trim().lowercase(Locale.ROOT)
            if (allowed.isBlank() || allowed == "all" || allowed == "all exams" || allowed == "all_exams") return@any true

            val isGrade4Plan = allowed.contains("grade 4") || allowed.contains("grade iv") || allowed.contains("class 4")
            val isGrade3Plan = allowed.contains("grade 3") || allowed.contains("grade iii") || allowed.contains("class 3")
            val isDriverPlan = allowed.contains("driver")
            val isPolicePlan = allowed.contains("police") || allowed.contains("constable") || allowed.contains("si")
            val isTetPlan = allowed.contains("tet")
            val isApscPlan = allowed.contains("apsc")

            val isGrade4Item = combined.contains("grade 4") || combined.contains("grade iv") || combined.contains("class 4")
            val isGrade3Item = combined.contains("grade 3") || combined.contains("grade iii") || combined.contains("class 3")
            val isDriverItem = combined.contains("driver")
            val isPoliceItem = combined.contains("police") || combined.contains("constable") || combined.contains("si")
            val isTetItem = combined.contains("tet")
            val isApscItem = combined.contains("apsc")

            if (isGrade4Plan) {
                return@any isGrade4Item || (!isGrade3Item && combined.contains("adre"))
            }
            if (isGrade3Plan) {
                return@any isGrade3Item || (!isGrade4Item && combined.contains("adre"))
            }
            if (isDriverPlan) return@any isDriverItem
            if (isPolicePlan) return@any isPoliceItem
            if (isTetPlan) return@any isTetItem
            if (isApscPlan) return@any isApscItem

            combined.contains(allowed) || cat.lowercase(Locale.ROOT).contains(allowed)
        }
    }

    /**
     * Determines whether a specific mock test is accessible.
     */
    fun isMockTestAccessible(
        mock: MockTestEntity,
        effectiveEntitlement: EffectiveUserEntitlement,
        isAdminOrOwner: Boolean
    ): Boolean {
        if (isAdminOrOwner) return true
        if (!mock.isPremium) return true
        if (!effectiveEntitlement.isPremium) return false
        if (effectiveEntitlement.hasAllExamsAccess) return true
        return matchesExamTarget(mock.category, mock.titleEn, effectiveEntitlement.combinedTargetExams.toList())
    }

    fun isMockTestAccessible(
        mock: MockTestEntity,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): Boolean {
        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime, isAdminOrOwner)
        return isMockTestAccessible(mock, effective, isAdminOrOwner)
    }

    fun isMockTestAccessible(
        mock: MockTestEntity,
        entitlement: EntitlementEntity?,
        activePlan: PlanEntity?,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): Boolean {
        val ents = if (entitlement != null) listOf(entitlement) else emptyList()
        val plans = if (activePlan != null) listOf(activePlan) else emptyList()
        return isMockTestAccessible(mock, ents, plans, isAdminOrOwner, currentTime)
    }

    /**
     * Determines whether a specific study note is accessible.
     */
    fun isStudyNoteAccessible(
        note: StudyNoteEntity,
        effectiveEntitlement: EffectiveUserEntitlement,
        isAdminOrOwner: Boolean
    ): Boolean {
        if (isAdminOrOwner) return true
        if (!note.isPremium) return true
        if (!effectiveEntitlement.isPremium) return false
        if (effectiveEntitlement.hasAllExamsAccess) return true
        return matchesExamTarget(note.subject, "${note.topic} ${note.titleEn}", effectiveEntitlement.combinedTargetExams.toList())
    }

    fun isStudyNoteAccessible(
        note: StudyNoteEntity,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): Boolean {
        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime, isAdminOrOwner)
        return isStudyNoteAccessible(note, effective, isAdminOrOwner)
    }

    fun isStudyNoteAccessible(
        note: StudyNoteEntity,
        entitlement: EntitlementEntity?,
        activePlan: PlanEntity?,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): Boolean {
        val ents = if (entitlement != null) listOf(entitlement) else emptyList()
        val plans = if (activePlan != null) listOf(activePlan) else emptyList()
        return isStudyNoteAccessible(note, ents, plans, isAdminOrOwner, currentTime)
    }

    /**
     * Determines whether a specific question is accessible.
     */
    fun isQuestionAccessible(
        question: QuestionEntity,
        effectiveEntitlement: EffectiveUserEntitlement,
        isAdminOrOwner: Boolean
    ): Boolean {
        if (isAdminOrOwner) return true
        if (!question.isPremium) return true
        if (!effectiveEntitlement.isPremium) return false
        if (effectiveEntitlement.hasAllExamsAccess) return true
        return matchesExamTarget(question.examCategory, "${question.subject} ${question.topic}", effectiveEntitlement.combinedTargetExams.toList())
    }

    fun isQuestionAccessible(
        question: QuestionEntity,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): Boolean {
        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime, isAdminOrOwner)
        return isQuestionAccessible(question, effective, isAdminOrOwner)
    }

    fun isQuestionAccessible(
        question: QuestionEntity,
        entitlement: EntitlementEntity?,
        activePlan: PlanEntity?,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): Boolean {
        val ents = if (entitlement != null) listOf(entitlement) else emptyList()
        val plans = if (activePlan != null) listOf(activePlan) else emptyList()
        return isQuestionAccessible(question, ents, plans, isAdminOrOwner, currentTime)
    }

    /**
     * Filters mock tests accessible under the user's active entitlements / plans.
     */
    fun filterAccessibleMockTests(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        mockTests: List<MockTestEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<MockTestEntity> {
        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime, isAdminOrOwner)
        return mockTests.filter { isMockTestAccessible(it, effective, isAdminOrOwner) }
    }
    
    fun filterAccessibleMockTests(
        userProfile: UserProfileEntity?,
        entitlement: EntitlementEntity?,
        plans: List<PlanEntity>,
        mockTests: List<MockTestEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<MockTestEntity> {
        val ents = if (entitlement != null) listOf(entitlement) else emptyList()
        return filterAccessibleMockTests(userProfile, ents, plans, mockTests, isAdminOrOwner, currentTime)
    }

    /**
     * Filters study notes accessible under the user's active entitlements / plans.
     */
    fun filterAccessibleStudyNotes(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        studyNotes: List<StudyNoteEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<StudyNoteEntity> {
        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime, isAdminOrOwner)
        return studyNotes.filter { isStudyNoteAccessible(it, effective, isAdminOrOwner) }
    }

    fun filterAccessibleStudyNotes(
        userProfile: UserProfileEntity?,
        entitlement: EntitlementEntity?,
        plans: List<PlanEntity>,
        studyNotes: List<StudyNoteEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<StudyNoteEntity> {
        val ents = if (entitlement != null) listOf(entitlement) else emptyList()
        return filterAccessibleStudyNotes(userProfile, ents, plans, studyNotes, isAdminOrOwner, currentTime)
    }

    /**
     * Filters questions accessible under the user's active entitlements / plans.
     */
    fun filterAccessibleQuestions(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        questions: List<QuestionEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<QuestionEntity> {
        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime, isAdminOrOwner)
        val validQuestions = questions.filter { !it.isReported }
        return validQuestions.filter { isQuestionAccessible(it, effective, isAdminOrOwner) }
    }

    fun filterAccessibleQuestions(
        userProfile: UserProfileEntity?,
        entitlement: EntitlementEntity?,
        plans: List<PlanEntity>,
        questions: List<QuestionEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<QuestionEntity> {
        val ents = if (entitlement != null) listOf(entitlement) else emptyList()
        return filterAccessibleQuestions(userProfile, ents, plans, questions, isAdminOrOwner, currentTime)
    }

    /**
     * Calculates authoritative counts of accessible content for the specific user's active plans.
     * Guaranteed user isolation based on current user's profile and entitlements.
     */
    fun calculateAccessibleCounts(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        mockTests: List<MockTestEntity>,
        studyNotes: List<StudyNoteEntity>,
        questions: List<QuestionEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): PlanAccessibleContentCounts {
        val accessibleMocks = filterAccessibleMockTests(userProfile, entitlements, plans, mockTests, isAdminOrOwner, currentTime)
        val accessibleNotes = filterAccessibleStudyNotes(userProfile, entitlements, plans, studyNotes, isAdminOrOwner, currentTime)
        val accessibleQs = filterAccessibleQuestions(userProfile, entitlements, plans, questions, isAdminOrOwner, currentTime)

        val nonCaCount = accessibleNotes.count { !it.subject.contains("Current Affairs", ignoreCase = true) }
        val caCount = accessibleNotes.count { it.subject.contains("Current Affairs", ignoreCase = true) }

        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com") || (userProfile?.email?.trim()?.lowercase() == "borapinku151@gmail.com")
        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime, isAdminOrOwner = isUserAdmin)

        val effectivePlanName = when {
            isUserAdmin -> "Admin Access"
            effective.isPremium -> effective.effectivePlanName
            else -> "Free Plan"
        }

        return PlanAccessibleContentCounts(
            mockTestsCount = accessibleMocks.size,
            studyNotesCount = nonCaCount,
            currentAffairsCount = caCount,
            questionsCount = accessibleQs.size,
            effectivePlanName = effectivePlanName,
            isPlanActive = isUserAdmin || effective.isPremium
        )
    }

    fun calculateAccessibleCounts(
        userProfile: UserProfileEntity?,
        entitlement: EntitlementEntity?,
        plans: List<PlanEntity>,
        mockTests: List<MockTestEntity>,
        studyNotes: List<StudyNoteEntity>,
        questions: List<QuestionEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): PlanAccessibleContentCounts {
        val ents = if (entitlement != null) listOf(entitlement) else emptyList()
        return calculateAccessibleCounts(userProfile, ents, plans, mockTests, studyNotes, questions, isAdminOrOwner, currentTime)
    }
}
