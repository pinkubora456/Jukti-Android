import re

with open("app/src/main/java/com/example/data/local/Daos.kt", "r") as f:
    daos = f.read()

daos = daos.replace("interface QuestionDao", "abstract class QuestionDao")
daos = daos.replace("interface MockTestDao", "abstract class MockTestDao")
daos = daos.replace("interface StudyNoteDao", "abstract class StudyNoteDao")

with open("app/src/main/java/com/example/data/local/Daos.kt", "w") as f:
    f.write(daos)

