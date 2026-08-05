import re

filepath = 'app/src/main/java/com/example/MainActivity.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

search = """                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                            when (screen) {"""

replace = """                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val screen = currentScreen
                        when (screen) {"""

content = content.replace(search, replace)

# Also remove the closing brace for Crossfade
search_end = """                                 Screen.MANAGE_EXAM_PATTERN_CUTOFF -> ManageExamPatternCutoffScreen(viewModel)
                                   Screen.MANAGE_BANNERS -> ManageBannersScreen(viewModel)
                                    Screen.REFUND_POLICY -> RefundPolicyScreen(viewModel)
                            }
                        }
                    }
                }
            }"""

replace_end = """                                 Screen.MANAGE_EXAM_PATTERN_CUTOFF -> ManageExamPatternCutoffScreen(viewModel)
                                   Screen.MANAGE_BANNERS -> ManageBannersScreen(viewModel)
                                    Screen.REFUND_POLICY -> RefundPolicyScreen(viewModel)
                            }
                    }
                }
            }"""
content = content.replace(search_end, replace_end)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
