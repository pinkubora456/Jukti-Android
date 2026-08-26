import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

target = """                val updatedProf = currentProf.copy(isPremium = (newEntitlement.status == "ACTIVE" && !planName.equals("Free Plan", ignoreCase = true)))
                repository.updateUserProfile(updatedProf)
            }"""

replacement = """                val updatedProf = currentProf.copy(isPremium = (newEntitlement.status == "ACTIVE" && !planName.equals("Free Plan", ignoreCase = true)))
                repository.updateUserProfile(updatedProf)
                // Download premium data since entitlement changed
                refreshDataFromFirebase()
            }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
        f.write(content)
    print("Patched JuktiViewModel grantPlanToUser")
else:
    print("Could not find target in grantPlanToUser")
