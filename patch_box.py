import os

filepath = 'app/src/main/java/com/example/MainActivity.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

search = """                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {"""

replace = """                ) { innerPadding ->
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
                    ) {"""

content = content.replace(search, replace)
with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
