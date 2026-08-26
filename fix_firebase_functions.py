import re

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "r") as f:
    text = f.read()

func_code = """
    suspend fun fetchPremiumQuestions(): List<QuestionEntity> {
        val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
        return try {
            val result = kotlinx.coroutines.tasks.await(functions.getHttpsCallable("getPremiumContent").call())
            val data = result.data as? Map<String, Any> ?: return emptyList()
            val qs = data["questions"] as? List<Map<String, Any>> ?: return emptyList()
            qs.mapNotNull { doc ->
                try {
                    val rawTopic = doc["topic"] as? String ?: ""
                    val rawSubject = doc["subject"] as? String ?: ""
                    QuestionEntity(
                        id = (doc["id"] as? Number)?.toLong() ?: 0L,
                        subject = normalizeSubjectName(rawSubject),
                        topic = normalizeChapterName(rawTopic),
                        difficulty = doc["difficulty"] as? String ?: "Medium",
                        questionEn = doc["questionEn"] as? String ?: "",
                        questionAs = doc["questionAs"] as? String ?: "",
                        optionAEn = doc["optionAEn"] as? String ?: "",
                        optionBEn = doc["optionBEn"] as? String ?: "",
                        optionCEn = doc["optionCEn"] as? String ?: "",
                        optionDEn = doc["optionDEn"] as? String ?: "",
                        optionAAs = doc["optionAAs"] as? String ?: "",
                        optionBAs = doc["optionBAs"] as? String ?: "",
                        optionCAs = doc["optionCAs"] as? String ?: "",
                        optionDAs = doc["optionDAs"] as? String ?: "",
                        correctOptionIndex = (doc["correctOptionIndex"] as? Number)?.toInt() ?: 0,
                        explanationEn = doc["explanationEn"] as? String ?: "",
                        explanationAs = doc["explanationAs"] as? String ?: "",
                        examCategory = doc["examCategory"] as? String ?: "ADRE",
                        isPremium = doc["isPremium"] as? Boolean ?: false,
                        accessType = doc["accessType"] as? String ?: (if (doc["isPremium"] as? Boolean == true) "PREMIUM" else "FREE"),
                        questionType = doc["questionType"] as? String ?: "Expected",
                        isReported = doc["isReported"] as? Boolean ?: false,
                        status = doc["status"] as? String ?: "ACTIVE",
                        cachedAt = System.currentTimeMillis()
                    )
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchPremiumMockTests(): List<MockTestEntity> {
        val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
        return try {
            val result = kotlinx.coroutines.tasks.await(functions.getHttpsCallable("getPremiumContent").call())
            val data = result.data as? Map<String, Any> ?: return emptyList()
            val ms = data["mockTests"] as? List<Map<String, Any>> ?: return emptyList()
            ms.mapNotNull { doc ->
                try {
                    MockTestEntity(
                        id = (doc["id"] as? Number)?.toLong() ?: 0L,
                        titleEn = doc["titleEn"] as? String ?: "",
                        titleAs = doc["titleAs"] as? String ?: "",
                        category = doc["category"] as? String ?: "ADRE",
                        durationMinutes = (doc["durationMinutes"] as? Number)?.toInt() ?: 0,
                        totalQuestions = (doc["totalQuestions"] as? Number)?.toInt() ?: 0,
                        totalMarks = (doc["totalMarks"] as? Number)?.toFloat() ?: 0f,
                        isScheduled = doc["isScheduled"] as? Boolean ?: false,
                        scheduledDate = doc["scheduledDate"] as? String ?: "",
                        isPublished = doc["isPublished"] as? Boolean ?: true,
                        testType = doc["testType"] as? String ?: "Full-Length",
                        subjectOrChapter = doc["subjectOrChapter"] as? String ?: "General Studies & Assam GK",
                        negativeMarking = doc["negativeMarking"] as? String ?: "0.25 Marks",
                        difficulty = doc["difficulty"] as? String ?: "Medium",
                        isPremium = doc["isPremium"] as? Boolean ?: false,
                        accessType = doc["accessType"] as? String ?: (if (doc["isPremium"] as? Boolean == true) "PREMIUM" else "FREE"),
                        createdAt = (doc["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchPremiumStudyNotes(): List<StudyNoteEntity> {
        val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
        return try {
            val result = kotlinx.coroutines.tasks.await(functions.getHttpsCallable("getPremiumContent").call())
            val data = result.data as? Map<String, Any> ?: return emptyList()
            val sn = data["studyNotes"] as? List<Map<String, Any>> ?: return emptyList()
            sn.mapNotNull { doc ->
                try {
                    StudyNoteEntity(
                        id = (doc["id"] as? Number)?.toLong() ?: 0L,
                        subject = doc["subject"] as? String ?: "",
                        topic = doc["topic"] as? String ?: "",
                        titleEn = doc["titleEn"] as? String ?: "",
                        titleAs = doc["titleAs"] as? String ?: "",
                        contentEn = doc["contentEn"] as? String ?: "",
                        contentAs = doc["contentAs"] as? String ?: "",
                        readTimeMinutes = (doc["readTimeMinutes"] as? Number)?.toInt() ?: 5,
                        isPremium = doc["isPremium"] as? Boolean ?: false,
                        accessType = doc["accessType"] as? String ?: (if (doc["isPremium"] as? Boolean == true) "PREMIUM" else "FREE")
                    )
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
"""

text = text.replace("    // Real-time Observers using addSnapshotListener", func_code + "\n\n    // Real-time Observers using addSnapshotListener")

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "w") as f:
    f.write(text)

