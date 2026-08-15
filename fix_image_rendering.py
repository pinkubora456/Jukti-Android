import re
import os

# Fix SplashScreen
with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'r') as f:
    splash_content = f.read()

splash_old = """                    val localLogo = java.io.File(androidx.compose.ui.platform.LocalContext.current.filesDir, "cached_logo.jpg")
                    if (localLogo.exists() && aboutConfig.logoUrl.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(localLogo)
                                .crossfade(true)
                                .build(),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(64.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                            error = androidx.compose.ui.graphics.vector.rememberVectorPainter(getLogoIcon(aboutConfig.logoIconName)),
                            fallback = androidx.compose.ui.graphics.vector.rememberVectorPainter(getLogoIcon(aboutConfig.logoIconName))
                        )
                    } else {
                        Icon(
                            imageVector = getLogoIcon(aboutConfig.logoIconName),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }"""

splash_new = """                    val localLogo = java.io.File(androidx.compose.ui.platform.LocalContext.current.filesDir, "cached_logo.jpg")
                    val modelData = if (localLogo.exists() && aboutConfig.logoUrl.isNotEmpty()) localLogo else if (aboutConfig.logoUrl.isNotEmpty()) aboutConfig.logoUrl else null
                    if (modelData != null) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(modelData)
                                .crossfade(true)
                                .build(),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(64.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                            error = androidx.compose.ui.graphics.vector.rememberVectorPainter(getLogoIcon(aboutConfig.logoIconName)),
                            fallback = androidx.compose.ui.graphics.vector.rememberVectorPainter(getLogoIcon(aboutConfig.logoIconName))
                        )
                    } else {
                        Icon(
                            imageVector = getLogoIcon(aboutConfig.logoIconName),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }"""
splash_content = splash_content.replace(splash_old, splash_new)

with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'w') as f:
    f.write(splash_content)

# Fix AboutScreen
with open('app/src/main/java/com/example/ui/screens/AboutScreen.kt', 'r') as f:
    about_content = f.read()

about_old = """                    Box(contentAlignment = Alignment.Center) {
                        if (aboutConfig.logoIconName.startsWith("custom_logo:")) {
                            val path = aboutConfig.logoIconName.removePrefix("custom_logo:")
                            AsyncImage(
                                model = File(path),
                                contentDescription = "Custom App Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = currentLogoIcon,
                                contentDescription = "App Logo",
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }"""

about_new = """                    Box(contentAlignment = Alignment.Center) {
                        val localLogo = java.io.File(LocalContext.current.filesDir, "cached_logo.jpg")
                        val modelData = if (localLogo.exists() && aboutConfig.logoUrl.isNotEmpty()) localLogo else if (aboutConfig.logoUrl.isNotEmpty()) aboutConfig.logoUrl else null
                        if (modelData != null) {
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(LocalContext.current)
                                    .data(modelData)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Custom App Logo",
                                modifier = Modifier.size(52.dp),
                                contentScale = ContentScale.Fit,
                                error = androidx.compose.ui.graphics.vector.rememberVectorPainter(currentLogoIcon),
                                fallback = androidx.compose.ui.graphics.vector.rememberVectorPainter(currentLogoIcon)
                            )
                        } else if (aboutConfig.logoIconName.startsWith("custom_logo:")) {
                            val path = aboutConfig.logoIconName.removePrefix("custom_logo:")
                            AsyncImage(
                                model = File(path),
                                contentDescription = "Custom App Logo",
                                modifier = Modifier.size(52.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = currentLogoIcon,
                                contentDescription = "App Logo",
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }"""

about_content = about_content.replace(about_old, about_new)

with open('app/src/main/java/com/example/ui/screens/AboutScreen.kt', 'w') as f:
    f.write(about_content)
