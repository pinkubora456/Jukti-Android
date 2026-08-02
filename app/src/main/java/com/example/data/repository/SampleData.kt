package com.example.data.repository

import com.example.data.local.*

object SampleData {

    val sampleQuestions = listOf(
        QuestionEntity(
            subject = "Assam History",
            topic = "Ahom Dynasty",
            difficulty = "Medium",
            questionEn = "Who was the first king of the Ahom Kingdom in Assam?",
            questionAs = "অসমৰ আহোম ৰাজ্যৰ প্ৰথমজন ৰজা কোন আছিল?",
            optionAEn = "Suhingmung",
            optionBEn = "Sukaphaa",
            optionCEn = "Rudra Singha",
            optionDEn = "Gadapani",
            optionAAs = "চুহিঙমুং",
            optionBAs = "চাওলুং চ্যুকাফা",
            optionCAs = "ৰুদ্ৰ সিংহ",
            optionDAs = "গদাপানি",
            correctOptionIndex = 1,
            explanationEn = "Chaolung Sukaphaa established the Ahom kingdom in 1228 AD in Charaideo after crossing the Patkai hills.",
            explanationAs = "১২২৮ খ্ৰীষ্টাব্দত পাটকাই পৰ্বত পাৰ হৈ চৰাইদেউত চাওলুং চ্যুকাফাই আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল।",
            isBookmarked = true,
            isLiked = true,
            examCategory = "ADRE"
        ),
        QuestionEntity(
            subject = "Assam History",
            topic = "Battle of Saraighat",
            difficulty = "Medium",
            questionEn = "In which year was the famous Battle of Saraighat fought between Ahoms and Mughals?",
            questionAs = "আহোম আৰু মোগলৰ মাজত বিখ্যাত শৰাইঘাটৰ ৰণ কোন চনত হৈছিল?",
            optionAEn = "1662",
            optionBEn = "1671",
            optionCEn = "1682",
            optionDEn = "1707",
            optionAAs = "১৬৬২",
            optionBAs = "১৬৭১",
            optionCAs = "১৬৮২",
            optionDAs = "১৭০৭",
            correctOptionIndex = 1,
            explanationEn = "The Battle of Saraighat was fought in 1671 on the Brahmaputra River. Lachit Borphukan led the Ahom army to a decisive victory against Ram Singh I.",
            explanationAs = "১৬৭১ চনত ব্ৰহ্মপুত্ৰ নদীত শৰাইঘাটৰ যুদ্ধ হৈছিল। মহাবীৰ লাচিত বৰফুকনে ৰাম সিংহৰ নেতৃত্বত অহা মোগল বাহিনীক পৰাস্ত কৰিছিল।",
            isBookmarked = false,
            isLiked = true,
            examCategory = "ADRE"
        ),
        QuestionEntity(
            subject = "Assam Geography",
            topic = "National Parks",
            difficulty = "Easy",
            questionEn = "Which National Park in Assam is world-famous for the One-Horned Rhinoceros?",
            questionAs = "এশিঙীয়া গঁৰাৰ বাবে অসমৰ কোনখন ৰাষ্ট্ৰীয় উদ্যান সমগ্ৰ বিশ্বতে প্ৰসিদ্ধ?",
            optionAEn = "Manas National Park",
            optionBEn = "Kaziranga National Park",
            optionCEn = "Nameri National Park",
            optionDEn = "Dibru-Saikhowa National Park",
            optionAAs = "মানস ৰাষ্ট্ৰীয় উদ্যান",
            optionBAs = "কাজিৰঙা ৰাষ্ট্ৰীয় উদ্যান",
            optionCAs = "নামেৰি ৰাষ্ট্ৰীয় উদ্যান",
            optionDAs = "ডিব্ৰু-ছৈখোৱা ৰাষ্ট্ৰীয় উদ্যান",
            correctOptionIndex = 1,
            explanationEn = "Kaziranga National Park hosts two-thirds of the world's great one-horned rhinoceroses and is a UNESCO World Heritage Site.",
            explanationAs = "কাজিৰঙা ৰাষ্ট্ৰীয় উদ্যানত বিশ্বৰ এশিঙীয়া গঁৰাৰ প্ৰায় দুই-তৃতীয়াংশ বাস কৰে। ই এখন ইউনেস্কো বিশ্ব ঐতিহ্য ক্ষেত্ৰ।",
            isBookmarked = true,
            isLiked = true,
            examCategory = "ADRE"
        ),
        QuestionEntity(
            subject = "Assamese Literature & Culture",
            topic = "Srimanta Sankardev",
            difficulty = "Medium",
            questionEn = "Who is known as the Father of Assamese Prose and Mahapurush of Ekasarana Dharma?",
            questionAs = "একশৰণ নামধৰ্মৰ প্ৰচাৰক আৰু অসমীয়া সংস্কৃতিৰ মহাপুৰুষ কোন আছিল?",
            optionAEn = "Madhavdev",
            optionBEn = "Srimanta Sankardev",
            optionCEn = "Bhattadeva",
            optionDEn = "Ananta Kandali",
            optionAAs = "শ্ৰীশ্ৰী মাধৱদেৱ",
            optionBAs = "মহাপুৰুষ শ্ৰীমন্ত শংকৰদেৱ",
            optionCAs = "ভট্টদেৱ",
            optionDAs = "অনন্ত কন্দলী",
            correctOptionIndex = 1,
            explanationEn = "Srimanta Sankardev (1449–1568) was a saint-scholar, poet, playwright, and social-religious reformer who founded Ekasarana Dharma.",
            explanationAs = "মহাপুৰুষ শ্ৰীমন্ত শংকৰদেৱ পঞ্চদশ-ষোড়শ শতিকাৰ এজন মহান বৈষ্ণৱ গুৰু, সাহিত্যিক আৰু সমাজ সংস্কাৰক আছিল।",
            isBookmarked = false,
            isLiked = true,
            examCategory = "APSC"
        ),
        QuestionEntity(
            subject = "General Knowledge",
            topic = "Assam State Symbols",
            difficulty = "Easy",
            questionEn = "What is the official State Tree of Assam?",
            questionAs = "অসমৰ ৰাজ্যিক গছ কি?",
            optionAEn = "Sal",
            optionBEn = "Hollong",
            optionCEn = "Banyan",
            optionDEn = "Teak",
            optionAAs = "শাল গছ",
            optionBAs = "হোলং গছ",
            optionCAs = "বৰগছ",
            optionDAs = "চেগুন গছ",
            correctOptionIndex = 1,
            explanationEn = "Hollong (Dipterocarpus retusus) is the official State Tree of Assam.",
            explanationAs = "হোলং হ'ল অসমৰ অধিসূচিত ৰাজ্যিক বৃক্ষ।",
            isBookmarked = false,
            isLiked = false,
            examCategory = "ADRE"
        ),
        QuestionEntity(
            subject = "Quantitative Aptitude",
            topic = "Percentages",
            difficulty = "Medium",
            questionEn = "If the price of tea in Assam increases by 20%, by what percentage should a household reduce tea consumption so as to keep expenditure unchanged?",
            questionAs = "যদি অসমত চাহৰ মূল্য ২০% বৃদ্ধি পায়, তেন্তে খৰচ অপৰিৱৰ্তিত ৰাখিবলৈ ব্যৱহাৰ কিমান শতাংশ হ্ৰাস কৰিব লাগিব?",
            optionAEn = "15%",
            optionBEn = "16.67%",
            optionCEn = "20%",
            optionDEn = "25%",
            optionAAs = "১৫%",
            optionBAs = "১৬.৬৭%",
            optionCAs = "২০%",
            optionDAs = "২৫%",
            correctOptionIndex = 1,
            explanationEn = "Reduction formula: [R / (100 + R)] * 100 = [20 / 120] * 100 = 16.67%.",
            explanationAs = "হ্ৰাসৰ সূত্ৰ: [২০ / (১০০ + ২০)] × ১০০ = ১৬.৬৭%",
            isBookmarked = true,
            isLiked = false,
            examCategory = "ADRE"
        ),
        QuestionEntity(
            subject = "Current Affairs",
            topic = "Assam Governance",
            difficulty = "Medium",
            questionEn = "Which major infrastructure bridge over the Brahmaputra connects Dibrugarh with Dhemaji?",
            questionAs = "ব্ৰহ্মপুত্ৰ নদীৰ ওপৰত নির্মিত ডিব্ৰুগড় আৰু ধেমাজিক সংযোগ কৰা বিখ্যাত দলংখনৰ নাম কি?",
            optionAEn = "Bhupen Hazarika Setu (Dhola-Sadiya)",
            optionBEn = "Bogibeel Bridge",
            optionCEn = "Saraighat Bridge",
            optionDEn = "Naranarayana Setu",
            optionAAs = "ভূপেন হাজৰিকা সেতু (ঢলা-শদিয়া)",
            optionBAs = "বগীবিল দলং",
            optionCAs = "শৰাইঘাট দলং",
            optionDAs = "নৰনাৰায়ণ সেতু",
            correctOptionIndex = 1,
            explanationEn = "Bogibeel Bridge is India's longest rail-cum-road bridge connecting Dibrugarh and Dhemaji.",
            explanationAs = "বগীবিল দলং হ'ল ভাৰতৰ দীঘল ৰেল তথা পথ দলং যিয়ে ডিব্ৰুগড় আৰু ধেমাজিক সংযোগ কৰে।",
            isBookmarked = false,
            isLiked = true,
            examCategory = "Assam Police"
        ),
        QuestionEntity(
            subject = "General English",
            topic = "Synonyms & Antonyms",
            difficulty = "Medium",
            questionEn = "Choose the correct synonym for the word 'CANDID':",
            questionAs = "'CANDID' শব্দটোৰ সঠিক সমাৰ্থক শব্দ বাছনি কৰক:",
            optionAEn = "Secretive",
            optionBEn = "Frank",
            optionCEn = "Dishonest",
            optionDEn = "Arrogant",
            optionAAs = "গোপনীয়",
            optionBAs = "স্পষ্টভাষী / খোলা-মেলা (Frank)",
            optionCAs = "অসাধু",
            optionDAs = "অহংকাৰী",
            correctOptionIndex = 1,
            explanationEn = "'Candid' means truthful and straightforward; frank. 'Frank' is the exact synonym.",
            explanationAs = "'Candid' মানে স্পষ্টভাষী বা প্ৰত্যক্ষ। ইয়াৰ সমাৰ্থক শব্দ হ'ল 'Frank'।",
            isBookmarked = false,
            isLiked = false,
            examCategory = "ADRE"
        ),
        QuestionEntity(
            subject = "General English",
            topic = "Grammar & Tenses",
            difficulty = "Easy",
            questionEn = "Identify the correct sentence using Subject-Verb Agreement:",
            questionAs = "শুদ্ধ বাক্যো বাছনি কৰক:",
            optionAEn = "Each of the boys have completed their homework.",
            optionBEn = "Each of the boys has completed his homework.",
            optionCEn = "Each of the boys are completing homework.",
            optionDEn = "Each of the boys were completed homework.",
            optionAAs = "Each of the boys have completed their homework.",
            optionBAs = "Each of the boys has completed his homework.",
            optionCAs = "Each of the boys are completing homework.",
            optionDAs = "Each of the boys were completed homework.",
            correctOptionIndex = 1,
            explanationEn = "'Each' is a singular indefinite pronoun and takes a singular verb 'has' and singular pronoun 'his'.",
            explanationAs = "'Each' প্ৰত্যেক বুজোৱা একবচন শব্দ, সেয়ে একবচন ক্ৰিয়া 'has' ব্যৱহাৰ হয়।",
            isBookmarked = true,
            isLiked = false,
            examCategory = "ADRE"
        ),
        QuestionEntity(
            subject = "General English",
            topic = "Idioms & Phrases",
            difficulty = "Medium",
            questionEn = "What is the meaning of the idiom 'To spill the beans'?",
            questionAs = "'To spill the beans' ফ্ৰেছটোৰ অৰ্থ কি?",
            optionAEn = "To drop food items",
            optionBEn = "To reveal a secret prematurely",
            optionCEn = "To waste money",
            optionDEn = "To start a quarrel",
            optionAAs = "খাদ্য বস্তু পেলোৱা",
            optionBAs = "গোপন কথা আগতীয়াকৈ প্ৰকাশ কৰা",
            optionCAs = "টকা অপব্যয় কৰা",
            optionDAs = "কাজিয়া আৰম্ভ কৰা",
            correctOptionIndex = 1,
            explanationEn = "'Spill the beans' means to reveal secret information unintentionally or prematurely.",
            explanationAs = "'Spill the beans' ইডিয়মৰ অৰ্থ গোপন ৰহস্য ফাদিল কৰা।",
            isBookmarked = false,
            isLiked = true,
            examCategory = "Assam Police"
        ),
        QuestionEntity(
            subject = "General Mathematics",
            topic = "Ratio & Proportion",
            difficulty = "Medium",
            questionEn = "The ratio of two numbers is 3:5. If 9 is subtracted from each, the ratio becomes 12:23. What is the smaller number?",
            questionAs = "দুটা সংখ্যাৰ অনুপাত ৩:৫। যদি প্ৰত্যেকৰ পৰা ৯ বিয়োগ কৰা হয়, অনুপাত ১২:২৩ হয়। সৰু সংখ্যাটো কিমান?",
            optionAEn = "27",
            optionBEn = "33",
            optionCEn = "36",
            optionDEn = "45",
            optionAAs = "২৭",
            optionBAs = "৩৩",
            optionCAs = "৩৬",
            optionDAs = "৪৫",
            correctOptionIndex = 1,
            explanationEn = "Let numbers be 3x and 5x. (3x - 9)/(5x - 9) = 12/23. Solving gives x = 11. Smaller number = 3 * 11 = 33.",
            explanationAs = "সংখ্যা দুটা ৩x আৰু ৫x ধৰিলে (৩x - ৯)/(৫x - ৯) = ১২/২৩ সমীকৰণৰ পৰা x = ১১ পোৱা যায়। সৰু সংখ্যাটো ৩৩।",
            isBookmarked = false,
            isLiked = false,
            examCategory = "ADRE"
        ),
        QuestionEntity(
            subject = "General Mathematics",
            topic = "Profit & Loss",
            difficulty = "Easy",
            questionEn = "A shopkeeper buys an article for ₹800 and sells it for ₹1000. What is his profit percentage?",
            questionAs = "এজন দোকানদাৰে ₹৮০০ টকাত ক্ৰয় কৰা বস্তু এটা ₹১০০০ টকাত বিক্ৰী কৰিলে। তেওঁৰ লাভৰ শতাংশ কিমান?",
            optionAEn = "20%",
            optionBEn = "25%",
            optionCEn = "15%",
            optionDEn = "30%",
            optionAAs = "২০%",
            optionBAs = "২৫%",
            optionCAs = "১৫%",
            optionDAs = "৩০%",
            correctOptionIndex = 1,
            explanationEn = "Profit = 1000 - 800 = ₹200. Profit % = (200 / 800) * 100 = 25%.",
            explanationAs = "লাভ = ১০০০ - ৮০০ = ২০০ টকা। লাভৰ শতাংশ = (২০০ / ৮০০) × ১০০ = ২৫%",
            isBookmarked = true,
            isLiked = true,
            examCategory = "ADRE"
        ),
        QuestionEntity(
            subject = "Reasoning",
            topic = "Coding-Decoding",
            difficulty = "Easy",
            questionEn = "If ASSAM is coded as 1-19-19-1-13 in a certain language, how will JUKTI be coded?",
            questionAs = "যদি কিবা কোডত ASSAM ক 1-19-19-1-13 বুলি লিখা হয়, তেন্তে JUKTI ক কেনেদৰে লিখা হ'ব?",
            optionAEn = "10-21-11-20-9",
            optionBEn = "10-20-11-20-9",
            optionCEn = "11-21-10-19-8",
            optionDEn = "9-20-10-19-8",
            optionAAs = "10-21-11-20-9",
            optionBAs = "10-20-11-20-9",
            optionCAs = "11-21-10-19-8",
            optionDAs = "9-20-10-19-8",
            correctOptionIndex = 0,
            explanationEn = "Letters are coded as per alphabetical order numbers: J=10, U=21, K=11, T=20, I=9.",
            explanationAs = "বৰ্ণমালাৰ স্থান অনুসৰি: J=10, U=21, K=11, T=20, I=9।",
            isBookmarked = false,
            isLiked = true,
            examCategory = "ADRE"
        ),
        QuestionEntity(
            subject = "Reasoning",
            topic = "Number Series",
            difficulty = "Medium",
            questionEn = "Find the missing number in the series: 4, 9, 19, 39, 79, ?",
            questionAs = "শ্ৰেণীটোৰ খালী স্থান পূৰ কৰক: 4, 9, 19, 39, 79, ?",
            optionAEn = "119",
            optionBEn = "139",
            optionCEn = "159",
            optionDEn = "169",
            optionAAs = "১১৯",
            optionBAs = "১৩৯",
            optionCAs = "১৫৯",
            optionDAs = "১৬৯",
            correctOptionIndex = 2,
            explanationEn = "Pattern: (x * 2) + 1. So 79 * 2 + 1 = 159.",
            explanationAs = "প্ৰতিটো পদ (পূৰ্বৱৰ্তী পদ × ২) + ১। গতিকে ৭৯ × ২ + ১ = ১৫৯।",
            isBookmarked = true,
            isLiked = true,
            examCategory = "APSC"
        ),
        QuestionEntity(
            subject = "Reasoning",
            topic = "Blood Relations",
            difficulty = "Medium",
            questionEn = "Pointing to a photograph, Rahul said, 'She is the daughter of my grandfather's only son.' How is the woman related to Rahul?",
            questionAs = "ছবি এখনলৈ আঙুলিয়াই ৰাহুলে ক'লে, 'তেওঁ মোৰ ককাদেউতাৰ একমাত্ৰ ল'ৰাৰ জীয়ৰী।' ৰাহুলৰ সৈতে মহিলাগৰাকীৰ সম্পৰ্ক কি?",
            optionAEn = "Mother",
            optionBEn = "Sister",
            optionCEn = "Aunt",
            optionDEn = "Cousin",
            optionAAs = "মাতৃ",
            optionBAs = "ভনী / বাইদেউ (Sister)",
            optionCAs = "পেহী / পেহীদেউ",
            optionDAs = "সম্পৰ্কীয় ভনী",
            correctOptionIndex = 1,
            explanationEn = "Grandfather's only son = Rahul's Father. Father's daughter = Rahul's sister.",
            explanationAs = "ককাদেউতাৰ একমাত্ৰ পুত্ৰ হ'ল ৰাহুলৰ দেউতাক। দেউতাকৰ জীয়েক হ'ল ৰাহুলৰ ভনী/বাইদেউ।",
            isBookmarked = false,
            isLiked = false,
            examCategory = "ADRE"
        )
    )

    val sampleMockTests = listOf(
        MockTestEntity(
            titleEn = "ADRE 3.0 Full Length Grand Mock - Paper I",
            titleAs = "এডিআৰই ৩.০ সম্পূৰ্ণ গ্ৰ্যান্ড মক টেষ্ট - প্ৰথম পাত্ৰ",
            category = "ADRE",
            testType = "Full-Length",
            subjectOrChapter = "Full Syllabus (Grade 3 & 4)",
            durationMinutes = 90,
            totalQuestions = 100,
            totalMarks = 150,
            negativeMarking = "-0.25 Marks",
            difficulty = "Medium",
            isPremium = false,
            isCompleted = true,
            userScore = 124,
            userAccuracy = 88.5f,
            userRank = 14,
            userPercentile = 96.8f
        ),
        MockTestEntity(
            titleEn = "APSC CCE Prelims GS Paper 1 Full Mock",
            titleAs = "এপিএছচি প্ৰাৰম্ভিক পৰীক্ষা জিএছ পেপাৰ ১ সম্পূৰ্ণ মক",
            category = "APSC",
            testType = "Full-Length",
            subjectOrChapter = "General Studies & Current Affairs",
            durationMinutes = 120,
            totalQuestions = 100,
            totalMarks = 200,
            negativeMarking = "-0.33 Marks",
            difficulty = "Hard",
            isPremium = true,
            inProgress = true,
            questionsAnswered = 34,
            timeRemainingSeconds = 3120,
            isCompleted = false
        ),
        MockTestEntity(
            titleEn = "Assam History Special Subject Mock",
            titleAs = "অসমৰ ইতিহাস বিশেষ বিষয় মক",
            category = "ADRE",
            testType = "Subject-wise",
            subjectOrChapter = "Assam History & Heritage",
            durationMinutes = 45,
            totalQuestions = 50,
            totalMarks = 50,
            negativeMarking = "-0.25 Marks",
            difficulty = "Medium",
            isPremium = false,
            isCompleted = false
        ),
        MockTestEntity(
            titleEn = "Ahom Dynasty & Kingdom Chapter Test",
            titleAs = "আহোম ৰাজবংশ আৰু ৰাজ্য অধ্যায় টেষ্ট",
            category = "ADRE",
            testType = "Chapter-wise",
            subjectOrChapter = "Ahom Kingdom & Battle of Saraighat",
            durationMinutes = 20,
            totalQuestions = 25,
            totalMarks = 25,
            negativeMarking = "Nil",
            difficulty = "Easy",
            isPremium = false,
            isCompleted = true,
            userScore = 23,
            userAccuracy = 92.0f,
            userRank = 5,
            userPercentile = 99.1f
        ),
        MockTestEntity(
            titleEn = "Assam Police SI Special Practice Test",
            titleAs = "অসম আৰক্ষী এছ আই বিশেষ অনুশীলন পৰীক্ষা",
            category = "Assam Police",
            testType = "Full-Length",
            subjectOrChapter = "General Knowledge & Reasoning",
            durationMinutes = 60,
            totalQuestions = 50,
            totalMarks = 100,
            negativeMarking = "-0.25 Marks",
            difficulty = "Medium",
            isPremium = false,
            isCompleted = false
        ),
        MockTestEntity(
            titleEn = "Logical Reasoning & Analytical Ability Subject Test",
            titleAs = "যুক্তিবিজ্ঞান আৰু বিশ্লেষণাত্মক দক্ষতা বিষয় টেষ্ট",
            category = "ADRE",
            testType = "Subject-wise",
            subjectOrChapter = "General Intelligence & Mental Ability",
            durationMinutes = 40,
            totalQuestions = 40,
            totalMarks = 40,
            negativeMarking = "-0.25 Marks",
            difficulty = "Hard",
            isPremium = true,
            isCompleted = false
        ),
        MockTestEntity(
            titleEn = "Assam Geography & Wildlife Sanctuaries Chapter Test",
            titleAs = "অসমৰ ভূগোল আৰু অভয়াৰণ্য অধ্যায় টেষ্ট",
            category = "TET",
            testType = "Chapter-wise",
            subjectOrChapter = "Rivers, Forests & National Parks of Assam",
            durationMinutes = 25,
            totalQuestions = 30,
            totalMarks = 30,
            negativeMarking = "Nil",
            difficulty = "Easy",
            isPremium = false,
            isCompleted = true,
            userScore = 28,
            userAccuracy = 93.3f,
            userRank = 3,
            userPercentile = 99.5f
        ),
        MockTestEntity(
            titleEn = "Assam History Previous Year PYQ Paper (2022-2025)",
            titleAs = "অসমৰ ইতিহাস বিগত বছৰৰ প্ৰশ্নকাকত",
            category = "PYQ",
            testType = "Subject-wise",
            subjectOrChapter = "Past Year Solved Papers",
            durationMinutes = 45,
            totalQuestions = 40,
            totalMarks = 80,
            negativeMarking = "-0.25 Marks",
            difficulty = "Medium",
            isPremium = false,
            isCompleted = true,
            userScore = 72,
            userAccuracy = 90.0f,
            userRank = 8,
            userPercentile = 98.4f
        )
    )

    val sampleStudyNotes = listOf(
        StudyNoteEntity(
            subject = "Assam History",
            topic = "Ahom Kingdom & Administration",
            titleEn = "Complete History of the Ahom Dynasty (1228-1826)",
            titleAs = "আহোম ৰাজবংশৰ সম্পূৰ্ণ ইতিহাস আৰু প্ৰশাসন",
            contentEn = """
                # Ahom Dynasty in Assam
                
                The Ahom kingdom was established in 1228 AD when Chaolung Sukaphaa led 9000 followers from Mong Mao across the Patkai hills.
                
                ## Key Monarchs & Achievements
                - **Sukaphaa (1228–1268)**: Founder of the Ahom kingdom in Charaideo.
                - **Suhingmung (1497–1539)**: Introduced the 'Swargadeo' title and expanded administration into Upper Assam.
                - **Pratap Singha**: Introduced the Paik System and created offices like Borpatragohain.
                - **Rudra Singha (1696–1714)**: Built Rang Ghar, Talatal Ghar and promoted trade & culture.
                
                ## The Paik System
                Every adult male citizen between 16 and 50 was a *Paik* obligated to render service to the state during peace and war. Four paiks formed a *Got*.
            """.trimIndent(),
            contentAs = """
                # অসমৰ আহোম ৰাজবংশ
                
                ১২২৮ খ্ৰীষ্টাব্দত চাওলুং চ্যুকাফাই পাটকাই পৰ্বত পাৰ হৈ আহোম ৰাজ্য প্ৰতিষ্ঠা কৰে।
                
                ## ঘাই ৰজাসকল আৰু গুৰুত্বপূৰ্ণ ঘটনা
                - **চাওলুং চ্যুকাফা (১২২৮-১২৬৮)**: আহোম ৰাজ্যৰ প্ৰতিষ্ঠাপক।
                - **চুহিঙমুং (১৪৯৭-১৫৩৯)**: 'স্বৰ্গদেউ' উপাধি ব্যৱহাৰ আৰু সম্প্ৰসাৰণ।
                - **প্ৰতাপ সিংহ**: পাইক প্ৰথা আৰু বৰপাত্ৰগোঁহাই পদৰ সৃষ্টি।
                - **ৰুদ্ৰ সিংহ (১৬৯৬-১৭১৪)**: ৰংঘৰ, তলাতল ঘৰ নির্মাণ।
            """.trimIndent(),
            isBookmarked = true,
            isDownloaded = true,
            readTimeMinutes = 8,
            isPremium = false
        ),
        StudyNoteEntity(
            subject = "Assam Geography",
            topic = "Brahmaputra Valley & Physical Features",
            titleEn = "Geography of Assam: Rivers, National Parks & Hills",
            titleAs = "অসমৰ ভূগোল: ব্ৰহ্মপুত্ৰ উপত্যকা আৰু ৰাষ্ট্ৰীয় উদ্যানসমূহ",
            contentEn = """
                # Physical Geography of Assam
                
                Assam is situated in Northeast India, surrounded by 6 North-Eastern states and Bhutan & Bangladesh.
                
                ## Major Rivers
                - **Brahmaputra**: Flows 916 km through Assam from East to West.
                - **Barak River**: Drains the Cachar and Barak Valley.
                
                ## 7 National Parks of Assam
                1. Kaziranga (Rhinos)
                2. Manas (Tiger Reserve & Golden Langur)
                3. Nameri (White-winged Wood Duck)
                4. Dibru-Saikhowa (Wild Feral Horses)
                5. Orang (Mini Kaziranga)
                6. Raimona (Golden Langur)
                7. Dehing Patkai (Rainforest)
            """.trimIndent(),
            contentAs = """
                # অসমৰ ভূগোল
                
                অসম উত্তৰ-পূব ভাৰতৰ এক গুৰুত্বপূৰ্ণ ৰাজ্য।
                
                ## ৭ খন ৰাষ্ট্ৰীয় উদ্যান
                ১. কাজিৰঙা
                ২. মানস
                ৩. নামেৰি
                ৪. ডিব্ৰু-ছৈখোৱা
                ৫. ওৰাং
                ৬. ৰায়মোনা
                ৭. দিহিং পাটকাই
            """.trimIndent(),
            isBookmarked = true,
            isDownloaded = false,
            readTimeMinutes = 6,
            isPremium = false
        ),
        StudyNoteEntity(
            subject = "Current Affairs",
            topic = "Assam & National Round-Up 2026",
            titleEn = "Assam & National Current Affairs Capsule (July 2026)",
            titleAs = "অসম আৰু ৰাষ্ট্ৰীয় কাৰেণ্ট এফেয়াৰ্ছ সমাহাৰ (জুলাই ২০২৬)",
            contentEn = """
                # Assam & National Current Affairs 2026

                ## Major Assam Highlights
                - **Kaziranga Elevated Corridor Project**: Construction accelerated on the 35km long elevated corridor through Kaziranga to protect wildlife during seasonal floods.
                - **Guwahati Riverfront Development**: New riverfront park & ropeway terminal inaugurated on Brahmaputra riverbank.
                - **Assam Skill University**: Campus near Mangaldai inaugurated for high-tech vocational training and robotics.
                - **Khel Maharan 2.0**: Grassroots sports tournament organized across 126 assembly constituencies of Assam with over 25 lakh youth participants.

                ## National & International Key Notes
                - **Chandrayaan-4 Mission Preparation**: ISRO reveals landing zone preparations for lunar sample return mission.
                - **Brahmaputra Biodiversity Survey**: New species survey launched along Upper Assam riverine ecosystems.
                - **National Education Policy 2020 Implementation**: Assam becomes one of the leading states to fully integrate NEP structural frameworks in higher education.
            """.trimIndent(),
            contentAs = """
                # অসম আৰু ৰাষ্ট্ৰীয় কাৰেণ্ট এফেয়াৰ্ছ ২০২৬

                ## অসমৰ প্ৰধান মুখ্য বাতৰি
                - **কাজিৰঙা এলিভেটেড কৰিডৰ**: বানপানীৰ সময়ত বন্যপ্ৰাণী সুৰক্ষিত কৰাৰ বাবে ৩৫ কিমি দৈৰ্ঘ্যৰ উৰণীয়া সেতু নিৰ্মাণৰ কাম ক্ষিপ্ৰতৰ।
                - **গুৱাহাটী ৰিভাৰফ্ৰন্ট উন্নয়ন**: ব্ৰহ্মপুত্ৰৰ পাৰত নতুন উদ্যান আৰু ৰ'পৱে' প্ৰকল্প ৰাইজৰ বাবে উন্মোচন।
                - **অসম স্কিল ইউনিভাৰ্চিটি**: মঙলদৈত আধুনিক কাৰিকৰী শিক্ষা আৰু ৰবটিক্স বিশ্ববিদ্যালয়ৰ প্ৰথম পৰ্যায় মুকলি।
                - **খেল মহাৰণ ২.০**: অসমৰ ১২৬ টা সমষ্টিত ২৫ লাখতকৈ অধিক যুৱক-যুৱতীৰ অংশগ্ৰহণেৰে ক্ৰীড়া মহোৎসৱ।

                ## ৰাষ্ট্ৰীয় আৰু আন্তঃৰাষ্ট্ৰীয় বিষয়
                - **চন্দ্ৰযান-৪ অভিযান**: মহাকাশ গৱেষণা সংস্থা ইছৰোৰ নতুন লেণ্ডাৰ আৰু নমুনা সংগ্ৰহ প্ৰস্তুতি।
                - **জাতীয় শিক্ষা নীতি ২০২০**: অসমত উচ্চ শিক্ষা খণ্ডত এনইপিৰ পূৰ্ণ বিকাশ।
            """.trimIndent(),
            isBookmarked = true,
            isDownloaded = true,
            readTimeMinutes = 5,
            isPremium = false
        ),
        StudyNoteEntity(
            subject = "Current Affairs",
            topic = "Schemes, Governance & Awards",
            titleEn = "Assam Government Schemes & Important Awards (2026)",
            titleAs = "অসম চৰকাৰৰ আঁচনি আৰু গুৰুত্বপূৰ্ণ বঁটাসমূহ (২০২৬)",
            contentEn = """
                # Assam Schemes & Recognitions 2026

                ## Government Schemes
                - **Orunodoi 3.0**: Financial assistance expanded to include beneficiary families with direct benefit transfer (DBT).
                - **Mukhya Mantri Atmanirbhar Asom Asani**: Financial aid provided to young entrepreneurs for self-employment ventures.
                - **Pragyan Bharti Scheme**: Free textbooks and scooty distribution for meritorious students scoring first division in Class 12 exams.

                ## Assam Civilian Awards
                - **Assam Baibhav**: Highest civilian award of Assam conferred for extraordinary service to humanity and state infrastructure.
                - **Assam Saurav & Assam Gaurav**: Conferred upon eminent litterateurs, sports icons, and social workers.
            """.trimIndent(),
            contentAs = """
                # অসমৰ চৰকাৰী আঁচনি আৰু সন্মান ২০২৬

                ## প্ৰধান চৰকাৰী আঁচনিসমূহ
                - **অৰুণোদই ৩.০**: হিতাধিকাৰী পৰিয়াললৈ প্ৰত্যক্ষ বেংক হস্তান্তৰ (DBT) যোগে অৰ্থনৈতিক সাহাৰ্য বৃদ্ধি।
                - **মুখ্যমন্ত্ৰী আত্মনিৰ্ভৰ অসম আসনি**: আত্মসংস্থানৰ বাবে যুৱ উদ্যমীসকললৈ বিত্তীয় অনুদান।
                - **প্ৰজ্ঞান ভাৰতী আঁচনি**: দ্বাদশ শ্ৰেণীৰ প্ৰথম বিভাগ প্ৰাপ্ত ছাত্ৰ-ছাত্ৰীলৈ বিনামূলীয়া পাঠ্যপুথি আৰু স্কুটি বিতৰণ।

                ## অসম অসামৰিক সন্মান
                - **অসম বৈভৱ**: ৰাজ্যৰ সৰ্বোচ্চ অসামৰিক সন্মান।
                - **অসম সৌৰভ আৰু অসম গৌৰৱ**: বিশিষ্ট সাহিত্যিক, ক্ৰীড়াবিদ আৰু সমাজকৰ্মীসকললৈ প্ৰদান।
            """.trimIndent(),
            isBookmarked = false,
            isDownloaded = true,
            readTimeMinutes = 4,
            isPremium = false
        )
    )

    val sampleExamUpdates = listOf(
        ExamUpdateEntity(
            examName = "ADRE Grade 3 & 4 (2026)",
            category = "Notification",
            titleEn = "State Level Recruitment Commission (SLRC) ADRE Notice",
            titleAs = "ৰাজ্যিক পৰ্যায়ৰ নিযুক্তি আয়োগ এডিআৰই নতুন জাননী",
            updateDate = "30 July 2026",
            detailEn = "Syllabus, exam date guidelines, and vacancy breakdown published for Grade 3 & 4 positions under Assam Government.",
            detailAs = "অসম চৰকাৰৰ তৃতীয় আৰু চতুৰ্থ শ্ৰেণীৰ নতুন পদৰ পাঠ্যক্ৰম আৰু পৰীক্ষাৰ দিন ঘোষণা।",
            officialLink = "https://slrc.assam.gov.in",
            isImportantNotice = true
        ),
        ExamUpdateEntity(
            examName = "APSC CCE 2026",
            category = "Syllabus",
            titleEn = "APSC CCE Prelims & Mains Exam Pattern Updated",
            titleAs = "এপিএছচি সন্মিলিত প্ৰতিযোগিতামূলক পৰীক্ষাৰ পাঠ্যক্ৰম",
            updateDate = "28 July 2026",
            detailEn = "Includes compulsory Assam history & Assam geography paper marks weightage.",
            detailAs = "অসম বিষয়ক বিষয়সমূহৰ বাবে বিশেষ নম্বৰ নিৰ্ধাৰণ।",
            officialLink = "https://apsc.nic.in",
            isImportantNotice = false
        )
    )

    val sampleBanners = listOf(
        BannerEntity(
            titleEn = "Jukti Pass Pro Unlimited",
            titleAs = "যুক্তি পাছ প্ৰ' আনলিমিটেড",
            subtitleEn = "Unlock 500+ ADRE & APSC Mocks, Premium Study Notes & No Ads",
            subtitleAs = "সকলো মক টেষ্ট, প্ৰিমিয়াম নোটছ আৰু বিজ্ঞাপন বিহীন অভিজ্ঞতা পাওক",
            type = "PROMOTIONAL",
            badgeText = "PREMIUM 50% OFF"
        ),
        BannerEntity(
            titleEn = "Daily Live Quiz Competition",
            titleAs = "দৈনিক লাইভ কুইজ প্ৰতিযোগিতা",
            subtitleEn = "Earn +100 XP daily and top Assam State Leaderboard!",
            subtitleAs = "দৈনিক ১০০ এক্সপি উপাৰ্জন কৰক আৰু লিডাৰব'ৰ্ডৰ শীৰ্ষত থাকক!",
            type = "INFORMATION",
            badgeText = "LIVE NOW"
        )
    )

    val sampleNotifications = listOf(
        NotificationEntity(
            title = "New ADRE Mock Test Released!",
            body = "Test your preparation with ADRE 3.0 Full Length Mock Paper I.",
            timestamp = "10 mins ago",
            category = "Mock Test"
        ),
        NotificationEntity(
            title = "Daily Assam Current Affairs Updated",
            body = "Read Assam & National Daily Capsules for 30 July 2026.",
            timestamp = "2 hours ago",
            category = "Current Affairs"
        )
    )

    val sampleExams = listOf(
        ExamEntity(title = "ADRE Grade 3 & 4", subtitle = "Assam Direct Recruitment Examination", status = "Active"),
        ExamEntity(title = "APSC CCE Prelims", subtitle = "Assam Public Service Commission", status = "Active"),
        ExamEntity(title = "Assam Police SI / Constable", subtitle = "State Level Recruitment Board (SLRB)", status = "Active"),
        ExamEntity(title = "Assam Special TET", subtitle = "Elementary & Secondary Education", status = "Upcoming")
    )

    val sampleSubjectsChapters = listOf(
        SubjectChapterEntity(subject = "General Knowledge", chapter = "History"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Polity & Constitution"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Geography"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Economy"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Science & Technology"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Environment & Ecology"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Art & Culture"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Government Schemes"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Organizations"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Awards & Honors"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Books & Authors"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Important Days"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Sports"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Current Affairs"),
        SubjectChapterEntity(subject = "General Knowledge", chapter = "Static GK"),

        SubjectChapterEntity(subject = "General Mathematics", chapter = "Number System"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Simplification"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "HCF & LCM"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Decimal & Fractions"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Percentage"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Profit & Loss"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Discount"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Simple Interest"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Compound Interest"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Ratio & Proportion"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Partnership"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Average"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Age Problems"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Time & Work"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Pipes & Cisterns"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Time, Speed & Distance"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Boats & Streams"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Train Problems"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Mensuration"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Geometry (Basic)"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Algebra (Basic)"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Data Interpretation"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Permutation & Combination"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Probability (Basic)"),

        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Analogy"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Classification"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Series (Number, Alphabet)"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Coding-Decoding"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Blood Relations"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Direction Sense"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Ranking & Order"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Seating Arrangement"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Syllogism"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Statement & Conclusion"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Statement & Assumption"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Cause & Effect"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Venn Diagrams"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Calendar"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Clock"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Mirror Image"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Water Image"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Paper Folding & Cutting"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Embedded Figures"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Non-Verbal Reasoning"),

        SubjectChapterEntity(subject = "General English", chapter = "Reading Comprehension"),
        SubjectChapterEntity(subject = "General English", chapter = "Vocabulary"),
        SubjectChapterEntity(subject = "General English", chapter = "Synonyms & Antonyms"),
        SubjectChapterEntity(subject = "General English", chapter = "One-Word & Idioms"),
        SubjectChapterEntity(subject = "General English", chapter = "Phrasal Verbs"),
        SubjectChapterEntity(subject = "General English", chapter = "Spotting Errors"),
        SubjectChapterEntity(subject = "General English", chapter = "Sentence Improvement"),
        SubjectChapterEntity(subject = "General English", chapter = "Fill in the Blanks"),
        SubjectChapterEntity(subject = "General English", chapter = "Cloze Test"),
        SubjectChapterEntity(subject = "General English", chapter = "Para Jumbles"),
        SubjectChapterEntity(subject = "General English", chapter = "Active & Passive Voice"),
        SubjectChapterEntity(subject = "General English", chapter = "Direct & Indirect Speech"),
        SubjectChapterEntity(subject = "General English", chapter = "Articles"),
        SubjectChapterEntity(subject = "General English", chapter = "Prepositions"),
        SubjectChapterEntity(subject = "General English", chapter = "Conjunctions"),
        SubjectChapterEntity(subject = "General English", chapter = "Tenses"),
        SubjectChapterEntity(subject = "General English", chapter = "Sub–Verb Agreement"),
        SubjectChapterEntity(subject = "General English", chapter = "Narration"),
        SubjectChapterEntity(subject = "General English", chapter = "Sentence Correction")
    )


    val initialUserProfile = UserProfileEntity(
        id = 1,
        name = "Assam Scholar",
        email = "scholar@jukti.in",
        mobile = "+91 98765 43210",
        district = "Kamrup Metropolitan",
        examGoal = "ADRE Grade 3 & APSC CCE",
        xp = 1450,
        level = 7,
        dailyStreak = 7,
        totalSolved = 186,
        correctCount = 152,
        totalTimeMinutes = 410,
        isPremium = false,
        role = "USER", // Standard user by default. Login as juktieducation@gmail.com unlocks Owner access.
        firebaseProjectId = "jukti-26035",
        joinedDate = "Jul 2026",
        isLoggedIn = false,
        currentDeviceId = "",
        activeDeviceId = ""
    )

    val initialAboutConfig = AboutConfigEntity(
        id = 1,
        appTitle = "Jukti (যুক্তি)",
        appSubtitleEn = "Test Your Knowledge",
        appSubtitleAs = "অসমৰ সৰ্ববৃহৎ পৰীক্ষা প্ৰস্তুতি এপ্প",
        versionText = "Version 2026.1.0",
        missionEn = "Jukti is engineered to democratize competitive exam preparation for aspirants across Assam. We provide comprehensive practice modules, high-yield Assam history and current affairs notes, full-length timed mock tests, and real-time state ranking analytics.",
        missionAs = "যুক্তি এপ্পৰ প্ৰধান উদ্দেশ্য হৈছে অসমৰ সকলো প্ৰতিযোগীতামূলক পৰীক্ষাৰ (APSC, ADRE 2.0, Assam Police, SLRC, TET) প্ৰাৰ্থীসকলক উচ্চমানদণ্ডৰ মক টেষ্ট, বিগত বৰ্ষৰ প্ৰশ্ন আৰু অধ্যয়ন সমল সম্পূৰ্ণ বিনামূলীয়াকৈ তথা সহজ ভাষাত যোগান ধৰা।",
        logoIconName = "School",
        copyrightText = "Copyright © 2026 Jukti Education Portal. All rights reserved.",
        developerTagline = "Designed & Developed for Assam Aspirants",
        contactEmail = "support@jukti.in",
        contactPhone = "+91 98765 43210",
        contactTelegram = "t.me/JuktiAssam",
        contactWhatsapp = "Community Group",
        adminEmails = "",
        refundPolicyEn = "Our policy lasts 7 days. If 7 days have gone by since your purchase, unfortunately, we cannot offer you a refund. To be eligible for a refund, your request must be due to technical billing issues or double charge. Please contact support@jukti.in with your transaction details.",
        refundPolicyAs = "আমাৰ ৰিফাণ্ড পলিচি ক্ৰয় কৰাৰ ৭ দিনৰ বাবে প্ৰযোজ্য। ক্ৰয় কৰাৰ ৭ দিন অতিক্ৰম কৰিলে কোনো ৰিফাণ্ড প্ৰদান কৰা নহ'ব। কেৱল কাৰিকৰী অসুবিধা বা ভুলতে দুবাৰ পইচা কটা গ’লেহে আপুনি ৰিফাণ্ডৰ বাবে আবেদন কৰিব পাৰিব। সহায়ৰ বাবে support@jukti.in ত যোগাযোগ কৰক।"
    )

    val initialFaqs = listOf(
        FaqEntity(
            id = 1,
            questionEn = "How do I take full-length mock tests?",
            questionAs = "মক টেষ্টসমূহ কিদৰে দিয়া হয়?",
            answerEn = "Navigate to the Mock Tests tab, pick your exam (APSC, ADRE, Police), and click 'Start Test'. Timer and negative marking rules apply.",
            answerAs = "মক টেষ্ট মেনুলৈ গৈ যিকোনো পৰীক্ষা চয়ন কৰক। তাত নিৰ্ধাৰিত সময় আৰু নিগেティブ মাৰ্কিং ব্যৱস্থা থাকিব।"
        ),
        FaqEntity(
            id = 2,
            questionEn = "Can I study offline without internet?",
            questionAs = "অফলাইনত অধ্যয়ন কৰিব পাৰিমনে?",
            answerEn = "Yes! Loaded study notes, downloaded e-books, and saved offline practice sets can be accessed anytime without internet.",
            answerAs = "হয়, এবাৰ ডাউন্মলোড কৰা প্ৰশ্ন আৰু নোটছসমূহ অফলাইনত পঢ়িব পাৰিব।"
        ),
        FaqEntity(
            id = 3,
            questionEn = "How to report a wrong question answer?",
            questionAs = "ভুল প্ৰশ্ন বা উত্তৰ কেনেকৈ ৰিপোৰ্ট কৰিব?",
            answerEn = "Tap the 'Report Question' flag icon inside any MCQ screen. Our Assam subject experts verify and correct reports within 12 hours.",
            answerAs = "প্ৰশ্নটোৰ তলত থকা ফ্ল্যাগ/ৰিপোৰ্ট আইকনটো টিপি আমালৈ জনাওক, আমাৰ ছাবজেক্ট এক্সপাৰ্টসকলে ১২ ঘণ্টাৰ ভিতৰত সংশোধন কৰিব।"
        )
    )
}
