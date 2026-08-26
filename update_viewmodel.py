import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

# Replace _userEntitlement definition
content = re.sub(
    r'private val _userEntitlement = MutableStateFlow<EntitlementEntity\?\(null\)>',
    r'private val _userEntitlements = MutableStateFlow<List<EntitlementEntity>>(emptyList())',
    content
)
content = content.replace(
    "private val _userEntitlement = MutableStateFlow<EntitlementEntity?>(null)",
    "private val _userEntitlements = MutableStateFlow<List<EntitlementEntity>>(emptyList())"
)

content = content.replace(
    "val userEntitlement: StateFlow<EntitlementEntity?> = _userEntitlement.asStateFlow()",
    "val userEntitlements: StateFlow<List<EntitlementEntity>> = _userEntitlements.asStateFlow()"
)

# Replace validateEntitlement
old_validate = """    fun validateEntitlement(entitlement: EntitlementEntity?, currentTime: Long = getTrustedTime()): Boolean {
        return com.example.data.util.PlanValidityEngine.isEntitlementActive(entitlement, currentTime)
    }"""
new_validate = """    fun validateEntitlements(entitlements: List<EntitlementEntity>?, currentTime: Long = getTrustedTime()): Boolean {
        val effective = com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(entitlements, plans.value, currentTime)
        return effective.isPremium
    }"""
content = content.replace(old_validate, new_validate)

# Replace isSpecificPlanActive
old_specific = """    fun isSpecificPlanActive(plan: com.example.data.local.PlanEntity): Boolean {
        val entitlement = userEntitlement.value
        val now = getTrustedTime()
        if (entitlement != null && com.example.data.util.PlanValidityEngine.isEntitlementActive(entitlement, now)) {
            val matchesId = entitlement.planId == plan.id.toString() || entitlement.planId.equals(plan.planName, ignoreCase = true)
            val matchesName = entitlement.planName.equals(plan.planName, ignoreCase = true)
            return matchesId || matchesName
        }
        return false
    }"""
new_specific = """    fun isSpecificPlanActive(plan: com.example.data.local.PlanEntity): Boolean {
        val entitlements = userEntitlements.value
        val now = getTrustedTime()
        val effective = com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(entitlements, plans.value, now)
        return effective.activePlans.any { entitlement ->
            entitlement.planId == plan.id.toString() || entitlement.planId.equals(plan.planName, ignoreCase = true) || entitlement.planName.equals(plan.planName, ignoreCase = true)
        }
    }"""
content = content.replace(old_specific, new_specific)

# Replace isUserPremium
old_is_premium = """    val isUserPremium: StateFlow<Boolean> = combine(userProfile, isAdminOrOwner, userEntitlement) { profile, admin, entitlement ->
        val email = profile?.email?.trim()?.lowercase() ?: ""
        val isOwner = email == "juktieducation@gmail.com"
        if (isOwner || admin) {
            true
        } else {
            com.example.data.util.PlanValidityEngine.isEntitlementActive(entitlement, getTrustedTime())
        }
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )"""
new_is_premium = """    val isUserPremium: StateFlow<Boolean> = combine(userProfile, isAdminOrOwner, userEntitlements, plans) { profile, admin, entitlements, allPlans ->
        val email = profile?.email?.trim()?.lowercase() ?: ""
        val isOwner = email == "juktieducation@gmail.com"
        if (isOwner || admin) {
            true
        } else {
            com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(entitlements, allPlans, getTrustedTime()).isPremium
        }
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )"""
content = content.replace(old_is_premium, new_is_premium)

# Replace combine flows for accessible stuff
content = content.replace(
    "combine(userProfile, mocks, plans, userEntitlement, isAdminOrOwner)",
    "combine(userProfile, mocks, plans, userEntitlements, isAdminOrOwner)"
)
content = content.replace(
    "{ profile, mocks, allPlans, entitlement, args ->",
    "{ profile, mocks, allPlans, entitlements, args ->"
)
content = content.replace(
    "entitlement = entitlement,",
    "entitlements = entitlements,"
)

content = content.replace(
    "combine(userProfile, notes, plans, userEntitlement, isAdminOrOwner)",
    "combine(userProfile, notes, plans, userEntitlements, isAdminOrOwner)"
)
content = content.replace(
    "{ profile, notes, allPlans, entitlement, args ->",
    "{ profile, notes, allPlans, entitlements, args ->"
)

content = content.replace(
    "combine(userProfile, questions, plans, userEntitlement, isAdminOrOwner)",
    "combine(userProfile, questions, plans, userEntitlements, isAdminOrOwner)"
)
content = content.replace(
    "{ profile, qs, allPlans, entitlement, args ->",
    "{ profile, qs, allPlans, entitlements, args ->"
)

# Update getUserEntitlement in observeUserData
old_observe = """                    repository.getUserEntitlement(sanitizedDocId, uid, email).collectLatest { ent ->
                        _userEntitlement.value = ent
                    }"""
new_observe = """                    repository.getUserEntitlements(sanitizedDocId, uid, email).collectLatest { ents ->
                        _userEntitlements.value = ents
                    }"""
content = content.replace(old_observe, new_observe)

content = content.replace("_userEntitlement.value = null", "_userEntitlements.value = emptyList()")
content = content.replace("_userEntitlement.value = newEntitlement", "_userEntitlements.value = _userEntitlements.value + newEntitlement")

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
