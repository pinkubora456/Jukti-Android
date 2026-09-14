import re
path = "app/src/main/java/com/example/util/CsvQuestionParser.kt"
with open(path, "r") as f:
    content = f.read()

pattern = r"(fun getSampleCsvTemplate\(\): String \{[\s\S]*?\n    \})"
replacement = r"""fun getSampleCsvTemplate(contentType: String = "Normal MCQ"): String {
        return if (contentType == "Reading Comprehension") {
            "passageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\n" +
            "passage1,\"Read this passage...\",\"Who was the first King?\",\"Sukaphaa\",\"Sutephaa\",\"Subinphaa\",\"Sudangphaa\",\"A\",\"Explanation\",\"\",\"Assam History\",\"Ahom Kingdom\",\"ADRE HS 2024\",\"Medium\""
        } else {
            "$SAMPLE_CSV_HEADER\n$SAMPLE_CSV_ROW_1\n$SAMPLE_CSV_ROW_2"
        }
    }"""
if re.search(pattern, content):
    content = re.sub(pattern, replacement, content)
    print("Replaced!")
else:
    print("Not found")

with open(path, "w") as f:
    f.write(content)
