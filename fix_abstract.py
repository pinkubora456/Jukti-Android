import re

with open("app/src/main/java/com/example/data/local/Daos.kt", "r") as f:
    lines = f.readlines()

out_lines = []
for i, line in enumerate(lines):
    if line.strip().startswith("suspend fun ") or line.strip().startswith("fun "):
        # Check if the line ends with { or if the next line has { (simple heuristic)
        # Actually in kotlin it's better to just check if it lacks a body.
        # But looking at Daos.kt, all the @Query, @Insert, @Update, @Delete methods don't have bodies.
        # Except the ones we added which have bodies and start with `@Transaction \n open suspend fun`
        # Wait, the ones with bodies now start with `open suspend fun`. 
        # So `suspend fun` without `open` is exactly the ones without bodies!
        # Same for `fun` (e.g. `fun getAllQuestions(): Flow<List<QuestionEntity>>`).
        
        # Let's replace 'suspend fun ' with 'abstract suspend fun ' and 'fun ' with 'abstract fun '
        # Only if it doesn't already have 'abstract ' or 'open '
        if "abstract " not in line and "open " not in line:
            line = line.replace("suspend fun ", "abstract suspend fun ")
            if "abstract " not in line: # if it didn't have suspend
                line = line.replace("fun ", "abstract fun ")
    
    out_lines.append(line)

with open("app/src/main/java/com/example/data/local/Daos.kt", "w") as f:
    f.writelines(out_lines)

