import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

idx = content.rfind("}")
# Actually we have multiple classes at the bottom.
# The class JuktiViewModel ends much earlier, or we can just put it inside JuktiViewModel.
