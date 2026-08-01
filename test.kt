class Screen {
    companion object {
        val MANAGE_QBANK = "MANAGE_QBANK"
        val MANAGE_MOCK = "MANAGE_MOCK"
        val MANAGE_PLAN = "MANAGE_PLAN"
        val MANAGE_USER_LOG = "MANAGE_USER_LOG"
    }
}
data class WorkspaceItem(val title: String, val onClick: () -> Unit = {})
fun main() {
    val items = mutableListOf<WorkspaceItem>()
    items.addAll(
        listOf(
            WorkspaceItem("Manage Q-Bank") {
                println("Navigate to ${Screen.MANAGE_QBANK}")
            },
            WorkspaceItem("Manage Mocks") {
                println("Navigate to ${Screen.MANAGE_MOCK}")
            },
            WorkspaceItem("Manage Plans") {
                println("Navigate to ${Screen.MANAGE_PLAN}")
            },
            WorkspaceItem("Manage User Log") {
                println("Navigate to ${Screen.MANAGE_USER_LOG}")
            }
        )
    )
    for (item in items) {
        print("${item.title} -> ")
        item.onClick()
    }
}
