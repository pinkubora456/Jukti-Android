path = "app/src/main/java/com/example/util/CsvQuestionParser.kt"
with open(path, "r") as f:
    content = f.read()

# Let's count { and }
open_braces = content.count("{")
close_braces = content.count("}")
print("Open:", open_braces, "Close:", close_braces)
