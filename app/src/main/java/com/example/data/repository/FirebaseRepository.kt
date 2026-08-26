package com.example.data.repository
import java.util.Locale

import android.util.Log
import com.example.data.local.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class FirebaseRepository {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Firestore not available", e)
            null
        }

    private fun logListenerError(tagMsg: String, error: com.google.firebase.firestore.FirebaseFirestoreException) {
        if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            Log.w("FirebaseRepository", "$tagMsg (PERMISSION_DENIED: ${error.message})")
        } else {
            Log.e("FirebaseRepository", tagMsg, error)
        }
    }

    fun getSanitizedUserDocId(email: String): String {
        val trimmed = email.trim().lowercase()
        if (trimmed.isBlank()) {
            val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Exception) { null }
            val uid = auth?.currentUser?.uid
            if (uid != null) {
                return uid
            }
            return "scholar_jukti_in"
        }
        return trimmed.replace("@", "_at_").replace(".", "_dot_")
    }

    suspend fun saveUserProfile(profile: UserProfileEntity, merge: Boolean = true) {
        try {
            val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Exception) { null }
            val currentUid = auth?.currentUser?.uid
            val sanitizedEmailDocId = getSanitizedUserDocId(profile.email)
            val docId = if (profile.uid.isNotBlank()) {
                profile.uid
            } else if (sanitizedEmailDocId.isNotBlank() && sanitizedEmailDocId != "scholar_jukti_in") {
                sanitizedEmailDocId
            } else {
                currentUid ?: "scholar_jukti_in"
            }
            val authUid = currentUid ?: docId
            val userMap = mutableMapOf<String, Any?>(
                "id" to profile.id,
                "name" to profile.name,
                "email" to profile.email,
                "mobile" to profile.mobile,
                "district" to profile.district,
                "examGoal" to profile.examGoal,
                "xp" to profile.xp,
                "level" to profile.level,
                "dailyStreak" to profile.dailyStreak,
                "totalSolved" to profile.totalSolved,
                "correctCount" to profile.correctCount,
                "totalTimeMinutes" to profile.totalTimeMinutes,
                "firebaseProjectId" to profile.firebaseProjectId,
                "joinedDate" to profile.joinedDate,
                "isLoggedIn" to profile.isLoggedIn,
                "currentDeviceId" to profile.currentDeviceId,
                "activeDeviceId" to profile.activeDeviceId,
                "uid" to profile.uid.ifBlank { authUid },
                "profileName" to profile.profileName,
                "registrationName" to profile.registrationName,
                "googleName" to profile.googleName,
                "lastSyncedAt" to System.currentTimeMillis()
            )
            val isOwnerUser = profile.role == "OWNER" || auth?.currentUser?.email?.contains("juktieducation", ignoreCase = true) == true
            if (isOwnerUser || profile.isPremium || !merge) {
                userMap["isPremium"] = profile.isPremium
            }
            if (isOwnerUser || !merge) {
                userMap["role"] = profile.role
            }
            if (merge) {
                firestore?.collection("users")?.document(docId)?.set(userMap, SetOptions.merge())?.await()
            } else {
                firestore?.collection("users")?.document(docId)?.set(userMap)?.await()
            }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving user profile to Firebase", e)
        }
    }

    suspend fun fetchUserProfile(email: String, explicitUid: String? = null): UserProfileEntity? {
        return try {
            val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Exception) { null }
            if (auth?.currentUser == null) return null
            val currentUid = explicitUid ?: auth.currentUser?.uid
            val trimmedEmail = email.trim().lowercase()
            val sanitizedEmailDocId = if (trimmedEmail.isNotBlank()) trimmedEmail.replace("@", "_at_").replace(".", "_dot_") else ""

            var snapshot = if (!currentUid.isNullOrBlank()) {
                firestore?.collection("users")?.document(currentUid)?.get()?.await()
            } else null

            // Fallback to legacy document ID if not found under UID
            if ((snapshot == null || !snapshot.exists()) && sanitizedEmailDocId.isNotBlank()) {
                val legacySnap = firestore?.collection("users")?.document(sanitizedEmailDocId)?.get()?.await()
                if (legacySnap != null && legacySnap.exists()) {
                    snapshot = legacySnap
                }
            }

            if (snapshot != null && snapshot.exists()) {
                val foundProfile = UserProfileEntity(
                    id = snapshot.getLong("id")?.toInt() ?: 1,
                    name = snapshot.getString("name") ?: "Assam Scholar",
                    email = snapshot.getString("email") ?: email,
                    mobile = snapshot.getString("mobile") ?: "",
                    district = snapshot.getString("district") ?: "",
                    examGoal = snapshot.getString("examGoal") ?: "",
                    xp = snapshot.getLong("xp")?.toInt() ?: 0,
                    level = snapshot.getLong("level")?.toInt() ?: 1,
                    dailyStreak = snapshot.getLong("dailyStreak")?.toInt() ?: 0,
                    totalSolved = snapshot.getLong("totalSolved")?.toInt() ?: 0,
                    correctCount = snapshot.getLong("correctCount")?.toInt() ?: 0,
                    totalTimeMinutes = snapshot.getLong("totalTimeMinutes")?.toInt() ?: 0,
                    isPremium = snapshot.getBoolean("isPremium") ?: false,
                    role = snapshot.getString("role") ?: "USER",
                    firebaseProjectId = snapshot.getString("firebaseProjectId") ?: "jukti-26035",
                    joinedDate = snapshot.getString("joinedDate") ?: "Jul 2026",
                    isLoggedIn = snapshot.getBoolean("isLoggedIn") ?: true,
                    currentDeviceId = snapshot.getString("currentDeviceId") ?: "",
                    activeDeviceId = snapshot.getString("activeDeviceId") ?: "",
                    uid = currentUid ?: snapshot.getString("uid") ?: snapshot.id,
                    profileName = snapshot.getString("profileName") ?: "",
                    registrationName = snapshot.getString("registrationName") ?: "",
                    googleName = snapshot.getString("googleName") ?: ""
                )
                // If found via legacy doc, migrate to current UID
                if (!currentUid.isNullOrBlank() && snapshot.id != currentUid) {
                    saveUserProfile(foundProfile.copy(uid = currentUid), merge = true)
                }
                foundProfile
            } else null
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error fetching user profile from Firebase", e)
            null
        }
    }

    private fun parseEntitlementDoc(snap: com.google.firebase.firestore.DocumentSnapshot, resolvedUserId: String): EntitlementEntity? {
        if (!snap.exists()) return null
        val planName = snap.getString("planName") ?: ""
        val isFree = planName.isBlank() || planName.equals("Free Plan", ignoreCase = true)
        val rawIsLifetime = snap.getBoolean("isLifetime") ?: false
        val rawVal = snap.getString("validity") ?: snap.getString("validityLabel") ?: ""
        val validityType = snap.getString("validityType") ?: if (rawIsLifetime) "LIFETIME" else com.example.data.util.PlanValidityEngine.inferValidityType(rawVal)
        val isLifetime = isFree || rawIsLifetime || validityType == "LIFETIME" || rawVal.equals("Lifetime", ignoreCase = true) || planName.contains("Lifetime", ignoreCase = true)
        val validityValue = snap.getLong("validityValue")?.toInt() ?: if (isLifetime) 0 else com.example.data.util.PlanValidityEngine.inferValidityValue(rawVal)
        val validityLabel = if (isLifetime) "Lifetime" else snap.getString("validityLabel") ?: com.example.data.util.PlanValidityEngine.formatValidityLabel(validityType, validityValue)
        val validFrom = snap.getLong("validFrom") ?: snap.getLong("activatedAt") ?: 0L
        val validUntil = if (isLifetime) 0L else snap.getLong("validUntil") ?: 0L
        val isExpired = !isLifetime && (validUntil <= 0L || validUntil <= System.currentTimeMillis())
        val status = snap.getString("status") ?: if (isExpired) "EXPIRED" else "ACTIVE"

        val rawPlanId = snap.getString("planId")?.trim().orEmpty()
        val planId = if (rawPlanId.isNotBlank() && rawPlanId != "current") {
            rawPlanId
        } else if (snap.id != "current") {
            snap.id
        } else {
            planName.lowercase(java.util.Locale.ROOT).replace(" ", "_")
        }

        return EntitlementEntity(
            userId = resolvedUserId,
            planId = if (isFree) "free_plan" else planId,
            planName = if (isFree) "Free Plan" else planName,
            status = if (isExpired && !isFree) "EXPIRED" else status,
            validFrom = validFrom,
            validUntil = validUntil,
            validityType = validityType,
            validityValue = validityValue,
            validityLabel = validityLabel,
            isLifetime = isLifetime,
            benefits = (snap.get("benefits") as? List<*>)?.joinToString(",") ?: snap.getString("benefits") ?: "All Premium MCQs, Mock Tests, Notes, Analytics",
            source = snap.getString("source") ?: "OWNER_ASSIGNED",
            purchaseId = snap.getString("purchaseId") ?: "",
            activatedAt = snap.getLong("activatedAt") ?: validFrom,
            updatedAt = snap.getLong("updatedAt") ?: System.currentTimeMillis()
        )
    }

    private fun parseUserDocForEntitlement(userDocSnapshot: com.google.firebase.firestore.DocumentSnapshot, resolvedUserId: String): EntitlementEntity? {
        if (!userDocSnapshot.exists()) return null
        val isPremium = userDocSnapshot.getBoolean("isPremium") ?: false
        val planName = userDocSnapshot.getString("planName") ?: userDocSnapshot.getString("assignedPlan") ?: ""
        val isFree = planName.isBlank() || planName.equals("Free Plan", ignoreCase = true)
        val rawIsLifetime = userDocSnapshot.getBoolean("isLifetime") ?: false
        val validity = userDocSnapshot.getString("validity") ?: userDocSnapshot.getString("assignedValidity") ?: ""
        val vType = userDocSnapshot.getString("validityType") ?: if (rawIsLifetime) "LIFETIME" else com.example.data.util.PlanValidityEngine.inferValidityType(validity)
        val isLifetime = isFree || rawIsLifetime || vType == "LIFETIME" || validity.equals("Lifetime", ignoreCase = true) || planName.contains("Lifetime", ignoreCase = true)
        val validUntil = if (isLifetime) 0L else userDocSnapshot.getLong("validUntil") ?: userDocSnapshot.getLong("assignedValidUntil") ?: 0L
        val isExpired = !isLifetime && (validUntil <= 0L || validUntil <= System.currentTimeMillis())

        if (isExpired || isFree || (!isPremium && planName.isBlank())) {
            return null
        }

        val resolvedPlanName = if (planName.isNotBlank()) planName else "Premium Pass"
        val planId = resolvedPlanName.lowercase(java.util.Locale.ROOT).replace(" ", "_")
        val vVal = userDocSnapshot.getLong("validityValue")?.toInt() ?: if (isLifetime) 0 else com.example.data.util.PlanValidityEngine.inferValidityValue(validity)
        val vLabel = if (isLifetime) "Lifetime" else com.example.data.util.PlanValidityEngine.formatValidityLabel(vType, vVal)
        val validFrom = userDocSnapshot.getLong("validFrom") ?: userDocSnapshot.getLong("assignedAt") ?: 0L

        return EntitlementEntity(
            userId = resolvedUserId,
            planId = planId,
            planName = resolvedPlanName,
            status = "ACTIVE",
            validFrom = validFrom,
            validUntil = validUntil,
            validityType = vType,
            validityValue = vVal,
            validityLabel = vLabel,
            isLifetime = isLifetime,
            benefits = "All Premium MCQs, Mock Tests, Notes, Analytics",
            source = userDocSnapshot.getString("assignedBy") ?: userDocSnapshot.getString("source") ?: "OWNER_ASSIGNED",
            purchaseId = "DOC_ASSIGNMENT",
            activatedAt = validFrom,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun fetchUserEntitlements(email: String, explicitUid: String? = null): List<EntitlementEntity> {
        return try {
            val currentUid = explicitUid
            val trimmedEmail = email.trim().lowercase()
            val sanitizedEmailDocId = if (trimmedEmail.isNotBlank()) getSanitizedUserDocId(trimmedEmail) else ""
            val resolvedUserId = sanitizedEmailDocId.ifBlank { currentUid ?: getSanitizedUserDocId(email) }

            val defaultFreeEntitlement = EntitlementEntity(
                userId = resolvedUserId,
                planId = "free_plan",
                planName = "Free Plan",
                status = "ACTIVE",
                validFrom = 0L,
                validUntil = 0L,
                validityType = "LIFETIME",
                validityValue = 0,
                validityLabel = "Lifetime",
                isLifetime = true,
                benefits = "Basic Questions, Daily Tests, Syllabus Updates",
                source = "DEFAULT_FREE",
                purchaseId = "FREE_LIFETIME",
                activatedAt = 0L,
                updatedAt = System.currentTimeMillis()
            )

            val parsedList = mutableListOf<EntitlementEntity>()

            val docPathsToTry = mutableListOf<String>()
            if (sanitizedEmailDocId.isNotBlank()) docPathsToTry.add(sanitizedEmailDocId)
            if (!currentUid.isNullOrBlank() && !docPathsToTry.contains(currentUid)) docPathsToTry.add(currentUid)

            for (docPath in docPathsToTry) {
                try {
                    val collSnap = firestore?.collection("users")?.document(docPath)?.collection("entitlements")?.get()?.await()
                    if (collSnap != null && !collSnap.isEmpty) {
                        for (doc in collSnap.documents) {
                            val ent = parseEntitlementDoc(doc, resolvedUserId)
                            if (ent != null) parsedList.add(ent)
                        }
                    }
                    val userDoc = firestore?.collection("users")?.document(docPath)?.get()?.await()
                    if (userDoc != null && userDoc.exists()) {
                        val userEnt = parseUserDocForEntitlement(userDoc, resolvedUserId)
                        if (userEnt != null) parsedList.add(userEnt)
                    }
                } catch (e: Exception) {
                    Log.w("FirebaseRepository", "Error fetching entitlements from path $docPath", e)
                }
            }

            if (parsedList.isEmpty() && trimmedEmail.isNotBlank()) {
                try {
                    val querySnap = firestore?.collection("users")?.whereEqualTo("email", trimmedEmail)?.get()?.await()
                    if (querySnap != null && !querySnap.isEmpty) {
                        for (userDoc in querySnap.documents) {
                            val collSnap = firestore?.collection("users")?.document(userDoc.id)?.collection("entitlements")?.get()?.await()
                            if (collSnap != null && !collSnap.isEmpty) {
                                for (doc in collSnap.documents) {
                                    val ent = parseEntitlementDoc(doc, resolvedUserId)
                                    if (ent != null) parsedList.add(ent)
                                }
                            }
                            val userEnt = parseUserDocForEntitlement(userDoc, resolvedUserId)
                            if (userEnt != null) parsedList.add(userEnt)
                        }
                    }
                } catch (e: Exception) {}
            }

            // Deduplicate by planId / planName (prefer active/longer expiry over expired)
            val distinctByPlan = parsedList.groupBy { it.planName.lowercase(Locale.ROOT) }
                .mapValues { entry ->
                    entry.value.maxByOrNull { ent ->
                        if (com.example.data.util.PlanValidityEngine.isEntitlementActive(ent)) {
                            if (ent.isLifetime) Long.MAX_VALUE else ent.validUntil
                        } else 0L
                    } ?: entry.value.first()
                }.values.toList()

            val activePlans = distinctByPlan.filter { com.example.data.util.PlanValidityEngine.isEntitlementActive(it) }
            if (activePlans.isNotEmpty()) {
                activePlans
            } else {
                listOf(defaultFreeEntitlement)
            }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error fetching user entitlements", e)
            emptyList()
        }
    }

    suspend fun fetchUserEntitlement(email: String, explicitUid: String? = null): EntitlementEntity? {
        val list = fetchUserEntitlements(email, explicitUid)
        return list.firstOrNull()
    }

    suspend fun saveUserEntitlement(
        email: String,
        planName: String,
        validUntil: Long,
        validFrom: Long = System.currentTimeMillis(),
        validity: String = "1 year",
        validityType: String = "MONTHS",
        validityValue: Int = 1,
        isLifetime: Boolean = false,
        assignedBy: String = "OWNER",
        source: String = "OWNER_ASSIGNED",
        purchaseId: String = ""
    ): Boolean {
        return try {
            val trimmedEmail = email.trim().lowercase()
            if (trimmedEmail.isBlank()) return false
            
            val sanitizedEmailDocId = getSanitizedUserDocId(trimmedEmail)
            val now = System.currentTimeMillis()
            val isFreePlan = planName.isBlank() || planName.equals("Free Plan", ignoreCase = true)
            val finalIsLifetime = isFreePlan || isLifetime || validityType == "LIFETIME" || validity.equals("Lifetime", ignoreCase = true) || planName.contains("Lifetime", ignoreCase = true)
            val finalValidUntil = if (finalIsLifetime) 0L else validUntil
            val finalValidityLabel = if (finalIsLifetime) "Lifetime" else if (validity.isNotBlank() && !validity.equals("Custom", ignoreCase = true)) validity else com.example.data.util.PlanValidityEngine.formatValidityLabel(validityType, validityValue)
            val finalValidityType = if (finalIsLifetime) "LIFETIME" else validityType
            val finalPlanName = if (isFreePlan) "Free Plan" else planName
            val finalPlanId = finalPlanName.lowercase(java.util.Locale.ROOT).replace(" ", "_")
            val finalStatus = if (finalIsLifetime || finalValidUntil > now) "ACTIVE" else "EXPIRED"
            val isUserPremium = (finalStatus == "ACTIVE" && !isFreePlan)
            val finalPurchaseId = purchaseId.ifBlank { "ASSIGNMENT_${validFrom}" }

            val entData = mapOf(
                "userId" to sanitizedEmailDocId,
                "planId" to finalPlanId,
                "planName" to finalPlanName,
                "status" to finalStatus,
                "validFrom" to validFrom,
                "validUntil" to finalValidUntil,
                "validity" to finalValidityLabel,
                "validityType" to finalValidityType,
                "validityValue" to (if (finalIsLifetime) 0 else validityValue),
                "validityLabel" to finalValidityLabel,
                "isLifetime" to finalIsLifetime,
                "benefits" to if (isFreePlan) listOf("Basic Questions", "Daily Tests", "Syllabus Updates") else listOf("All Premium MCQs", "Mock Tests", "Notes", "Analytics"),
                "source" to (if (source.isNotBlank()) source else if (assignedBy.isNotBlank()) assignedBy else "OWNER_ASSIGNED"),
                "purchaseId" to finalPurchaseId,
                "activatedAt" to validFrom,
                "updatedAt" to now
            )

            val userDocUpdates = mapOf(
                "email" to trimmedEmail,
                "isPremium" to isUserPremium,
                "planName" to finalPlanName,
                "validity" to finalValidityLabel,
                "validityType" to finalValidityType,
                "validityValue" to (if (finalIsLifetime) 0 else validityValue),
                "isLifetime" to finalIsLifetime,
                "validUntil" to finalValidUntil,
                "validFrom" to validFrom,
                "assignedPlan" to finalPlanName,
                "assignedValidity" to finalValidityLabel,
                "assignedValidUntil" to finalValidUntil,
                "assignedBy" to assignedBy,
                "assignedAt" to validFrom,
                "lastSyncedAt" to now
            )

            val historyData = mapOf(
                "userId" to sanitizedEmailDocId,
                "userEmail" to trimmedEmail,
                "eventType" to if (source == "GOOGLE_PLAY") "PURCHASED" else if (isFreePlan) "FREE_PLAN_ASSIGNED" else "MANUALLY_ASSIGNED",
                "newPlan" to finalPlanName,
                "newExpiry" to finalValidUntil,
                "validityGranted" to finalValidityLabel,
                "validityType" to finalValidityType,
                "validityValue" to (if (finalIsLifetime) 0 else validityValue),
                "isLifetime" to finalIsLifetime,
                "source" to source,
                "actor" to assignedBy,
                "timestamp" to now
            )

            // Always update primary sanitized document
            val primaryDocRef = firestore?.collection("users")?.document(sanitizedEmailDocId)
            primaryDocRef?.collection("entitlements")?.document(finalPlanId)?.set(entData, SetOptions.merge())?.await()
            primaryDocRef?.collection("entitlements")?.document("current")?.set(entData, SetOptions.merge())?.await()
            primaryDocRef?.collection("entitlement_history")?.document(now.toString())?.set(historyData)?.await()
            primaryDocRef?.set(userDocUpdates, SetOptions.merge())?.await()

            // Also update any secondary documents matching this email
            try {
                val querySnap = firestore?.collection("users")?.whereEqualTo("email", trimmedEmail)?.get()?.await()
                if (querySnap != null && !querySnap.isEmpty) {
                    for (doc in querySnap.documents) {
                        if (doc.id != sanitizedEmailDocId) {
                            val targetUid = doc.id
                            val docRef = firestore?.collection("users")?.document(targetUid)
                            docRef?.collection("entitlements")?.document(finalPlanId)?.set(entData.plus("userId" to targetUid), SetOptions.merge())?.await()
                            docRef?.collection("entitlements")?.document("current")?.set(entData.plus("userId" to targetUid), SetOptions.merge())?.await()
                            docRef?.collection("entitlement_history")?.document(now.toString())?.set(historyData.plus("userId" to targetUid))?.await()
                            docRef?.set(userDocUpdates, SetOptions.merge())?.await()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("FirebaseRepository", "Could not update secondary user docs for $trimmedEmail", e)
            }

            Log.i("FirebaseRepository", "Successfully updated entitlement for user $trimmedEmail: $finalPlanName ($finalValidityLabel)")
            true
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving user entitlement to Firebase", e)
            false
        }
    }

    suspend fun fetchEntitlementHistory(email: String, explicitUid: String? = null): List<EntitlementHistoryEntity> {
        return try {
            val trimmedEmail = email.trim().lowercase()
            val sanitizedEmailDocId = if (trimmedEmail.isNotBlank()) getSanitizedUserDocId(trimmedEmail) else ""
            val docId = sanitizedEmailDocId.ifBlank { explicitUid ?: "" }
            if (docId.isBlank()) return emptyList()

            val snapshot = firestore?.collection("users")?.document(docId)
                ?.collection("entitlement_history")?.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                ?.limit(50)?.get()?.await()

            snapshot?.documents?.mapNotNull { doc ->
                try {
                    EntitlementHistoryEntity(
                        id = 0L,
                        userId = doc.getString("userId") ?: docId,
                        userEmail = doc.getString("userEmail") ?: trimmedEmail,
                        eventType = doc.getString("eventType") ?: "MANUALLY_ASSIGNED",
                        previousPlan = doc.getString("previousPlan") ?: "",
                        newPlan = doc.getString("newPlan") ?: "",
                        previousExpiry = doc.getLong("previousExpiry") ?: 0L,
                        newExpiry = doc.getLong("newExpiry") ?: 0L,
                        validityGranted = doc.getString("validityGranted") ?: "",
                        validityType = doc.getString("validityType") ?: "",
                        validityValue = doc.getLong("validityValue")?.toInt() ?: 0,
                        isLifetime = doc.getBoolean("isLifetime") ?: false,
                        source = doc.getString("source") ?: "",
                        actor = doc.getString("actor") ?: "",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching entitlement history", e)
            emptyList()
        }
    }

    suspend fun fetchAllUsers(): List<UserProfileEntity> {
        return try {
            val snapshot = firestore?.collection("users")?.get()?.await()
            val users = mutableListOf<UserProfileEntity>()
            snapshot?.documents?.forEach { doc ->
                val role = doc.getString("role") ?: "USER"
                if (role.equals("DELETED", ignoreCase = true)) {
                    return@forEach
                }
                users.add(UserProfileEntity(
                    id = doc.getLong("id")?.toInt() ?: 1,
                    name = doc.getString("name") ?: "Assam Scholar",
                    email = doc.getString("email") ?: "",
                    mobile = doc.getString("mobile") ?: "",
                    district = doc.getString("district") ?: "",
                    examGoal = doc.getString("examGoal") ?: "",
                    xp = doc.getLong("xp")?.toInt() ?: 0,
                    level = doc.getLong("level")?.toInt() ?: 1,
                    dailyStreak = doc.getLong("dailyStreak")?.toInt() ?: 0,
                    totalSolved = doc.getLong("totalSolved")?.toInt() ?: 0,
                    correctCount = doc.getLong("correctCount")?.toInt() ?: 0,
                    totalTimeMinutes = doc.getLong("totalTimeMinutes")?.toInt() ?: 0,
                    isPremium = doc.getBoolean("isPremium") ?: false,
                                        accessType = doc.getString("accessType") ?: (if (doc.getBoolean("isPremium") == true) "PREMIUM" else "FREE"),
                    role = role,
                    firebaseProjectId = doc.getString("firebaseProjectId") ?: "jukti-26035",
                    joinedDate = doc.getString("joinedDate") ?: "Jul 2026",
                    isLoggedIn = doc.getBoolean("isLoggedIn") ?: true,
                    currentDeviceId = doc.getString("currentDeviceId") ?: "",
                    activeDeviceId = doc.getString("activeDeviceId") ?: "",
                    uid = doc.getString("uid") ?: doc.id
                ))
            }
            users.sortedByDescending { it.uid.isNotBlank() && it.uid != "scholar_jukti_in" && it.uid != it.email.replace("@", "_at_").replace(".", "_dot_") }
                .distinctBy { it.email.lowercase() }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching all users", e)
            emptyList()
        }
    }

    suspend fun saveUserQuestionState(email: String, state: UserQuestionStateEntity) {
        try {
            val docId = getSanitizedUserDocId(email)
            val map = mapOf(
                "userId" to state.userId,
                "questionId" to state.questionId,
                "isBookmarked" to state.isBookmarked,
                "isLiked" to state.isLiked,
                "isHidden" to state.isHidden,
                "isMastered" to state.isMastered,
                "everGotWrong" to state.everGotWrong,
                "incorrectCount" to state.incorrectCount,
                "totalAttempts" to state.totalAttempts,
                "lastUpdated" to System.currentTimeMillis()
            )
            firestore?.collection("users")?.document(docId)
                ?.collection("user_question_states")?.document(state.questionId)
                ?.set(map, SetOptions.merge())?.await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving user question state to Firebase", e)
        }
    }

    suspend fun fetchUserQuestionStateList(email: String): List<UserQuestionStateEntity> {
        return try {
            val docId = getSanitizedUserDocId(email)
            val snapshot = firestore?.collection("users")?.document(docId)
                ?.collection("user_question_states")?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                try {
                    UserQuestionStateEntity(
                        userId = doc.getString("userId") ?: "",
                        questionId = doc.getString("questionId") ?: "",
                        isBookmarked = doc.getBoolean("isBookmarked") ?: false,
                        isLiked = doc.getBoolean("isLiked") ?: false,
                        isHidden = doc.getBoolean("isHidden") ?: false,
                        isMastered = doc.getBoolean("isMastered") ?: false,
                        everGotWrong = doc.getBoolean("everGotWrong") ?: false,
                        incorrectCount = doc.getLong("incorrectCount")?.toInt() ?: 0,
                        totalAttempts = doc.getLong("totalAttempts")?.toInt() ?: 0,
                        lastUpdated = doc.getLong("lastUpdated") ?: 0L
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching user question states", e)
            emptyList()
        }
    }

    private fun questionToMap(q: QuestionEntity): Map<String, Any?> = mapOf(
        "id" to q.id,
        "subject" to q.subject,
        "topic" to q.topic,
        "difficulty" to q.difficulty,
        "questionEn" to q.questionEn,
        "questionAs" to q.questionAs,
        "optionAEn" to q.optionAEn,
        "optionBEn" to q.optionBEn,
        "optionCEn" to q.optionCEn,
        "optionDEn" to q.optionDEn,
        "optionAAs" to q.optionAAs,
        "optionBAs" to q.optionBAs,
        "optionCAs" to q.optionCAs,
        "optionDAs" to q.optionDAs,
        "correctOptionIndex" to q.correctOptionIndex,
        "explanationEn" to q.explanationEn,
        "explanationAs" to q.explanationAs,
        "examCategory" to q.examCategory,
        "isPremium" to q.isPremium,
            "accessType" to q.accessType,
        "questionType" to q.questionType,
        "isReported" to q.isReported,
        "cachedAt" to q.cachedAt,
        "lastAccessedAt" to q.lastAccessedAt,
        "version" to q.version,
        "updatedAt" to q.updatedAt,
        "firebaseId" to q.firebaseId
    )

    private fun mockTestToMap(m: MockTestEntity): Map<String, Any?> = mapOf(
        "id" to m.id,
        "titleEn" to m.titleEn,
        "titleAs" to m.titleAs,
        "category" to m.category,
        "durationMinutes" to m.durationMinutes,
        "totalQuestions" to m.totalQuestions,
        "totalMarks" to m.totalMarks,
        "questionMarksJson" to m.questionMarksJson,
        "isScheduled" to m.isScheduled,
        "scheduledDate" to m.scheduledDate,
        "isCompleted" to m.isCompleted,
        "userScore" to m.userScore,
        "userAccuracy" to m.userAccuracy,
        "userRank" to m.userRank,
        "userPercentile" to m.userPercentile,
        "isPublished" to m.isPublished,
        "testType" to m.testType,
        "subjectOrChapter" to m.subjectOrChapter,
        "negativeMarking" to m.negativeMarking,
        "difficulty" to m.difficulty,
        "isPremium" to m.isPremium,
            "accessType" to m.accessType,
        "inProgress" to m.inProgress,
        "questionsAnswered" to m.questionsAnswered,
        "timeRemainingSeconds" to m.timeRemainingSeconds,
        "questionIds" to m.questionIds,
        "markPerQuestion" to m.markPerQuestion
    )

    private fun studyNoteToMap(n: StudyNoteEntity): Map<String, Any?> = mapOf(
        "id" to n.id,
        "subject" to n.subject,
        "topic" to n.topic,
        "titleEn" to n.titleEn,
        "titleAs" to n.titleAs,
        "contentEn" to n.contentEn,
        "contentAs" to n.contentAs,
        "isBookmarked" to n.isBookmarked,
        "isDownloaded" to n.isDownloaded,
        "readTimeMinutes" to n.readTimeMinutes,
        "isPremium" to n.isPremium,
            "accessType" to n.accessType
    )

    private fun examUpdateToMap(u: ExamUpdateEntity): Map<String, Any?> = mapOf(
        "id" to u.id,
        "examName" to u.examName,
        "category" to u.category,
        "titleEn" to u.titleEn,
        "titleAs" to u.titleAs,
        "updateDate" to u.updateDate,
        "detailEn" to u.detailEn,
        "detailAs" to u.detailAs,
        "officialLink" to u.officialLink,
        "isImportantNotice" to u.isImportantNotice
    )

    private fun bannerToMap(b: BannerEntity): Map<String, Any?> = mapOf(
        "id" to b.id,
        "titleEn" to b.titleEn,
        "titleAs" to b.titleAs,
        "subtitleEn" to b.subtitleEn,
        "subtitleAs" to b.subtitleAs,
        "type" to b.type,
        "badgeText" to b.badgeText,
        "actionUrl" to b.actionUrl,
        "isActive" to b.isActive,
        "imageUrl" to b.imageUrl,
        "actionType" to b.actionType,
        "offerValidity" to b.offerValidity,
        "planPrice" to b.planPrice,
        "discount" to b.discount,
        "finalPrice" to b.finalPrice
    )

    private fun planToMap(p: PlanEntity): Map<String, Any?> = mapOf(
        "id" to p.id,
        "planName" to p.planName,
        "planPrice" to p.planPrice,
        "discount" to p.discount,
        "finalPrice" to p.finalPrice,
        "offerValidity" to p.offerValidity,
        "planValidity" to p.planValidity,
        "validityType" to p.validityType,
        "validityValue" to p.validityValue,
        "validityLabel" to p.validityLabel,
        "isLifetime" to p.isLifetime,
        "contents" to p.contents,
        "features" to p.features,
        "isActive" to p.isActive,
        "imageUrl" to p.imageUrl,
        "examTarget" to p.examTarget,
        "googlePlayProductId" to p.googlePlayProductId,
        "createdAt" to (if (p.createdAt > 0L) p.createdAt else System.currentTimeMillis()),
        "updatedAt" to System.currentTimeMillis()
    )

    private fun faqToMap(f: FaqEntity): Map<String, Any?> = mapOf(
        "id" to f.id,
        "questionEn" to f.questionEn,
        "questionAs" to f.questionAs,
        "answerEn" to f.answerEn,
        "answerAs" to f.answerAs
    )

    private fun subjectChapterToMap(sc: SubjectChapterEntity): Map<String, Any?> = mapOf(
        "id" to sc.id,
        "subject" to sc.subject,
        "chapter" to sc.chapter
    )

    suspend fun saveQuestion(question: QuestionEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("questions").document(question.id.toString())
                .set(questionToMap(question), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving question", e)
        }
    }

    suspend fun deleteQuestion(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("questions").document(id.toString()).delete().await()
            val query = db.collection("questions").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting question", e)
        }
    }

    suspend fun saveMockTest(mock: MockTestEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("mock_tests").document(mock.id.toString())
                .set(mockTestToMap(mock), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving mock test", e)
        }
    }

    suspend fun deleteMockTest(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("mock_tests").document(id.toString()).delete().await()
            val query = db.collection("mock_tests").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting mock test", e)
        }
    }

    suspend fun saveStudyNote(note: StudyNoteEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("study_notes").document(note.id.toString())
                .set(studyNoteToMap(note), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving study note", e)
        }
    }

    suspend fun deleteStudyNote(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("study_notes").document(id.toString()).delete().await()
            val query = db.collection("study_notes").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting study note", e)
        }
    }

    suspend fun saveExamUpdate(update: ExamUpdateEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("exam_updates").document(update.id.toString())
                .set(examUpdateToMap(update), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving exam update", e)
        }
    }

    suspend fun deleteExamUpdate(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("exam_updates").document(id.toString()).delete().await()
            val query = db.collection("exam_updates").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting exam update", e)
        }
    }

    suspend fun saveBanner(banner: BannerEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("banners").document(banner.id.toString())
                .set(bannerToMap(banner), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving banner", e)
        }
    }

    suspend fun deleteBanner(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("banners").document(id.toString()).delete().await()
            val query = db.collection("banners").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting banner", e)
        }
    }

    suspend fun savePlan(plan: PlanEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("plans").document(plan.id.toString())
                .set(planToMap(plan), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving plan", e)
        }
    }

    suspend fun deletePlan(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("plans").document(id.toString()).delete().await()
            val query = db.collection("plans").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting plan", e)
        }
    }

    suspend fun saveFaq(faq: FaqEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("faqs").document(faq.id.toString())
                .set(faqToMap(faq), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving faq", e)
        }
    }

    suspend fun deleteFaq(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("faqs").document(id.toString()).delete().await()
            val query = db.collection("faqs").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting faq", e)
        }
    }

    suspend fun saveSubjectChapter(sc: SubjectChapterEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("subjects_chapters").document(sc.id.toString())
                .set(subjectChapterToMap(sc), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving subject chapter", e)
        }
    }

    suspend fun deleteSubjectChapter(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("subjects_chapters").document(id.toString()).delete().await()
            val query = db.collection("subjects_chapters").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting subject chapter", e)
        }
    }

    suspend fun batchSaveAllData(
        questions: List<QuestionEntity>,
        mockTests: List<MockTestEntity>,
        studyNotes: List<StudyNoteEntity>,
        plans: List<PlanEntity>,
        subjectChapters: List<SubjectChapterEntity>,
        banners: List<BannerEntity>,
        examUpdates: List<ExamUpdateEntity>,
        faqs: List<FaqEntity>
    ): Int = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val db = firestore ?: return@withContext 0
        var totalUploaded = 0

        suspend fun saveCollection(collectionName: String, items: List<Pair<com.google.firebase.firestore.DocumentReference, Map<String, Any?>>>) {
            if (items.isEmpty()) return
            val chunks = items.chunked(400)
            chunks.forEach { chunk ->
                try {
                    val batch = db.batch()
                    chunk.forEach { (docRef, dataMap) ->
                        batch.set(docRef, dataMap, SetOptions.merge())
                    }
                    batch.commit().await()
                    totalUploaded += chunk.size
                    Log.d("FirebaseRepository", "Successfully synced ${chunk.size} documents for collection: $collectionName")
                } catch (e: kotlinx.coroutines.CancellationException) { 
                    throw e 
                } catch (e: Throwable) {
                    Log.e("FirebaseRepository", "Error committing batch chunk for collection $collectionName. Error: ${e.localizedMessage ?: e.javaClass.simpleName}", e)
                }
            }
        }

        saveCollection("questions", questions.map { db.collection("questions").document(it.id.toString()) to questionToMap(it) })
        saveCollection("mock_tests", mockTests.map { db.collection("mock_tests").document(it.id.toString()) to mockTestToMap(it) })
        saveCollection("study_notes", studyNotes.map { db.collection("study_notes").document(it.id.toString()) to studyNoteToMap(it) })
        saveCollection("plans", plans.map { db.collection("plans").document(it.id.toString()) to planToMap(it) })
        saveCollection("subjects_chapters", subjectChapters.map { db.collection("subjects_chapters").document(it.id.toString()) to subjectChapterToMap(it) })
        saveCollection("banners", banners.map { db.collection("banners").document(it.id.toString()) to bannerToMap(it) })
        saveCollection("exam_updates", examUpdates.map { db.collection("exam_updates").document(it.id.toString()) to examUpdateToMap(it) })
        saveCollection("faqs", faqs.map { db.collection("faqs").document(it.id.toString()) to faqToMap(it) })

        return@withContext totalUploaded
    }

    suspend fun fetchAllBanners(): List<BannerEntity> {
        return try {
            val snapshot = firestore?.collection("banners")?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                BannerEntity(
                    id = doc.getLong("id") ?: 0L,
                    titleEn = doc.getString("titleEn") ?: "",
                    titleAs = doc.getString("titleAs") ?: "",
                    subtitleEn = doc.getString("subtitleEn") ?: "",
                    subtitleAs = doc.getString("subtitleAs") ?: "",
                    badgeText = doc.getString("badgeText") ?: "",
                    type = doc.getString("type") ?: "INFORMATION",
                    actionUrl = doc.getString("actionUrl") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    actionType = doc.getString("actionType") ?: "Link",
                    offerValidity = doc.getString("offerValidity") ?: "",
                    planPrice = doc.getString("planPrice") ?: "",
                    discount = doc.getString("discount") ?: "",
                    finalPrice = doc.getString("finalPrice") ?: ""
                )
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching banners", e)
            emptyList()
        }
    }

    suspend fun fetchAllPlans(): List<PlanEntity> {
        return try {
            val snapshot = firestore?.collection("plans")?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                val rawVal = doc.getString("planValidity") ?: doc.getString("offerValidity") ?: ""
                val isLifetime = doc.getBoolean("isLifetime") ?: (rawVal.equals("Lifetime", ignoreCase = true) || (doc.getString("validityType") ?: "").equals("LIFETIME", ignoreCase = true))
                val validityType = doc.getString("validityType") ?: if (isLifetime) "LIFETIME" else com.example.data.util.PlanValidityEngine.inferValidityType(rawVal)
                val validityValue = doc.getLong("validityValue")?.toInt() ?: if (isLifetime) 0 else com.example.data.util.PlanValidityEngine.inferValidityValue(rawVal)
                val validityLabel = doc.getString("validityLabel") ?: if (isLifetime) "Lifetime" else com.example.data.util.PlanValidityEngine.formatValidityLabel(validityType, validityValue)

                PlanEntity(
                    id = doc.getLong("id") ?: 0L,
                    planName = doc.getString("planName") ?: "",
                    planPrice = doc.getString("planPrice") ?: "",
                    discount = doc.getString("discount") ?: "",
                    finalPrice = doc.getString("finalPrice") ?: "",
                    offerValidity = doc.getString("offerValidity") ?: validityLabel,
                    planValidity = doc.getString("planValidity") ?: validityLabel,
                    validityType = validityType,
                    validityValue = validityValue,
                    validityLabel = validityLabel,
                    isLifetime = isLifetime,
                    features = doc.getString("features") ?: "",
                    contents = doc.getString("contents") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    examTarget = doc.getString("examTarget") ?: "",
                    googlePlayProductId = doc.getString("googlePlayProductId") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L,
                    updatedAt = doc.getLong("updatedAt") ?: 0L
                )
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching plans", e)
            emptyList()
        }
    }

    suspend fun fetchAllQuestions(): List<QuestionEntity> {
        return try {
            val snapshot = firestore?.collection("questions")?.whereEqualTo("isPremium", false)?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                try {
                    val rawTopic = doc.getString("topic") ?: ""
                    val rawSubject = doc.getString("subject") ?: ""
                    val normTopic = normalizeChapterName(rawTopic)
                    val normSubject = normalizeSubjectName(rawSubject)
                    QuestionEntity(
                        id = doc.getLong("id") ?: 0L,
                        subject = normSubject,
                        topic = normTopic,
                        difficulty = doc.getString("difficulty") ?: "Medium",
                        questionEn = doc.getString("questionEn") ?: "",
                        questionAs = doc.getString("questionAs") ?: "",
                        optionAEn = doc.getString("optionAEn") ?: "",
                        optionBEn = doc.getString("optionBEn") ?: "",
                        optionCEn = doc.getString("optionCEn") ?: "",
                        optionDEn = doc.getString("optionDEn") ?: "",
                        optionAAs = doc.getString("optionAAs") ?: "",
                        optionBAs = doc.getString("optionBAs") ?: "",
                        optionCAs = doc.getString("optionCAs") ?: "",
                        optionDAs = doc.getString("optionDAs") ?: "",
                        correctOptionIndex = doc.getLong("correctOptionIndex")?.toInt() ?: 0,
                        explanationEn = doc.getString("explanationEn") ?: "",
                        explanationAs = doc.getString("explanationAs") ?: "",
                        examCategory = doc.getString("examCategory") ?: "ADRE",
                        isPremium = doc.getBoolean("isPremium") ?: false,
                                        accessType = doc.getString("accessType") ?: (if (doc.getBoolean("isPremium") == true) "PREMIUM" else "FREE"),
                        questionType = doc.getString("questionType") ?: "Expected",
                        isReported = doc.getBoolean("isReported") ?: false,
                        cachedAt = doc.getLong("cachedAt") ?: System.currentTimeMillis(),
                        lastAccessedAt = doc.getLong("lastAccessedAt") ?: System.currentTimeMillis(),
                        version = doc.getLong("version")?.toInt() ?: 1,
                        updatedAt = doc.getLong("updatedAt") ?: 0L,
                        firebaseId = doc.getString("firebaseId") ?: doc.id
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching questions", e)
            emptyList()
        }
    }

    suspend fun fetchAllMockTests(): List<MockTestEntity> {
        return try {
            val snapshot = firestore?.collection("mock_tests")?.whereEqualTo("isPremium", false)?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                try {
                    MockTestEntity(
                        id = doc.getLong("id") ?: 0L,
                        titleEn = doc.getString("titleEn") ?: "",
                        titleAs = doc.getString("titleAs") ?: "",
                        category = doc.getString("category") ?: "ADRE",
                        durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: 45,
                        totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 10,
                        totalMarks = doc.getDouble("totalMarks")?.toFloat() ?: 10f,
                        questionMarksJson = doc.getString("questionMarksJson") ?: "{}",
                        isScheduled = doc.getBoolean("isScheduled") ?: false,
                        scheduledDate = doc.getString("scheduledDate") ?: "",
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        userScore = doc.getDouble("userScore")?.toFloat() ?: 0f,
                        userAccuracy = doc.getDouble("userAccuracy")?.toFloat() ?: 0f,
                        userRank = doc.getLong("userRank")?.toInt() ?: 0,
                        userPercentile = doc.getDouble("userPercentile")?.toFloat() ?: 0f,
                        isPublished = doc.getBoolean("isPublished") ?: true,
                        testType = doc.getString("testType") ?: "Full-Length",
                        subjectOrChapter = doc.getString("subjectOrChapter") ?: "",
                        negativeMarking = doc.getString("negativeMarking") ?: "0.25 Marks",
                        difficulty = doc.getString("difficulty") ?: "Medium",
                        isPremium = doc.getBoolean("isPremium") ?: false,
                                        accessType = doc.getString("accessType") ?: (if (doc.getBoolean("isPremium") == true) "PREMIUM" else "FREE"),
                        inProgress = doc.getBoolean("inProgress") ?: false,
                        questionsAnswered = doc.getLong("questionsAnswered")?.toInt() ?: 0,
                        timeRemainingSeconds = doc.getLong("timeRemainingSeconds")?.toInt() ?: 0,
                        questionIds = doc.getString("questionIds") ?: "",
                        markPerQuestion = doc.getDouble("markPerQuestion")?.toFloat() ?: 1f
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching mock tests", e)
            emptyList()
        }
    }

    suspend fun fetchAllStudyNotes(): List<StudyNoteEntity> {
        return try {
            val snapshot = firestore?.collection("study_notes")?.whereEqualTo("isPremium", false)?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                try {
                    StudyNoteEntity(
                        id = doc.getLong("id") ?: 0L,
                        subject = doc.getString("subject") ?: "",
                        topic = doc.getString("topic") ?: "",
                        titleEn = doc.getString("titleEn") ?: "",
                        titleAs = doc.getString("titleAs") ?: "",
                        contentEn = doc.getString("contentEn") ?: "",
                        contentAs = doc.getString("contentAs") ?: "",
                        isBookmarked = doc.getBoolean("isBookmarked") ?: false,
                        isDownloaded = doc.getBoolean("isDownloaded") ?: false,
                        readTimeMinutes = doc.getLong("readTimeMinutes")?.toInt() ?: 5,
                        isPremium = doc.getBoolean("isPremium") ?: false,
                                        accessType = doc.getString("accessType") ?: (if (doc.getBoolean("isPremium") == true) "PREMIUM" else "FREE")
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching study notes", e)
            emptyList()
        }
    }

    suspend fun fetchAllExamUpdates(): List<ExamUpdateEntity> {
        return try {
            val snapshot = firestore?.collection("exam_updates")?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                try {
                    ExamUpdateEntity(
                        id = doc.getLong("id") ?: 0L,
                        examName = doc.getString("examName") ?: "",
                        category = doc.getString("category") ?: "",
                        titleEn = doc.getString("titleEn") ?: "",
                        titleAs = doc.getString("titleAs") ?: "",
                        updateDate = doc.getString("updateDate") ?: "",
                        detailEn = doc.getString("detailEn") ?: "",
                        detailAs = doc.getString("detailAs") ?: "",
                        officialLink = doc.getString("officialLink") ?: "https://assam.gov.in",
                        isImportantNotice = doc.getBoolean("isImportantNotice") ?: false
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching exam updates", e)
            emptyList()
        }
    }


    suspend fun fetchPremiumQuestions(): List<QuestionEntity> {
        val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
        return try {
            val result = functions.getHttpsCallable("getPremiumContent").call().await()
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
            val result = functions.getHttpsCallable("getPremiumContent").call().await()
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
                        accessType = doc["accessType"] as? String ?: (if (doc["isPremium"] as? Boolean == true) "PREMIUM" else "FREE")
                        
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
            val result = functions.getHttpsCallable("getPremiumContent").call().await()
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


    // Real-time Observers using addSnapshotListener

    fun observeQuestions(): Flow<List<QuestionEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("questions").whereEqualTo("isPremium", false)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logListenerError("Error observing questions", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    val rawTopic = doc.getString("topic") ?: ""
                                    val rawSubject = doc.getString("subject") ?: ""
                                    val normTopic = normalizeChapterName(rawTopic)
                                    val normSubject = normalizeSubjectName(rawSubject)
                                    QuestionEntity(
                                        id = doc.getLong("id") ?: 0L,
                                        subject = normSubject,
                                        topic = normTopic,
                                        difficulty = doc.getString("difficulty") ?: "Medium",
                                        questionEn = doc.getString("questionEn") ?: "",
                                        questionAs = doc.getString("questionAs") ?: "",
                                        optionAEn = doc.getString("optionAEn") ?: "",
                                        optionBEn = doc.getString("optionBEn") ?: "",
                                        optionCEn = doc.getString("optionCEn") ?: "",
                                        optionDEn = doc.getString("optionDEn") ?: "",
                                        optionAAs = doc.getString("optionAAs") ?: "",
                                        optionBAs = doc.getString("optionBAs") ?: "",
                                        optionCAs = doc.getString("optionCAs") ?: "",
                                        optionDAs = doc.getString("optionDAs") ?: "",
                                        correctOptionIndex = doc.getLong("correctOptionIndex")?.toInt() ?: 0,
                                        explanationEn = doc.getString("explanationEn") ?: "",
                                        explanationAs = doc.getString("explanationAs") ?: "",
                                        examCategory = doc.getString("examCategory") ?: "ADRE",
                                        isPremium = doc.getBoolean("isPremium") ?: false,
                                        accessType = doc.getString("accessType") ?: (if (doc.getBoolean("isPremium") == true) "PREMIUM" else "FREE"),
                        questionType = doc.getString("questionType") ?: "Expected",
                                        isReported = doc.getBoolean("isReported") ?: false,
                                        cachedAt = doc.getLong("cachedAt") ?: System.currentTimeMillis(),
                                        lastAccessedAt = doc.getLong("lastAccessedAt") ?: System.currentTimeMillis(),
                                        version = doc.getLong("version")?.toInt() ?: 1,
                                        updatedAt = doc.getLong("updatedAt") ?: 0L,
                                        firebaseId = doc.getString("firebaseId") ?: doc.id
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up questions observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeMockTests(): Flow<List<MockTestEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("mock_tests").whereEqualTo("isPremium", false)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logListenerError("Error observing mock tests", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    MockTestEntity(
                                        id = doc.getLong("id") ?: 0L,
                                        titleEn = doc.getString("titleEn") ?: "",
                                        titleAs = doc.getString("titleAs") ?: "",
                                        category = doc.getString("category") ?: "ADRE",
                                        durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: 45,
                                        totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 10,
                                        totalMarks = doc.getDouble("totalMarks")?.toFloat() ?: 10f,
                        questionMarksJson = doc.getString("questionMarksJson") ?: "{}",
                                        isScheduled = doc.getBoolean("isScheduled") ?: false,
                                        scheduledDate = doc.getString("scheduledDate") ?: "",
                                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                                        userScore = doc.getDouble("userScore")?.toFloat() ?: 0f,
                                        userAccuracy = doc.getDouble("userAccuracy")?.toFloat() ?: 0f,
                                        userRank = doc.getLong("userRank")?.toInt() ?: 0,
                                        userPercentile = doc.getDouble("userPercentile")?.toFloat() ?: 0f,
                                        isPublished = doc.getBoolean("isPublished") ?: true,
                                        testType = doc.getString("testType") ?: "Full-Length",
                                        subjectOrChapter = doc.getString("subjectOrChapter") ?: "",
                                        negativeMarking = doc.getString("negativeMarking") ?: "0.25 Marks",
                                        difficulty = doc.getString("difficulty") ?: "Medium",
                                        isPremium = doc.getBoolean("isPremium") ?: false,
                                        accessType = doc.getString("accessType") ?: (if (doc.getBoolean("isPremium") == true) "PREMIUM" else "FREE"),
                        inProgress = doc.getBoolean("inProgress") ?: false,
                                        questionsAnswered = doc.getLong("questionsAnswered")?.toInt() ?: 0,
                                        timeRemainingSeconds = doc.getLong("timeRemainingSeconds")?.toInt() ?: 0,
                                        questionIds = doc.getString("questionIds") ?: "",
                                        markPerQuestion = doc.getDouble("markPerQuestion")?.toFloat() ?: 1f
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up mock tests observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeStudyNotes(): Flow<List<StudyNoteEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("study_notes").whereEqualTo("isPremium", false)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logListenerError("Error observing study notes", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    StudyNoteEntity(
                                        id = doc.getLong("id") ?: 0L,
                                        subject = doc.getString("subject") ?: "",
                                        topic = doc.getString("topic") ?: "",
                                        titleEn = doc.getString("titleEn") ?: "",
                                        titleAs = doc.getString("titleAs") ?: "",
                                        contentEn = doc.getString("contentEn") ?: "",
                                        contentAs = doc.getString("contentAs") ?: "",
                                        isBookmarked = doc.getBoolean("isBookmarked") ?: false,
                                        isDownloaded = doc.getBoolean("isDownloaded") ?: false,
                                        readTimeMinutes = doc.getLong("readTimeMinutes")?.toInt() ?: 5,
                                        isPremium = doc.getBoolean("isPremium") ?: false,
                                        accessType = doc.getString("accessType") ?: (if (doc.getBoolean("isPremium") == true) "PREMIUM" else "FREE")
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up study notes observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeExamUpdates(): Flow<List<ExamUpdateEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("exam_updates")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logListenerError("Error observing exam updates", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    ExamUpdateEntity(
                                        id = doc.getLong("id") ?: 0L,
                                        examName = doc.getString("examName") ?: "",
                                        category = doc.getString("category") ?: "",
                                        titleEn = doc.getString("titleEn") ?: "",
                                        titleAs = doc.getString("titleAs") ?: "",
                                        updateDate = doc.getString("updateDate") ?: "",
                                        detailEn = doc.getString("detailEn") ?: "",
                                        detailAs = doc.getString("detailAs") ?: "",
                                        officialLink = doc.getString("officialLink") ?: "https://assam.gov.in",
                                        isImportantNotice = doc.getBoolean("isImportantNotice") ?: false
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up exam updates observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeBanners(): Flow<List<BannerEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("banners")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logListenerError("Error observing banners", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    BannerEntity(
                                        id = doc.getLong("id") ?: 0L,
                                        titleEn = doc.getString("titleEn") ?: "",
                                        titleAs = doc.getString("titleAs") ?: "",
                                        subtitleEn = doc.getString("subtitleEn") ?: "",
                                        subtitleAs = doc.getString("subtitleAs") ?: "",
                                        badgeText = doc.getString("badgeText") ?: "",
                                        type = doc.getString("type") ?: "INFORMATION",
                                        actionUrl = doc.getString("actionUrl") ?: "",
                                        isActive = doc.getBoolean("isActive") ?: true,
                                        imageUrl = doc.getString("imageUrl") ?: "",
                                        actionType = doc.getString("actionType") ?: "Link",
                                        offerValidity = doc.getString("offerValidity") ?: "",
                                        planPrice = doc.getString("planPrice") ?: "",
                                        discount = doc.getString("discount") ?: "",
                                        finalPrice = doc.getString("finalPrice") ?: ""
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up banners observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observePlans(): Flow<List<PlanEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("plans")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logListenerError("Error observing plans", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    val rawVal = doc.getString("planValidity") ?: doc.getString("offerValidity") ?: ""
                                    val isLifetime = doc.getBoolean("isLifetime") ?: (rawVal.equals("Lifetime", ignoreCase = true) || (doc.getString("validityType") ?: "").equals("LIFETIME", ignoreCase = true))
                                    val validityType = doc.getString("validityType") ?: if (isLifetime) "LIFETIME" else com.example.data.util.PlanValidityEngine.inferValidityType(rawVal)
                                    val validityValue = doc.getLong("validityValue")?.toInt() ?: if (isLifetime) 0 else com.example.data.util.PlanValidityEngine.inferValidityValue(rawVal)
                                    val validityLabel = doc.getString("validityLabel") ?: if (isLifetime) "Lifetime" else com.example.data.util.PlanValidityEngine.formatValidityLabel(validityType, validityValue)

                                    PlanEntity(
                                        id = doc.getLong("id")?.takeIf { it != 0L } ?: (doc.id.hashCode().toLong().let { if (it < 0) -it else it }),
                                        planName = doc.getString("planName") ?: "",
                                        planPrice = doc.getString("planPrice") ?: "",
                                        discount = doc.getString("discount") ?: "",
                                        finalPrice = doc.getString("finalPrice") ?: "",
                                        offerValidity = doc.getString("offerValidity") ?: validityLabel,
                                        planValidity = doc.getString("planValidity") ?: validityLabel,
                                        validityType = validityType,
                                        validityValue = validityValue,
                                        validityLabel = validityLabel,
                                        isLifetime = isLifetime,
                                        features = doc.getString("features") ?: "",
                                        contents = doc.getString("contents") ?: "",
                                        isActive = doc.getBoolean("isActive") ?: true,
                                        imageUrl = doc.getString("imageUrl") ?: "",
                                        examTarget = doc.getString("examTarget") ?: "",
                                        googlePlayProductId = doc.getString("googlePlayProductId") ?: "",
                                        createdAt = doc.getLong("createdAt") ?: 0L,
                                        updatedAt = doc.getLong("updatedAt") ?: 0L
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up plans observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeExams(): Flow<List<ExamEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("exams")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logListenerError("Error observing exams", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    ExamEntity(
                                        id = doc.getLong("id")?.takeIf { it != 0L } ?: (doc.id.hashCode().toLong().let { if (it < 0) -it else it }),
                                        firebaseId = doc.id,
                                        title = doc.getString("title") ?: "",
                                        subtitle = doc.getString("subtitle") ?: "",
                                        status = doc.getString("status") ?: "Active",
                                        updatedAt = doc.getLong("updatedAt") ?: 0L,
                                        version = doc.getLong("version")?.toInt() ?: 1,
                                        syncStatus = "SYNCED"
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up exams observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeSubjectsChapters(): Flow<List<SubjectChapterEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("subjects_chapters")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logListenerError("Error observing subjects chapters", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    val rawChap = doc.getString("chapter") ?: ""
                                    val rawSubj = doc.getString("subject") ?: ""
                                    val normChap = normalizeChapterName(rawChap)
                                    val normSubj = normalizeSubjectName(rawSubj)
                                    if (normChap.isBlank() || normSubj.isBlank()) {
                                        null
                                    } else {
                                        SubjectChapterEntity(
                                            id = doc.getLong("id") ?: 0L,
                                            subject = normSubj,
                                            chapter = normChap
                                        )
                                    }
                                } catch (e: Throwable) {
                                    null
                                }
                            }.distinctBy { "${it.subject.trim().lowercase()}|${it.chapter.trim().lowercase()}" }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up subjects chapters observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeFaqs(): Flow<List<FaqEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("faqs")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logListenerError("Error observing faqs", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    FaqEntity(
                                        id = doc.getLong("id") ?: 0L,
                                        questionEn = doc.getString("questionEn") ?: "",
                                        questionAs = doc.getString("questionAs") ?: "",
                                        answerEn = doc.getString("answerEn") ?: "",
                                        answerAs = doc.getString("answerAs") ?: ""
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up faqs observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observePendingRequests(): Flow<List<PendingRequestEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("pending_requests")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    PendingRequestEntity(
                                        id = doc.getLong("id") ?: (doc.id.hashCode().toLong().let { if (it < 0) -it else it }),
                                        requestType = doc.getString("requestType") ?: "",
                                        title = doc.getString("title") ?: "",
                                        description = doc.getString("description") ?: "",
                                        targetId = doc.getString("targetId") ?: "",
                                        payloadJson = doc.getString("payloadJson") ?: "",
                                        requestedBy = doc.getString("requestedBy") ?: "",
                                        timestamp = doc.getString("timestamp") ?: "",
                                        status = doc.getString("status") ?: "PENDING"
                                    )
                                } catch (e: Throwable) { null }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) { trySend(emptyList()) }
        awaitClose { listener?.remove() }
    }

    fun observeActivityLogs(): Flow<List<ActivityLogEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("activity_logs")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    ActivityLogEntity(
                                        id = doc.getLong("id") ?: (doc.id.hashCode().toLong().let { if (it < 0) -it else it }),
                                        role = doc.getString("role") ?: "",
                                        actionDetails = doc.getString("action") ?: doc.getString("details") ?: "",
                                        userEmail = doc.getString("userEmail") ?: "",
                                        timestamp = doc.getLong("timestamp") ?: 0L
                                    )
                                } catch (e: Throwable) { null }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) { trySend(emptyList()) }
        awaitClose { listener?.remove() }
    }

    fun observeNotifications(): Flow<List<NotificationEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("notifications")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    NotificationEntity(
                                        id = doc.getLong("id") ?: (doc.id.hashCode().toLong().let { if (it < 0) -it else it }),
                                        title = doc.getString("title") ?: "",
                                        body = doc.getString("body") ?: "",
                                        timestamp = doc.getString("timestamp") ?: "Just now",
                                        category = doc.getString("category") ?: "General",
                                        isRead = doc.getBoolean("isRead") ?: false
                                    )
                                } catch (e: Throwable) { null }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) { trySend(emptyList()) }
        awaitClose { listener?.remove() }
    }

    private fun docToAboutConfig(doc: com.google.firebase.firestore.DocumentSnapshot): AboutConfigEntity {
        return AboutConfigEntity(
            id = doc.getLong("id")?.toInt() ?: 1,
            appTitle = doc.getString("appTitle") ?: "Jukti",
            appSubtitleEn = doc.getString("appSubtitleEn") ?: "Test Your Knowledge",
            appSubtitleAs = doc.getString("appSubtitleAs") ?: "অসমৰ সৰ্ববৃহৎ পৰীক্ষা প্ৰস্তুতি এপ্প",
            versionText = doc.getString("versionText") ?: "Version 2026.1.0",
            missionEn = doc.getString("missionEn") ?: "Jukti is engineered to democratize competitive exam preparation for aspirants across Assam...",
            missionAs = doc.getString("missionAs") ?: "যুক্তি এপ্পৰ প্ৰধান উদ্দেশ্য হৈছে অসমৰ সকলো প্ৰতিযোগীতামূলক পৰীক্ষাৰ...",
            logoIconName = doc.getString("logoIconName") ?: "School",
            logoUrl = doc.getString("logoUrl") ?: "",
            logoUpdatedAt = doc.getLong("logoUpdatedAt") ?: 0L,
            copyrightText = doc.getString("copyrightText") ?: "Copyright © 2026 Jukti Education Portal. All rights reserved.",
            developerTagline = doc.getString("developerTagline") ?: "Designed & Developed for Assam Aspirants",
            contactEmail = doc.getString("contactEmail") ?: "juktieducation@gmail.com",
            contactPhone = doc.getString("contactPhone") ?: "+91 98765 43210",
            contactTelegram = doc.getString("contactTelegram") ?: "t.me/JuktiAssam",
            contactWhatsapp = doc.getString("contactWhatsapp") ?: "Community Group",
            adminEmails = doc.getString("adminEmails") ?: "",
            refundPolicyEn = doc.getString("refundPolicyEn") ?: "Our policy lasts 7 days...",
            refundPolicyAs = doc.getString("refundPolicyAs") ?: "আমাৰ ৰিফাণ্ড পলিচি ক্ৰয় কৰাৰ ৭ দিনৰ বাবে প্ৰযোজ্য...",
            founderName = doc.getString("founderName") ?: "Pinku Bora",
            founderTitle = doc.getString("founderTitle") ?: "Founder & Creator of Jukti",
            founderCredential = doc.getString("founderCredential") ?: "ADRE 2022 Qualifier",
            founderDescription = doc.getString("founderDescription") ?: "Jukti was created with a simple vision...",
            founderPhotoUrl = doc.getString("founderPhotoUrl") ?: "",
            founderTagline = doc.getString("founderTagline") ?: "Jukti — Test Your Knowledge.",
            privacyPolicyContent = doc.getString("privacyPolicyContent") ?: "",
            termsConditionsContent = doc.getString("termsConditionsContent") ?: "",
            playStoreUrl = doc.getString("playStoreUrl") ?: "https://ais-dev-mbq2e6ge5z4qs5wk3gkstx-397582032913.asia-southeast1.run.app"
        )
    }

    suspend fun fetchAboutConfig(): AboutConfigEntity? {
        return try {
            val db = firestore ?: return null
            val doc1 = db.collection("app_config").document("1").get().await()
            if (doc1.exists()) return docToAboutConfig(doc1)
            val docMain = db.collection("app_config").document("main_config").get().await()
            if (docMain.exists()) return docToAboutConfig(docMain)
            null
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error fetching AboutConfig", e)
            null
        }
    }

    fun observeAboutConfig(): Flow<AboutConfigEntity?> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(null)
            } else {
                listener = db.collection("app_config").document("1")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logListenerError("Error observing app_config/1", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            trySend(docToAboutConfig(snapshot))
                        }
                    }
            }
        } catch (e: Throwable) {
            trySend(null)
        }
        awaitClose { listener?.remove() }
    }

    fun observeUserProfile(email: String, explicitUid: String? = null): Flow<UserProfileEntity?> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Exception) { null }
            if (auth?.currentUser == null) {
                trySend(null)
                close()
                return@callbackFlow
            }
            val currentUid = explicitUid ?: auth.currentUser?.uid
            val trimmedEmail = email.trim().lowercase()
            val sanitizedEmailDocId = if (trimmedEmail.isNotBlank()) trimmedEmail.replace("@", "_at_").replace(".", "_dot_") else ""
            val docId = if (!currentUid.isNullOrBlank()) currentUid else sanitizedEmailDocId

            if (db == null || docId.isBlank()) {
                trySend(null)
            } else {
                listener = db.collection("users").document(docId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logListenerError("Error observing user profile", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            val foundProfile = UserProfileEntity(
                                id = snapshot.getLong("id")?.toInt() ?: 1,
                                name = snapshot.getString("name") ?: "Assam Scholar",
                                email = snapshot.getString("email") ?: email,
                                mobile = snapshot.getString("mobile") ?: "",
                                district = snapshot.getString("district") ?: "",
                                examGoal = snapshot.getString("examGoal") ?: "",
                                xp = snapshot.getLong("xp")?.toInt() ?: 0,
                                level = snapshot.getLong("level")?.toInt() ?: 1,
                                dailyStreak = snapshot.getLong("dailyStreak")?.toInt() ?: 0,
                                totalSolved = snapshot.getLong("totalSolved")?.toInt() ?: 0,
                                correctCount = snapshot.getLong("correctCount")?.toInt() ?: 0,
                                totalTimeMinutes = snapshot.getLong("totalTimeMinutes")?.toInt() ?: 0,
                                isPremium = snapshot.getBoolean("isPremium") ?: false,
                                role = snapshot.getString("role") ?: "USER",
                                firebaseProjectId = snapshot.getString("firebaseProjectId") ?: "jukti-26035",
                                joinedDate = snapshot.getString("joinedDate") ?: "Jul 2026",
                                isLoggedIn = snapshot.getBoolean("isLoggedIn") ?: true,
                                currentDeviceId = snapshot.getString("currentDeviceId") ?: "",
                                activeDeviceId = snapshot.getString("activeDeviceId") ?: "",
                                uid = currentUid ?: snapshot.getString("uid") ?: snapshot.id,
                                profileName = snapshot.getString("profileName") ?: "",
                                registrationName = snapshot.getString("registrationName") ?: "",
                                googleName = snapshot.getString("googleName") ?: ""
                            )
                            trySend(foundProfile)
                        } else {
                            trySend(null)
                        }
                    }
            }
        } catch (e: Throwable) {
            trySend(null)
        }
        awaitClose { listener?.remove() }
    }

    fun observeUserEntitlements(email: String, explicitUid: String? = null): Flow<List<EntitlementEntity>> = callbackFlow {
        var collListener: ListenerRegistration? = null
        var userDocListener: ListenerRegistration? = null
        try {
            val db = firestore
            val trimmedEmail = email.trim().lowercase()
            val sanitizedEmailDocId = if (trimmedEmail.isNotBlank()) getSanitizedUserDocId(trimmedEmail) else ""
            val currentUid = explicitUid?.trim()?.ifBlank { null }
            val resolvedUserId = sanitizedEmailDocId.ifBlank { currentUid ?: "default_user" }

            val defaultFreeEntitlement = EntitlementEntity(
                userId = resolvedUserId,
                planId = "free_plan",
                planName = "Free Plan",
                status = "ACTIVE",
                validFrom = 0L,
                validUntil = 0L,
                validityType = "LIFETIME",
                validityValue = 0,
                validityLabel = "Lifetime",
                isLifetime = true,
                benefits = "Basic Questions, Daily Tests, Syllabus Updates",
                source = "DEFAULT_FREE",
                purchaseId = "FREE_LIFETIME",
                activatedAt = 0L,
                updatedAt = System.currentTimeMillis()
            )

            if (db == null || (sanitizedEmailDocId.isBlank() && currentUid == null)) {
                trySend(listOf(defaultFreeEntitlement))
            } else {
                val primaryDocPath = if (sanitizedEmailDocId.isNotBlank()) sanitizedEmailDocId else currentUid!!

                var lastCollSnap: com.google.firebase.firestore.QuerySnapshot? = null
                var lastUserSnap: DocumentSnapshot? = null

                fun emitCombined() {
                    val parsed = mutableListOf<EntitlementEntity>()
                    if (lastCollSnap != null) {
                        for (doc in lastCollSnap!!.documents) {
                            val ent = parseEntitlementDoc(doc, resolvedUserId)
                            if (ent != null) parsed.add(ent)
                        }
                    }
                    if (lastUserSnap != null) {
                        val userEnt = parseUserDocForEntitlement(lastUserSnap!!, resolvedUserId)
                        if (userEnt != null) parsed.add(userEnt)
                    }

                    val distinctByPlan = parsed.groupBy { it.planName.lowercase(Locale.ROOT) }
                        .mapValues { entry ->
                            entry.value.maxByOrNull { ent ->
                                if (com.example.data.util.PlanValidityEngine.isEntitlementActive(ent)) {
                                    if (ent.isLifetime) Long.MAX_VALUE else ent.validUntil
                                } else 0L
                            } ?: entry.value.first()
                        }.values.toList()

                    val activePlans = distinctByPlan.filter { com.example.data.util.PlanValidityEngine.isEntitlementActive(it) }
                    if (activePlans.isNotEmpty()) {
                        trySend(activePlans)
                    } else {
                        trySend(listOf(defaultFreeEntitlement))
                    }
                }

                collListener = db.collection("users").document(primaryDocPath)
                    .collection("entitlements")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logListenerError("Error observing entitlements collection", error)
                            return@addSnapshotListener
                        }
                        lastCollSnap = snapshot
                        emitCombined()
                    }

                userDocListener = db.collection("users").document(primaryDocPath)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logListenerError("Error observing user doc for entitlement", error)
                            return@addSnapshotListener
                        }
                        lastUserSnap = snapshot
                        emitCombined()
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up user entitlements observer", e)
        }
        awaitClose {
            collListener?.remove()
            userDocListener?.remove()
        }
    }

    fun observeUserEntitlement(email: String, explicitUid: String? = null): Flow<EntitlementEntity?> =
        observeUserEntitlements(email, explicitUid).map { it.firstOrNull() }

    suspend fun deleteUserAccount(uid: String, email: String) {
        try {
            val db = firestore
            val trimmedEmail = email.trim().lowercase()
            val sanitizedEmailDocId = if (trimmedEmail.isNotBlank()) getSanitizedUserDocId(trimmedEmail) else ""

            if (db != null) {
                val docIdsToDelete = mutableSetOf<String>()
                if (uid.isNotBlank()) docIdsToDelete.add(uid)
                if (sanitizedEmailDocId.isNotBlank()) docIdsToDelete.add(sanitizedEmailDocId)

                // Also find any documents matching the email
                if (trimmedEmail.isNotBlank()) {
                    try {
                        val querySnap = db.collection("users").whereEqualTo("email", trimmedEmail).get().await()
                        querySnap.documents.forEach { docIdsToDelete.add(it.id) }
                    } catch (e: Exception) {
                        Log.w("FirebaseRepository", "Could not query users by email: ${e.message}")
                    }
                }

                for (docId in docIdsToDelete) {
                    try {
                        val userRef = db.collection("users").document(docId)
                        
                        // Delete subcollections
                        val subcollections = listOf("entitlements", "entitlement_history", "user_question_states", "purchaseRequests", "mock_attempts")
                        for (sub in subcollections) {
                            try {
                                val subDocs = userRef.collection(sub).get().await()
                                for (subDoc in subDocs.documents) {
                                    subDoc.reference.delete().await()
                                }
                            } catch (e: Exception) {
                                Log.w("FirebaseRepository", "Error cleaning subcollection $sub for user $docId", e)
                            }
                        }

                        // Mark role as DELETED first for immediate client exclusion
                        userRef.set(mapOf("role" to "DELETED", "isDeleted" to true, "email" to trimmedEmail), SetOptions.merge()).await()
                        // Then delete the document
                        userRef.delete().await()
                        Log.i("FirebaseRepository", "Successfully deleted user document $docId")
                    } catch (e: Exception) {
                        Log.e("FirebaseRepository", "Error deleting user document $docId", e)
                    }
                }
            }

            // Only delete FirebaseAuth account if the user being deleted is the CURRENT logged-in user
            val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Exception) { null }
            val currentAuthUser = auth?.currentUser
            val isCurrentLoggedInUser = currentAuthUser != null && (
                (uid.isNotBlank() && currentAuthUser.uid == uid) ||
                (trimmedEmail.isNotBlank() && currentAuthUser.email?.trim()?.equals(trimmedEmail, ignoreCase = true) == true)
            )

            if (isCurrentLoggedInUser) {
                try {
                    currentAuthUser?.delete()?.await()
                } catch (e: Exception) {
                    Log.w("FirebaseRepository", "Could not delete auth user: ${e.message}")
                }
                try {
                    auth?.signOut()
                } catch (e: Exception) {
                    Log.w("FirebaseRepository", "Error signing out after user deletion: ${e.message}")
                }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting user account from Firebase", e)
        }
    }

    suspend fun saveMockAttempt(attempt: com.example.data.local.MockAttemptEntity) {
        try {
            val db = firestore ?: return
            val map = mapOf(
                "id" to attempt.id,
                "mockTestId" to attempt.mockTestId,
                "userId" to attempt.userId,
                "timestamp" to attempt.timestamp,
                "questionIds" to attempt.questionIds,
                "userAnswersJson" to attempt.userAnswersJson,
                "score" to attempt.score,
                "totalMarks" to attempt.totalMarks,
                "accuracy" to attempt.accuracy,
                "correctCount" to attempt.correctCount,
                "totalAttempted" to attempt.totalAttempted,
                "questionMarksJson" to attempt.questionMarksJson
            )
            // Save to global mock_attempts collection
            db.collection("mock_attempts").document(attempt.id.toString()).set(map, SetOptions.merge()).await()
            // Also save under user's private subcollection if userId is valid
            if (attempt.userId.isNotBlank()) {
                db.collection("users").document(attempt.userId)
                    .collection("mock_attempts").document(attempt.id.toString())
                    .set(map, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.w("FirebaseRepository", "Could not sync mock attempt to Firestore: ${e.message}")
        }
    }

    suspend fun getMockAttemptsForMock(mockTestId: Long): List<com.example.data.local.MockAttemptEntity> {
        return try {
            val db = firestore ?: return emptyList()
            val snap = db.collection("mock_attempts").whereEqualTo("mockTestId", mockTestId).get().await()
            snap.documents.mapNotNull { doc ->
                try {
                    com.example.data.local.MockAttemptEntity(
                        id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
                        mockTestId = doc.getLong("mockTestId") ?: mockTestId,
                        userId = doc.getString("userId") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        questionIds = doc.getString("questionIds") ?: "",
                        userAnswersJson = doc.getString("userAnswersJson") ?: "{}",
                        score = doc.getDouble("score")?.toFloat() ?: 0f,
                        totalMarks = doc.getDouble("totalMarks")?.toFloat() ?: 100f,
                        accuracy = doc.getDouble("accuracy")?.toFloat() ?: 0f,
                        correctCount = doc.getLong("correctCount")?.toInt() ?: 0,
                        totalAttempted = doc.getLong("totalAttempted")?.toInt() ?: 0,
                        questionMarksJson = doc.getString("questionMarksJson") ?: "{}"
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
