package com.example.data.repository

import com.example.data.local.QuestionEntity
import com.example.data.local.StudyNoteEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val db by lazy { FirebaseFirestore.getInstance() }

    suspend fun saveQuestion(question: QuestionEntity) {
        db.collection("questions").document(question.id.toString()).set(question).await()
    }

    suspend fun saveStudyNote(note: StudyNoteEntity) {
        db.collection("study_notes").document(note.id.toString()).set(note).await()
    }
}
