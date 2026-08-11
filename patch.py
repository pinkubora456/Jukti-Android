import sys

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

# Replace the signature
content = content.replace('fun loginWithEmail(emailInput: String, nameInput: String = "", passwordInput: String = "") {', 'fun loginWithEmail(emailInput: String, nameInput: String = "", passwordInput: String = "", isRegister: Boolean = false) {')

# Find the try catch block and replace it
old_block = """                try {
                    auth.signInWithEmailAndPassword(trimmedEmail, pwd).await()
                } catch (e: Exception) {
                    val msg = e.message ?: ""
                    try {
                        if (msg.contains("There is no user record") || e is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                            auth.createUserWithEmailAndPassword(trimmedEmail, pwd).await()
                        } else if (passwordInput.isBlank()) {
                            auth.signInAnonymously().await()
                        } else {
                            throw e
                        }
                    } catch (e2: Exception) {
                        throw e2
                    }
                }"""

new_block = """                try {
                    if (isRegister) {
                        auth.createUserWithEmailAndPassword(trimmedEmail, pwd).await()
                    } else {
                        auth.signInWithEmailAndPassword(trimmedEmail, pwd).await()
                    }
                } catch (e: Exception) {
                    if (!isRegister && passwordInput.isBlank()) {
                        auth.signInAnonymously().await()
                    } else {
                        throw e
                    }
                }"""

content = content.replace(old_block, new_block)

old_error = """                    } else if (msg.contains("authentication credentials is incorrect, malformed or has expired", ignoreCase = true)) {
                        _sessionMessage.value = "Login failed: API key is restricted or Email/Password Auth is disabled in Firebase Console."
                    } else {
                        _sessionMessage.value = "Login failed: ${e.localizedMessage}"
                    }"""
                    
new_error = """                    } else {
                        _sessionMessage.value = "Auth failed: ${e.localizedMessage}"
                    }"""

content = content.replace(old_error, new_error)

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)

