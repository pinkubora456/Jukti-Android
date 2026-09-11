with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('val isUserPremium: StateFlow<Boolean> = combine(userProfile, isAdminOrOwner, userEntitlements, plans) { userEmail, admin, entitlements, allPlans ->',
'val isUserPremium: StateFlow<Boolean> = combine(userProfile.map { it?.email }.distinctUntilChanged(), isAdminOrOwner, userEntitlements, plans) { userEmail, admin, entitlements, allPlans ->')

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)
