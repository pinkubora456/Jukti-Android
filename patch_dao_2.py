import re

with open("app/src/main/java/com/example/data/local/Daos.kt", "r") as f:
    content = f.read()

target = """    @Query("SELECT * FROM subscription_plans ORDER BY id DESC")
    fun getAllPlans(): Flow<List<PlanEntity>>"""

replacement = """    @Query("SELECT * FROM subscription_plans ORDER BY id DESC")
    fun getAllPlans(): Flow<List<PlanEntity>>
    
    @Query("SELECT * FROM subscription_plans ORDER BY id DESC")
    suspend fun getAllPlansDirect(): List<PlanEntity>"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/data/local/Daos.kt", "w") as f:
        f.write(content)
    print("Patched Daos.kt")
else:
    print("Could not find target in Daos.kt")
