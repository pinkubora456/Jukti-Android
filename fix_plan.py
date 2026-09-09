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
    # let's insert it at the very bottom, outside the class
    content = content + "\n\n" + missing_classes

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)

