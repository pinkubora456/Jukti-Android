import re

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'r') as f:
    content = f.read()

replacement = """@Composable
fun PyqFocusSection(pyqData: List<PyqFocusEntity>, onPractice: (String) -> Unit) {
    val groupedBySubject = pyqData.groupBy { it.subject }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        groupedBySubject.forEach { (subject, chapters) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, bottom = 8.dp)) {
                        Text("Chapter", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text("PYQs", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        Text("Exams", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        Text("Avg", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                    
                    chapters.sortedByDescending { if (it.examsCovered > 0) it.pyqCount.toFloat() / it.examsCovered else 0f }.forEach { chapterData ->
                        val avg = if (chapterData.examsCovered > 0) chapterData.pyqCount.toFloat() / chapterData.examsCovered else 0f
                        
                        val (statusColor, containerColor) = when {
                            avg >= 2.0f -> Pair(Color(0xFFC62828), Color(0xFFFFEBEE)) // Light Red bg, Dark Red text
                            avg >= 1.0f -> Pair(Color(0xFFE65100), Color(0xFFFFF3E0)) // Light Orange bg, Dark Orange text
                            else -> Pair(Color(0xFF424242), Color(0xFFF5F5F5)) // Light Gray bg, Dark Gray text
                        }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onPractice(chapterData.chapter) },
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(chapterData.chapter, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = statusColor)
                                Text("${chapterData.pyqCount}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = statusColor)
                                Text("${chapterData.examsCovered}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = statusColor)
                                Text(String.format("%.1f", avg), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = statusColor)
                            }
                        }
                    }
                }
            }
        }
    }
}"""

pattern = r'@Composable\s*fun PyqFocusSection\(pyqData: List<PyqFocusEntity>, onPractice: \(String\) -> Unit\) \{.*?(?=@Composable\s*fun PriorityTopicsSection)'

new_content = re.sub(pattern, replacement + "\n\n", content, flags=re.MULTILINE | re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'w') as f:
    f.write(new_content)
