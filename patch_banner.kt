        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(colors = gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable {
                    if (onBannerClick != null) {
                        onBannerClick(banner)
                    } else {
                        onUpgradeClick()
                    }
                }
        ) {
            // Subtle Jaapi pattern in the background
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw a few faint Jaapis
                val jaapiPath = androidx.compose.ui.graphics.Path()
                val color = Color.White.copy(alpha = 0.1f)
                
                // Jaapi 1
                jaapiPath.moveTo(size.width * 0.8f, size.height * 0.2f)
                jaapiPath.lineTo(size.width * 0.95f, size.height * 0.5f)
                jaapiPath.lineTo(size.width * 0.65f, size.height * 0.5f)
                jaapiPath.close()
                drawPath(jaapiPath, color)
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(size.width * 0.65f, size.height * 0.5f),
                    end = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.5f),
                    strokeWidth = 10f
                )
                
                // Jaapi 2
                val jaapiPath2 = androidx.compose.ui.graphics.Path()
                jaapiPath2.moveTo(size.width * 0.1f, size.height * 0.7f)
                jaapiPath2.lineTo(size.width * 0.3f, size.height * 1.0f)
                jaapiPath2.lineTo(size.width * -0.1f, size.height * 1.0f)
                jaapiPath2.close()
                drawPath(jaapiPath2, color)
                
                // Silhouette of Assam map (simplified abstract shape)
                val mapPath = androidx.compose.ui.graphics.Path()
                mapPath.moveTo(size.width * 0.2f, size.height * 0.8f)
                mapPath.quadraticBezierTo(size.width * 0.4f, size.height * 0.9f, size.width * 0.6f, size.height * 0.6f)
                mapPath.quadraticBezierTo(size.width * 0.8f, size.height * 0.5f, size.width * 0.9f, size.height * 0.2f)
                mapPath.quadraticBezierTo(size.width * 0.7f, size.height * 0.3f, size.width * 0.5f, size.height * 0.2f)
                mapPath.quadraticBezierTo(size.width * 0.3f, size.height * 0.3f, size.width * 0.2f, size.height * 0.8f)
                drawPath(mapPath, Color.White.copy(alpha = 0.05f))
                
                // Gamosa border along the bottom
                val dashWidth = 15f
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color.Red.copy(alpha = 0.7f),
                        start = androidx.compose.ui.geometry.Offset(x, size.height - 4f),
                        end = androidx.compose.ui.geometry.Offset(x + dashWidth, size.height - 4f),
                        strokeWidth = 8f
                    )
                    x += dashWidth + 10f
                }
            }
