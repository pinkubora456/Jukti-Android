import re
import glob

files = ["app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "app/src/main/java/com/example/ui/screens/GlobalSearchScreen.kt", "app/src/main/java/com/example/ui/screens/HomeScreen.kt"]

for filepath in files:
    with open(filepath, "r") as f:
        content = f.read()

    # Find the block where QuestionStudyCard is called
    target = r"QuestionStudyCard\([\s\S]*?onReportClick.*?\n\s*\)"
    
    def replacer(match):
        text = match.group(0)
        if "isUserPremium =" not in text:
            # We need to insert isUserPremium, isAdminOrOwner, and onUnlockClick before onBookmarkToggle
            # Let's just insert it after bookmarkedIds
            text = text.replace("onBookmarkToggle =", "isUserPremium = isUserPremium,\n                            isAdminOrOwner = isAdminOrOwner,\n                            onUnlockClick = { viewModel.showPaywall() },\n                            onBookmarkToggle =")
        return text
    
    new_content = re.sub(target, replacer, content)
    
    with open(filepath, "w") as f:
        f.write(new_content)
