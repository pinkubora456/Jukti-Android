package com.example.data.repository

import android.content.Context
import com.example.data.local.QuestionEntity
import org.json.JSONArray
import org.json.JSONObject

data class SessionDeckResult(
    val orderedQuestions: List<QuestionEntity>,
    val currentIndex: Int,
    val totalQuestions: Int,
    val cycleCount: Int
)

object SessionDeckManager {

    private const val PREF_NAME = "jukti_session_decks_v1"

    /**
     * Builds a unique scope key for filtering (e.g. "ALL", "SUBJECT:General Knowledge", etc.)
     */
    fun buildScopeKey(subject: String, selectedChapters: Set<String>): String {
        val cleanSubject = subject.trim().ifBlank { "All Subjects" }
        return if (selectedChapters.isEmpty()) {
            "SUBJECT:$cleanSubject"
        } else {
            val sortedCh = selectedChapters.map { it.trim() }.sorted().joinToString(",")
            "SUBJECT:$cleanSubject|CHAPTERS:$sortedCh"
        }
    }

    /**
     * Retrieves or initializes/updates a randomized deck for a user, section, and scope.
     */
    fun getOrUpdateDeck(
        context: Context,
        userId: String,
        section: String,
        scopeKey: String,
        eligibleQuestions: List<QuestionEntity>
    ): SessionDeckResult {
        if (eligibleQuestions.isEmpty()) {
            return SessionDeckResult(emptyList(), 0, 0, 1)
        }

        val effectiveUserId = userId.ifBlank { "guest_user" }
        val prefsKey = "$effectiveUserId:$section:$scopeKey"
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val eligibleMap = eligibleQuestions.associateBy { it.id }
        val eligibleIdsSet = eligibleMap.keys

        val savedJsonStr = prefs.getString(prefsKey, null)

        var shuffledIds: MutableList<Long> = mutableListOf()
        var savedIndex = 0
        var cycleCount = 1

        if (!savedJsonStr.isNullOrBlank()) {
            try {
                val json = JSONObject(savedJsonStr)
                val jsonArray = json.optJSONArray("shuffledIds")
                if (jsonArray != null) {
                    for (i in 0 until jsonArray.length()) {
                        shuffledIds.add(jsonArray.getLong(i))
                    }
                }
                savedIndex = json.optInt("currentIndex", 0)
                cycleCount = json.optInt("cycleCount", 1)
            } catch (e: Exception) {
                shuffledIds.clear()
            }
        }

        // Filter out question IDs that no longer exist in eligibleMap
        shuffledIds = shuffledIds.filter { eligibleMap.containsKey(it) }.toMutableList()

        // Check for any new question IDs that are not yet in shuffledIds
        val currentDeckSet = shuffledIds.toSet()
        val newQuestionIds = eligibleIdsSet.filter { !currentDeckSet.contains(it) }.shuffled()

        if (newQuestionIds.isNotEmpty()) {
            shuffledIds.addAll(newQuestionIds)
        }

        // If shuffledIds is empty OR savedIndex >= shuffledIds.size (deck completed), reshuffle!
        if (shuffledIds.isEmpty() || savedIndex >= shuffledIds.size) {
            shuffledIds = createShuffledDeckForSection(section, eligibleQuestions)
            savedIndex = 0
            if (!savedJsonStr.isNullOrBlank()) {
                cycleCount++
            }
        }

        // Save state to SharedPreferences
        saveDeckToPrefs(prefs, prefsKey, shuffledIds, savedIndex, cycleCount)

        // Construct ordered questions
        val orderedQuestions = shuffledIds.mapNotNull { eligibleMap[it] }

        return SessionDeckResult(
            orderedQuestions = orderedQuestions,
            currentIndex = savedIndex.coerceIn(0, (orderedQuestions.size - 1).coerceAtLeast(0)),
            totalQuestions = orderedQuestions.size,
            cycleCount = cycleCount
        )
    }

    /**
     * Updates and saves the current question index for an active session.
     */
    fun saveCurrentIndex(
        context: Context,
        userId: String,
        section: String,
        scopeKey: String,
        newIndex: Int,
        totalSize: Int
    ) {
        val effectiveUserId = userId.ifBlank { "guest_user" }
        val prefsKey = "$effectiveUserId:$section:$scopeKey"
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val savedJsonStr = prefs.getString(prefsKey, null) ?: return
        try {
            val json = JSONObject(savedJsonStr)
            val cycleCount = json.optInt("cycleCount", 1)

            if (newIndex >= totalSize && totalSize > 0) {
                json.put("currentIndex", totalSize)
            } else {
                json.put("currentIndex", newIndex.coerceAtLeast(0))
            }
            json.put("cycleCount", cycleCount)
            json.put("lastUpdatedAt", System.currentTimeMillis())

            prefs.edit().putString(prefsKey, json.toString()).apply()
        } catch (e: Exception) {
            android.util.Log.e("SessionDeckManager", "Error saving index", e)
        }
    }

    /**
     * Reshuffles the deck completely (e.g. "Practice Again" or "Restart Session").
     */
    fun resetAndReshuffleDeck(
        context: Context,
        userId: String,
        section: String,
        scopeKey: String,
        eligibleQuestions: List<QuestionEntity>
    ): SessionDeckResult {
        if (eligibleQuestions.isEmpty()) {
            return SessionDeckResult(emptyList(), 0, 0, 1)
        }

        val effectiveUserId = userId.ifBlank { "guest_user" }
        val prefsKey = "$effectiveUserId:$section:$scopeKey"
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val eligibleMap = eligibleQuestions.associateBy { it.id }

        val savedJsonStr = prefs.getString(prefsKey, null)
        var cycleCount = 1
        if (!savedJsonStr.isNullOrBlank()) {
            try {
                val json = JSONObject(savedJsonStr)
                cycleCount = json.optInt("cycleCount", 1) + 1
            } catch (_: Exception) {}
        }

        val newShuffledIds = createShuffledDeckForSection(section, eligibleQuestions)
        val savedIndex = 0

        saveDeckToPrefs(prefs, prefsKey, newShuffledIds, savedIndex, cycleCount)

        val orderedQuestions = newShuffledIds.mapNotNull { eligibleMap[it] }

        return SessionDeckResult(
            orderedQuestions = orderedQuestions,
            currentIndex = 0,
            totalQuestions = orderedQuestions.size,
            cycleCount = cycleCount
        )
    }

    private fun createShuffledDeckForSection(
        section: String,
        eligibleQuestions: List<QuestionEntity>
    ): MutableList<Long> {
        return eligibleQuestions.shuffled().map { it.id }.toMutableList()
    }

    private fun saveDeckToPrefs(
        prefs: android.content.SharedPreferences,
        prefsKey: String,
        shuffledIds: List<Long>,
        currentIndex: Int,
        cycleCount: Int
    ) {
        try {
            val json = JSONObject()
            val jsonArray = JSONArray()
            shuffledIds.forEach { jsonArray.put(it) }
            json.put("shuffledIds", jsonArray)
            json.put("currentIndex", currentIndex)
            json.put("cycleCount", cycleCount)
            json.put("lastUpdatedAt", System.currentTimeMillis())

            prefs.edit().putString(prefsKey, json.toString()).apply()
        } catch (e: Exception) {
            android.util.Log.e("SessionDeckManager", "Error saving deck to prefs", e)
        }
    }
}
