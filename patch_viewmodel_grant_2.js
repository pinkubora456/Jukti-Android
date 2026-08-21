const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'utf8');

const regex = /suspend fun grantPlanToUser\(email: String, planName: String, validity: String\): Boolean \{[\s\S]*?fun exportUsersCsv\(context: android\.content\.Context\)/;

const replacement = `suspend fun grantPlanToUser(email: String, planName: String, validity: String): Boolean {
        return try {
            val durationMs = when (validity.lowercase()) {
                "1 month" -> 30L * 24 * 60 * 60 * 1000
                "3 months" -> 90L * 24 * 60 * 60 * 1000
                "6 months" -> 180L * 24 * 60 * 60 * 1000
                "1 year" -> 365L * 24 * 60 * 60 * 1000
                "lifetime" -> 100L * 365 * 24 * 60 * 60 * 1000
                else -> {
                    val formats = listOf("dd MMM yyyy", "dd/MM/yyyy", "yyyy-MM-dd")
                    var parsedTime = -1L
                    for (fmt in formats) {
                        try {
                            val sdf = java.text.SimpleDateFormat(fmt, java.util.Locale.getDefault())
                            sdf.isLenient = false
                            val date = sdf.parse(validity)
                            if (date != null) {
                                parsedTime = date.time
                                break
                            }
                        } catch (e: Exception) {
                            // try next format
                        }
                    }
                    if (parsedTime != -1L) {
                        parsedTime - System.currentTimeMillis()
                    } else {
                        365L * 24 * 60 * 60 * 1000
                    }
                }
            }
            
            val data = mapOf(
                "targetEmail" to email.trim(),
                "planName" to planName,
                "durationMs" to durationMs
            )
            
            val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
            functions.getHttpsCallable("grantPlanToUser").call(data).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun exportUsersCsv(context: android.content.Context)`;

code = code.replace(regex, replacement);
fs.writeFileSync('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', code);
