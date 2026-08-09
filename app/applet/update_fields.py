import glob
import re

files = glob.glob("app/src/main/java/**/*.kt", recursive=True)
for filepath in files:
    if "SafeOutlinedTextField.kt" in filepath:
        continue
    with open(filepath, "r") as f:
        content = f.read()
    
    # Replace OutlinedTextField( not preceded by Safe
    new_content = re.sub(r'\b(?<!Safe)OutlinedTextField\(', 'SafeOutlinedTextField(', content)
    
    if new_content != content:
        import_stmt = "import com.example.ui.components.SafeOutlinedTextField"
        if import_stmt not in new_content:
            if "package " in new_content:
                parts = new_content.split("\n", 1)
                new_content = parts[0] + "\n\n" + import_stmt + "\n" + parts[1]
            else:
                new_content = import_stmt + "\n" + new_content
                
        with open(filepath, "w") as f:
            f.write(new_content)
        print(f"Updated {filepath}")
