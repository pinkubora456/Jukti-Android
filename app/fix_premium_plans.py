
import os

file_path = '/app/applet/app/src/main/java/com/example/ui/screens/PremiumPlansScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Define the new forEach block clearly
new_foreach = """activePlans.forEach { plan ->
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
        }"""

# Identify the start and end of the duplicated part to remove
# This is tricky in flattened text. Let's find the `activePlans.forEach { plan ->` and the end of the `FeaturedPlanBanner`
import re
# The flattened text is one long string.
# Let's search for the whole duplicated block including the forEach

# I know it contains `activePlans.forEach { plan -> val isSpecificActive = viewModel.isSpecificPlanActive(plan);`
# and ends with `modifier = Modifier.padding(vertical = 8.dp)) ) }` or similar.

# Let's use a broad regex
pattern = re.compile(r'activePlans\.forEach \{ plan ->.*?modifier = Modifier\.padding\(vertical = 8\.dp\)\) \}\}')

# Actually, I'll just look for the `activePlans.forEach` and replace until `Spacer`
# This might be risky.

# Re-read: The file content has `activePlans.forEach` then the duplicated code.
# The content is actually:
# `activePlans.forEach { plan -> val isSpecificActive = ...; ... } val isSpecificActive = ... FeaturedPlanBanner( ... ) }`
# I want to keep the one *after* the first `forEach`.

# Let's just use string replacement for the exact problematic segment.
bad_part = 'activePlans.forEach { plan -> val isSpecificActive = viewModel.isSpecificPlanActive(plan); if (plan.googlePlayProductId == "STARTER_7_DAY") { FeaturedPlanBanner(plan = plan, isPlanActive = isSpecificActive, onBuyClick = { coroutineScope.launch { val (canBuy, reasonMsg) = viewModel.validatePurchaseEligibility(plan); if (!canBuy) { Toast.makeText(context, reasonMsg, Toast.LENGTH_LONG).show() } else { if (activity != null) { selectedPlan = plan; billingManager.buyPlan(activity = activity, planId = plan.id.toString(), planName = plan.planName, explicitProductId = plan.googlePlayProductId, planValidity = "7 days") } else { Toast.makeText(context, "Activity reference not available for Play Billing.", Toast.LENGTH_SHORT).show() } } } }, modifier = Modifier.padding(vertical = 8.dp)) }            val isSpecificActive = viewModel.isSpecificPlanActive(plan)            FeaturedPlanBanner(                plan = plan,                isPlanActive = isSpecificActive,                onBuyClick = {                    coroutineScope.launch {                        val (canBuy, reasonMsg) = viewModel.validatePurchaseEligibility(plan)                        if (!canBuy) {                            Toast.makeText(context, reasonMsg, Toast.LENGTH_LONG).show()                        } else {                            if (activity != null) {                                selectedPlan = plan                                billingManager.buyPlan(                                    activity = activity,                                    planId = plan.id.toString(),                                    planName = plan.planName,                                    explicitProductId = plan.googlePlayProductId,                                    planValidity = plan.planValidity.ifBlank { "1 year" }                                )                            } else {                                Toast.makeText(context, "Activity reference not available for Play Billing.", Toast.LENGTH_SHORT).show()                            }                        }                    }                },                modifier = Modifier.padding(vertical = 8.dp)            )        }'

# This `bad_part` is very long and might be slightly different.
# I'll use the python script to just replace the whole content of the `activePlans.forEach` block.

# Since I know the content has `val activePlans = plans.filter { it.isActive }` before it.
# I will split and rebuild.
parts = content.split('val activePlans = plans.filter { it.isActive }')
# The second part contains the loop.
loop_part = parts[1]
# Reconstruct the loop part from scratch
fixed_loop = """
        if (activePlans.isEmpty()) {
            Text("No active plans available right now.", style = MaterialTheme.typography.titleMedium)
            return@Column
        }
        activePlans.forEach { plan ->
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
        }
        Spacer(modifier = Modifier.height(16.dp))"""

# Need to find where the Spacer starts.
# The `loop_part` ends at `)            )        }        Spacer(modifier = Modifier.height(16.dp))`
# I can split by `Spacer(modifier = Modifier.height(16.dp))`

final_content = parts[0] + 'val activePlans = plans.filter { it.isActive }' + fixed_loop + loop_part.split('Spacer(modifier = Modifier.height(16.dp))')[1]

with open(file_path, 'w') as f:
    f.write(final_content)
