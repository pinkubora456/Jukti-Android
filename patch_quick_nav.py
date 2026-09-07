with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Replace Mock Test with Guidance
content = content.replace(
    'QuickNavItem("Mock Test", "মক পৰীক্ষা", Icons.Default.Timer, Screen.MOCK_TESTS, BrandAccent),',
    'QuickNavItem("Guidance", "মাৰ্গদৰ্শন", Icons.Default.Explore, Screen.GUIDANCE, BrandAccent),'
)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
