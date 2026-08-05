import kotlinx.coroutines.delay

class Test {
    private var isNavigating = false
    fun navigateTo() {
        if (isNavigating) return
        isNavigating = true
        // navigate
        // delay(300)
        // isNavigating = false
    }
}
