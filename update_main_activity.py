import re

main_file = "app/src/main/java/com/example/MainActivity.kt"
with open(main_file, "r") as f:
    content = f.read()

# Add GUIDANCE to navigation
if "Screen.GUIDANCE ->" not in content:
    content = content.replace("Screen.WORKSPACE -> WorkspaceScreen(viewModel)", 
                              "Screen.WORKSPACE -> WorkspaceScreen(viewModel)\n                    Screen.GUIDANCE -> GuidanceScreen(viewModel)")

with open(main_file, "w") as f:
    f.write(content)
print("Updated MainActivity.kt")
