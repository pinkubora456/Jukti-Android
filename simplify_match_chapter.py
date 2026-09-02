import re

def simplify_match_chapter(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # The block we want to replace starts at `val matchChapter = if (selectedChapters.isEmpty()) {` 
    # OR `val matchChapter = if (currentSelectedChapters.isEmpty()) {`
    
    # Let's use a regex to find the start and carefully replace it up to the end of the `else` block.
    # Actually, it might be safer to replace the whole `val matchChapter = ...` expression by parsing curly braces.
    
    def replace_block(text, var_name):
        search_str = f"val matchChapter = if ({var_name}.isEmpty()) {{"
        start_idx = text.find(search_str)
        if start_idx == -1:
            return text
            
        # find matching closing brace for the `val matchChapter = ...` assignment.
        # It's an `if (...) { ... } else { ... }` block.
        # We can just count braces starting from `start_idx`.
        open_braces = 0
        in_block = False
        end_idx = -1
        for i in range(start_idx, len(text)):
            if text[i] == '{':
                open_braces += 1
                in_block = True
            elif text[i] == '}':
                open_braces -= 1
                if in_block and open_braces == 0:
                    end_idx = i + 1
                    break
        
        if end_idx != -1:
            generic_logic = f"""val matchChapter = if ({var_name}.isEmpty()) {{
                                true
                            }} else {{
                                val topicStr = q.topic ?: ""
                                val normTopic = com.example.data.repository.normalizeChapterName(topicStr, q.subject)
                                {var_name}.any {{ ch ->
                                    val nCh = com.example.data.repository.normalizeChapterName(ch, q.subject)
                                    normTopic.equals(nCh, ignoreCase = true) || 
                                    topicStr.equals(ch, ignoreCase = true) || 
                                    topicStr.contains(ch, ignoreCase = true) || 
                                    ch.contains(topicStr, ignoreCase = true) || 
                                    normTopic.contains(ch, ignoreCase = true) || 
                                    ch.contains(normTopic, ignoreCase = true)
                                }}
                            }}"""
            # Indentation might be a bit off, but Kotlin doesn't care.
            text = text[:start_idx] + generic_logic + text[end_idx:]
        return text

    content = replace_block(content, "selectedChapters")
    content = replace_block(content, "currentSelectedChapters")

    with open(filepath, "w") as f:
        f.write(content)

simplify_match_chapter("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
simplify_match_chapter("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
