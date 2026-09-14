path = "app/src/main/java/com/example/data/local/Entities.kt"
with open(path, "r") as f:
    content = f.read()

content = content.replace('\\"normal\\"', '"normal"')
content = content.replace('\\"\\"', '""')

with open(path, "w") as f:
    f.write(content)
print("Fixed Entities.kt")
