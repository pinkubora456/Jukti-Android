import re

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'r') as f:
    content = f.read()

# Let's fix the layout of the first row inside the Card to ensure it doesn't overflow.
# Find the text block for the subject.
subject_text = """                                        Text(
                                            text = question.subject,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )"""
subject_text_fixed = """                                        Text(
                                            text = question.subject,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )"""
content = content.replace(subject_text, subject_text_fixed)

# Find the Make Free/Make Premium button and remove it to save space
make_free_btn = """                                        Spacer(modifier = Modifier.width(4.dp))
                                        OutlinedButton(
                                            onClick = { questionToToggleAccess = question },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(
                                                text = if (question.isPremium) "Make Free" else "Make Premium",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }"""
content = content.replace(make_free_btn, "")

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'w') as f:
    f.write(content)
