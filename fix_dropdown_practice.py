import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    bad_block = """                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = chapter)
          
                                        },
                                }
                                onClick = {"""

    good_block = """                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = chapter)
                                    }
                                },
                                onClick = {"""

    content = content.replace(bad_block, good_block)
    with open(filepath, 'w') as f:
        f.write(content)

fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
