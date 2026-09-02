import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    bad = """                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = chapter)
          
                                        },
                                }
                                onClick = {
                                    val updated = if (isChecked) selectedChapters - chapter else selectedChapters + chapter
                                    onChaptersChanged(updated)
      
                                }
                            )"""
                            
    good = """                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = chapter)
                                    }
                                },
                                onClick = {
                                    val updated = if (isChecked) selectedChapters - chapter else selectedChapters + chapter
                                    onChaptersChanged(updated)
                                }
                            )"""
                            
    content = content.replace(bad, good)
    with open(filepath, 'w') as f:
        f.write(content)
        
fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
