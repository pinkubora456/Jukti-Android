import re
path = "app/src/main/java/com/example/util/CsvQuestionParser.kt"
with open(path, "r") as f:
    content = f.read()

# I will find the first "private fun parseCorrectOption" and remove it if there are two.
# Wait, let's just see where validateAndParseQuestions is.
print("Count of parseCorrectOption:", content.count("fun parseCorrectOption"))
print("Count of validateAndParseQuestions:", content.count("fun validateAndParseQuestions"))
