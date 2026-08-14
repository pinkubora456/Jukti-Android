import re

with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'r') as f:
    content = f.read()

# Fix the background color to support dark mode
content = content.replace(
    ".background(Color(0xFFF8FAFC)), // Very light blue/white",
    ".background(MaterialTheme.colorScheme.background),"
)

# Fix the hardcoded tagline and style
old_tagline = """                        Text(
                text = "Jukti Test Your Knowledge",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )"""

new_tagline = """                        Text(
                text = aboutConfig.appSubtitleEn.ifEmpty { "Test Your Knowledge" },
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 1.2.sp
                ),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )"""

content = content.replace(old_tagline, new_tagline)

if "import androidx.compose.ui.unit.sp" not in content:
    content = "import androidx.compose.ui.unit.sp\n" + content

with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'w') as f:
    f.write(content)
