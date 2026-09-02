import re

def fix(filepath, var_name):
    with open(filepath, "r") as f:
        content = f.read()

    # Find where `val matchChapter = if ({var_name}.isEmpty()) {` starts
    start_idx = content.find(f"val matchChapter = if ({var_name}.isEmpty()) {{")
    if start_idx == -1:
        return

    # To find the end, let's look for the next logical statement.
    # In both screens, `matchChapter` is followed by:
    # PracticeScreen:
    # val matchChapter = ...
    # matchSubject && matchChapter
    
    # McqStudyScreen:
    # val matchChapter = ...
    # matchSubject && matchChapter

    end_search = "\n                            matchSubject && matchChapter"
    end_idx = content.find(end_search, start_idx)
    
    if end_idx == -1:
        # try another indentation
        end_search = "matchSubject && matchChapter"
        end_idx = content.find(end_search, start_idx)
        
    if end_idx == -1:
        print(f"Could not find end for {filepath}")
        return
        
    # Find the line start of matchSubject && matchChapter
    end_idx = content.rfind("\n", start_idx, end_idx)

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

    new_content = content[:start_idx] + generic_logic + content[end_idx:]

    # But wait, in PracticeScreen, there are actually TWO `matchChapter` declarations?
    # Let's check how many there are.
    
    with open(filepath, "w") as f:
        f.write(new_content)

fix("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "selectedChapters")
# Wait, McqStudyScreen actually has TWO declarations of matchChapter!
# Wait, NO. Let's just do a simpler string replace.
