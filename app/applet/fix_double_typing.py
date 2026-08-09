import glob
import re

# 1. Update SafeOutlinedTextField.kt to add TextFieldValue overload if not present
safe_file = "app/src/main/java/com/example/ui/components/SafeOutlinedTextField.kt"
with open(safe_file, "r") as f:
    safe_content = f.read()

if "TextFieldValue" not in safe_content:
    overload = """
@Composable
fun SafeOutlinedTextField(
    value: androidx.compose.ui.text.input.TextFieldValue,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newVal ->
            val sanitizedText = sanitizeInput(value.text, newVal.text)
            if (sanitizedText != newVal.text) {
                onValueChange(value.copy(text = sanitizedText))
            } else {
                onValueChange(newVal)
            }
        },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        shape = shape,
        colors = colors
    )
}
"""
    safe_content += overload
    with open(safe_file, "w") as f:
        f.write(safe_content)
    print("Updated SafeOutlinedTextField.kt with TextFieldValue overload")

# 2. Replace OutlinedTextField( with SafeOutlinedTextField( in all other files
files = glob.glob("app/src/main/java/**/*.kt", recursive=True)
for filepath in files:
    if "SafeOutlinedTextField.kt" in filepath:
        continue
    with open(filepath, "r") as f:
        content = f.read()
    
    # Replace OutlinedTextField( when not preceded by Safe
    # We can check line by line or use regex
    lines = content.splitlines()
    updated_lines = []
    changed = False
    for line in lines:
        if "OutlinedTextField(" in line and "SafeOutlinedTextField" not in line:
            # Replace OutlinedTextField( with SafeOutlinedTextField(
            newLine = line.replace("OutlinedTextField(", "SafeOutlinedTextField(")
            updated_lines.append(newLine)
            changed = True
        else:
            updated_lines.append(line)
            
    if changed:
        new_content = "\n".join(updated_lines)
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
print("Double typing fix script completed successfully.")
