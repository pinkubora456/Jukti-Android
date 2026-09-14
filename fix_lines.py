import re

path = "app/src/main/java/com/example/ui/screens/BatchImportQuestionScreen.kt"
with open(path, "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "statement,statementAssamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty" in line:
        if i + 1 < len(lines):
            # This is the line with the example
            # We want to replace all " with \" INSIDE the string literal.
            # But the line starts and ends with "
            l = lines[i+1]
            if l.strip().startswith('"Who') and l.strip().endswith(',"Medium"",'):
                # Let's just hardcode the fixed line
                lines[i+1] = '                                    "\\"Who was the first King of the Ahom Kingdom?\\",\\"আহোম ৰাজ্যৰ প্ৰথম ৰজা কোন আছিল?\\",\\"Sukaphaa\\",\\"চ্যুকাফা\\",\\"Sutephaa\\",\\"চ্যুটেফা\\",\\"Subinphaa\\",\\"চুবিনফা\\",\\"Sudangphaa\\",\\"চুডাংফা\\",\\"A\\",\\"Sukaphaa founded the Ahom Kingdom in medieval Assam.\\",\\"চ্যুকাফাই মধ্যযুগীয় অসমত আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল।\\",\\"Assam History\\",\\"Ahom Kingdom\\",\\"ADRE HS 2024\\",\\"Medium\\"",\n'

with open(path, "w") as f:
    f.writelines(lines)
