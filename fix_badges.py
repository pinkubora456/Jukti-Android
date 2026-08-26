import re

# 1. Fix StudyNoteListItem in StudyNotesScreen.kt
with open("app/src/main/java/com/example/ui/screens/StudyNotesScreen.kt", "r") as f:
    content = f.read()

badge_code = """
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = note.subject,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
"""

replacement_badge_code = """
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = note.subject,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (note.isPremium) {
                        Surface(
                            color = MaterialTheme.colorScheme.warningContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.warning),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Premium",
                                    tint = MaterialTheme.colorScheme.warning,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "PREMIUM",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.warning
                                )
                            }
                        }
                    }
                }
"""

if badge_code in content:
    content = content.replace(badge_code, replacement_badge_code)
else:
    print("Could not find badge code in StudyNotesScreen.kt")

with open("app/src/main/java/com/example/ui/screens/StudyNotesScreen.kt", "w") as f:
    f.write(content)

