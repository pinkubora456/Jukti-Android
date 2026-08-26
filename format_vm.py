import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm = f.read()

# I will find all standalone "    init {" that are followed by "                }" or similar garbage
# Let's fix the duplicated garbage manually.

