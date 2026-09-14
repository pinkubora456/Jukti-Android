path = "app/src/main/java/com/example/util/CsvQuestionParser.kt"
with open(path, "r") as f:
    content = f.readlines()

start_idx = -1
end_idx = -1
for i, line in enumerate(content):
    if 'fun getSampleCsvTemplate(contentType: String' in line:
        start_idx = i
    if start_idx != -1 and i > start_idx and '}' in line:
        # found first closing brace for if block
        pass
    if start_idx != -1 and i > start_idx + 1 and line.strip() == '}':
        end_idx = i + 1
        break

if start_idx != -1 and end_idx != -1:
    replacement = [
        '    fun getSampleCsvTemplate(contentType: String = "Normal MCQ"): String {\n',
        '        return if (contentType == "Reading Comprehension") {\n',
        '            "passageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n" +\n',
        '            "\\"passage1\\",\\"Read this passage...\\",\\"Who was the first King?\\",\\"Sukaphaa\\",\\"Sutephaa\\",\\"Subinphaa\\",\\"Sudangphaa\\",\\"A\\",\\"Explanation\\",\\"\\",\\"Assam History\\",\\"Ahom Kingdom\\",\\"ADRE HS 2024\\",\\"Medium\\""\n',
        '        } else {\n',
        '            "${SAMPLE_CSV_HEADER}\\n${SAMPLE_CSV_ROW_1}\\n${SAMPLE_CSV_ROW_2}"\n',
        '        }\n',
        '    }\n'
    ]
    content = content[:start_idx] + replacement + content[end_idx:]
    with open(path, "w") as f:
        f.writelines(content)
