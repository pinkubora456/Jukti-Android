with open('firestore.rules', 'r') as f:
    content = f.read()

# Fix users update rule to remove the diff check that causes issues with merge()
old_users = """    // User profiles collection
    match /users/{userId} {
      allow read: if true;
      allow create: if isSignedIn() && request.auth.uid == userId;
      allow update: if isSignedIn() && (
        isAdminOrOwner() || (
          request.auth.uid == userId &&
          !request.resource.data.diff(resource.data).affectedKeys().hasAny([
            'role', 'isPremium', 'xp', 'level', 'coins', 'subscriptionStatus', 'planId', 'paymentStatus'
          ])
        )
      );
      allow delete: if isOwner();
    }"""

new_users = """    // User profiles collection
    match /users/{userId} {
      allow read: if true;
      allow write: if isSignedIn() && request.auth.uid == userId;
    }
    
    // Question Progress (Subcollection)
    match /users/{userId}/question_progress/{progressId} {
      allow read, write: if isSignedIn() && request.auth.uid == userId;
    }

    // Sync Test
    match /firestore_sync_test/{userId} {
      allow read, write: if isSignedIn() && request.auth.uid == userId;
    }"""

content = content.replace(old_users, new_users)

with open('firestore.rules', 'w') as f:
    f.write(content)
