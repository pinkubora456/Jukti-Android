with open("app/src/main/java/com/example/data/local/JuktiDatabase.kt", "r") as f:
    content = f.read()

import re
# We need to make sure guidanceDao() function is removed if it points to old Dao, but I completely replaced GuidanceDao interface with the new one.
# So `abstract fun guidanceDao(): GuidanceDao` is still valid and needed!

if "abstract fun guidanceDao(): GuidanceDao" not in content:
    print("WARNING: guidanceDao() not found in JuktiDatabase.kt")
else:
    print("guidanceDao() is present.")
