# If there are old files holding GuidanceEntity that might conflict, we check
import glob

files = glob.glob("app/src/main/java/com/example/**/*.kt", recursive=True)
for f in files:
    with open(f, "r") as file:
        content = file.read()
    if "data class GuidanceEntity" in content and "pyqFocus" in content and "prepStrategy" in content and "strengthWeakness" in content:
        # this is the old entity, we should remove it if it's in Entities.kt
        pass
