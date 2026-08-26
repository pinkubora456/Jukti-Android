import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

target = """    fun selectMockTest(mock: MockTestEntity) {
        val isUserAdmin = isAdminOrOwner.value
        val isAccessible = isUserAdmin || accessibleMockTests.value.any { it.id == mock.id }
        if (!isAccessible || (mock.isPremium && !isUserPremium.value && !isUserAdmin)) {
            _showPremiumPaywall.value = true
            return
        }"""

replacement = """    fun selectMockTest(mock: MockTestEntity) {
        if (!canAccessMockTest(mock)) {
            _showPremiumPaywall.value = true
            return
        }"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched selectMockTest")
else:
    print("Target selectMockTest not found")


target = """    fun selectStudyNote(note: StudyNoteEntity?) {
        if (note != null) {
            val isUserAdmin = isAdminOrOwner.value
            val isAccessible = isUserAdmin || accessibleStudyNotes.value.any { it.id == note.id }
            if (!isAccessible || (note.isPremium && !isUserPremium.value && !isUserAdmin)) {
                _showPremiumPaywall.value = true
                return
            }"""

replacement = """    fun selectStudyNote(note: StudyNoteEntity?) {
        if (note != null) {
            if (!canAccessStudyNote(note)) {
                _showPremiumPaywall.value = true
                return
            }"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched selectStudyNote")
else:
    print("Target selectStudyNote not found")

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)

