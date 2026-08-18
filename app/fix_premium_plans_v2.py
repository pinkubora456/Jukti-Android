
file_path = '/app/applet/app/src/main/java/com/example/ui/screens/PremiumPlansScreen.kt'
import os
print(f"Checking if {file_path} exists: {os.path.exists(file_path)}")

with open(file_path, 'r') as f:
    content = f.read()

# Fix the duplication by just taking the first forEach block.
# The `cat` output shows the file has only ONE line actually, it's just very long.

# I will use a simple find and replace.
# Find: `activePlans.forEach { plan -> ... } ... FeaturedPlanBanner(...) }`

# I'll just write the correct content, I have it from the earlier step.
# I'll just replace the entire content of the file.
new_content = content.split('activePlans.forEach { plan ->')[0] + """activePlans.forEach { plan ->
            val isSpecificActive = viewModel.isSpecificPlanActive(plan)
            FeaturedPlanBanner(
                plan = plan,
                isPlanActive = isSpecificActive,
                onBuyClick = {
                    coroutineScope.launch {
                        val (canBuy, reasonMsg) = viewModel.validatePurchaseEligibility(plan)
                        if (!canBuy) {
                            Toast.makeText(context, reasonMsg, Toast.LENGTH_LONG).show()
                        } else {
                            if (activity != null) {
                                selectedPlan = plan
                                val validity = if (plan.googlePlayProductId == "STARTER_7_DAY") "7 days" else plan.planValidity.ifBlank { "1 year" }
                                billingManager.buyPlan(
                                    activity = activity,
                                    planId = plan.id.toString(),
                                    planName = plan.planName,
                                    explicitProductId = plan.googlePlayProductId,
                                    planValidity = validity
                                )
                            } else {
                                Toast.makeText(context, "Activity reference not available for Play Billing.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }""" + content.split('Spacer(modifier = Modifier.height(16.dp))')[1]

with open(file_path, 'w') as f:
    f.write(new_content)
