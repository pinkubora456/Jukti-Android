import re

filepath = 'app/src/main/java/com/example/MainActivity.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Make sure imports are there
if "import androidx.compose.ui.input.pointer.pointerInput" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", 
                              "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.input.pointer.pointerInput\nimport androidx.compose.ui.input.pointer.PointerEventPass\nimport kotlinx.coroutines.delay\nimport androidx.compose.runtime.LaunchedEffect\nimport androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.setValue\nimport androidx.compose.runtime.remember")


search = """                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val screen = currentScreen"""

replace = """                ) { innerPadding ->
                    var isTransitioning by remember { mutableStateOf(false) }
                    LaunchedEffect(currentScreen) {
                        isTransitioning = true
                        delay(500) // Block clicks for 500ms after navigation
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
                        val screen = currentScreen"""

content = content.replace(search, replace)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
