import re
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

imports = """
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
"""
content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\n" + imports)

permission_logic = """
            JuktiTheme(darkTheme = isDarkTheme ?: systemDark) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        // Do nothing, we just asked
                    }
                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
"""

content = content.replace("            JuktiTheme(darkTheme = isDarkTheme ?: systemDark) {", permission_logic)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
