import re

with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'r') as f:
    content = f.read()

# Make sure import is on a new line
if "import androidx.compose.ui.unit.sppackage" in content:
    content = content.replace("import androidx.compose.ui.unit.sppackage", "package")

content = re.sub(
    r'Text\(\s*text = "Jukti Test Your Knowledge",\s*style = MaterialTheme\.typography\.titleMedium,\s*color = MaterialTheme\.colorScheme\.onSurfaceVariant\s*\)',
    """Text(
                text = aboutConfig.appSubtitleEn.ifEmpty { "Test Your Knowledge" },
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 1.2.sp
                ),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )""",
    content
)

with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'w') as f:
    f.write(content)
