import re

with open("app/src/main/java/com/example/data/util/PlanValidityEngine.kt", "r") as f:
    content = f.read()

# Fix activePaidEntitlements to exclude free plans
old_active = "val activePaidEntitlements = userEnts.filter { isEntitlementActive(it, currentTime) }"
new_active = 'val activePaidEntitlements = userEnts.filter { isEntitlementActive(it, currentTime) && !it.planName.equals("Free Plan", ignoreCase = true) && !it.planId.equals("free_plan", ignoreCase = true) }'

if old_active in content:
    content = content.replace(old_active, new_active)
else:
    print("Could not find activePaidEntitlements declaration.")

with open("app/src/main/java/com/example/data/util/PlanValidityEngine.kt", "w") as f:
    f.write(content)
