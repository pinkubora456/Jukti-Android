import re

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

new_sync_logic = """
    init {
        viewModelScope.launch {
            aboutConfig.collect { config ->
                if (config.logoUrl.isNotEmpty()) {
                    syncLogoLocally(config.logoUrl, config.logoUpdatedAt)
                }
            }
        }
    }

    private fun syncLogoLocally(url: String, updatedAt: Long) {
        val app = getApplication<android.app.Application>()
        val prefs = app.getSharedPreferences("jukti_prefs", android.content.Context.MODE_PRIVATE)
        val cachedUpdatedAt = prefs.getLong("logo_updated_at", 0L)
        
        if (updatedAt > cachedUpdatedAt) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val request = okhttp3.Request.Builder().url(url).build()
                    val client = okhttp3.OkHttpClient()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.byteStream()?.use { input ->
                            val file = java.io.File(app.filesDir, "cached_logo.jpg")
                            java.io.FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                        prefs.edit().putLong("logo_updated_at", updatedAt).apply()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("JuktiViewModel", "Error syncing logo", e)
                }
            }
        }
    }
"""

if "private fun syncLogoLocally" not in content:
    # insert before updateAboutConfig
    content = content.replace("fun updateAboutConfig(config: AboutConfigEntity) {", new_sync_logic + "\n    fun updateAboutConfig(config: AboutConfigEntity) {")

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)
