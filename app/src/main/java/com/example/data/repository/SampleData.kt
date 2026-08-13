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
    val sampleSubjectsChapters = listOf(
        // 1. Assam History & Culture
        SubjectChapterEntity(id = 1L, subject = "Assam History & Culture", chapter = "Ancient and Medieval Assam"),
        SubjectChapterEntity(id = 2L, subject = "Assam History & Culture", chapter = "Ahom Kingdom and Administration"),
        SubjectChapterEntity(id = 3L, subject = "Assam History & Culture", chapter = "British Rule and Freedom Struggle in Assam"),
        SubjectChapterEntity(id = 4L, subject = "Assam History & Culture", chapter = "Art, Culture, Festivals and Traditions of Assam"),

        // 2. Assam Geography & Economy
        SubjectChapterEntity(id = 5L, subject = "Assam Geography & Economy", chapter = "Physiography, Climate and River Systems of Assam"),
        SubjectChapterEntity(id = 6L, subject = "Assam Geography & Economy", chapter = "National Parks, Wildlife Sanctuaries and Biodiversity"),
        SubjectChapterEntity(id = 7L, subject = "Assam Geography & Economy", chapter = "Minerals, Industries and Natural Resources"),
        SubjectChapterEntity(id = 8L, subject = "Assam Geography & Economy", chapter = "Demography, Agriculture and Economy of Assam"),

        // 3. Indian Polity & Constitution
        SubjectChapterEntity(id = 9L, subject = "Indian Polity & Constitution", chapter = "Preamble, Fundamental Rights and DPSP"),
        SubjectChapterEntity(id = 10L, subject = "Indian Polity & Constitution", chapter = "Union Executive and Parliament"),
        SubjectChapterEntity(id = 11L, subject = "Indian Polity & Constitution", chapter = "Judiciary and Constitutional Bodies"),
        SubjectChapterEntity(id = 12L, subject = "Indian Polity & Constitution", chapter = "Panchayati Raj and Local Government"),

        // 4. Indian History & National Movement
        SubjectChapterEntity(id = 13L, subject = "Indian History & National Movement", chapter = "Indus Valley Civilization & Vedic Period"),
        SubjectChapterEntity(id = 14L, subject = "Indian History & National Movement", chapter = "Maurya, Gupta and Mughal Empires"),
        SubjectChapterEntity(id = 15L, subject = "Indian History & National Movement", chapter = "Revolt of 1857 and Social Reform Movements"),
        SubjectChapterEntity(id = 16L, subject = "Indian History & National Movement", chapter = "Indian National Movement (1885-1947)"),

        // 5. General English
        SubjectChapterEntity(id = 17L, subject = "General English", chapter = "Grammar & Sentence Correction"),
        SubjectChapterEntity(id = 18L, subject = "General English", chapter = "Synonyms, Antonyms & Vocabulary"),
        SubjectChapterEntity(id = 19L, subject = "General English", chapter = "Idioms, Phrases & One-Word Substitution"),
        SubjectChapterEntity(id = 20L, subject = "General English", chapter = "Reading Comprehension & Para Jumbles"),

        // 6. General Mathematics
        SubjectChapterEntity(id = 21L, subject = "General Mathematics", chapter = "Number System, LCM & HCF"),
        SubjectChapterEntity(id = 22L, subject = "General Mathematics", chapter = "Percentage, Ratio & Proportion"),
        SubjectChapterEntity(id = 23L, subject = "General Mathematics", chapter = "Profit, Loss, Discount & Simple/Compound Interest"),
        SubjectChapterEntity(id = 24L, subject = "General Mathematics", chapter = "Time, Work, Speed, Distance & Mensuration"),

        // 7. Logical Reasoning & Mental Ability
        SubjectChapterEntity(id = 25L, subject = "Logical Reasoning & Mental Ability", chapter = "Coding-Decoding, Series & Analogy"),
        SubjectChapterEntity(id = 26L, subject = "Logical Reasoning & Mental Ability", chapter = "Blood Relations & Direction Sense Test"),
        SubjectChapterEntity(id = 27L, subject = "Logical Reasoning & Mental Ability", chapter = "Seating Arrangement, Puzzles & Venn Diagrams"),
        SubjectChapterEntity(id = 28L, subject = "Logical Reasoning & Mental Ability", chapter = "Syllogism, Statements & Assumptions"),

        // 8. General Science
        SubjectChapterEntity(id = 29L, subject = "General Science", chapter = "Physics: Mechanics, Light & Electricity"),
        SubjectChapterEntity(id = 30L, subject = "General Science", chapter = "Chemistry: Elements, Compounds & Reactions"),
        SubjectChapterEntity(id = 31L, subject = "General Science", chapter = "Biology: Human Anatomy, Nutrition & Diseases"),

        // 9. Computer Knowledge
        SubjectChapterEntity(id = 32L, subject = "Computer Knowledge", chapter = "Computer Fundamentals & Architecture"),
        SubjectChapterEntity(id = 33L, subject = "Computer Knowledge", chapter = "Operating Systems & MS Office (Word, Excel, PowerPoint)"),
        SubjectChapterEntity(id = 34L, subject = "Computer Knowledge", chapter = "Internet, Networking & Cyber Security"),

        // 10. Current Affairs & General Awareness
        SubjectChapterEntity(id = 35L, subject = "Current Affairs & General Awareness", chapter = "National Current Affairs (2025-2026)"),
        SubjectChapterEntity(id = 36L, subject = "Current Affairs & General Awareness", chapter = "State (Assam) Current Affairs & Government Schemes"),
        SubjectChapterEntity(id = 37L, subject = "Current Affairs & General Awareness", chapter = "Sports, Awards and Important Personalities")
    )

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
