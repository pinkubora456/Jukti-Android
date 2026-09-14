import re

path = "app/src/main/java/com/example/util/CsvQuestionParser.kt"
with open(path, "r") as f:
    c = f.read()

c = re.sub(
    r'(fun getSampleCsvTemplate.*?)(return if.*?\{)(.*?\}) else \{',
    r'''\1\2
            "passageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n" +
            "\\"passage1\\",\\"Read this passage...\\",\\"Who was the first King?\\",\\"Sukaphaa\\",\\"Sutephaa\\",\\"Subinphaa\\",\\"Sudangphaa\\",\\"A\\",\\"Explanation\\",\\"\\",\\"Assam History\\",\\"Ahom Kingdom\\",\\"ADRE HS 2024\\",\\"Medium\\""
        } else {''',
    c, flags=re.DOTALL
)

c = c.replace(
    '"$SAMPLE_CSV_HEADER$SAMPLE_CSV_ROW_1$SAMPLE_CSV_ROW_2"',
    '"$SAMPLE_CSV_HEADER\\n$SAMPLE_CSV_ROW_1\\n$SAMPLE_CSV_ROW_2"'
)

with open(path, "w") as f:
    f.write(c)

