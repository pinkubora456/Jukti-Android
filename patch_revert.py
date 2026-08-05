import os

filepath = 'app/src/main/java/com/example/MainActivity.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

search = """                ) { innerPadding ->
                    var isTransitioning by remember { mutableStateOf(false) }
                    LaunchedEffect(currentScreen) {
                        isTransitioning = true
                        delay(400)
                        isTransitioning = false
                    }
                    Box(modifier = Modifier
                        .padding(innerPadding)
                        .pointerInput(isTransitioning) {
                            if (isTransitioning) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            }
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val screen = currentScreen"""

replace = """                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->"""

content = content.replace(search, replace)

# also fix the closing brace
content = content.replace("                                Screen.CREATE_MOCK -> CreateMockScreen(viewModel)\n                            }\n                        }\n                    }", "                                Screen.CREATE_MOCK -> CreateMockScreen(viewModel)\n                            }\n                        }\n                    }")
# wait, the closing braces for Crossfade need to be matched.
with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
