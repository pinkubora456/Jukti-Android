package com.example.data.repository

import com.example.data.local.*

object SampleData {
    val sampleQuestions = emptyList<QuestionEntity>()
    val sampleMockTests = emptyList<MockTestEntity>()
    val sampleStudyNotes = emptyList<StudyNoteEntity>()
    val sampleExamUpdates = emptyList<ExamUpdateEntity>()
    val sampleBanners = emptyList<BannerEntity>()
    val sampleNotifications = emptyList<NotificationEntity>()
    val initialFaqs = emptyList<FaqEntity>()
    val samplePlans = emptyList<PlanEntity>()
    val sampleExams = emptyList<ExamEntity>()
    val CANONICAL_SUBJECTS = listOf(
        "General Knowledge",
        "General Mathematics",
        "Reasoning & Mental Ability",
        "General English",
        "Reading Comprehension",
        "Basic Computer",
        "Transport Rule"
    )

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
        contactEmail = "juktieducation@gmail.com",
        contactPhone = "",
        contactTelegram = "",
        contactWhatsapp = "",
        adminEmails = "",
        refundPolicyEn = "Our policy lasts 7 days.",
        refundPolicyAs = "আমাৰ ৰিফাণ্ড পলিচি ক্ৰয় কৰাৰ ৭ দিনৰ বাবে প্ৰযোজ্য。",
        founderName = "Pinku Bora",
        founderTitle = "Founder & Creator of Jukti",
        founderCredential = "",
        founderDescription = "Jukti was created with a simple vision — to make competitive exam preparation smarter, more accessible, and more effective for aspirants.\n\nHaving experienced the competitive exam preparation journey myself, I understand the importance of consistent practice, quality questions, performance analysis, and identifying areas that need improvement.\n\nThrough Jukti, my goal is to provide aspirants with a focused platform where they can practice, test their knowledge, track their progress, and prepare with greater confidence.",
        founderPhotoUrl = "",
        founderTagline = "Jukti — Test Your Knowledge."
    )
}
