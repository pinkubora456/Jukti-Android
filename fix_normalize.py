import os
import re

files_to_check = [
    "app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt",
    "app/src/main/java/com/example/data/repository/FirebaseRepository.kt",
    "app/src/main/java/com/example/util/CsvQuestionParser.kt",
    "app/src/main/java/com/example/data/repository/JuktiRepository.kt",
    "app/src/main/java/com/example/ui/screens/PracticeScreen.kt",
    "app/src/main/java/com/example/ui/screens/McqStudyScreen.kt"
]

def fix_single_question():
    path = "app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt"
    with open(path, 'r') as f:
        content = f.read()
    
    # In var rawChapters = ...
    # fromList = allSubjectsChapters
    # .map { com.example.data.repository.normalizeChapterName(it.chapter) } -> .map { com.example.data.repository.normalizeChapterName(it.chapter, it.subject) }
    content = content.replace(
        ".map { com.example.data.repository.normalizeChapterName(it.chapter) }",
        ".map { com.example.data.repository.normalizeChapterName(it.chapter, it.subject) }"
    )
    
    # In insert logic
    content = content.replace(
        "val normSubject = com.example.data.repository.normalizeSubjectName(subject)\n                            val normChapter = com.example.data.repository.normalizeChapterName(chapter)",
        "val normSubject = com.example.data.repository.normalizeSubjectName(subject)\n                            val normChapter = com.example.data.repository.normalizeChapterName(chapter, normSubject)"
    )

    with open(path, 'w') as f:
        f.write(content)
        
def fix_firebase():
    path = "app/src/main/java/com/example/data/repository/FirebaseRepository.kt"
    with open(path, 'r') as f:
        content = f.read()
        
    old1 = """                    val normTopic = normalizeChapterName(rawTopic)
                    val normSubject = normalizeSubjectName(rawSubject)"""
    new1 = """                    val normSubject = normalizeSubjectName(rawSubject)
                    val normTopic = normalizeChapterName(rawTopic, normSubject)"""
    content = content.replace(old1, new1)
    
    old2 = """                        topic = normalizeChapterName(rawTopic),
                        difficulty = doc.getString("difficulty") ?: "Medium","""
    new2 = """                        topic = normalizeChapterName(rawTopic, normalizeSubjectName(doc.getString("subject"))),
                        difficulty = doc.getString("difficulty") ?: "Medium","""
    content = content.replace(old2, new2)
    
    old3 = """                                    val normTopic = normalizeChapterName(rawTopic)
                                    val normSubject = normalizeSubjectName(rawSubject)"""
    new3 = """                                    val normSubject = normalizeSubjectName(rawSubject)
                                    val normTopic = normalizeChapterName(rawTopic, normSubject)"""
    content = content.replace(old3, new3)
    
    old4 = """                                    val normChap = normalizeChapterName(rawChap)"""
    new4 = """                                    val normChap = normalizeChapterName(rawChap, rawSubj)"""
    content = content.replace(old4, new4)
    
    with open(path, 'w') as f:
        f.write(content)

def fix_csv():
    path = "app/src/main/java/com/example/util/CsvQuestionParser.kt"
    with open(path, 'r') as f:
        content = f.read()
        
    content = content.replace(
        "val finalTopic = if (topic.isNotBlank()) normalizeChapterName(topic) else normalizeChapterName(defaultChapter.ifBlank { \"General\" })",
        "val finalTopic = if (topic.isNotBlank()) normalizeChapterName(topic, finalSubject) else normalizeChapterName(defaultChapter.ifBlank { \"General\" }, finalSubject)"
    )
    with open(path, 'w') as f:
        f.write(content)

def fix_jukti_repo():
    path = "app/src/main/java/com/example/data/repository/JuktiRepository.kt"
    with open(path, 'r') as f:
        content = f.read()
        
    content = content.replace(
        'val remoteKeys = remote.map { "${it.subject.trim().lowercase()}|${normalizeChapterName(it.chapter).lowercase()}" }.toSet()',
        'val remoteKeys = remote.map { "${it.subject.trim().lowercase()}|${normalizeChapterName(it.chapter, it.subject).lowercase()}" }.toSet()'
    )
    content = content.replace(
        'val extraLocal = local.filter { "${it.subject.trim().lowercase()}|${normalizeChapterName(it.chapter).lowercase()}" !in remoteKeys }',
        'val extraLocal = local.filter { "${it.subject.trim().lowercase()}|${normalizeChapterName(it.chapter, it.subject).lowercase()}" !in remoteKeys }'
    )
    content = content.replace(
        "val normChap = normalizeChapterName(sc.chapter)",
        "val normChap = normalizeChapterName(sc.chapter, sc.subject)"
    )
    content = content.replace(
        "val normChap = normalizeChapterName(subjectChapter.chapter)",
        "val normChap = normalizeChapterName(subjectChapter.chapter, subjectChapter.subject)"
    )
    
    with open(path, 'w') as f:
        f.write(content)

def fix_screens():
    for p in ["app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "app/src/main/java/com/example/ui/screens/PracticeScreen.kt"]:
        with open(p, 'r') as f:
            content = f.read()
        
        # .forEach { if (it.topic.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.topic)) }
        content = re.sub(
            r'set\.add\(com\.example\.data\.repository\.normalizeChapterName\((it\.topic|it\.chapter)\)\)',
            r'set.add(com.example.data.repository.normalizeChapterName(\1, it.subject))',
            content
        )
        
        content = re.sub(
            r'val normTopic = com\.example\.data\.repository\.normalizeChapterName\(topicStr\)',
            r'val normTopic = com.example.data.repository.normalizeChapterName(topicStr, subjStr)',
            content
        )
        
        with open(p, 'w') as f:
            f.write(content)
        
fix_single_question()
fix_firebase()
fix_csv()
fix_jukti_repo()
fix_screens()
print("All files patched")
