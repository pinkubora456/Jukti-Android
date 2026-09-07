with open("app/src/main/java/com/example/ui/screens/WorkspaceScreen.kt", "r") as f:
    content = f.read()

# Make sure Manage Guidance banner exists
if 'AdminFeatureCard(title = "Manage Guidance",' not in content:
    print("Manage Guidance banner not found in WorkspaceScreen, let's add it")
    import re
    content = re.sub(
        r'(AdminFeatureCard\(title = "Manage Q-Bank",.*?\}\))',
        r'\1\n                    AdminFeatureCard(title = "Manage Guidance", icon = Icons.Default.Explore, color = BrandAccent) { viewModel.navigateTo(Screen.MANAGE_GUIDANCE) }',
        content,
        flags=re.DOTALL
    )
    with open("app/src/main/java/com/example/ui/screens/WorkspaceScreen.kt", "w") as f:
        f.write(content)
else:
    print("Manage Guidance banner exists")
