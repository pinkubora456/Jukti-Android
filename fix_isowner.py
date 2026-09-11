with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

import re

isowner_regex = r'val isOwner: StateFlow<Boolean> = userProfile\.map \{ it\?\.email \}\.distinctUntilChanged\(\)\.map \{ email ->\n        val emailTrimmed = email\?\.trim\(\)\?\.lowercase\(\) \?: ""\n        emailTrimmed == "juktieducation@gmail.com" \|\| emailTrimmed == "borapinku151@gmail.com"\n    \}\n        val role = profile\?\.role\?\.uppercase\(java\.util\.Locale\.ROOT\) \?: ""\n        val isRoleOwner = role == "OWNER"\n        val email = profile\?\.email\?\.trim\(\)\?\.lowercase\(\) \?: ""\n        val isOwnerEmail = email == "juktieducation@gmail.com" \|\| email == "borapinku151@gmail.com"\n        isRoleOwner \|\| isOwnerEmail\n    \}\.stateIn\(\n        viewModelScope, SharingStarted\.Eagerly, false\n    \)'

isowner_replacement = r'val isOwner: StateFlow<Boolean> = userProfile.map { it?.role to it?.email }.distinctUntilChanged().map { (userRole, userEmail) ->\n        val role = userRole?.uppercase(java.util.Locale.ROOT) ?: ""\n        val isRoleOwner = role == "OWNER"\n        val email = userEmail?.trim()?.lowercase() ?: ""\n        val isOwnerEmail = email == "juktieducation@gmail.com" || email == "borapinku151@gmail.com"\n        isRoleOwner || isOwnerEmail\n    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)'

content = re.sub(isowner_regex, isowner_replacement, content)

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)
