import re

def fix_file(filepath):
    try:
        with open(filepath, "r") as f:
            content = f.read()

        old_rules = """    // Content management collections
    match /questions/{questionId} {
      allow read: if resource == null || resource.data.isPremium == false || !('isPremium' in resource.data) || isAdminOrOwner();
      allow write: if isAdminOrOwner();
    }"""
        
        new_rules = """    // Content management collections
    match /questions/{questionId} {
      allow read: if resource == null || resource.data.isPremium == false || !('isPremium' in resource.data) || isAdminOrOwner();
      allow write: if isAdminOrOwner();
      allow update: if isSignedIn() && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['isReported', 'updatedAt']) && request.resource.data.isReported == true;
    }"""

        if old_rules in content:
            content = content.replace(old_rules, new_rules)
            with open(filepath, "w") as f:
                f.write(content)
            print("Fixed rules in", filepath)
    except Exception as e:
        print("Skipped", filepath, str(e))

fix_file("firestore.rules")
fix_file("app/firestore.rules")
