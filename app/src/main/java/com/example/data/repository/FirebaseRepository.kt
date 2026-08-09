package com.example.data.repository

import android.util.Log
import com.example.data.local.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Firestore not available", e)
            null
        }

    suspend fun saveQuestion(question: QuestionEntity) {
        try {
            firestore?.collection("questions")?.document(question.id.toString())?.set(question)?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving question", e)
        }
    }

    suspend fun deleteQuestion(id: Long) {
        try {
            firestore?.collection("questions")?.document(id.toString())?.delete()?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting question", e)
        }
    }

    suspend fun saveMockTest(mock: MockTestEntity) {
        try {
            firestore?.collection("mock_tests")?.document(mock.id.toString())?.set(mock)?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving mock test", e)
        }
    }

    suspend fun deleteMockTest(id: Long) {
        try {
            firestore?.collection("mock_tests")?.document(id.toString())?.delete()?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting mock test", e)
        }
    }

    suspend fun saveStudyNote(note: StudyNoteEntity) {
        try {
            firestore?.collection("study_notes")?.document(note.id.toString())?.set(note)?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving study note", e)
        }
    }

    suspend fun deleteStudyNote(id: Long) {
        try {
            firestore?.collection("study_notes")?.document(id.toString())?.delete()?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting study note", e)
        }
    }

    suspend fun saveExamUpdate(update: ExamUpdateEntity) {
        try {
            firestore?.collection("exam_updates")?.document(update.id.toString())?.set(update)?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving exam update", e)
        }
    }

    suspend fun deleteExamUpdate(id: Long) {
        try {
            firestore?.collection("exam_updates")?.document(id.toString())?.delete()?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting exam update", e)
        }
    }

    suspend fun saveBanner(banner: BannerEntity) {
        try {
            firestore?.collection("banners")?.document(banner.id.toString())?.set(banner)?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving banner", e)
        }
    }

    suspend fun deleteBanner(id: Long) {
        try {
            firestore?.collection("banners")?.document(id.toString())?.delete()?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting banner", e)
        }
    }

    suspend fun savePlan(plan: PlanEntity) {
        try {
            firestore?.collection("plans")?.document(plan.id.toString())?.set(plan)?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving plan", e)
        }
    }

    suspend fun deletePlan(id: Long) {
        try {
            firestore?.collection("plans")?.document(id.toString())?.delete()?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting plan", e)
        }
    }

    suspend fun saveFaq(faq: FaqEntity) {
        try {
            firestore?.collection("faqs")?.document(faq.id.toString())?.set(faq)?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving faq", e)
        }
    }

    suspend fun deleteFaq(id: Long) {
        try {
            firestore?.collection("faqs")?.document(id.toString())?.delete()?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting faq", e)
        }
    }

    suspend fun saveSubjectChapter(sc: SubjectChapterEntity) {
        try {
            firestore?.collection("subjects_chapters")?.document(sc.id.toString())?.set(sc)?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving subject chapter", e)
        }
    }

    suspend fun deleteSubjectChapter(id: Long) {
        try {
            firestore?.collection("subjects_chapters")?.document(id.toString())?.delete()?.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting subject chapter", e)
        }
    }
}
