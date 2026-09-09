package com.example

import com.example.data.local.*
import com.example.data.util.PlanValidityEngine
import com.example.data.util.GuidanceEngine
import org.junit.Assert.*
import org.junit.Test

class PlanValidityEngineTest {

    private val now = 1756166400000L // arbitrary fixed time

    @Test
    fun testFreePlanIsNotConsideredActivePremium() {
        val freeEntitlement = EntitlementEntity(
            userId = "user1",
            planId = "free_plan",
            planName = "Free Plan",
            status = "ACTIVE",
            validUntil = 0L,
            activatedAt = now - 100000L
        )

        assertFalse(
            "Free Plan should NOT be considered active premium",
            PlanValidityEngine.isEntitlementActive(freeEntitlement, now)
        )
    }

    @Test
    fun testExpiredPaidPlanIsNotActive() {
        val expiredEntitlement = EntitlementEntity(
            userId = "user1",
            planId = "adre_combo",
            planName = "ADRE 3.0 Mega Combo",
            status = "ACTIVE",
            validUntil = now - 1000L, // Expired
            activatedAt = now - 100000L
        )

        assertFalse(
            "Expired entitlement should return false",
            PlanValidityEngine.isEntitlementActive(expiredEntitlement, now)
        )
    }

    @Test
    fun testActivePaidPlanIsValid() {
        val activeEntitlement = EntitlementEntity(
            userId = "user1",
            planId = "adre_combo",
            planName = "ADRE 3.0 Mega Combo",
            status = "ACTIVE",
            validUntil = now + 86400000L, // Valid for 1 more day
            activatedAt = now - 100000L
        )

        assertTrue(
            "Valid active paid entitlement should return true",
            PlanValidityEngine.isEntitlementActive(activeEntitlement, now)
        )
    }

    @Test
    fun testQuestionsFilteringForFreeUser() {
        val freeQuestions = (1..100).map { i ->
            QuestionEntity(
                id = i.toLong(),
                subject = "General Knowledge",
                topic = "Assam History",
                difficulty = "Easy",
                questionEn = "Question $i",
                questionAs = "প্ৰশ্ন $i",
                optionAEn = "A",
                optionBEn = "B",
                optionCEn = "C",
                optionDEn = "D",
                optionAAs = "ক",
                optionBAs = "খ",
                optionCAs = "গ",
                optionDAs = "ঘ",
                correctOptionIndex = 0,
                explanationEn = "Exp",
                explanationAs = "ব্যাখ্যা",
                examCategory = "ADRE Grade III",
                isPremium = false
            )
        }

        val premiumQuestions = (101..150).map { i ->
            QuestionEntity(
                id = i.toLong(),
                subject = "General Knowledge",
                topic = "Assam History",
                difficulty = "Hard",
                questionEn = "Premium Question $i",
                questionAs = "প্ৰিমিয়াম প্ৰশ্ন $i",
                optionAEn = "A",
                optionBEn = "B",
                optionCEn = "C",
                optionDEn = "D",
                optionAAs = "ক",
                optionBAs = "খ",
                optionCAs = "গ",
                optionDAs = "ঘ",
                correctOptionIndex = 0,
                explanationEn = "Exp",
                explanationAs = "ব্যাখ্যা",
                examCategory = "ADRE Grade III",
                isPremium = true
            )
        }

        val allQuestions = freeQuestions + premiumQuestions

        val regularProfile = UserProfileEntity(
            id = 1,
            email = "test@example.com",
            role = "USER"
        )

        // Free user without active entitlement
        val accessibleForFree = PlanValidityEngine.filterAccessibleQuestions(
            questions = allQuestions,
            entitlement = null,
            plans = emptyList(),
            userProfile = regularProfile,
            isAdminOrOwner = false,
            currentTime = now
        )

        // Must NOT contain any premium questions
        assertTrue("Free user should not have access to premium questions", accessibleForFree.none { it.isPremium })

        // Owner/Admin should have access to ALL questions
        val accessibleForAdmin = PlanValidityEngine.filterAccessibleQuestions(
            questions = allQuestions,
            entitlement = null,
            plans = emptyList(),
            userProfile = regularProfile,
            isAdminOrOwner = true,
            currentTime = now
        )

        assertEquals("Admin should have access to all questions", allQuestions.size, accessibleForAdmin.size)
    }

    @Test
    fun testMockTestsFilteringForFreeUser() {
        val mockTests = listOf(
            MockTestEntity(
                id = 1L,
                titleEn = "Free Mock 1",
                titleAs = "ফ্ৰী মক ১",
                category = "ADRE",
                durationMinutes = 60,
                totalQuestions = 50,
                totalMarks = 50f,
                isPremium = false
            ),
            MockTestEntity(
                id = 2L,
                titleEn = "Premium Mock 1",
                titleAs = "প্ৰিমিয়াম মক ১",
                category = "ADRE",
                durationMinutes = 60,
                totalQuestions = 50,
                totalMarks = 50f,
                isPremium = true
            )
        )

        val regularProfile = UserProfileEntity(
            id = 1,
            email = "student@example.com",
            role = "USER"
        )

        val accessibleForFree = PlanValidityEngine.filterAccessibleMockTests(
            mockTests = mockTests,
            entitlement = null,
            plans = emptyList(),
            userProfile = regularProfile,
            isAdminOrOwner = false,
            currentTime = now
        )

        assertEquals(1, accessibleForFree.size)
        assertEquals(1L, accessibleForFree.first().id)
    }

    // --- 7 Test Cases for Plan-Based Guidance Access ---

    private val sampleExams = listOf(
        ExamEntity(id = 1L, title = "Grade 3", subtitle = "ADRE Grade 3"),
        ExamEntity(id = 2L, title = "Grade 4", subtitle = "ADRE Grade 4"),
        ExamEntity(id = 3L, title = "Graduate Level", subtitle = "Assam Graduate Level")
    )

    @Test
    fun testCase1_FreeUser_GuidanceLocked() {
        val effective = PlanValidityEngine.resolveEffectiveEntitlement(
            entitlements = emptyList(),
            plans = emptyList(),
            currentTime = now,
            isAdminOrOwner = false
        )

        // Free user has no premium entitlement, guidanceEnabled is false
        assertFalse(effective.isPremium)
        assertFalse(effective.guidanceEnabled)

        // Guidance accessible exams must be empty
        val accessibleExams = PlanValidityEngine.filterAccessibleGuidanceExams(sampleExams, effective, isAdminOrOwner = false)
        assertTrue(accessibleExams.isEmpty())

        // GuidanceEngine blocks calculateGuidance
        val guidance = GuidanceEngine.calculateGuidance(
            exam = "Grade 4",
            subject = null,
            allPyqFocus = emptyList<PyqFocusEntity>(),
            allQuestions = emptyList<QuestionEntity>(),
            userStates = emptyList<UserQuestionStateEntity>(),
            allPrepStrategies = emptyList<PrepStrategyEntity>(),
            effectiveEntitlement = effective,
            isAdminOrOwner = false
        )
        assertTrue(guidance.isAccessDenied)
    }

    @Test
    fun testCase2_UserWithGrade4Plan_GuidanceUnlockedForGrade4Only() {
        val plan = PlanEntity(
            id = 101L,
            planName = "Jukti Pass – Grade 4",
            planPrice = "199",
            discount = "0",
            finalPrice = "199",
            offerValidity = "1 Year",
            validityType = "YEARS",
            validityValue = 1,
            guidanceEnabled = true,
            guidanceAllowedExams = "Grade 4"
        )
        val entitlement = EntitlementEntity(
            userId = "user1",
            planId = "101",
            planName = "Jukti Pass – Grade 4",
            status = "ACTIVE",
            validUntil = now + 864000000L,
            activatedAt = now - 10000L
        )

        val effective = PlanValidityEngine.resolveEffectiveEntitlement(
            entitlements = listOf(entitlement),
            plans = listOf(plan),
            currentTime = now,
            isAdminOrOwner = false
        )

        assertTrue(effective.isPremium)
        assertTrue(effective.guidanceEnabled)
        assertTrue(PlanValidityEngine.isGuidanceAccessibleForExam("Grade 4", effective, false))
        assertFalse(PlanValidityEngine.isGuidanceAccessibleForExam("Grade 3", effective, false))
        assertFalse(PlanValidityEngine.isGuidanceAccessibleForExam("Graduate Level", effective, false))

        val accessibleExams = PlanValidityEngine.filterAccessibleGuidanceExams(sampleExams, effective, false)
        assertEquals(1, accessibleExams.size)
        assertEquals("Grade 4", accessibleExams.first().title)
    }

    @Test
    fun testCase3_UserWithGrade3Plan_GuidanceUnlockedForGrade3Only() {
        val plan = PlanEntity(
            id = 102L,
            planName = "Jukti Pass – Grade 3",
            planPrice = "199",
            discount = "0",
            finalPrice = "199",
            offerValidity = "1 Year",
            validityType = "YEARS",
            validityValue = 1,
            guidanceEnabled = true,
            guidanceAllowedExams = "Grade 3"
        )
        val entitlement = EntitlementEntity(
            userId = "user1",
            planId = "102",
            planName = "Jukti Pass – Grade 3",
            status = "ACTIVE",
            validUntil = now + 864000000L,
            activatedAt = now - 10000L
        )

        val effective = PlanValidityEngine.resolveEffectiveEntitlement(
            entitlements = listOf(entitlement),
            plans = listOf(plan),
            currentTime = now,
            isAdminOrOwner = false
        )

        assertTrue(effective.isPremium)
        assertTrue(effective.guidanceEnabled)
        assertTrue(PlanValidityEngine.isGuidanceAccessibleForExam("Grade 3", effective, false))
        assertFalse(PlanValidityEngine.isGuidanceAccessibleForExam("Grade 4", effective, false))

        val accessibleExams = PlanValidityEngine.filterAccessibleGuidanceExams(sampleExams, effective, false)
        assertEquals(1, accessibleExams.size)
        assertEquals("Grade 3", accessibleExams.first().title)
    }

    @Test
    fun testCase4_UserWithGraduateLevelPlan_GuidanceUnlockedForGraduateOnly() {
        val plan = PlanEntity(
            id = 103L,
            planName = "Jukti Pass – Graduate Level",
            planPrice = "299",
            discount = "0",
            finalPrice = "299",
            offerValidity = "1 Year",
            validityType = "YEARS",
            validityValue = 1,
            guidanceEnabled = true,
            guidanceAllowedExams = "Graduate Level"
        )
        val entitlement = EntitlementEntity(
            userId = "user1",
            planId = "103",
            planName = "Jukti Pass – Graduate Level",
            status = "ACTIVE",
            validUntil = now + 864000000L,
            activatedAt = now - 10000L
        )

        val effective = PlanValidityEngine.resolveEffectiveEntitlement(
            entitlements = listOf(entitlement),
            plans = listOf(plan),
            currentTime = now,
            isAdminOrOwner = false
        )

        assertTrue(effective.isPremium)
        assertTrue(effective.guidanceEnabled)
        assertTrue(PlanValidityEngine.isGuidanceAccessibleForExam("Graduate Level", effective, false))
        assertFalse(PlanValidityEngine.isGuidanceAccessibleForExam("Grade 4", effective, false))

        val accessibleExams = PlanValidityEngine.filterAccessibleGuidanceExams(sampleExams, effective, false)
        assertEquals(1, accessibleExams.size)
        assertEquals("Graduate Level", accessibleExams.first().title)
    }

    @Test
    fun testCase5_UserWithGrade3PlusGrade4Plan_GuidanceUnlockedForBoth() {
        val plan = PlanEntity(
            id = 104L,
            planName = "Jukti Pass – Grade 3 + Grade 4",
            planPrice = "349",
            discount = "0",
            finalPrice = "349",
            offerValidity = "1 Year",
            validityType = "YEARS",
            validityValue = 1,
            guidanceEnabled = true,
            guidanceAllowedExams = "Grade 3, Grade 4"
        )
        val entitlement = EntitlementEntity(
            userId = "user1",
            planId = "104",
            planName = "Jukti Pass – Grade 3 + Grade 4",
            status = "ACTIVE",
            validUntil = now + 864000000L,
            activatedAt = now - 10000L
        )

        val effective = PlanValidityEngine.resolveEffectiveEntitlement(
            entitlements = listOf(entitlement),
            plans = listOf(plan),
            currentTime = now,
            isAdminOrOwner = false
        )

        assertTrue(effective.isPremium)
        assertTrue(effective.guidanceEnabled)
        assertTrue(PlanValidityEngine.isGuidanceAccessibleForExam("Grade 3", effective, false))
        assertTrue(PlanValidityEngine.isGuidanceAccessibleForExam("Grade 4", effective, false))
        assertFalse(PlanValidityEngine.isGuidanceAccessibleForExam("Graduate Level", effective, false))

        val accessibleExams = PlanValidityEngine.filterAccessibleGuidanceExams(sampleExams, effective, false)
        assertEquals(2, accessibleExams.size)
        val titles = accessibleExams.map { it.title }.toSet()
        assertTrue(titles.contains("Grade 3"))
        assertTrue(titles.contains("Grade 4"))
    }

    @Test
    fun testCase6_UserWithAllExams_FullAccess() {
        val plan = PlanEntity(
            id = 105L,
            planName = "All Access Mega Plan",
            planPrice = "599",
            discount = "0",
            finalPrice = "599",
            offerValidity = "Lifetime",
            validityType = "LIFETIME",
            validityValue = 0,
            guidanceEnabled = true,
            guidanceAllowedExams = "All, Grade 3, Grade 4, Graduate Level"
        )
        val entitlement = EntitlementEntity(
            userId = "user1",
            planId = "105",
            planName = "All Access Mega Plan",
            status = "ACTIVE",
            validUntil = 0L,
            isLifetime = true,
            activatedAt = now - 10000L
        )

        val effective = PlanValidityEngine.resolveEffectiveEntitlement(
            entitlements = listOf(entitlement),
            plans = listOf(plan),
            currentTime = now,
            isAdminOrOwner = false
        )

        assertTrue(effective.isPremium)
        assertTrue(effective.guidanceEnabled)
        assertTrue(PlanValidityEngine.isGuidanceAccessibleForExam("Grade 3", effective, false))
        assertTrue(PlanValidityEngine.isGuidanceAccessibleForExam("Grade 4", effective, false))
        assertTrue(PlanValidityEngine.isGuidanceAccessibleForExam("Graduate Level", effective, false))

        val accessibleExams = PlanValidityEngine.filterAccessibleGuidanceExams(sampleExams, effective, false)
        assertEquals(3, accessibleExams.size)
    }

    @Test
    fun testCase7_UnauthorizedDirectRequest_IsBlocked() {
        val plan = PlanEntity(
            id = 106L,
            planName = "Jukti Pass – Grade 4",
            planPrice = "199",
            discount = "0",
            finalPrice = "199",
            offerValidity = "1 Year",
            validityType = "YEARS",
            validityValue = 1,
            guidanceEnabled = true,
            guidanceAllowedExams = "Grade 4"
        )
        val entitlement = EntitlementEntity(
            userId = "user1",
            planId = "106",
            planName = "Jukti Pass – Grade 4",
            status = "ACTIVE",
            validUntil = now + 864000000L,
            activatedAt = now - 10000L
        )

        val effective = PlanValidityEngine.resolveEffectiveEntitlement(
            entitlements = listOf(entitlement),
            plans = listOf(plan),
            currentTime = now,
            isAdminOrOwner = false
        )

        // Grade 4 should succeed
        val authorizedGuidance = GuidanceEngine.calculateGuidance(
            exam = "Grade 4",
            subject = null,
            allPyqFocus = emptyList<PyqFocusEntity>(),
            allQuestions = emptyList<QuestionEntity>(),
            userStates = emptyList<UserQuestionStateEntity>(),
            allPrepStrategies = emptyList<PrepStrategyEntity>(),
            effectiveEntitlement = effective,
            isAdminOrOwner = false
        )
        assertFalse(authorizedGuidance.isAccessDenied)

        // Grade 3 (unauthorized) must be blocked and return isAccessDenied = true with no guidance data
        val unauthorizedGuidance = GuidanceEngine.calculateGuidance(
            exam = "Grade 3",
            subject = null,
            allPyqFocus = emptyList<PyqFocusEntity>(),
            allQuestions = emptyList<QuestionEntity>(),
            userStates = emptyList<UserQuestionStateEntity>(),
            allPrepStrategies = emptyList<PrepStrategyEntity>(),
            effectiveEntitlement = effective,
            isAdminOrOwner = false
        )
        assertTrue("Unauthorized direct request for Grade 3 must be blocked", unauthorizedGuidance.isAccessDenied)
        assertTrue(unauthorizedGuidance.pyqFocus.isEmpty())
        assertTrue(unauthorizedGuidance.priorityTopics.isEmpty())
        assertTrue(unauthorizedGuidance.strengthWeakness.isEmpty())
        assertTrue(unauthorizedGuidance.preparationStrategy.isEmpty())
    }
}
