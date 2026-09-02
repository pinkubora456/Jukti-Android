import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
        
    old_code = """                                            Box(contentAlignment = Alignment.Center) {
                                                if (isSubmitted && isCorrect) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                }
                     else if (isSubmitted && isSelected && !isCorrect) {
                                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                     else {
                                                    Text(
                                                        text = optionLetter,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                      
                
            """
            
    # let's just use regex to replace this whole block
    # since whitespace might be weird, we match the box start to the spacer
    
    start_str = "Box(contentAlignment = Alignment.Center) {"
    end_str = "Spacer(modifier = Modifier.width(10.dp))"
    
    start_idx = content.find(start_str)
    end_idx = content.find(end_str, start_idx)
    
    if start_idx != -1 and end_idx != -1:
        new_block = """Box(contentAlignment = Alignment.Center) {
                                                if (isSubmitted && isCorrect) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                } else if (isSubmitted && isSelected && !isCorrect) {
                                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                } else {
                                                    Text(
                                                        text = optionLetter,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                        }
                                        """
        content = content[:start_idx] + new_block + content[end_idx:]
        with open(filepath, 'w') as f:
            f.write(content)
        print("Replaced box block")

fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
