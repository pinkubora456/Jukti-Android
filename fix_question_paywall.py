import re

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

# Find the question display block
target = """
                    // Question Text
                    com.example.ui.components.QuestionTypeBadge(
                        questionType = currentQuestion.questionType,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    BilingualText(
                        textEn = currentQuestion.questionEn,
                        textAs = currentQuestion.questionAs,
                        language = language,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
"""

replacement = """
                    // Question Text
                    if (currentQuestion.isPremium && !isUserPremium && !isAdminOrOwner) {
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
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("This question is only available to Premium users.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.showPaywall() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                    Text("Unlock Premium")
                                }
                            }
                        }
                    } else {
                        com.example.ui.components.QuestionTypeBadge(
                            questionType = currentQuestion.questionType,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        BilingualText(
                            textEn = currentQuestion.questionEn,
                            textAs = currentQuestion.questionAs,
                            language = language,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("Could not find Question text block in McqStudyScreen")
    
with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
    f.write(content)
