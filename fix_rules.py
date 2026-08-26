import re

with open("firestore.rules", "r") as f:
    text = f.read()

def replace_rule(collection):
    global text
    
    pattern = r"match /" + collection + r"/\{([^}]+)\} \{\s*allow read: if true;\s*allow write: if isAdminOrOwner\(\);\s*\}"
    replacement = r"match /" + collection + r"/{\1} {\n      allow read: if resource == null || resource.data.isPremium == false || !('isPremium' in resource.data);\n      allow write: if isAdminOrOwner();\n    }"
    
    text = re.sub(pattern, replacement, text)

replace_rule("questions")
replace_rule("mock_tests")
replace_rule("study_notes")

with open("firestore.rules", "w") as f:
    f.write(text)

