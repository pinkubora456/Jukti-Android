import re

with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'r') as f:
    content = f.read()

old_coil = """                    if (aboutConfig.logoUrl.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(aboutConfig.logoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(64.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                            error = androidx.compose.ui.graphics.vector.rememberVectorPainter(getLogoIcon(aboutConfig.logoIconName)),
                            fallback = androidx.compose.ui.graphics.vector.rememberVectorPainter(getLogoIcon(aboutConfig.logoIconName))
                        )
                    }"""

new_coil = """                    val localLogo = java.io.File(androidx.compose.ui.platform.LocalContext.current.filesDir, "cached_logo.jpg")
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
                    }"""

content = content.replace(old_coil, new_coil)

with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'w') as f:
    f.write(content)
