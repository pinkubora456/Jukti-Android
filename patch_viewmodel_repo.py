import re
path = "app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt"
with open(path, "r") as f:
    content = f.read()

pattern = r"(database\.examDao\(\),)"
if re.search(pattern, content):
    content = re.sub(pattern, r"\1\n        database.rcPassageDao(),", content)
    with open(path, "w") as f:
        f.write(content)
    print("Patched JuktiViewModel.kt (repo creation)")
else:
    # Let's try matching something closer to what was modified in Repo.
    # We put rcPassageDao before syncManager in Repo. So it should be before syncManager here.
    pattern2 = r"(database\.entitlementHistoryDao\(\),\n\s*com\.example\.data\.repository\.FirebaseSyncManager\(database\))"
    if re.search(pattern2, content):
        content = re.sub(pattern2, r"database.entitlementHistoryDao(),\n        database.rcPassageDao(),\n        com.example.data.repository.FirebaseSyncManager(database)", content)
        with open(path, "w") as f:
            f.write(content)
        print("Patched JuktiViewModel.kt via syncmanager match")
