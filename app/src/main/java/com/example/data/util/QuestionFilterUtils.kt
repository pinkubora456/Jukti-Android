package com.example.data.util

import com.example.data.local.QuestionEntity
import com.example.data.repository.normalizeChapterName
import com.example.data.repository.normalizeSubjectName

object QuestionFilterUtils {

    fun filterQuestions(
        allQuestions: List<QuestionEntity>,
        targetExam: String,
        targetSubject: String,
        targetChapters: Set<String>
    ): List<QuestionEntity> {
        return allQuestions.filter { q ->
            isEligible(q, targetExam, targetSubject, targetChapters)
        }
    }

    fun isEligible(
        q: QuestionEntity,
        targetExam: String,
        targetSubject: String,
        targetChapters: Set<String>
    ): Boolean {
        // Exam matching
        val examMatches = targetExam == "All Exams" || targetExam.isBlank() || 
            q.examCategory.contains(targetExam, ignoreCase = true)
        if (!examMatches) return false

        // Subject matching
        val isAllSubjects = targetSubject.equals("All Subjects", ignoreCase = true) || 
            targetSubject.equals("All Subject", ignoreCase = true) || 
            targetSubject.isBlank()
            
        val subjectMatches = if (isAllSubjects) {
            true
        } else {
            val qNorm = normalizeSubjectName(q.subject)
            val tNorm = normalizeSubjectName(targetSubject)
            qNorm.equals(tNorm, ignoreCase = true)
        }
        if (!subjectMatches) return false

        // Chapter matching
        if (targetChapters.isEmpty()) return true

        val qTopicStr = q.topic?.trim() ?: ""
        val qSubjStr = q.subject?.trim() ?: ""
        val qNormTopic = normalizeChapterName(qTopicStr, qSubjStr).ifBlank { qTopicStr }

        return targetChapters.any { rawCh ->
            val selSubj = if (rawCh.contains(": ")) rawCh.substringBefore(": ").trim() else ""
            val ch = if (rawCh.contains(": ")) rawCh.substringAfter(": ").trim() else rawCh.trim()

            val chSubjMatches = if (selSubj.isNotBlank()) {
                val qSubjNorm = normalizeSubjectName(qSubjStr)
                val selSubjNorm = normalizeSubjectName(selSubj)
                qSubjNorm.equals(selSubjNorm, ignoreCase = true)
            } else {
                true
            }

            if (!chSubjMatches) return@any false

            val nCh = normalizeChapterName(ch, qSubjStr).ifBlank { ch }
            
            qNormTopic.equals(nCh, ignoreCase = true) || qNormTopic.equals(ch, ignoreCase = true) || qTopicStr.equals(ch, ignoreCase = true)
        }
    }
}
