package com.example

import com.example.data.local.EntitlementEntity
import com.example.data.local.MockTestEntity
import com.example.data.local.QuestionEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.local.UserProfileEntity
import com.example.data.util.PlanValidityEngine
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
}
