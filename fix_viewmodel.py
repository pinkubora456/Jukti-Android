path = "app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt"
with open(path, "r") as f:
    content = f.read()

# The bad block starts with:
bad_block_start = "    private fun isQuestionSubjectMatch("

start_idx = content.find(bad_block_start)
if start_idx != -1:
    # Find the end of this block
    end_str = "    )\n" # wait, it ends with initialValue = emptyMap() \n    )
    # let's just search for the exact string it ended with.
    # it ends with `        initialValue = emptyMap()\n    )\n`
    end_idx = content.find("        initialValue = emptyMap()\n    )\n", start_idx)
    if end_idx != -1:
        end_idx += len("        initialValue = emptyMap()\n    )\n")
        block_to_move = content[start_idx:end_idx]
        
        # Remove it from the current position
        new_content = content[:start_idx] + content[end_idx:]
        
        # Insert it right before init {
        init_idx = new_content.find("    init {")
        if init_idx != -1:
            new_content = new_content[:init_idx] + block_to_move + "\n" + new_content[init_idx:]
            with open(path, "w") as f:
                f.write(new_content)
            print("Successfully moved block to right before init {")
        else:
            print("Could not find init {")
    else:
        print("Could not find end of bad block")
else:
    print("Could not find start of bad block")
