import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

missing_classes = """
data class PlanPurchaseSummaryItem(
    val planName: String,
    val purchasedUserCount: Int
)

data class PlanPurchaseSummaryState(
    val isLoading: Boolean = false,
    val items: List<PlanPurchaseSummaryItem> = emptyList(),
    val error: String? = null
)
"""

if "data class PlanPurchaseSummaryState" not in content:
    idx = content.rfind("companion object {")
    if idx != -1:
        content = content[:idx] + missing_classes + "\n    " + content[idx:]

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
