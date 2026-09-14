import re

path = "app/src/main/java/com/example/ui/screens/BatchImportQuestionScreen.kt"
with open(path, "r") as f:
    content = f.read()

pattern = r'placeholder = \{\n\s+Text\(\n\s+"Paste CSV content here\.\.\.\\ne\.g\.\\nstatement,statementAssamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n\\"Who was the first King of the Ahom Kingdom\?\\",\\"আহোম ৰাজ্যৰ প্ৰথম ৰজা কোন আছিল\?\\",\\"Sukaphaa\\",\\"চ্যুকাফা\\",\\"Sutephaa\\",\\"চ্যুটেফা\\",\\"Subinphaa\\",\\"চুবিনফা\\",\\"Sudangphaa\\",\\"চুডাংফা\\",\\"A\\",\\"Sukaphaa founded the Ahom Kingdom in medieval Assam\.\\",\\"চ্যুকাফাই মধ্যযুগীয় অসমত আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল।\\",\\"Assam History\\",\\"Ahom Kingdom\\",\\"ADRE HS 2024\\",\\"Medium\\"",\n\s+style = MaterialTheme\.typography\.bodySmall\.copy\(fontFamily = FontFamily\.Monospace\),\n\s+color = MaterialTheme\.colorScheme\.onSurfaceVariant\.copy\(alpha = 0\.7f\)\n\s+\)\n\s+\},'

replacement = r"""placeholder = {
                                Text(
                                    if (selectedContentType == "Reading Comprehension")
                                        "Paste CSV content here...\ne.g.\npassageId,passage,statement,a,b,c,d,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\n\"passage1\",\"Read this passage...\",\"Who was the first King?\",\"Sukaphaa\",\"Sutephaa\",\"Subinphaa\",\"Sudangphaa\",\"A\",\"Explanation\",\"\",\"Assam History\",\"Ahom Kingdom\",\"ADRE HS 2024\",\"Medium\""
                                    else
                                        "Paste CSV content here...\ne.g.\nstatement,statementAssamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\n\"Who was the first King of the Ahom Kingdom?\",\"আহোম ৰাজ্যৰ প্ৰথম ৰজা কোন আছিল?\",\"Sukaphaa\",\"চ্যুকাফা\",\"Sutephaa\",\"চ্যুটেফা\",\"Subinphaa\",\"চুবিনফা\",\"Sudangphaa\",\"চুডাংফা\",\"A\",\"Sukaphaa founded the Ahom Kingdom in medieval Assam.\",\"চ্যুকাফাই মধ্যযুগীয় অসমত আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল。\",\"Assam History\",\"Ahom Kingdom\",\"ADRE HS 2024\",\"Medium\"",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },"""

if re.search(pattern, content):
    content = re.sub(pattern, replacement, content)
    print("Replaced placeholder")
else:
    print("Placeholder pattern not found")

with open(path, "w") as f:
    f.write(content)

