
file_path = '/app/applet/app/src/main/java/com/example/ui/screens/EditPlanScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

broken_part = """                Row(
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {"""
                
fixed_part = """                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {"""

new_content = content.replace(broken_part, fixed_part)
with open(file_path, 'w') as f:
    f.write(new_content)
