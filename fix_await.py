with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "r") as f:
    text = f.read()

text = text.replace('kotlinx.coroutines.tasks.await(functions.getHttpsCallable("getPremiumContent").call())', 'functions.getHttpsCallable("getPremiumContent").call().await()')

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "w") as f:
    f.write(text)
