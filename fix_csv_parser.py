path = "app/src/main/java/com/example/util/CsvQuestionParser.kt"
with open(path, "r") as f:
    content = f.read()

import re
pattern = re.compile(r"fun getSampleCsvTemplate.*?\}\n    \}", re.DOTALL)
replacement = '''fun getSampleCsvTemplate(contentType: String = "Normal MCQ"): String {
        return if (contentType == "Reading Comprehension") {
            "passageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n" +
            "\\"passage1\\",\\"Read this passage...\\",\\"Who was the first King?\\",\\"Sukaphaa\\",\\"Sutephaa\\",\\"Subinphaa\\",\\"Sudangphaa\\",\\"A\\",\\"Explanation\\",\\"\\",\\"Assam History\\",\\"Ahom Kingdom\\",\\"ADRE HS 2024\\",\\"Medium\\""
        } else {
            "$SAMPLE_CSV_HEADER\\n$SAMPLE_CSV_ROW_1\\n$SAMPLE_CSV_ROW_2"
        }
    }'''

content = pattern.sub(replacement, content)
with open(path, "w") as f:
    f.write(content)

