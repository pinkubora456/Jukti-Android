import re

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'r') as f:
    content = f.read()

# For PyqFocusSection
replacement1 = """                        val (baseColor, bgColor) = when {
                            avg >= 2.0f -> Pair(Color(0xFFE53935), Color(0xFFE53935).copy(alpha = 0.1f)) // Red
                            avg >= 1.0f -> Pair(Color(0xFFFB8C00), Color(0xFFFB8C00).copy(alpha = 0.1f)) // Orange
                            else -> Pair(Color(0xFF757575), Color(0xFF757575).copy(alpha = 0.1f)) // Gray
                        }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onPractice(chapterData.chapter) },
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = BorderStroke(1.dp, baseColor.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(chapterData.chapter, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("${chapterData.pyqCount}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                                Text("${chapterData.examsCovered}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                                Text(String.format("%.1f", avg), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = baseColor)
                            }
                        }"""

pattern1 = r'\s*val \(statusColor, containerColor\) = when \{\s*avg >= 2\.0f -> Pair\(Color\(0xFFC62828\), Color\(0xFFFFEBEE\)\).*?Text\(String\.format\("%.1f", avg\), modifier = Modifier\.weight\(1f\), style = MaterialTheme\.typography\.bodySmall, fontWeight = FontWeight\.Bold, textAlign = TextAlign\.Center, color = statusColor\)\s*\}\s*\}'

content = re.sub(pattern1, replacement1, content, flags=re.MULTILINE | re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'w') as f:
    f.write(content)
