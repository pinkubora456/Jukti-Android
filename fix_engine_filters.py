import re

with open("app/src/main/java/com/example/data/util/PlanValidityEngine.kt", "r") as f:
    content = f.read()

# 1. filterAccessibleMockTests
old_mock_func = """    fun filterAccessibleMockTests(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        mockTests: List<MockTestEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<MockTestEntity> {
        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com")
        if (isUserAdmin) return mockTests

        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime)

        if (!effective.isPremium) {
            val freeMocks = mockTests.filter { !it.isPremium }
            val freePlan = plans.find { it.planName.equals("Free Plan", ignoreCase = true) || it.finalPrice == "0" || it.finalPrice == "₹0" }
            val freeFeatures = buildList {
                freePlan?.features?.let { addAll(it.split("|", ",")) }
                freePlan?.contents?.let { addAll(it.split("|", ",")) }
                effective.combinedBenefits.forEach { add(it) }
            }.map { it.trim() }.filter { it.isNotBlank() }
            val mockLimitFeature = freeFeatures.find { 
                it.startsWith("Mock Test", ignoreCase = true) || it.startsWith("Mock Tests", ignoreCase = true) || it.contains("Mock", ignoreCase = true)
            }
            val mockLimit = mockLimitFeature?.let { extractNumericalLimit(it) } ?: 1
            return freeMocks.take(mockLimit)
        }

        val candidateMocks = if (effective.hasAllExamsAccess || effective.combinedTargetExams.isEmpty()) {
            mockTests
        } else {
            mockTests.filter { mock ->
                !mock.isPremium || matchesExamTarget(mock.category, mock.titleEn, effective.combinedTargetExams.toList())
            }
        }

        val mockLimitFeature = effective.combinedBenefits.find { 
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
    }"""

new_mock_func = """    fun filterAccessibleMockTests(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        mockTests: List<MockTestEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<MockTestEntity> {
        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com")
        if (isUserAdmin) return mockTests

        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime)

        if (!effective.isPremium) {
            return mockTests.filter { !it.isPremium }
        }

        return if (effective.hasAllExamsAccess || effective.combinedTargetExams.isEmpty()) {
            mockTests
        } else {
            mockTests.filter { mock ->
                !mock.isPremium || matchesExamTarget(mock.category, mock.titleEn, effective.combinedTargetExams.toList())
            }
        }
    }"""


# 2. filterAccessibleStudyNotes
old_note_func = """    fun filterAccessibleStudyNotes(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        studyNotes: List<StudyNoteEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<StudyNoteEntity> {
        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com")
        if (isUserAdmin) return studyNotes

        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime)

        if (!effective.isPremium) {
            val freeNotes = studyNotes.filter { !it.isPremium }
            val freePlan = plans.find { it.planName.equals("Free Plan", ignoreCase = true) || it.finalPrice == "0" || it.finalPrice == "₹0" }
            val freeFeatures = buildList {
                freePlan?.features?.let { addAll(it.split("|", ",")) }
                freePlan?.contents?.let { addAll(it.split("|", ",")) }
                effective.combinedBenefits.forEach { add(it) }
            }.map { it.trim() }.filter { it.isNotBlank() }
            val notesLimitFeature = freeFeatures.find { 
                it.startsWith("Study Note", ignoreCase = true) || it.startsWith("Study Notes", ignoreCase = true) || it.contains("Notes", ignoreCase = true)
            }
            val notesLimit = notesLimitFeature?.let { extractNumericalLimit(it) } ?: 3
            val nonCaNotes = freeNotes.filter { !it.subject.contains("Current Affairs", ignoreCase = true) }
            val caNotes = freeNotes.filter { it.subject.contains("Current Affairs", ignoreCase = true) }
            
            val finalNonCa = nonCaNotes.take(notesLimit)
            
            val caLimitFeature = freeFeatures.find { 
                it.startsWith("Current Affair", ignoreCase = true) || it.startsWith("Current Affairs", ignoreCase = true) || it.contains("Current Affairs", ignoreCase = true)
            }
            val caLimit = caLimitFeature?.let { extractNumericalLimit(it) } ?: 2
            val finalCa = caNotes.take(caLimit)
            
            return finalNonCa + finalCa
        }

        val nonCaNotes = studyNotes.filter { !it.subject.contains("Current Affairs", ignoreCase = true) }
        val caNotes = studyNotes.filter { it.subject.contains("Current Affairs", ignoreCase = true) }

        val candidateNonCa = if (effective.hasAllExamsAccess || effective.combinedTargetExams.isEmpty()) {
            nonCaNotes
        } else {
            nonCaNotes.filter { note ->
                !note.isPremium || matchesExamTarget(note.subject, "${note.topic} ${note.titleEn}", effective.combinedTargetExams.toList())
            }
        }
        
        val candidateCa = if (effective.hasAllExamsAccess || effective.combinedTargetExams.isEmpty()) {
            caNotes
        } else {
            caNotes.filter { note ->
                !note.isPremium || matchesExamTarget(note.subject, "${note.topic} ${note.titleEn}", effective.combinedTargetExams.toList())
            }
        }

        val notesLimitFeature = effective.combinedBenefits.find { 
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
        
        val caLimitFeature = effective.combinedBenefits.find { 
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
    }"""

new_note_func = """    fun filterAccessibleStudyNotes(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        studyNotes: List<StudyNoteEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<StudyNoteEntity> {
        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com")
        if (isUserAdmin) return studyNotes

        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime)

        if (!effective.isPremium) {
            return studyNotes.filter { !it.isPremium }
        }

        return if (effective.hasAllExamsAccess || effective.combinedTargetExams.isEmpty()) {
            studyNotes
        } else {
            studyNotes.filter { note ->
                !note.isPremium || matchesExamTarget(note.subject, "${note.topic} ${note.titleEn}", effective.combinedTargetExams.toList())
            }
        }
    }"""

# 3. filterAccessibleQuestions
old_quest_func = """    fun filterAccessibleQuestions(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        questions: List<QuestionEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<QuestionEntity> {
        val validQuestions = questions.filter { !it.isReported }
        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com")
        if (isUserAdmin) return validQuestions

        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime)

        if (!effective.isPremium) {
            val freeQuestions = validQuestions.filter { !it.isPremium }
            val freePlan = plans.find { it.planName.equals("Free Plan", ignoreCase = true) || it.finalPrice == "0" || it.finalPrice == "₹0" }
            val freeFeatures = buildList {
                freePlan?.features?.let { addAll(it.split("|", ",")) }
                freePlan?.contents?.let { addAll(it.split("|", ",")) }
                effective.combinedBenefits.forEach { add(it) }
            }.map { it.trim() }.filter { it.isNotBlank() }
            val qLimitFeature = freeFeatures.find { 
                it.startsWith("Question", ignoreCase = true) || it.startsWith("Questions", ignoreCase = true) || it.contains("MCQ", ignoreCase = true)
            }
            val qLimit = qLimitFeature?.let { extractNumericalLimit(it) } ?: 30
            return freeQuestions.take(qLimit)
        }

        val candidateQuestions = if (effective.hasAllExamsAccess || effective.combinedTargetExams.isEmpty()) {
            validQuestions
        } else {
            validQuestions.filter { q ->
                !q.isPremium || matchesExamTarget(q.examCategory, "${q.subject} ${q.topic}", effective.combinedTargetExams.toList())
            }
        }

        val qLimitFeature = effective.combinedBenefits.find { 
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
    }"""

new_quest_func = """    fun filterAccessibleQuestions(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        questions: List<QuestionEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<QuestionEntity> {
        val validQuestions = questions.filter { !it.isReported }
        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com")
        if (isUserAdmin) return validQuestions

        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime)

        if (!effective.isPremium) {
            return validQuestions.filter { !it.isPremium }
        }

        return if (effective.hasAllExamsAccess || effective.combinedTargetExams.isEmpty()) {
            validQuestions
        } else {
            validQuestions.filter { q ->
                !q.isPremium || matchesExamTarget(q.examCategory, "${q.subject} ${q.topic}", effective.combinedTargetExams.toList())
            }
        }
    }"""


if old_mock_func in content:
    content = content.replace(old_mock_func, new_mock_func)
else:
    print("Could not find old_mock_func")

if old_note_func in content:
    content = content.replace(old_note_func, new_note_func)
else:
    print("Could not find old_note_func")
    
if old_quest_func in content:
    content = content.replace(old_quest_func, new_quest_func)
else:
    print("Could not find old_quest_func")

with open("app/src/main/java/com/example/data/util/PlanValidityEngine.kt", "w") as f:
    f.write(content)
