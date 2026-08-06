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
content = content.replace("package com.example", "package com.example\n" + imports)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
