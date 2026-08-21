package com.example.data.repository

import com.example.data.local.*

object SampleData {
    val sampleQuestions = emptyList<QuestionEntity>()
    val sampleMockTests = emptyList<MockTestEntity>()
    val sampleStudyNotes = emptyList<StudyNoteEntity>()
    val sampleExamUpdates = listOf(
        ExamUpdateEntity(
            id = 1L,
            examName = "ADRE Grade 3",
            category = "Syllabus",
            titleEn = "ADRE Grade 3 & 4 Detailed Syllabus & Topics",
            titleAs = "ADRE গ্ৰেড ৩ আৰু ৪ ৰ সম্পূৰ্ণ পাঠ্যক্ৰম",
            detailEn = "Comprehensive syllabus covering General Knowledge, Social Studies, General Mathematics, Logical Reasoning, General English, and Mental Ability for Bachelor's, HSLC, and HSSLC levels.",
            detailAs = "স্নাতক, উচ্চতৰ মাধ্যমিক আৰু হাইস্কুল শিক্ষান্ত পৰ্যায়ৰ বাবে সাধাৰণ জ্ঞান, সামাজিক বিজ্ঞান, গণিত, যুক্তিবিদ্যা আৰু ইংৰাজীৰ সম্পূৰ্ণ পাঠ্যক্ৰম।",
            updateDate = "August 2026",
            officialLink = "https://sebaonline.org"
        ),
        ExamUpdateEntity(
            id = 2L,
            examName = "ADRE Grade 3",
            category = "Pattern",
            titleEn = "ADRE Grade 3 Exam Pattern & Marking Scheme",
            titleAs = "ADRE গ্ৰেড ৩ পৰীক্ষাৰ আৰ্হি আৰু নম্বৰ বিভাজন",
            detailEn = "150 Multiple Choice Questions (MCQs) carrying 1 mark each. Negative marking of 0.25 marks for every incorrect answer. Time duration: 3 hours.",
            detailAs = "১৫০ টা বহুবিকল্পী প্ৰশ্ন (MCQ), প্ৰতিটোৰ নম্বৰ ১। প্ৰতিটো ভুল উত্তৰৰ বাবে ০.২৫ নম্বৰ কৰ্তন কৰা হ'ব। সময়: ৩ ঘণ্টা।",
            updateDate = "August 2026",
            officialLink = "https://sebaonline.org"
        ),
        ExamUpdateEntity(
            id = 3L,
            examName = "ADRE Grade 3",
            category = "Cutoff",
            titleEn = "ADRE Grade 3 & 4 Expected Category-wise Cutoff",
            titleAs = "ADRE গ্ৰেড ৩ আৰু ৪ ৰ প্ৰত্যাশিত কাট-অফ নম্বৰ",
            detailEn = "Unreserved (UR): 118-124 Marks | OBC/MOBC: 110-116 Marks | EWS: 108-114 Marks | SC: 102-108 Marks | ST: 96-102 Marks out of 150 marks.",
            detailAs = "সংৰক্ষিত নোহোৱা (UR): ১১৮-১২৪ | OBC/MOBC: ১১০-১১৬ | EWS: ১০৮-১১৪ | SC: ১০২-১০৮ | ST: ৯৬-১০২ (১৫০ নম্বৰৰ ভিতৰত)।",
            updateDate = "August 2026",
            officialLink = "https://sebaonline.org"
        ),
        ExamUpdateEntity(
            id = 4L,
            examName = "APSC CCE",
            category = "Syllabus",
            titleEn = "APSC CCE Prelims GS Paper 1 & Paper 2 Syllabus",
            titleAs = "APSC CCE প্ৰিলিমছ GS পেপাৰ ১ আৰু পেপাৰ ২ পাঠ্যক্ৰম",
            detailEn = "General Studies Paper 1 (History, Geography, Polity, Economy, Science, Assam GK) and CSAT Paper 2 (Logical Reasoning, Comprehension, Interpersonal skills, Basic Numeracy).",
            detailAs = "সাধাৰণ অধ্যয়ন পেপাৰ ১ (ইতিহাস, ভূগো, ৰাজনীতি, অর্থনীতি, বিজ্ঞান, অসমৰ জ্ঞান) আৰু CSAT পেপাৰ ২ (যুক্তিবিদ্যা, বুজাবুজি, গণিত)।",
            updateDate = "August 2026",
            officialLink = "https://apsc.nic.in"
        ),
        ExamUpdateEntity(
            id = 5L,
            examName = "APSC CCE",
            category = "Pattern",
            titleEn = "APSC Combined Competitive Examination Pattern",
            titleAs = "APSC সংযুক্ত প্ৰতিযোগিতামূলক পৰীক্ষাৰ আৰ্হি",
            detailEn = "Prelims (Objective, 200 marks each for GS1 & GS2), Mains (Descriptive papers including Essay, General Studies 1-4, and Assam Specific papers), and Personality Test (275 Marks).",
            detailAs = "প্ৰিলিমছ (বস্তুনিষ্ঠ, ২০০ নম্বৰ), মেইনছ (বৰ্ণনাত্মক পেপাৰ, সাধাৰণ অধ্যয়ন ১-৪ আৰু অসম বিষয়ক পেপাৰ), আৰু সাক্ষাৎকাৰ (২৭৫ নম্বৰ)।",
            updateDate = "August 2026",
            officialLink = "https://apsc.nic.in"
        ),
        ExamUpdateEntity(
            id = 6L,
            examName = "APSC CCE",
            category = "Cutoff",
            titleEn = "APSC CCE Prelims Expected Cutoff Marks",
            titleAs = "APSC CCE প্ৰিলিমছ প্ৰত্যাশিত কাট-অফ নম্বৰ",
            detailEn = "General (UR Male): 112-118 | General (UR Female): 106-112 | OBC/MOBC: 104-110 | SC/ST: 95-102 out of 200 marks.",
            detailAs = "সাধাৰণ (পুৰুষ): ১১২-১১৮ | সাধাৰণ (মহিলা): ১০৬-১১২ | OBC/MOBC: ১০৪-১১০ | SC/ST: ৯৫-১০২ (২০০ নম্বৰৰ ভিতৰত)।",
            updateDate = "August 2026",
            officialLink = "https://apsc.nic.in"
        ),
        ExamUpdateEntity(
            id = 7L,
            examName = "Assam Police SI",
            category = "Pattern",
            titleEn = "Assam Police SI Written Test Pattern & Marking",
            titleAs = "অসম পুলিচ SI লিখিত পৰীক্ষাৰ আৰ্হি আৰু নম্বৰ",
            detailEn = "100 questions of 1 mark each. Subjects: Logical reasoning, aptitude, comprehension, current affairs, general knowledge, and Assam history/polity. Negative marking of 1/2 mark for wrong answers.",
            detailAs = "১০০ টা প্ৰশ্ন, প্ৰতিটোৰ নম্বৰ ১। বিষয়সমূহ: যুক্তিবিদ্যা, গণিত, বৰ্তমানৰ ঘটনাবলী, সাধাৰণ জ্ঞান আৰু অসমৰ ইতিহাস। প্ৰতিটো ভুল উত্তৰৰ বাবে ০.৫ নম্বৰ কৰ্তন।",
            updateDate = "August 2026",
            officialLink = "https://slprbassam.in"
        ),
        ExamUpdateEntity(
            id = 8L,
            examName = "Assam Police SI",
            category = "Cutoff",
            titleEn = "Assam Police SI Expected Cutoff Marks",
            titleAs = "অসম পুলিচ SI প্ৰত্যাশিত কাট-অফ নম্বৰ",
            detailEn = "UR Male: 74-78 | UR Female: 68-72 | OBC/MOBC: 69-73 | SC/ST: 62-67 out of 100 marks.",
            detailAs = "UR পুৰুষ: ৭৪-৭৮ | UR মহিলা: ৬৮-৭২ | OBC/MOBC: ৬৯-৭৩ | SC/ST: ৬২-৬৭ (১০০ নম্বৰৰ ভিতৰত)।",
            updateDate = "August 2026",
            officialLink = "https://slprbassam.in"
        ),
        ExamUpdateEntity(
            id = 9L,
            examName = "Assam TET",
            category = "Cutoff",
            titleEn = "Assam Special TET Qualifying Cutoff Marks",
            titleAs = "অসম টেট উত্তীৰ্ণৰ কাট-অফ নম্বৰ",
            detailEn = "General / Unreserved: 90 Marks (60%) | OBC / SC / ST / PwD: 83.5 Marks (55%) for Lower Primary & Upper Primary levels.",
            detailAs = "সাধাৰণ / সংৰক্ষিত নোহোৱা: ৯০ নম্বৰ (৬০%) | OBC / SC / ST / PwD: ৮৩.৫ নম্বৰ (৫৫%) নিম্ন আৰু উচ্চ প্ৰাথমিক পৰ্যায়ৰ বাবে।",
            updateDate = "August 2026",
            officialLink = "https://ssa.assam.gov.in"
        ),
        ExamUpdateEntity(
            id = 10L,
            examName = "ADRE Grade 3",
            category = "Admit Card",
            titleEn = "ADRE Grade 3 Phase II Admit Card Download Link Active",
            titleAs = "ADRE গ্ৰেড ৩ দ্বিতীয় পৰ্যায়ৰ এডমিট কাৰ্ড ডাইনলোড",
            detailEn = "Candidates who have successfully registered can now download their admit cards from the official SEBA portal using registration number and date of birth.",
            detailAs = "পঞ্জীভভুক্ত প্ৰাৰ্থীসকলে অফিচিয়েল পৰ্টেলৰ পৰা পঞ্জীয়ন নম্বৰ আৰু জন্ম তাৰিখ ব্যৱহাৰ কৰি এডমিট কাৰ্ড ডাউনলোড কৰিব পাৰিব।",
            updateDate = "August 2026",
            officialLink = "https://sebaonline.org"
        )
    )
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
        contactEmail = "juktieducation@gmail.com",
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
