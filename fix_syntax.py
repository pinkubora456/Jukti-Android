import re

def fix(filepath, var_name):
    with open(filepath, "r") as f:
        content = f.read()

    # Find where `val matchChapter = if ({var_name}.isEmpty()) {` starts
    start_idx = content.find(f"val matchChapter = if ({var_name}.isEmpty()) {{")
    
    if start_idx == -1:
        print("not found")
        return

    # find `matchSubject && matchChapter`
    end_idx = content.find("matchSubject && matchChapter", start_idx)
    
    if end_idx == -1:
        print("not found end")
        return
        
    end_idx = content.find("\n", end_idx) + 1

    replacement = f"""val matchChapter = if ({var_name}.isEmpty()) {{
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
                    }}
                    matchSubject && matchChapter
"""
    new_content = content[:start_idx] + replacement + content[end_idx:]

    with open(filepath, "w") as f:
        f.write(new_content)

fix("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "selectedChapters")
fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt", "currentSelectedChapters")
