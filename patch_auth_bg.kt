    Box(modifier = Modifier.fillMaxSize()) {
        // Light Kaziranga/Brahmaputra background
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            // Very light Jaapi pattern in the sky
            val jaapiColor = Color(0xFFE2E8F0).copy(alpha = 0.5f) // very subtle gray/blue
            for (i in 0..5) {
                for (j in 0..8) {
                    if ((i+j)%2 == 0) {
                        val cx = i * (size.width / 5)
                        val cy = j * (size.height / 8)
                        val p = androidx.compose.ui.graphics.Path()
                        p.moveTo(cx, cy)
                        p.lineTo(cx + 40f, cy + 20f)
                        p.lineTo(cx - 40f, cy + 20f)
                        p.close()
                        drawPath(p, jaapiColor)
                    }
                }
            }
            
            // Brahmaputra River at the bottom
            val riverColor = Color(0xFFBAE6FD).copy(alpha = 0.6f) // light blue
            val riverPath = androidx.compose.ui.graphics.Path()
            riverPath.moveTo(0f, size.height * 0.85f)
            riverPath.quadraticBezierTo(size.width * 0.25f, size.height * 0.8f, size.width * 0.5f, size.height * 0.85f)
            riverPath.quadraticBezierTo(size.width * 0.75f, size.height * 0.9f, size.width, size.height * 0.8f)
            riverPath.lineTo(size.width, size.height)
            riverPath.lineTo(0f, size.height)
            riverPath.close()
            drawPath(riverPath, riverColor)
            
            // Kaziranga Grasslands
            val grassColor = Color(0xFFBBF7D0).copy(alpha = 0.6f) // light green
            val grassPath = androidx.compose.ui.graphics.Path()
            grassPath.moveTo(0f, size.height * 0.9f)
            grassPath.quadraticBezierTo(size.width * 0.2f, size.height * 0.85f, size.width * 0.4f, size.height * 0.92f)
            grassPath.quadraticBezierTo(size.width * 0.7f, size.height * 0.82f, size.width, size.height * 0.88f)
            grassPath.lineTo(size.width, size.height)
            grassPath.lineTo(0f, size.height)
            grassPath.close()
            drawPath(grassPath, grassColor)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                // .background(MaterialTheme.colorScheme.background) // removed this line to show Canvas
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
