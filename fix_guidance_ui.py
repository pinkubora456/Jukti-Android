import re

with open('app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt', 'r') as f:
    content = f.read()

# Replace the two buttons with a single sync button
old_buttons = """                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSyncCloud,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Publish to Cloud", style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(
                            onClick = onRefreshCloud,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Refresh Cloud", style = MaterialTheme.typography.labelMedium)
                        }
                    }"""

new_buttons = """                    Button(
                        onClick = { 
                            onSyncCloud()
                            onRefreshCloud()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guidance Data Sync", style = MaterialTheme.typography.labelMedium)
                    }"""

content = content.replace(old_buttons, new_buttons)

# Remove the numbers from the banner cards
content = content.replace('title = "1. PYQ Focus",', 'title = "PYQ Focus",')
content = content.replace('title = "2. Priority Topics",', 'title = "Priority Topics",')
content = content.replace('title = "3. Strength & Weakness",', 'title = "Strength & Weakness",')
content = content.replace('title = "4. Preparation Strategy",', 'title = "Preparation Strategy",')

# Also remove "Preparation Strategy" entirely if needed, but for safety I will just remove the number. Wait, the instructions say:
# "The final workspace should be clean and simple:
# Manage Guidance
# PYQ Focus ...
# Priority Topics ...
# Strength & Weakness ...
# Guidance Data Sync
# One centralized Firestore sync option"
# This implies I should replace the entire Cloud Sync Card with a banner card at the end that just says Guidance Data Sync.

with open('app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt', 'w') as f:
    f.write(content)
