import re

with open("app/src/main/java/com/example/ui/screens/EditPlanScreen.kt", "r") as f:
    content = f.read()

new_logic = """
            val plans by viewModel.plans.collectAsState()
            
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (plans.isEmpty()) {
                    item {
                        Text("No plans created yet.")
                    }
                } else {
                    items(plans) { plan ->
                        PlanManageCard(
                            plan = plan,
                            onDelete = { viewModel.deletePlan(plan) }
                        )
                    }
                }
            }
        }
    }
}
"""

content = re.sub(r'            LazyColumn\(.*?\}\s*\}\s*\}\s*\}', new_logic, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/EditPlanScreen.kt", "w") as f:
    f.write(content)
