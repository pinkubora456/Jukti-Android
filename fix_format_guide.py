import re

path = "app/src/main/java/com/example/ui/screens/BatchImportQuestionScreen.kt"
with open(path, "r") as f:
    content = f.read()

pattern = re.compile(r"Column\(\n\s+modifier = Modifier\n\s+\.fillMaxWidth\(\)\n\s+\.heightIn\(max = 420\.dp\),\n\s+verticalArrangement = Arrangement\.spacedBy\(10\.dp\)\n\s+\) \{([\s\S]*?)\s*Text\(\n\s+text = \"Tips:")
match = pattern.search(content)

if match:
    replacement = '''
                    if (selectedContentType == "Reading Comprehension") {
                        Text(
                            text = "For Reading Comprehension, ensure you provide passageId. The passage text is only needed in the first row. Other rows can just use passageId.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Reading Comprehension Format (14 columns):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "passageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Your CSV file can use either the full 17-column schema or the simplified 7-column schema:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Standard Format (17 columns):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = CsvQuestionParser.SAMPLE_CSV_HEADER,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Simplified Format (7 columns):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Question, Option A, Option B, Option C, Option D, Correct Answer (A/B/C/D), Subject",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
'''
    content = content[:match.start(1)] + replacement + content[match.end(1):]
    with open(path, "w") as f:
        f.write(content)
