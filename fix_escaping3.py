import re

path = "app/src/main/java/com/example/util/CsvQuestionParser.kt"
with open(path, "r") as f:
    c = f.read()

c = c.replace(
    '"passageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty" +',
    '"passageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n" +'
)

c = c.replace(
    '"$SAMPLE_CSV_HEADER$SAMPLE_CSV_ROW_1$SAMPLE_CSV_ROW_2"',
    '"$SAMPLE_CSV_HEADER\\n$SAMPLE_CSV_ROW_1\\n$SAMPLE_CSV_ROW_2"'
)

with open(path, "w") as f:
    f.write(c)

path2 = "app/src/main/java/com/example/ui/screens/BatchImportQuestionScreen.kt"
with open(path2, "r") as f:
    c2 = f.read()

c2 = re.sub(
    r'placeholder = \{\s*Text\(\s*text = if \(selectedContentType == "Reading Comprehension"\) \{[\s\S]*?\},',
    '''placeholder = {
                                Text(
                                    text = if (selectedContentType == "Reading Comprehension") {
                                        "Paste CSV content here...\\ne.g.\\npassageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n\\"passage1\\",\\"Read this passage...\\",\\"Who was the first King?\\",\\"Sukaphaa\\",\\"Sutephaa\\",\\"Subinphaa\\",\\"Sudangphaa\\",\\"A\\",\\"Explanation\\",\\"\\",\\"Assam History\\",\\"Ahom Kingdom\\",\\"ADRE HS 2024\\",\\"Medium\\""
                                    } else {
                                        "Paste CSV content here...\\ne.g.\\nstatement,statementAssamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n\\"Who was the first King of the Ahom Kingdom?\\",\\"আহোম ৰাজ্যৰ প্ৰথম ৰজা কোন আছিল?\\",\\"Sukaphaa\\",\\"চ্যুকাফা\\",\\"Sutephaa\\",\\"চ্যুটেফা\\",\\"Subinphaa\\",\\"চুবিনফা\\",\\"Sudangphaa\\",\\"চুডাংফা\\",\\"A\\",\\"Sukaphaa founded the Ahom Kingdom in medieval Assam.\\",\\"চ্যুকাফাই মধ্যযুগীয় অসমত আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল。\\",\\"Assam History\\",\\"Ahom Kingdom\\",\\"ADRE HS 2024\\",\\"Medium\\""
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },''',
    c2
)

# And also there was a double comma: },, 
c2 = c2.replace("},,", "},")

with open(path2, "w") as f:
    f.write(c2)

