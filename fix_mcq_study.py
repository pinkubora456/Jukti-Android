import re

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

# Remove the paywall trigger
paywall_trigger = """            if (!isUserPremium && !isAdminOrOwner && studiedQuestionsCountInSession >= 25) {
                viewModel.showPaywall()
            }"""
if paywall_trigger in content:
    content = content.replace(paywall_trigger, "")

# Remove the showLimitModal variable and dialog
show_limit_modal_var = "    var showLimitModal by remember { mutableStateOf(false) }"
if show_limit_modal_var in content:
    content = content.replace(show_limit_modal_var, "")

modal_dialog = r'        // Free Plan Limit Dialog.*?if \(showLimitModal\) \{.*?\AlertDialog\(.*?\}\s+\)'
content = re.sub(modal_dialog, '', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
    f.write(content)
