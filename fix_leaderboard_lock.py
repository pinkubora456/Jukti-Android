import re

with open("app/src/main/java/com/example/ui/screens/LeaderboardAnalyticsScreen.kt", "r") as f:
    content = f.read()

# The lock starts with `if (!isUserPremium && !isAdminOrOwner) {`
# and ends with `} else {` right before `Crossfade`
lock_pattern = r'        if \(!isUserPremium && !isAdminOrOwner\) \{.*?\} else \{\s+(Crossfade\(targetState = selectedTab, label = "TabSwitch"\) \{ tab ->.*?\}\s+)\}'
content = re.sub(lock_pattern, r'        \1', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/LeaderboardAnalyticsScreen.kt", "w") as f:
    f.write(content)
