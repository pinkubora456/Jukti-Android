
file_path = '/app/applet/app/src/main/java/com/example/ui/screens/PremiumPlansScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# The string to remove:
duplicated_substring = '            val isSpecificActive = viewModel.isSpecificPlanActive(plan)            FeaturedPlanBanner(                plan = plan,                isPlanActive = isSpecificActive,                onBuyClick = {                    coroutineScope.launch {                        val (canBuy, reasonMsg) = viewModel.validatePurchaseEligibility(plan)                        if (!canBuy) {                            Toast.makeText(context, reasonMsg, Toast.LENGTH_LONG).show()                        } else {                            if (activity != null) {                                selectedPlan = plan                                billingManager.buyPlan(                                    activity = activity,                                    planId = plan.id.toString(),                                    planName = plan.planName,                                    explicitProductId = plan.googlePlayProductId,                                    planValidity = plan.planValidity.ifBlank { "1 year" }                                )                            } else {                                Toast.makeText(context, "Activity reference not available for Play Billing.", Toast.LENGTH_SHORT).show()                            }                        }                    }                },                modifier = Modifier.padding(vertical = 8.dp)            )        }'

# Replace only once
new_content = content.replace(duplicated_substring, '', 1)

with open(file_path, 'w') as f:
    f.write(new_content)
