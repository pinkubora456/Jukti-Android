import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val path = Paths.get("app/src/main/java/com/example/MainActivity.kt")
    var content = String(Files.readAllBytes(path))
    val search = """                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {"""
    
    val replace = """                ) { innerPadding ->
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
    Files.write(path, content.toByteArray())
}
