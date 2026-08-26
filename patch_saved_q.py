import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

target = """@Composable
fun SavedQuestionsDialog(
    questions: List<QuestionEntity>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onToggleBookmark: (QuestionEntity) -> Unit
) {"""

replacement = """@Composable
fun SavedQuestionsDialog(
    questions: List<QuestionEntity>,
    language: AppLanguage,
    isUserPremium: Boolean = false,
    isAdminOrOwner: Boolean = false,
    onDismiss: () -> Unit,
    onToggleBookmark: (QuestionEntity) -> Unit
) {"""

content = content.replace(target, replacement)

target2 = """                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                BilingualText(
                                    textEn = q.questionEn,
                                    textAs = q.questionAs,
                                    language = language,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )"""

replacement2 = """                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                if (q.isPremium && !isUserPremium && !isAdminOrOwner) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = "Premium", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Premium Question", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                } else {
                                    BilingualText(
                                        textEn = q.questionEn,
                                        textAs = q.questionAs,
                                        language = language,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }"""

content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)

