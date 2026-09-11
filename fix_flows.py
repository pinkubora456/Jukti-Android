with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

import re

# Fix isAdminOrOwner
admin_regex = r'val isAdminOrOwner:\s*StateFlow<Boolean>\s*=\s*combine\(userProfile,\s*aboutConfig\)'
admin_replacement = r'val isAdminOrOwner: StateFlow<Boolean> = combine(userProfile.map { it?.role to it?.email }.distinctUntilChanged(), aboutConfig)'
content = re.sub(admin_regex, admin_replacement, content)

# Fix effectiveEntitlement
entitlement_regex = r'val effectiveEntitlement:\s*StateFlow<com.example.data.util.EffectiveUserEntitlement\?>\s*=\s*combine\(\s*userProfile,\s*isAdminOrOwner,\s*userEntitlements,\s*plans\s*\)'
entitlement_replacement = r'val effectiveEntitlement: StateFlow<com.example.data.util.EffectiveUserEntitlement?> = combine(\n        userProfile.map { it?.email }.distinctUntilChanged(), isAdminOrOwner, userEntitlements, plans\n    )'
content = re.sub(entitlement_regex, entitlement_replacement, content)
content = content.replace('profile, admin, entitlements, allPlans ->\n        val email = profile?.email?.trim()?.lowercase() ?: ""', 'userEmail, admin, entitlements, allPlans ->\n        val email = userEmail?.trim()?.lowercase() ?: ""')

# Fix isOwner
isowner_regex = r'val isOwner:\s*StateFlow<Boolean>\s*=\s*userProfile\.map\s*\{\s*profile\s*->'
isowner_replacement = r'val isOwner: StateFlow<Boolean> = userProfile.map { it?.email }.distinctUntilChanged().map { userEmail ->\n        val profile = userEmail' # wait, it uses profile.email
content = re.sub(isowner_regex, r'val isOwner: StateFlow<Boolean> = userProfile.map { it?.email }.distinctUntilChanged().map { email ->\n        val emailTrimmed = email?.trim()?.lowercase() ?: ""\n        emailTrimmed == "juktieducation@gmail.com" || emailTrimmed == "borapinku151@gmail.com"\n    }', content)
# wait, removing original map content
content = re.sub(r'val isOwner: StateFlow<Boolean> = userProfile.map \{ profile ->\s*val email = profile\?\.email\?\.trim\(\)\?\.lowercase\(\) \?: ""\s*email == "juktieducation@gmail.com" \|\| email == "borapinku151@gmail.com"\s*\}\.stateIn\(viewModelScope, SharingStarted\.WhileSubscribed\(5000\), false\)', 
r'val isOwner: StateFlow<Boolean> = userProfile.map { it?.email }.distinctUntilChanged().map { email ->\n        val e = email?.trim()?.lowercase() ?: ""\n        e == "juktieducation@gmail.com" || e == "borapinku151@gmail.com"\n    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)', content)


# Fix isUserPremium
premium_regex = r'val isUserPremium:\s*StateFlow<Boolean>\s*=\s*combine\(userProfile,\s*isAdminOrOwner,\s*userEntitlements,\s*plans\)\s*\{\s*profile,\s*admin,\s*entitlements,\s*allPlans\s*->\s*val\s*email\s*=\s*profile\?\.email\?\.trim\(\)\?\.lowercase\(\)\s*\?:\s*""'
premium_replacement = r'val isUserPremium: StateFlow<Boolean> = combine(userProfile.map { it?.email }.distinctUntilChanged(), isAdminOrOwner, userEntitlements, plans) { userEmail, admin, entitlements, allPlans ->\n        val email = userEmail?.trim()?.lowercase() ?: ""'
content = re.sub(premium_regex, premium_replacement, content)

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)
