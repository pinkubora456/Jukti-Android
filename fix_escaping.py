import re

path = "app/src/main/java/com/example/util/CsvQuestionParser.kt"
with open(path, "r") as f:
    c = f.read()

c = re.sub(
    r'"passageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty" \+',
    r'"passageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n" +',
    c
)

c = re.sub(
    r'"\$SAMPLE_CSV_HEADER\$SAMPLE_CSV_ROW_1\$SAMPLE_CSV_ROW_2"',
    r'"$SAMPLE_CSV_HEADER\\n$SAMPLE_CSV_ROW_1\\n$SAMPLE_CSV_ROW_2"',
    c
)

with open(path, "w") as f:
    f.write(c)


path2 = "app/src/main/java/com/example/ui/screens/BatchImportQuestionScreen.kt"
with open(path2, "r") as f:
    c2 = f.read()

c2 = re.sub(
    r'"Paste CSV content here\.\.\.e\.g\.passageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\"passage1\\"',
    r'"Paste CSV content here...\\ne.g.\\npassageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n\\"passage1\\"',
    c2
)

c2 = re.sub(
    r'"Paste CSV content here\.\.\.e\.g\.statement,statementAssamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\"Who',
    r'"Paste CSV content here...\\ne.g.\\nstatement,statementAssamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n\\"Who',
    c2
)

with open(path2, "w") as f:
    f.write(c2)

