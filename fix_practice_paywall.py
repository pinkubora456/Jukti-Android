import re

with open("app/src/main/java/com/example/ui/screens/PracticeScreen.kt", "r") as f:
    content = f.read()

# Add isUserPremium and isAdminOrOwner if they are not already collected
if "val isUserPremium by viewModel.isUserPremium.collectAsState()" not in content:
    content = content.replace("val userProfile by viewModel.userProfile.collectAsState()", "val userProfile by viewModel.userProfile.collectAsState()\n    val isUserPremium by viewModel.isUserPremium.collectAsState()\n    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()")

target = """
                            // Question Text
                            com.example.ui.components.QuestionTypeBadge(
                                questionType = currentQuestion.questionType,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            BilingualText(
                                textEn = currentQuestion.questionEn,
                                textAs = currentQuestion.questionAs,
                                language = questionLanguage,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
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
                                    language = questionLanguage,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
"""
content = content.replace(target, replacement)

target_opts = """
                            Spacer(modifier = Modifier.height(24.dp))

                            // Options List
"""

replacement_opts = """
                            Spacer(modifier = Modifier.height(24.dp))

                            if (!currentQuestion.isPremium || isUserPremium || isAdminOrOwner) {
                            // Options List
"""

target_end_opts = """
                            // Question Reporting and Stats Row
                            Row(
"""

replacement_end_opts = """
                            }
                            // Question Reporting and Stats Row
                            Row(
"""

content = content.replace(target_opts, replacement_opts)
content = content.replace(target_end_opts, replacement_end_opts)

with open("app/src/main/java/com/example/ui/screens/PracticeScreen.kt", "w") as f:
    f.write(content)
