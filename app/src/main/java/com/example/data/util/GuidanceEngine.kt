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
    val preparationStrategy: String
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
        allPrepStrategies: List<com.example.data.local.PrepStrategyEntity>
    ): GuidanceData {
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
        }
        
        // Build ChapterPerformance
        val chapterPerformances = examPyqs.map { pyq ->
            val key = Pair(pyq.subject, pyq.chapter)
            val perf = performanceMap[key]
            val totalAtt = perf?.first ?: 0
            val correctAtt = perf?.second ?: 0
            val acc = if (totalAtt >= minAttemptsRequired) (correctAtt.toFloat() / totalAtt) * 100f else null
            val avg = if (pyq.examsCovered > 0) pyq.pyqCount.toFloat() / pyq.examsCovered else 0f
            
            ChapterPerformance(
                subject = pyq.subject,
                chapter = pyq.chapter,
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
        }
        
        // Priority Topics
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
            
            if (perf.pyqAvg >= highImportancePyqAvg && (perf.accuracy != null && perf.accuracy < weakAccuracyThreshold)) {
                label = "High Priority"
                reason = "Frequently asked + weak performance"
            } else if (perf.pyqAvg >= highImportancePyqAvg && (perf.accuracy != null && perf.accuracy >= strongAccuracyThreshold)) {
                label = "Maintain"
                reason = "Important topic but current performance is strong"
            } else if (perf.pyqAvg >= highImportancePyqAvg && perf.accuracy == null) {
                label = "High Priority"
                reason = "Important topic (PYQ). Practice to determine accuracy."
            } else if (priorityScore >= 60f) {
                label = "Medium Priority"
                reason = "Needs improvement"
            } else {
                label = "Low Priority"
                reason = "Less frequent or already strong"
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
