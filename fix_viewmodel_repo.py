import re
path = "app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt"
with open(path, "r") as f:
    content = f.read()

# Remove the incorrectly placed rcPassageDao()
content = content.replace("        database.examDao(),\n        database.rcPassageDao(),", "        database.examDao(),")

# Insert it in the correct place, after entitlementHistoryDao()
pattern = r"(database\.entitlementHistoryDao\(\),\n\s*com\.example\.data\.repository\.FirebaseSyncManager\(database\))"
if re.search(pattern, content):
    content = re.sub(pattern, r"database.entitlementHistoryDao(),\n        database.rcPassageDao(),\n        com.example.data.repository.FirebaseSyncManager(database)", content)
    with open(path, "w") as f:
        f.write(content)
    print("Fixed JuktiViewModel constructor placement")
