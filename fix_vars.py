import re

def add_vars(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    if "val isUserPremium by viewModel.isUserPremium.collectAsState()" not in content:
        content = content.replace("val language by viewModel.language.collectAsState()", "val language by viewModel.language.collectAsState()\n    val isUserPremium by viewModel.isUserPremium.collectAsState()\n    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()")

    with open(filepath, "w") as f:
        f.write(content)

add_vars("app/src/main/java/com/example/ui/screens/GlobalSearchScreen.kt")
add_vars("app/src/main/java/com/example/ui/screens/HomeScreen.kt")
