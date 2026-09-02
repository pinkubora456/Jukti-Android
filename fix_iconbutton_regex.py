import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # The issue is:
    #                                         }
    #                                         },
    #                                         modifier = Modifier.size(36.dp)
    
    bad_pattern = r"                                        }\n                                        },\n                                        modifier = Modifier.size\(36.dp\)"
    good_pattern = r"                                        },\n                                        modifier = Modifier.size(36.dp)"
    
    content = re.sub(bad_pattern, good_pattern, content)
    
    with open(filepath, 'w') as f:
        f.write(content)

fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
