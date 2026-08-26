import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

# Replace userEntitlement, with userEntitlements, in combine arrays
content = re.sub(
    r'(\s+)userEntitlement,\n(\s+)',
    r'\1userEntitlements,\n\2',
    content
)

# Replace the array extraction inside combine
content = re.sub(
    r'val entitlement = args\[1\] as\? EntitlementEntity',
    r'@Suppress("UNCHECKED_CAST")\n        val entitlements = args[1] as? List<EntitlementEntity> ?: emptyList()',
    content
)

# Fix calculateAccessibleCounts which currently passes 'entitlements = entitlements,' because I partly changed it, let's make sure it's correct.
# Actually my python script changed 'entitlement = entitlement' to 'entitlements = entitlements' previously.

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
