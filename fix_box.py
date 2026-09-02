import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
        
    bad_block = """                                    Box(contentAlignment = Alignment.Center) {
                                        if (isCorrect) {
                                            Icon(Icons.Default.Check, contentDescription = "Correct Answer", tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
             else if (isSelected && !isCorrect) {
                                            Icon(Icons.Default.Close, contentDescription = "Incorrect Option", tint = Color.White, modifier = Modifier.size(18.dp))
             } else {
                                            Text(
                                                text = optionLetter,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
            """

    good_block = """                                    Box(contentAlignment = Alignment.Center) {
                                        if (isCorrect) {
                                            Icon(Icons.Default.Check, contentDescription = "Correct Answer", tint = Color.White, modifier = Modifier.size(18.dp))
                                        } else if (isSelected && !isCorrect) {
                                            Icon(Icons.Default.Close, contentDescription = "Incorrect Option", tint = Color.White, modifier = Modifier.size(18.dp))
                                        } else {
                                            Text(
                                                text = optionLetter,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
            """
    
    # Just do a broad regex replace
    start = "Box(contentAlignment = Alignment.Center) {"
    end = "color = MaterialTheme.colorScheme.onPrimaryContainer\n                                            )"
    
    s = content.find(start)
    e = content.find(end, s)
    if s != -1 and e != -1:
        # We need to find the exact end block. 
        # Actually it's easier to just use string replace without formatting exact whitespace
        pass

fix("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
