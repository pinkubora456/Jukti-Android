import sqlite3

conn = sqlite3.connect(':memory:')
conn.execute('''CREATE TABLE questions (subject TEXT, topic TEXT, difficulty TEXT, examCategory TEXT)''')
conn.execute("INSERT INTO questions VALUES ('English', 'Articles', 'Easy', 'Grade 3 HS Level, APSC')")
conn.execute("INSERT INTO questions VALUES ('English', 'Articles', 'Medium', 'Grade 4')")
conn.execute("INSERT INTO questions VALUES ('English', 'Verbs', 'Hard', 'Grade 3 HS Level')")
conn.execute("INSERT INTO questions VALUES ('Math', 'Algebra', 'Hard', 'Grade 3 HS Level')")

query = """
    SELECT topic as chapter, 
           COUNT(*) as total, 
           SUM(CASE WHEN difficulty = 'Easy' THEN 1 ELSE 0 END) as easy,
           SUM(CASE WHEN difficulty = 'Medium' THEN 1 ELSE 0 END) as medium,
           SUM(CASE WHEN difficulty = 'Hard' THEN 1 ELSE 0 END) as hard
    FROM questions
    WHERE subject = :subject AND (examCategory LIKE '%' || :exam || '%' OR :exam = 'All Exams')
    GROUP BY topic
    ORDER BY total DESC
"""

for row in conn.execute(query, {'subject': 'English', 'exam': 'Grade 3 HS Level'}):
    print(row)

for row in conn.execute(query, {'subject': 'English', 'exam': 'All Exams'}):
    print(row)

