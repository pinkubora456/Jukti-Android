import re
path = "app/src/main/java/com/example/util/CsvQuestionParser.kt"
with open(path, "r") as f:
    content = f.read()

pattern_cols = r"(val colTargetExams = findHeaderCol\(\"targetexams\", \"targetexam\", \"examcategory\", \"exam\"\))"
replacement_cols = r"\1\n        val colPassageId = findHeaderCol(\"passageid\", \"passage_id\")\n        val colPassage = findHeaderCol(\"passage\", \"passage_text\")"
if re.search(pattern_cols, content):
    content = re.sub(pattern_cols, replacement_cols, content)

# I should use an external script to rewrite validateAndParseQuestions carefully.
# Or just rewrite the whole validateAndParseQuestions function.
