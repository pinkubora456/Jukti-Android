import re

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "r") as f:
    text = f.read()

# For questions
text = text.replace(
    'db.collection("questions")\n                    .addSnapshotListener',
    'db.collection("questions").whereEqualTo("isPremium", false)\n                    .addSnapshotListener'
)
text = text.replace(
    'firestore?.collection("questions")?.get()?.await()',
    'firestore?.collection("questions")?.whereEqualTo("isPremium", false)?.get()?.await()'
)

# For mock tests
text = text.replace(
    'db.collection("mock_tests")\n                    .addSnapshotListener',
    'db.collection("mock_tests").whereEqualTo("isPremium", false)\n                    .addSnapshotListener'
)
text = text.replace(
    'firestore?.collection("mock_tests")?.get()?.await()',
    'firestore?.collection("mock_tests")?.whereEqualTo("isPremium", false)?.get()?.await()'
)

# For study notes
text = text.replace(
    'db.collection("study_notes")\n                    .addSnapshotListener',
    'db.collection("study_notes").whereEqualTo("isPremium", false)\n                    .addSnapshotListener'
)
text = text.replace(
    'firestore?.collection("study_notes")?.get()?.await()',
    'firestore?.collection("study_notes")?.whereEqualTo("isPremium", false)?.get()?.await()'
)

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "w") as f:
    f.write(text)

