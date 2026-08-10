package com.example.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

object UserSessionManager {
    private val activeSessions = MutableStateFlow<Map<String, String>>(emptyMap())

    fun registerSession(email: String, deviceId: String) {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank()) return
        val currentMap = activeSessions.value.toMutableMap()
        currentMap[normalizedEmail] = deviceId
        activeSessions.value = currentMap
    }

    fun unregisterSession(email: String) {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank()) return
        val currentMap = activeSessions.value.toMutableMap()
        currentMap.remove(normalizedEmail)
        activeSessions.value = currentMap
    }

    fun getActiveDeviceId(email: String): String? {
        val normalizedEmail = email.trim().lowercase()
        return activeSessions.value[normalizedEmail]
    }

    fun observeActiveDeviceId(email: String): Flow<String?> {
        val normalizedEmail = email.trim().lowercase()
        return activeSessions.map { it[normalizedEmail] }
    }
}
