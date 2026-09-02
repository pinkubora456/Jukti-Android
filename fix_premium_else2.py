import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    bad_pattern = r"                                        Button\(onClick = \{ viewModel\.showPaywall\(\) \}, colors = ButtonDefaults\.buttonColors\(containerColor = MaterialTheme\.colorScheme\.error\)\) \{\n                                            Text\(\"Unlock Premium\"\)\n\s*\n\s*\n\s*\n                                        \}\n                                    \}\n                                \}\n                            \}\n                                \} else \{"
    good_pattern = r"                                        Button(onClick = { viewModel.showPaywall() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {\n                                            Text(\"Unlock Premium\")\n                                        }\n                                    }\n                                }\n                            } else {"

    content = re.sub(bad_pattern, good_pattern, content)

    with open(filepath, 'w') as f:
        f.write(content)

fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
