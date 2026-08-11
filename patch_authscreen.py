import sys

with open('app/src/main/java/com/example/ui/screens/AuthScreen.kt', 'r') as f:
    content = f.read()

old_register = """                        if (trimmedPassword != confirmPasswordInput.trim()) {
                            errorMessage = "Passwords do not match."
                            return@Button
                        }
                        viewModel.loginWithEmail(trimmedEmail, trimmedName, trimmedPassword)
                        viewModel.toggleGuestMode(false)
"""

new_register = """                        if (trimmedPassword != confirmPasswordInput.trim()) {
                            errorMessage = "Passwords do not match."
                            return@Button
                        }
                        viewModel.loginWithEmail(trimmedEmail, trimmedName, trimmedPassword, isRegister = true)
                        viewModel.toggleGuestMode(false)
"""

content = content.replace(old_register, new_register)

with open('app/src/main/java/com/example/ui/screens/AuthScreen.kt', 'w') as f:
    f.write(content)

