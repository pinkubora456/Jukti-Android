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
     * Resolves the effective plan name for a user.
     * If the user has not bought a plan, or their plan has expired, returns "Free Plan".
     */
    fun getEffectivePlanName(entitlement: EntitlementEntity?, currentTime: Long = System.currentTimeMillis()): String {
        if (entitlement == null) return "Free Plan"
        if (!isEntitlementActive(entitlement, currentTime)) return "Free Plan"
        if (entitlement.planName.isBlank() || entitlement.planName.equals("Free Plan", ignoreCase = true)) return "Free Plan"
        return entitlement.planName
    }

    /**
     * Resolves the effective validity string for a user.
     * Free plan users or users with expired plans get "Lifetime".
     * Active time-limited plans return formatted expiry date and label.
     */
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
     * E.g. "Mock Test (ADRE): 20" -> 20
     *      "Questions (All Exams): 2000" -> 2000
     *      "Study Notes: 100" -> 100
     *      "Current Affairs: 500" -> 500
     * Returns null if "All" or unlimited.
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
        if (allowedExams.any { it.equals("All Exams", ignoreCase = true) || it.equals("All", ignoreCase = true) }) return true
        val cat = itemExamCategory?.trim().orEmpty()
        val title = itemTitleOrTopic?.trim().orEmpty()
        return allowedExams.any { allowed ->
            cat.contains(allowed, ignoreCase = true) ||
            title.contains(allowed, ignoreCase = true) ||
            allowed.contains(cat, ignoreCase = true)
        }
    }

    /**
     * Determines whether a specific mock test is accessible under the given entitlement and plan configuration.
     */
    fun isMockTestAccessible(
        mock: MockTestEntity,
        entitlement: EntitlementEntity?,
        activePlan: PlanEntity?,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): Boolean {
        if (isAdminOrOwner) return true
        if (!mock.isPremium) return true
        if (!isEntitlementActive(entitlement, currentTime)) return false
        val rawExamTarget = activePlan?.examTarget ?: ""
        val allowedExams = rawExamTarget.split(",").map { it.trim() }.filter { it.isNotEmpty() && !it.equals("All Exams", ignoreCase = true) }
        return matchesExamTarget(mock.category, mock.titleEn, allowedExams)
    }

    /**
     * Determines whether a specific study note is accessible under the given entitlement and plan configuration.
     */
    fun isStudyNoteAccessible(
        note: StudyNoteEntity,
        entitlement: EntitlementEntity?,
        activePlan: PlanEntity?,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): Boolean {
        if (isAdminOrOwner) return true
        if (!note.isPremium) return true
        if (!isEntitlementActive(entitlement, currentTime)) return false
        val rawExamTarget = activePlan?.examTarget ?: ""
        val allowedExams = rawExamTarget.split(",").map { it.trim() }.filter { it.isNotEmpty() && !it.equals("All Exams", ignoreCase = true) }
        return matchesExamTarget(note.subject, "${note.topic} ${note.titleEn}", allowedExams)
    }

    /**
     * Determines whether a specific question is accessible under the given entitlement and plan configuration.
     */
    fun isQuestionAccessible(
        question: QuestionEntity,
        entitlement: EntitlementEntity?,
        activePlan: PlanEntity?,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): Boolean {
        if (isAdminOrOwner) return true
        if (!question.isPremium) return true
        if (!isEntitlementActive(entitlement, currentTime)) return false
        val rawExamTarget = activePlan?.examTarget ?: ""
        val allowedExams = rawExamTarget.split(",").map { it.trim() }.filter { it.isNotEmpty() && !it.equals("All Exams", ignoreCase = true) }
        return matchesExamTarget(question.examCategory, "${question.subject} ${question.topic}", allowedExams)
    }

    /**
     * Filters mock tests accessible under the user's active entitlement / plan.
     */
    fun filterAccessibleMockTests(
        userProfile: UserProfileEntity?,
        entitlement: EntitlementEntity?,
        plans: List<PlanEntity>,
        mockTests: List<MockTestEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<MockTestEntity> {
        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com")
        if (isUserAdmin) return mockTests

        val isActive = isEntitlementActive(entitlement, currentTime)

        if (!isActive) {
            val freeMocks = mockTests.filter { !it.isPremium }
            val freePlan = plans.find { it.planName.equals("Free Plan", ignoreCase = true) || it.finalPrice == "0" || it.finalPrice == "₹0" }
            val freeFeatures = buildList {
                freePlan?.features?.let { addAll(it.split("|", ",")) }
                freePlan?.contents?.let { addAll(it.split("|", ",")) }
                entitlement?.benefits?.let { addAll(it.split("|", ",")) }
            }.map { it.trim() }.filter { it.isNotBlank() }
            val mockLimitFeature = freeFeatures.find { 
                it.startsWith("Mock Test", ignoreCase = true) || it.startsWith("Mock Tests", ignoreCase = true) || it.contains("Mock", ignoreCase = true)
            }
            val mockLimit = mockLimitFeature?.let { extractNumericalLimit(it) }
            return if (mockLimit != null && mockLimit < freeMocks.size) {
                freeMocks.take(mockLimit)
            } else {
                freeMocks
            }
        }

        val activePlan = entitlement?.let { ent ->
            plans.find { it.id.toString() == ent.planId || it.planName.equals(ent.planName, ignoreCase = true) }
        }

        val rawExamTarget = activePlan?.examTarget ?: ""
        val allowedExams = rawExamTarget.split(",").map { it.trim() }.filter { it.isNotEmpty() && !it.equals("All Exams", ignoreCase = true) }

        val candidateMocks = if (allowedExams.isEmpty()) {
            mockTests
        } else {
            mockTests.filter { mock ->
                !mock.isPremium || matchesExamTarget(mock.category, mock.titleEn, allowedExams)
            }
        }

        val featuresAndContents = buildList {
            activePlan?.features?.let { addAll(it.split("|", ",")) }
            activePlan?.contents?.let { addAll(it.split("|", ",")) }
            entitlement?.benefits?.let { addAll(it.split("|", ",")) }
        }.map { it.trim() }.filter { it.isNotBlank() }

        val mockLimitFeature = featuresAndContents.find { 
            it.startsWith("Mock Test", ignoreCase = true) || it.startsWith("Mock Tests", ignoreCase = true) || it.contains("Mock", ignoreCase = true)
        }
        val mockLimit = mockLimitFeature?.let { extractNumericalLimit(it) }

        return if (mockLimit != null && mockLimit < candidateMocks.size) {
            val freeMocks = candidateMocks.filter { !it.isPremium }
            val premiumMocks = candidateMocks.filter { it.isPremium }
            val remainingLimit = (mockLimit - freeMocks.size).coerceAtLeast(0)
            (freeMocks + premiumMocks.take(remainingLimit)).distinctBy { it.id }
        } else {
            candidateMocks
        }
    }

    /**
     * Filters study notes accessible under the user's active entitlement / plan.
     */
    fun filterAccessibleStudyNotes(
        userProfile: UserProfileEntity?,
        entitlement: EntitlementEntity?,
        plans: List<PlanEntity>,
        studyNotes: List<StudyNoteEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<StudyNoteEntity> {
        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com")
        if (isUserAdmin) return studyNotes

        val isActive = isEntitlementActive(entitlement, currentTime)

        if (!isActive) {
            val freeNotes = studyNotes.filter { !it.isPremium }
            val freePlan = plans.find { it.planName.equals("Free Plan", ignoreCase = true) || it.finalPrice == "0" || it.finalPrice == "₹0" }
            val freeFeatures = buildList {
                freePlan?.features?.let { addAll(it.split("|", ",")) }
                freePlan?.contents?.let { addAll(it.split("|", ",")) }
                entitlement?.benefits?.let { addAll(it.split("|", ",")) }
            }.map { it.trim() }.filter { it.isNotBlank() }

            val notesLimitFeature = freeFeatures.find { 
                it.startsWith("Study Note", ignoreCase = true) || it.startsWith("Study Notes", ignoreCase = true) || it.contains("Notes", ignoreCase = true)
            }
            val notesLimit = notesLimitFeature?.let { extractNumericalLimit(it) }

            val nonCaNotes = freeNotes.filter { !it.subject.contains("Current Affairs", ignoreCase = true) }
            val caNotes = freeNotes.filter { it.subject.contains("Current Affairs", ignoreCase = true) }

            val finalNonCa = if (notesLimit != null && notesLimit < nonCaNotes.size) {
                nonCaNotes.take(notesLimit)
            } else {
                nonCaNotes
            }

            val caLimitFeature = freeFeatures.find { 
                it.startsWith("Current Affair", ignoreCase = true) || it.startsWith("Current Affairs", ignoreCase = true) || it.contains("Current Affairs", ignoreCase = true)
            }
            val caLimit = caLimitFeature?.let { extractNumericalLimit(it) }

            val finalCa = if (caLimit != null && caLimit < caNotes.size) {
                caNotes.take(caLimit)
            } else {
                caNotes
            }

            return finalNonCa + finalCa
        }

        val activePlan = entitlement?.let { ent ->
            plans.find { it.id.toString() == ent.planId || it.planName.equals(ent.planName, ignoreCase = true) }
        }

        val rawExamTarget = activePlan?.examTarget ?: ""
        val allowedExams = rawExamTarget.split(",").map { it.trim() }.filter { it.isNotEmpty() && !it.equals("All Exams", ignoreCase = true) }

        val nonCaNotes = studyNotes.filter { !it.subject.contains("Current Affairs", ignoreCase = true) }
        val caNotes = studyNotes.filter { it.subject.contains("Current Affairs", ignoreCase = true) }

        val candidateNonCa = if (allowedExams.isEmpty()) {
            nonCaNotes
        } else {
            nonCaNotes.filter { note ->
                !note.isPremium || matchesExamTarget(note.subject, "${note.topic} ${note.titleEn}", allowedExams)
            }
        }

        val candidateCa = if (allowedExams.isEmpty()) {
            caNotes
        } else {
            caNotes.filter { note ->
                !note.isPremium || matchesExamTarget(note.subject, "${note.topic} ${note.titleEn}", allowedExams)
            }
        }

        val featuresAndContents = buildList {
            activePlan?.features?.let { addAll(it.split("|", ",")) }
            activePlan?.contents?.let { addAll(it.split("|", ",")) }
            entitlement?.benefits?.let { addAll(it.split("|", ",")) }
        }.map { it.trim() }.filter { it.isNotBlank() }

        val notesLimitFeature = featuresAndContents.find { 
            it.startsWith("Study Note", ignoreCase = true) || it.startsWith("Study Notes", ignoreCase = true) || it.contains("Notes", ignoreCase = true)
        }
        val notesLimit = notesLimitFeature?.let { extractNumericalLimit(it) }

        val finalNonCa = if (notesLimit != null && notesLimit < candidateNonCa.size) {
            val freeNotes = candidateNonCa.filter { !it.isPremium }
            val premiumNotes = candidateNonCa.filter { it.isPremium }
            val remainingLimit = (notesLimit - freeNotes.size).coerceAtLeast(0)
            (freeNotes + premiumNotes.take(remainingLimit)).distinctBy { it.id }
        } else {
            candidateNonCa
        }

        val caLimitFeature = featuresAndContents.find { 
            it.startsWith("Current Affair", ignoreCase = true) || it.startsWith("Current Affairs", ignoreCase = true) || it.contains("Current Affairs", ignoreCase = true)
        }
        val caLimit = caLimitFeature?.let { extractNumericalLimit(it) }

        val finalCa = if (caLimit != null && caLimit < candidateCa.size) {
            val freeCa = candidateCa.filter { !it.isPremium }
            val premiumCa = candidateCa.filter { it.isPremium }
            val remainingLimit = (caLimit - freeCa.size).coerceAtLeast(0)
            (freeCa + premiumCa.take(remainingLimit)).distinctBy { it.id }
        } else {
            candidateCa
        }

        return finalNonCa + finalCa
    }

    /**
     * Filters questions accessible under the user's active entitlement / plan.
     */
    fun filterAccessibleQuestions(
        userProfile: UserProfileEntity?,
        entitlement: EntitlementEntity?,
        plans: List<PlanEntity>,
        questions: List<QuestionEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<QuestionEntity> {
        val validQuestions = questions.filter { !it.isReported }
        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com")
        if (isUserAdmin) return validQuestions

        val isActive = isEntitlementActive(entitlement, currentTime)

        if (!isActive) {
            val freeQuestions = validQuestions.filter { !it.isPremium }
            val freePlan = plans.find { it.planName.equals("Free Plan", ignoreCase = true) || it.finalPrice == "0" || it.finalPrice == "₹0" }
            val freeFeatures = buildList {
                freePlan?.features?.let { addAll(it.split("|", ",")) }
                freePlan?.contents?.let { addAll(it.split("|", ",")) }
                entitlement?.benefits?.let { addAll(it.split("|", ",")) }
            }.map { it.trim() }.filter { it.isNotBlank() }

            val qLimitFeature = freeFeatures.find { 
                it.startsWith("Question", ignoreCase = true) || it.startsWith("Questions", ignoreCase = true) || it.contains("MCQ", ignoreCase = true)
            }
            val qLimit = qLimitFeature?.let { extractNumericalLimit(it) }
            return if (qLimit != null && qLimit < freeQuestions.size) {
                freeQuestions.take(qLimit)
            } else {
                freeQuestions
            }
        }

        val activePlan = entitlement?.let { ent ->
            plans.find { it.id.toString() == ent.planId || it.planName.equals(ent.planName, ignoreCase = true) }
        }

        val rawExamTarget = activePlan?.examTarget ?: ""
        val allowedExams = rawExamTarget.split(",").map { it.trim() }.filter { it.isNotEmpty() && !it.equals("All Exams", ignoreCase = true) }

        val candidateQuestions = if (allowedExams.isEmpty()) {
            validQuestions
        } else {
            validQuestions.filter { q ->
                !q.isPremium || matchesExamTarget(q.examCategory, "${q.subject} ${q.topic}", allowedExams)
            }
        }

        val featuresAndContents = buildList {
            activePlan?.features?.let { addAll(it.split("|", ",")) }
            activePlan?.contents?.let { addAll(it.split("|", ",")) }
            entitlement?.benefits?.let { addAll(it.split("|", ",")) }
        }.map { it.trim() }.filter { it.isNotBlank() }

        val qLimitFeature = featuresAndContents.find { 
            it.startsWith("Question", ignoreCase = true) || it.startsWith("Questions", ignoreCase = true) || it.contains("MCQ", ignoreCase = true)
        }
        val qLimit = qLimitFeature?.let { extractNumericalLimit(it) }

        return if (qLimit != null && qLimit < candidateQuestions.size) {
            val freeQuestions = candidateQuestions.filter { !it.isPremium }
            val premiumQuestions = candidateQuestions.filter { it.isPremium }
            val remainingLimit = (qLimit - freeQuestions.size).coerceAtLeast(0)
            (freeQuestions + premiumQuestions.take(remainingLimit)).distinctBy { it.id }
        } else {
            candidateQuestions
        }
    }

    /**
     * Calculates authoritative counts of accessible content for the specific user's active plan.
     * Guaranteed user isolation based on current user's profile and entitlement.
     */
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
        val accessibleMocks = filterAccessibleMockTests(userProfile, entitlement, plans, mockTests, isAdminOrOwner, currentTime)
        val accessibleNotes = filterAccessibleStudyNotes(userProfile, entitlement, plans, studyNotes, isAdminOrOwner, currentTime)
        val accessibleQs = filterAccessibleQuestions(userProfile, entitlement, plans, questions, isAdminOrOwner, currentTime)

        val nonCaCount = accessibleNotes.count { !it.subject.contains("Current Affairs", ignoreCase = true) }
        val caCount = accessibleNotes.count { it.subject.contains("Current Affairs", ignoreCase = true) }

        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com")
        val isActive = isEntitlementActive(entitlement, currentTime)

        val activePlan = entitlement?.let { ent ->
            plans.find { it.id.toString() == ent.planId || it.planName.equals(ent.planName, ignoreCase = true) }
        }

        val effectivePlanName = when {
            isUserAdmin -> "Admin Access"
            isActive && activePlan != null -> activePlan.planName
            isActive && entitlement != null && entitlement.planName.isNotBlank() -> entitlement.planName
            else -> "Free Plan"
        }

        return PlanAccessibleContentCounts(
            mockTestsCount = accessibleMocks.size,
            studyNotesCount = nonCaCount,
            currentAffairsCount = caCount,
            questionsCount = accessibleQs.size,
            effectivePlanName = effectivePlanName,
            isPlanActive = isUserAdmin || isActive
        )
    }
}
