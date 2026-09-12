package com.example.data.util

import com.example.data.local.PyqFocusEntity
import com.example.data.local.UserQuestionStateEntity
import com.example.data.local.QuestionEntity

data class ChapterPerformance(
    val subject: String,
    val chapter: String,
    val totalAttempts: Int,
    val correctAttempts: Int,
    val accuracy: Float?, // percentage 0-100 if attempts >= 10, else null
    val pyqAvg: Float
)

data class PriorityTopicItem(
    val subject: String,
    val chapter: String,
    val priorityScore: Float,
    val priorityLabel: String, // "High Priority", "Medium Priority", "Maintain", "Low Priority"
    val reason: String,
    val pyqAvg: Float,
    val accuracy: Float?
)

data class StrengthWeaknessItem(
    val subject: String,
    val chapter: String,
    val statusLabel: String, // "Weak & Important", "Strong", "Needs Improvement", "Not Enough Data"
    val accuracy: Float?,
    val pyqAvg: Float,
    val isImportant: Boolean,
    val isWeak: Boolean
)

data class GuidanceData(
    val exam: String,
    val subject: String?,
    val pyqFocus: List<PyqFocusEntity>,
    val priorityTopics: List<PriorityTopicItem>,
    val strengthWeakness: List<StrengthWeaknessItem>,
    val preparationStrategy: String,
    val isAccessDenied: Boolean = false,
    val accessDeniedReason: String = ""
)

object GuidanceEngine {
    var minAttemptsRequired: Int = 10
    var weakAccuracyThreshold: Float = 60f
    var strongAccuracyThreshold: Float = 80f
    var highImportancePyqAvg: Float = 1.5f

    fun setThresholds(minAttempts: Int, weakThreshold: Float, strongThreshold: Float, highPyqAvg: Float) {
        minAttemptsRequired = minAttempts
        weakAccuracyThreshold = weakThreshold
        strongAccuracyThreshold = strongThreshold
        highImportancePyqAvg = highPyqAvg
    }
    
    fun calculateGuidance(
        exam: String,
        subject: String?, // "All Subjects" can be passed as null
        allPyqFocus: List<PyqFocusEntity>,
        allQuestions: List<QuestionEntity>,
        userStates: List<UserQuestionStateEntity>,
        allPrepStrategies: List<com.example.data.local.PrepStrategyEntity>,
        effectiveEntitlement: EffectiveUserEntitlement? = null,
        isAdminOrOwner: Boolean = false
    ): GuidanceData {
        // Enforce backend/logic restriction: verify user's plan permits Guidance for this exam
        if (!isAdminOrOwner) {
            if (effectiveEntitlement == null || !effectiveEntitlement.isPremium || !effectiveEntitlement.guidanceEnabled) {
                return GuidanceData(
                    exam = exam,
                    subject = subject,
                    pyqFocus = emptyList(),
                    priorityTopics = emptyList(),
                    strengthWeakness = emptyList(),
                    preparationStrategy = "",
                    isAccessDenied = true,
                    accessDeniedReason = "Guidance access is not included with your current plan."
                )
            }
            if (!PlanValidityEngine.isGuidanceAccessibleForExam(exam, effectiveEntitlement, isAdminOrOwner)) {
                return GuidanceData(
                    exam = exam,
                    subject = subject,
                    pyqFocus = emptyList(),
                    priorityTopics = emptyList(),
                    strengthWeakness = emptyList(),
                    preparationStrategy = "",
                    isAccessDenied = true,
                    accessDeniedReason = "Your current plan does not include Guidance access for '$exam'."
                )
            }
        }
        // Filter PYQ for exam and subject with flexible matching and Q-Bank fallback
        val matchedPyqs = allPyqFocus.filter { 
            it.exam.equals(exam, ignoreCase = true) || 
            it.exam.contains(exam, ignoreCase = true) || 
            exam.contains(it.exam, ignoreCase = true)
        }
        val examPyqs = if (matchedPyqs.isNotEmpty()) {
            matchedPyqs
        } else {
            // Fallback: derive from allQuestions tagged as PYQ
            val pyqs = allQuestions.filter { 
                (it.examCategory.contains(exam, ignoreCase = true) || exam.contains(it.examCategory, ignoreCase = true) || it.examCategory.isEmpty()) &&
                it.questionType.equals("PYQ", ignoreCase = true)
            }
            if (pyqs.isNotEmpty()) {
                pyqs.groupBy { Pair(it.subject, it.topic) }.map { (pair, qList) ->
                    PyqFocusEntity(
                        exam = exam,
                        subject = pair.first,
                        chapter = pair.second,
                        pyqCount = qList.size,
                        examsCovered = 1
                    )
                }
            } else {
                emptyList()
            }
        }

        val filteredPyqs = if (subject != null && subject != "All Subjects") {
            examPyqs.filter { it.subject.equals(subject, ignoreCase = true) }
        } else {
            examPyqs
        }
        
        // Map questions to chapters
        // questionId -> QuestionEntity
        val questionMap = allQuestions.associateBy { it.id.toString() }
        
        // Aggregate user performance by Subject + Chapter
        val performanceMap = mutableMapOf<Pair<String, String>, Pair<Int, Int>>() // Pair(Subject, Chapter) -> Pair(TotalAttempts, CorrectAttempts)
        
        for (state in userStates) {
            val q = questionMap[state.questionId]
            if (q != null && (q.examCategory.contains(exam, ignoreCase = true) || q.examCategory.isEmpty())) { 
                val key = Pair(q.subject, q.topic)
                val current = performanceMap.getOrDefault(key, Pair(0, 0))
                val correct = state.totalAttempts - state.incorrectCount
                performanceMap[key] = Pair(current.first + state.totalAttempts, current.second + correct)
            }
        }        // Combine chapters from PYQs and Questions
        val allExamChapters = mutableSetOf<Pair<String, String>>()
        examPyqs.forEach { pyq ->
            if (pyq.subject.isNotBlank() && pyq.chapter.isNotBlank()) {
                allExamChapters.add(Pair(pyq.subject, pyq.chapter))
            }
        }
        allQuestions.forEach { q ->
            if (q.examCategory.contains(exam, ignoreCase = true) || q.examCategory.isEmpty() || exam.contains(q.examCategory, ignoreCase = true)) {
                if (q.subject.isNotBlank() && q.topic.isNotBlank()) {
                    allExamChapters.add(Pair(q.subject, q.topic))
                }
            }
        }
        
        val pyqAvgMap = examPyqs.associate { Pair(it.subject, it.chapter) to (if (it.examsCovered > 0) it.pyqCount.toFloat() / it.examsCovered else 0f) }

        // Build ChapterPerformance
        val chapterPerformances = allExamChapters.map { key ->
            val perf = performanceMap[key]
            val totalAtt = perf?.first ?: 0
            val correctAtt = perf?.second ?: 0
            val acc = if (totalAtt >= minAttemptsRequired) (correctAtt.toFloat() / totalAtt) * 100f else null
            val avg = pyqAvgMap[key] ?: 0f
            
            ChapterPerformance(
                subject = key.first,
                chapter = key.second,
                totalAttempts = totalAtt,
                correctAttempts = correctAtt,
                accuracy = acc,
                pyqAvg = avg
            )
        }
        
        // Filter by selected subject
        val filteredPerformances = if (subject != null && subject != "All Subjects") {
            chapterPerformances.filter { it.subject.equals(subject, ignoreCase = true) }
        } else {
            chapterPerformances
        }        // Priority Topics
        val priorityTopics = filteredPerformances.map { perf ->
            val pyqScore = (perf.pyqAvg.coerceAtMost(5f) / 5f) * 100f
            
            val weaknessScore = if (perf.accuracy != null) {
                100f - perf.accuracy
            } else {
                50f
            }
            
            val priorityScore = (pyqScore * 0.6f) + (weaknessScore * 0.4f)
            
            val label: String
            val reason: String
            
            if (perf.pyqAvg >= 2.0f) {
                label = "High Priority"
                reason = "Avg PYQ >= 2.0 (High Importance)"
            } else if (perf.pyqAvg >= 1.0f) {
                label = "Medium Priority"
                reason = "Avg PYQ 1.0 - 2.0 (Medium Importance)"
            } else {
                label = "Low Priority"
                reason = "Avg PYQ < 1.0 (Low Importance)"
            }
            
            PriorityTopicItem(
                subject = perf.subject,
                chapter = perf.chapter,
                priorityScore = priorityScore,
                priorityLabel = label,
                reason = reason,
                pyqAvg = perf.pyqAvg,
                accuracy = perf.accuracy
            )
        }.sortedByDescending { it.priorityScore }
        
        // Strength & Weakness
        val strengthWeakness = filteredPerformances.map { perf ->
            val isImportant = perf.pyqAvg >= highImportancePyqAvg
            val isWeak = perf.accuracy != null && perf.accuracy < weakAccuracyThreshold
            val isStrong = perf.accuracy != null && perf.accuracy >= strongAccuracyThreshold
            
            val label = if (perf.accuracy == null) {
                "Not Enough Data"
            } else if (isWeak && isImportant) {
                "Weak & Important"
            } else if (isStrong && isImportant) {
                "Strong & Important"
            } else if (isWeak) {
                "Weak & Less Important"
            } else if (isStrong) {
                "Strong & Less Important"
            } else if (isImportant) {
                "Average & Important"
            } else {
                "Average / Needs Improvement"
            }
            
            StrengthWeaknessItem(
                subject = perf.subject,
                chapter = perf.chapter,
                statusLabel = label,
                accuracy = perf.accuracy,
                pyqAvg = perf.pyqAvg,
                isImportant = isImportant,
                isWeak = isWeak
            )
        }.sortedByDescending { it.pyqAvg }
        
        // Preparation Strategy (overall exam level or subject level)
        val subjectKey = if (subject != "All Subjects") subject else null
        val prepStrategyStr = allPrepStrategies.find { 
            (it.exam.equals(exam, ignoreCase = true) || exam.contains(it.exam, ignoreCase = true)) && 
            it.subject != null && it.subject.equals(subjectKey, ignoreCase = true) 
        }?.content 
            ?: allPrepStrategies.find { 
                (it.exam.equals(exam, ignoreCase = true) || exam.contains(it.exam, ignoreCase = true)) && 
                (it.subject == null || it.subject == "All Subjects") 
            }?.content 
            ?: generateDynamicStrategy(exam, chapterPerformances)
        
        return GuidanceData(
            exam = exam,
            subject = subject,
            pyqFocus = filteredPyqs,
            priorityTopics = priorityTopics,
            strengthWeakness = strengthWeakness,
            preparationStrategy = prepStrategyStr
        )
    }
    
    private fun generateDynamicStrategy(exam: String, chapterPerformances: List<ChapterPerformance>): String {
        val totalAccuracies = chapterPerformances.mapNotNull { it.accuracy }
        val overallAcc = if (totalAccuracies.isNotEmpty()) totalAccuracies.average().toFloat() else null
        val strongCount = chapterPerformances.count { it.accuracy != null && it.accuracy >= 80f }
        val weakCount = chapterPerformances.count { it.accuracy != null && it.accuracy < 60f }
        
        val progress = overallAcc?.let { "${String.format("%.0f", it)}%" } ?: "0%"
        
        val strategyType = if (overallAcc == null) "BEGINNER"
        else if (overallAcc < 60f) "BEGINNER"
        else if (overallAcc < 80f) "INTERMEDIATE"
        else "ADVANCED"
        
        val recommendation = when (strategyType) {
            "BEGINNER" -> "1. Build foundation\n2. Complete important chapters\n3. Learn concepts\n4. Start basic practice"
            "INTERMEDIATE" -> "1. Increase PYQ practice\n2. Focus on weak topics\n3. Start timed practice\n4. Revise mistakes"
            else -> "1. Full mock tests\n2. Time management\n3. Accuracy improvement\n4. Weak-topic revision\n5. Final PYQ revision"
        }
        
        return "Your Current Status\n" +
               "Preparation Progress: $progress\n" +
               "Strong Areas: $strongCount\n" +
               "Weak Areas: $weakCount\n\n" +
               "Recommended Approach ($strategyType)\n\n" +
               recommendation
    }
}
