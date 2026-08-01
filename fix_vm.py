import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

# Remove all single-line `fun addPlan` and `fun deletePlan`
content = re.sub(r'\s*fun addPlan\(plan: PlanEntity, onComplete: \(\) -> Unit\) \{ viewModelScope\.launch \{ repository\.insertPlan\(plan\); onComplete\(\) \} \}\n*', '\n', content)
content = re.sub(r'\s*fun deletePlan\(plan: PlanEntity\) \{ viewModelScope\.launch \{ repository\.deletePlan\(plan\) \} \}\n*', '\n', content)

# Remove the multi-line ones at the very end to avoid duplicates
content = re.sub(r'\s*fun addPlan\(plan: PlanEntity, onComplete: \(\) -> Unit\) \{\s*viewModelScope\.launch \{\s*repository\.insertPlan\(plan\)\s*onComplete\(\)\s*\}\s*\}\s*fun deletePlan\(plan: PlanEntity\) \{\s*viewModelScope\.launch \{\s*repository\.deletePlan\(plan\)\s*\}\s*\}\s*\}$', '\n}', content)


methods = """
    fun addPlan(plan: PlanEntity, onComplete: () -> Unit) { 
        viewModelScope.launch { 
            repository.insertPlan(plan)
            onComplete() 
        } 
    }

    fun deletePlan(plan: PlanEntity) { 
        viewModelScope.launch { 
            repository.deletePlan(plan) 
        } 
    }
}"""
content = content.rstrip()
if content.endswith("}"):
    content = content[:-1] + methods

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
