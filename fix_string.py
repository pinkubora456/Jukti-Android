import re
path = "app/src/main/java/com/example/ui/screens/BatchImportQuestionScreen.kt"
with open(path, "r") as f:
    content = f.read()

bad_string = '"Paste CSV content here...\\ne.g.\\nstatement,statementAssamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n"Who was the first King of the Ahom Kingdom?","আহোম ৰাজ্যৰ প্ৰথম ৰজা কোন আছিল?","Sukaphaa","চ্যুকাফা","Sutephaa","চ্যুটেফা","Subinphaa","চুবিনফা","Sudangphaa","চুডাংফা","A","Sukaphaa founded the Ahom Kingdom in medieval Assam.","চ্যুকাফাই মধ্যযুগীয় অসমত আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল।","Assam History","Ahom Kingdom","ADRE HS 2024","Medium""'

good_string = '"Paste CSV content here...\\ne.g.\\nstatement,statementAssamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\\n\\"Who was the first King of the Ahom Kingdom?\\",\\"আহোম ৰাজ্যৰ প্ৰথম ৰজা কোন আছিল?\\",\\"Sukaphaa\\",\\"চ্যুকাফা\\",\\"Sutephaa\\",\\"চ্যুটেফা\\",\\"Subinphaa\\",\\"চুবিনফা\\",\\"Sudangphaa\\",\\"চুডাংফা\\",\\"A\\",\\"Sukaphaa founded the Ahom Kingdom in medieval Assam.\\",\\"চ্যুকাফাই মধ্যযুগীয় অসমত আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল।\\",\\"Assam History\\",\\"Ahom Kingdom\\",\\"ADRE HS 2024\\",\\"Medium\\""'

if bad_string in content:
    content = content.replace(bad_string, good_string)
    print("Replaced!")
else:
    print("Not found! Let's just find the part that starts with 'Paste CSV'")
    
    # Try regex
    pattern = r'"Paste CSV content here\.\.\.\\ne\.g\.\\nstatement.*?Medium""'
    if re.search(pattern, content):
        content = re.sub(pattern, good_string.replace('\\', '\\\\'), content)
        print("Replaced with regex!")
    else:
        print("Regex not found")

with open(path, "w") as f:
    f.write(content)
