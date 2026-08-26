import re

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    content = f.read()

target = """    suspend fun refreshDataFromFirebase(): Result<String> {
        return try {
            syncManager.fetchAllExams()
            
            val questions = firebaseRepository.fetchAllQuestions()
            if (questions.isNotEmpty()) {
                questionDao.insertAll(questions)
            }

            val mocks = firebaseRepository.fetchAllMockTests()
            if (mocks.isNotEmpty()) {
                mockTestDao.insertAll(mocks)
            }

            val notes = firebaseRepository.fetchAllStudyNotes()
            if (notes.isNotEmpty()) {
                studyNoteDao.insertAll(notes)
            }"""

replacement = """    suspend fun refreshDataFromFirebase(currentTime: Long = System.currentTimeMillis()): Result<String> {
        return try {
            val userProfile = userProfileDao.getUserProfileDirect()
            val isAdminOrOwner = userProfile?.role == "ADMIN" || userProfile?.role == "OWNER" || userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com"
            val entitlements = entitlementDao.getEntitlementsDirect()
            val allPlans = planDao.getAllPlansDirect()
            val effectiveEntitlement = com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(entitlements, allPlans, currentTime)

            syncManager.fetchAllExams()
            
            val questions = firebaseRepository.fetchAllQuestions()
            val scrubbedQuestions = questions.map { q ->
                if (com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, effectiveEntitlement, isAdminOrOwner)) {
                    q
                } else {
                    q.copy(
                        questionEn = "Premium Content 🔒",
                        questionAs = "প্ৰিমিয়াম সমল 🔒",
                        optionAEn = "Unlock to view options", optionAAs = "",
                        optionBEn = "Unlock to view options", optionBAs = "",
                        optionCEn = "Unlock to view options", optionCAs = "",
                        optionDEn = "Unlock to view options", optionDAs = "",
                        correctOptionIndex = -1,
                        explanationEn = "This explanation is locked. Please upgrade to a Premium plan to view the full answer and explanation.",
                        explanationAs = "এই ব্যাখ্যাটো তলা লগোৱা আছে। সম্পূৰ্ণ উত্তৰ আৰু ব্যাখ্যা চাবলৈ অনুগ্ৰহ কৰি এটা প্ৰিমিয়াম প্লেনলৈ আপগ্ৰেড কৰক।"
                    )
                }
            }
            if (scrubbedQuestions.isNotEmpty()) {
                questionDao.insertAll(scrubbedQuestions)
            }

            val mocks = firebaseRepository.fetchAllMockTests()
            if (mocks.isNotEmpty()) {
                mockTestDao.insertAll(mocks)
            }

            val notes = firebaseRepository.fetchAllStudyNotes()
            val scrubbedNotes = notes.map { n ->
                if (com.example.data.util.PlanValidityEngine.isStudyNoteAccessible(n, effectiveEntitlement, isAdminOrOwner)) {
                    n
                } else {
                    n.copy(
                        contentEn = "This content is locked. Please upgrade to a Premium plan to view the full content.",
                        contentAs = "এই সমলটো তলা লগোৱা আছে। সম্পূৰ্ণ বিষয়বস্তু চাবলৈ অনুগ্ৰহ কৰি এটা প্ৰিমিয়াম প্লেনলৈ আপগ্ৰেড কৰক।"
                    )
                }
            }
            if (scrubbedNotes.isNotEmpty()) {
                studyNoteDao.insertAll(scrubbedNotes)
            }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
        f.write(content)
    print("Patched JuktiRepository.kt")
else:
    print("Could not find target in JuktiRepository.kt")
