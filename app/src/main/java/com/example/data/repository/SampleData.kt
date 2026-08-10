package com.example.data.repository

import com.example.data.local.*

object SampleData {
    val sampleQuestions = emptyList<QuestionEntity>()
    val sampleMockTests = emptyList<MockTestEntity>()
    val sampleStudyNotes = emptyList<StudyNoteEntity>()
    val sampleExamUpdates = emptyList<ExamUpdateEntity>()
    val sampleBanners = listOf(
        BannerEntity(
            id = 1L,
            titleEn = "Assam Competitive Exam Prep 2026",
            titleAs = "অসম প্ৰতিযোগিতামূলক পৰীক্ষা প্ৰস্তুতি ২০২৬",
            subtitleEn = "Comprehensive Practice Tests, Study Notes & Daily Quizzes for ADRE, APDCL & Assam Police.",
            subtitleAs = "ADRE, APDCL আৰু অসম পুলিচৰ বাবে সম্পূৰ্ণ প্ৰস্তুতি।",
            badgeText = "FEATURED",
            type = "INFORMATION",
            actionType = "Link",
            isActive = true
        ),
        BannerEntity(
            id = 2L,
            titleEn = "Full Mock Test Series & QBank",
            titleAs = "সম্পূৰ্ণ মক টেষ্ট শৃংখলা আৰু প্ৰশ্ন বেংক",
            subtitleEn = "Attempt real-exam level mock tests with detailed performance analysis and ranking.",
            subtitleAs = "বাস্তৱ পৰীক্ষাৰ দৰে মক টেষ্ট দিয়ক আৰু আপোনাৰ স্থান জানক।",
            badgeText = "MOCK TESTS",
            type = "INFORMATION",
            actionType = "Mock Test",
            isActive = true
        ),
        BannerEntity(
            id = 3L,
            titleEn = "Unlock All Exam Resources with Premium",
            titleAs = "প্ৰিমিয়ামৰ সৈতে সকলো সম্পদ অনলক কৰক",
            subtitleEn = "Get unlimited access to all Mock Tests, Subject Chapters & Study Notes.",
            subtitleAs = "সকলো মক টেষ্ট আৰু নোটছৰ বাবে অসীমিত প্ৰৱেশাধিকাৰ পাওক।",
            badgeText = "SPECIAL OFFER",
            type = "PROMOTIONAL",
            actionType = "Link",
            planPrice = "999",
            discount = "50% OFF",
            finalPrice = "499",
            offerValidity = "1 Year Validity",
            isActive = true
        )
    )
    val sampleNotifications = emptyList<NotificationEntity>()
    val initialFaqs = emptyList<FaqEntity>()
    val samplePlans = emptyList<PlanEntity>()
    val sampleExams = emptyList<ExamEntity>()
    val sampleSubjectsChapters = emptyList<SubjectChapterEntity>()

    val initialUserProfile = UserProfileEntity(
        id = 1,
        name = "Guest User",
        email = "",
        mobile = "",
        district = "",
        examGoal = "",
        xp = 0,
        level = 1,
        dailyStreak = 0,
        totalSolved = 0,
        correctCount = 0,
        totalTimeMinutes = 0,
        isPremium = false,
        role = "USER",
        firebaseProjectId = "",
        joinedDate = "Aug 2026",
        isLoggedIn = false,
        currentDeviceId = "",
        activeDeviceId = ""
    )

    val initialAboutConfig = AboutConfigEntity(
        id = 1,
        appTitle = "Jukti",
        appSubtitleEn = "Test Your Knowledge",
        appSubtitleAs = "অসমৰ সৰ্ববৃহৎ পৰীক্ষা প্ৰস্তুতি এপ্প",
        versionText = "Version 1.0.0",
        missionEn = "Jukti is engineered to democratize competitive exam preparation.",
        missionAs = "যুক্তি এপ্পৰ প্ৰধান উদ্দেশ্য হৈছে প্ৰতিযোগীতামূলক পৰীক্ষাৰ প্ৰস্তুতি।",
        logoIconName = "School",
        copyrightText = "Copyright © 2026 Jukti Education. All rights reserved.",
        developerTagline = "Designed & Developed for Aspirants",
        contactEmail = "support@jukti.in",
        contactPhone = "",
        contactTelegram = "",
        contactWhatsapp = "",
        adminEmails = "",
        refundPolicyEn = "Our policy lasts 7 days.",
        refundPolicyAs = "আমাৰ ৰিফাণ্ড পলিচি ক্ৰয় কৰাৰ ৭ দিনৰ বাবে প্ৰযোজ্য。",
        founderName = "Pinku Bora",
        founderTitle = "Founder & Creator of Jukti",
        founderCredential = "ADRE 2022 Qualifier",
        founderDescription = "Jukti was created with a simple vision — to make competitive exam preparation smarter, more accessible, and more effective for aspirants.\n\nHaving experienced the competitive exam preparation journey myself, I understand the importance of consistent practice, quality questions, performance analysis, and identifying areas that need improvement.\n\nThrough Jukti, my goal is to provide aspirants with a focused platform where they can practice, test their knowledge, track their progress, and prepare with greater confidence.",
        founderPhotoUrl = "",
        founderTagline = "Jukti — Test Your Knowledge."
    )
}
