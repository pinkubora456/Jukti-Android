import re

with open("app/src/main/java/com/example/data/local/Daos.kt", "r") as f:
    daos = f.read()

daos = re.sub(r"@Transaction\s+suspend fun", "@Transaction\n    open suspend fun", daos)

with open("app/src/main/java/com/example/data/local/Daos.kt", "w") as f:
    f.write(daos)

