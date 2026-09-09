import re

with open('app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt', 'r') as f:
    content = f.read()

# Replace ManageGuidanceLanding with the correct structure
new_landing = """@Composable
fun ManageGuidanceLanding(
    onNavigate: (ManageGuidanceSection) -> Unit,
    onSyncCloud: () -> Unit,
    onRefreshCloud: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ManageSectionBannerCard(
                title = "PYQ Focus",
                subtitle = "View entries, Add PYQ Focus Entry, Edit, Delete",
                icon = Icons.Default.History,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onNavigate(ManageGuidanceSection.PYQ_FOCUS) }
            )
        }
        item {
            ManageSectionBannerCard(
                title = "Priority Topics",
                subtitle = "View entries, Add, Edit, Delete",
                icon = Icons.Default.TrackChanges,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onNavigate(ManageGuidanceSection.PRIORITY_TOPICS) }
            )
        }
        item {
            ManageSectionBannerCard(
                title = "Strength & Weakness",
                subtitle = "View entries, Add, Edit, Delete",
                icon = Icons.Default.FitnessCenter,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onNavigate(ManageGuidanceSection.STRENGTH_WEAKNESS) }
            )
        }
        
        // Guidance Data Sync
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    onSyncCloud()
                    onRefreshCloud()
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Guidance Data Sync",
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(10.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Guidance Data Sync",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "One centralized Firestore sync option",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}"""

content = re.sub(
    r'@Composable\nfun ManageGuidanceLanding\(.*?\}\n\}\n',
    new_landing + '\n',
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt', 'w') as f:
    f.write(content)
