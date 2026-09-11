with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

import re

# Fix isAdminOrOwner
admin_regex = r'val isAdminOrOwner: StateFlow<Boolean> = combine\(userProfile.map \{ it\?\.role to it\?\.email \}\.distinctUntilChanged\(\), aboutConfig\) \{ profile, config ->\s*val role = profile\?\.role\?\.uppercase\(java\.util\.Locale\.ROOT\) \?: ""\s*val isRoleAdminOrOwner = role == "ADMIN" \|\| role == "OWNER"\s*val email = profile\?\.email\?\.trim\(\)\?\.lowercase\(\) \?: ""'

admin_replacement = r'val isAdminOrOwner: StateFlow<Boolean> = combine(userProfile.map { it?.role to it?.email }.distinctUntilChanged(), aboutConfig) { (userRole, userEmail), config ->\n        val role = userRole?.uppercase(java.util.Locale.ROOT) ?: ""\n        val isRoleAdminOrOwner = role == "ADMIN" || role == "OWNER"\n        val email = userEmail?.trim()?.lowercase() ?: ""'
content = re.sub(admin_regex, admin_replacement, content)

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)
