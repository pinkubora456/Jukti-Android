import re

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'r') as f:
    content = f.read()

# Priority Topics
replacement1 = """                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(topic.subject + " - " + topic.chapter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = statusColor)
                            Text("PYQ Avg: ${String.format("%.1f", topic.pyqAvg)} | Your Accuracy: ${topic.accuracy?.let { String.format("%.0f%%", it) } ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(topic.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        TextButton(onClick = { onPractice(topic.subject, topic.chapter) }) {
                            Text("Practice")
                        }
                    }"""

pattern1 = r'\s*Row\(modifier = Modifier\.fillMaxWidth\(\), verticalAlignment = Alignment\.CenterVertically\) \{\s*Column\(modifier = Modifier\.weight\(1f\)\) \{\s*Text\(topic\.priorityLabel\.uppercase\(\), style = MaterialTheme\.typography\.labelSmall, fontWeight = FontWeight\.Bold, color = statusColor\)\s*Text\(topic\.subject \+ " - " \+ topic\.chapter, fontWeight = FontWeight\.Bold, style = MaterialTheme\.typography\.bodyMedium\)\s*Text\("PYQ Avg: \$\{String\.format\("%.1f", topic\.pyqAvg\)\} | Your Accuracy: \$\{topic\.accuracy\?\.let \{ String\.format\("%.0f%%", it\) \} \?: "N/A"\}", style = MaterialTheme\.typography\.bodySmall, color = MaterialTheme\.colorScheme\.onSurfaceVariant\)\s*Text\(topic\.reason, style = MaterialTheme\.typography\.bodySmall, color = MaterialTheme\.colorScheme\.onSurfaceVariant\)\s*\}\s*TextButton\(onClick = \{ onPractice\(topic\.subject, topic\.chapter\) \}\) \{\s*Text\("Practice"\)\s*\}\s*\}'

content = re.sub(pattern1, replacement1, content, flags=re.MULTILINE)

# Strength & Weakness
replacement2 = """                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.subject + " - " + item.chapter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = statusColor)
                            Text("Avg PYQ: ${String.format("%.1f", item.pyqAvg)} | Accuracy: ${item.accuracy?.let { String.format("%.0f%%", it) } ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        TextButton(onClick = { onImprove(item.subject, item.chapter) }) {
                            Text("Practice")
                        }
                    }"""

pattern2 = r'\s*Row\(modifier = Modifier\.fillMaxWidth\(\), verticalAlignment = Alignment\.CenterVertically\) \{\s*Column\(modifier = Modifier\.weight\(1f\)\) \{\s*Text\(item\.subject \+ " - " \+ item\.chapter, fontWeight = FontWeight\.Bold, style = MaterialTheme\.typography\.bodyMedium\)\s*Text\("Avg PYQ: \$\{String\.format\("%.1f", item\.pyqAvg\)\} | Accuracy: \$\{item\.accuracy\?\.let \{ String\.format\("%.0f%%", it\) \} \?: "N/A"\}", style = MaterialTheme\.typography\.bodySmall, color = MaterialTheme\.colorScheme\.onSurfaceVariant\)\s*\}\s*Row\(verticalAlignment = Alignment\.CenterVertically, modifier = Modifier\.background\(statusColor\.copy\(alpha = 0\.1f\), RoundedCornerShape\(16\.dp\)\)\.padding\(horizontal = 8\.dp, vertical = 4\.dp\)\.clickable \{ onImprove\(item\.subject, item\.chapter\) \}\) \{\s*Icon\(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier\.size\(16\.dp\)\)\s*Spacer\(modifier = Modifier\.width\(4\.dp\)\)\s*Text\(item\.statusLabel, color = statusColor, style = MaterialTheme\.typography\.labelSmall, fontWeight = FontWeight\.Bold\)\s*\}\s*\}'

content = re.sub(pattern2, replacement2, content, flags=re.MULTILINE)

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'w') as f:
    f.write(content)
