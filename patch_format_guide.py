import re
path = "app/src/main/java/com/example/ui/screens/BatchImportQuestionScreen.kt"
with open(path, "r") as f:
    content = f.read()

placeholder_normal = r'                                    "\"Who was the first King of the Ahom Kingdom?\",\"আহোম ৰাজ্যৰ প্ৰথম ৰজা কোন আছিল?\",\"Sukaphaa\",\"চ্যুকাফা\",\"Sutephaa\",\"চ্যুটেফা\",\"Subinphaa\",\"চুবিনফা\",\"Sudangphaa\",\"চুডাংফা\",\"A\",\"Sukaphaa founded the Ahom Kingdom in medieval Assam.\",\"চ্যুকাফাই মধ্যযুগীয় অসমত আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল।\",\"Assam History\",\"Ahom Kingdom\",\"ADRE HS 2024\",\"Medium\""'

placeholder_rc = r'                                    "\"passage1\",\"Read this passage...\",\"Who was the first King?\",\"Sukaphaa\",\"Sutephaa\",\"Subinphaa\",\"Sudangphaa\",\"A\",\"Explanation\",\"Assam History\",\"Ahom Kingdom\",\"ADRE HS 2024\",\"Medium\""'

pattern_placeholder = r'(placeholder = \{\n                                Text\(\n                                    "Paste CSV content here\.\.\.\\ne\.g\.\\nstatement,statementAssamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n" \+\n' + placeholder_normal + r',\n                                    style = MaterialTheme.typography.bodySmall.copy\(fontFamily = FontFamily.Monospace\),\n                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy\(alpha = 0\.7f\)\n                                \)\n                            \},)'

# Wait, my previous replacement concatenated it? Let's check how the file is currently.
