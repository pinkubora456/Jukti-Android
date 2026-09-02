import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    old_refresh = """            val questions = firebaseRepository.fetchAllQuestions()
            questionDao.deletePremiumQuestions()
            val freeQuestions = questions.filter { !it.isPremium }
            val premiumQs = questions.filter { it.isPremium }
            if (freeQuestions.isNotEmpty()) {
                questionDao.insertAll(freeQuestions)
            }
            _premiumQuestions.value = premiumQs

            val mocks = firebaseRepository.fetchAllMockTests()
            mockTestDao.deletePremiumMockTests()
            val freeMocks = mocks.filter { !it.isPremium }
            val premiumMs = mocks.filter { it.isPremium }
            if (freeMocks.isNotEmpty()) {
                mockTestDao.insertAll(freeMocks)
            }
            _premiumMockTests.value = premiumMs

            val notes = firebaseRepository.fetchAllStudyNotes()
            studyNoteDao.deletePremiumStudyNotes()
            val freeNotes = notes.filter { !it.isPremium }
            val premiumNs = notes.filter { it.isPremium }
            if (freeNotes.isNotEmpty()) {
                studyNoteDao.insertAll(freeNotes)
            }
            _premiumStudyNotes.value = premiumNs"""

    new_refresh = """            val questions = firebaseRepository.fetchAllQuestions()
            questionDao.deletePremiumQuestions()
            if (questions.isNotEmpty()) {
                questionDao.insertAll(questions)
            }

            val mocks = firebaseRepository.fetchAllMockTests()
            mockTestDao.deletePremiumMockTests()
            if (mocks.isNotEmpty()) {
                mockTestDao.insertAll(mocks)
            }

            val notes = firebaseRepository.fetchAllStudyNotes()
            studyNoteDao.deletePremiumStudyNotes()
            if (notes.isNotEmpty()) {
                studyNoteDao.insertAll(notes)
            }

            if (isAdminOrOwner || effectiveEntitlement != null) {
                refreshPremiumContent()
            } else {
                clearPremiumCache()
            }"""

    if old_refresh in content:
        content = content.replace(old_refresh, new_refresh)
        with open(filepath, "w") as f:
            f.write(content)
        print("Fixed refreshDataFromFirebase")
    else:
        print("Could not find old_refresh")

fix_file("app/src/main/java/com/example/data/repository/JuktiRepository.kt")
