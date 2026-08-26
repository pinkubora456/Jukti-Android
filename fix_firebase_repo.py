import re

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "r") as f:
    text = f.read()

text = re.sub(
    r'isPremium = doc.getBoolean\("isPremium"\) \?\: false,',
    r'isPremium = doc.getBoolean("isPremium") ?: false,\n                                        accessType = doc.getString("accessType") ?: (if (doc.getBoolean("isPremium") == true) "PREMIUM" else "FREE"),',
    text
)

text = re.sub(
    r'isPremium = doc.getBoolean\("isPremium"\) \?\: false\n',
    r'isPremium = doc.getBoolean("isPremium") ?: false,\n                                        accessType = doc.getString("accessType") ?: (if (doc.getBoolean("isPremium") == true) "PREMIUM" else "FREE")\n',
    text
)

text = re.sub(
    r'"isPremium" to q.isPremium,',
    r'"isPremium" to q.isPremium,\n            "accessType" to q.accessType,',
    text
)
text = re.sub(
    r'"isPremium" to m.isPremium,',
    r'"isPremium" to m.isPremium,\n            "accessType" to m.accessType,',
    text
)
text = re.sub(
    r'"isPremium" to n.isPremium',
    r'"isPremium" to n.isPremium,\n            "accessType" to n.accessType',
    text
)


with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "w") as f:
    f.write(text)
