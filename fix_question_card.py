import re

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

# 1. Add parameters to QuestionStudyCard
target_sig = """fun QuestionStudyCard(
    question: QuestionEntity,
    language: AppLanguage,
    bookmarkedIds: Set<Long>,
    onBookmarkToggle: () -> Unit,
    onLikeToggle: () -> Unit,
    onReportClick: () -> Unit,
    onHideClick: (() -> Unit)? = null
) {"""
replacement_sig = """fun QuestionStudyCard(
    question: QuestionEntity,
    language: AppLanguage,
    bookmarkedIds: Set<Long>,
    isUserPremium: Boolean = false,
    isAdminOrOwner: Boolean = false,
    onUnlockClick: () -> Unit = {},
    onBookmarkToggle: () -> Unit,
    onLikeToggle: () -> Unit,
    onReportClick: () -> Unit,
    onHideClick: (() -> Unit)? = null
) {"""
content = content.replace(target_sig, replacement_sig)

# 2. Add Paywall placeholder inside the card
target_body = """            com.example.ui.components.QuestionTypeBadge(
                questionType = question.questionType,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            BilingualText(
                textEn = question.questionEn,
                textAs = question.questionAs,
                language = language,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )"""

replacement_body = """            if (question.isPremium && !isUserPremium && !isAdminOrOwner) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Premium Content", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Premium Question", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onUnlockClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                            Text("Unlock Premium")
                        }
                    }
                }
            } else {
                com.example.ui.components.QuestionTypeBadge(
                    questionType = question.questionType,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                BilingualText(
                    textEn = question.questionEn,
                    textAs = question.questionAs,
                    language = language,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }"""
content = content.replace(target_body, replacement_body)

# 3. Wrap expanded options
target_opts = """            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))"""
replacement_opts = """            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                if (!question.isPremium || isUserPremium || isAdminOrOwner) {"""

target_end_opts = """                // Explanation
                if (question.explanationEn.isNotBlank() || question.explanationAs.isNotBlank()) {"""
replacement_end_opts = """                } // end of premium check
                
                // Explanation
                if (question.explanationEn.isNotBlank() || question.explanationAs.isNotBlank()) {"""

content = content.replace(target_opts, replacement_opts)
content = content.replace(target_end_opts, replacement_end_opts)

# Wrap Explanation too
target_exp = """                // Explanation
                if (question.explanationEn.isNotBlank() || question.explanationAs.isNotBlank()) {"""
replacement_exp = """                // Explanation
                if ((!question.isPremium || isUserPremium || isAdminOrOwner) && (question.explanationEn.isNotBlank() || question.explanationAs.isNotBlank())) {"""
content = content.replace(target_exp, replacement_exp)

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
    f.write(content)
