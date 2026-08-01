import re

with open("app/src/main/java/com/example/ui/screens/CreatePlanScreen.kt", "r") as f:
    content = f.read()

# Replace variables
new_vars = """
    // Content & Benefits Lists
    var contentsList by remember { mutableStateOf(mutableStateListOf<String>()) }
    var featuresList by remember { mutableStateOf(mutableStateListOf<String>()) }
    
    // Inputs for adding content/features
    var mockTestExam by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    var mockTestLimit by remember { mutableStateOf("") }
    
    var customFeatureInput by remember { mutableStateOf("") }
"""

content = re.sub(r'    // Validity.*?var currentAffairsIncluded by remember \{ mutableStateOf\(false\) \}', new_vars, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/CreatePlanScreen.kt", "w") as f:
    f.write(content)
