import re

path = "app/src/main/java/com/example/ui/screens/BatchImportQuestionScreen.kt"
with open(path, "r") as f:
    content = f.readlines()

start_idx = -1
end_idx = -1
for i, line in enumerate(content):
    if 'testTag("tf_paste_csv_data"),' in line:
        start_idx = i + 1
    if start_idx != -1 and i > start_idx and 'textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),' in line:
        end_idx = i
        break

if start_idx != -1 and end_idx != -1:
    replacement = [
        '                            placeholder = {\n',
        '                                Text(\n',
        '                                    text = if (selectedContentType == "Reading Comprehension") {\n',
        '                                        "Paste CSV content here...\\ne.g.\\npassageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n\\"passage1\\",\\"Read this passage...\\",\\"Who was the first King?\\",\\"Sukaphaa\\",\\"Sutephaa\\",\\"Subinphaa\\",\\"Sudangphaa\\",\\"A\\",\\"Explanation\\",\\"\\",\\"Assam History\\",\\"Ahom Kingdom\\",\\"ADRE HS 2024\\",\\"Medium\\""\n',
        '                                    } else {\n',
        '                                        "Paste CSV content here...\\ne.g.\\nstatement,statementAssamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n\\"Who was the first King of the Ahom Kingdom?\\",\\"আহোম ৰাজ্যৰ প্ৰথম ৰজা কোন আছিল?\\",\\"Sukaphaa\\",\\"চ্যুকাফা\\",\\"Sutephaa\\",\\"চ্যুটেফা\\",\\"Subinphaa\\",\\"চুবিনফা\\",\\"Sudangphaa\\",\\"চুডাংফা\\",\\"A\\",\\"Sukaphaa founded the Ahom Kingdom in medieval Assam.\\",\\"চ্যুকাফাই মধ্যযুগীয় অসমত আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল。\\",\\"Assam History\\",\\"Ahom Kingdom\\",\\"ADRE HS 2024\\",\\"Medium\\""\n',
        '                                    },\n',
        '                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),\n',
        '                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)\n',
        '                                )\n',
        '                            },\n'
    ]
    content = content[:start_idx] + replacement + content[end_idx:]

with open(path, "w") as f:
    f.writelines(content)

