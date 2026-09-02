import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    start_str = "val matchChapter = if (selectedChapters.isEmpty()) {"
    end_str = "matchSubject && matchChapter"
    
    start_idx = content.find(start_str)
    end_idx = content.find(end_str, start_idx)
    
    if start_idx != -1 and end_idx != -1:
        new_block = """val matchChapter = if (selectedChapters.isEmpty()) {
                                true
                            } else {
                                val topicStr = q.topic ?: ""
                                val normTopic = com.example.data.repository.normalizeChapterName(topicStr, q.subject)
                                selectedChapters.any { ch ->
                                    val nCh = com.example.data.repository.normalizeChapterName(ch, q.subject)
                                    normTopic.equals(nCh, ignoreCase = true) || 
                                    topicStr.equals(ch, ignoreCase = true) || 
                                    topicStr.contains(ch, ignoreCase = true) || 
                                    ch.contains(topicStr, ignoreCase = true) || 
                                    normTopic.contains(ch, ignoreCase = true) || 
                                    ch.contains(normTopic, ignoreCase = true)
                                }
                            }
                            """
        content = content[:start_idx] + new_block + content[end_idx:]
        with open(filepath, 'w') as f:
            f.write(content)
        print("Replaced matchChapter 1")

fix("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
