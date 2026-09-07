with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm_content = f.read()

if "GUIDANCE" not in vm_content:
    vm_content = vm_content.replace("SPLASH,", "SPLASH, GUIDANCE,")

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(vm_content)
