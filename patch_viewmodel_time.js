const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'utf8');

const classDeclMatch = code.match(/class JuktiViewModel.*?\n/);
if (classDeclMatch) {
    const insertPos = classDeclMatch.index + classDeclMatch[0].length;
    const timeLogic = `
    private val timePrefs by lazy { getApplication<Application>().getSharedPreferences("jukti_time_prefs", android.content.Context.MODE_PRIVATE) }
    
    private var sessionTrustedServerTime: Long = 0L
    private var sessionTrustedRealtime: Long = 0L

    init {
        syncTrustedTime()
    }

    private fun syncTrustedTime() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Fetch header from a reliable Google endpoint
                val url = java.net.URL("https://us-central1-jukti-examprep.cloudfunctions.net")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "HEAD"
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                val dateStr = conn.getHeaderField("Date")
                if (dateStr != null) {
                    val format = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US)
                    val serverTime = format.parse(dateStr)?.time ?: return@launch
                    
                    sessionTrustedServerTime = serverTime
                    sessionTrustedRealtime = android.os.SystemClock.elapsedRealtime()
                    
                    timePrefs.edit()
                        .putLong("last_server_time", serverTime)
                        .putLong("last_device_realtime", sessionTrustedRealtime)
                        .putLong("max_seen_time", serverTime)
                        .apply()
                }
            } catch (e: Exception) {
                // Ignore, will fallback to cached or monotonic
            }
        }
    }

    fun getTrustedTime(): Long {
        val currentRealtime = android.os.SystemClock.elapsedRealtime()
        
        // 1. If we have a session-synced server time, use it + monotonic elapsed time
        if (sessionTrustedServerTime > 0L) {
            val elapsed = currentRealtime - sessionTrustedRealtime
            if (elapsed >= 0) {
                val calculated = sessionTrustedServerTime + elapsed
                updateMaxSeenTime(calculated)
                return calculated
            }
        }
        
        // 2. Fallback to persisted synced time if device hasn't rebooted since last sync
        val lastRealtime = timePrefs.getLong("last_device_realtime", 0L)
        val lastServerTime = timePrefs.getLong("last_server_time", 0L)
        if (lastServerTime > 0L && lastRealtime > 0L && currentRealtime >= lastRealtime) {
            val elapsed = currentRealtime - lastRealtime
            // basic sanity check: if elapsed is > 30 days without reboot, maybe suspicious, but let's trust it
            if (elapsed < 30L * 24 * 60 * 60 * 1000) {
                val calculated = lastServerTime + elapsed
                updateMaxSeenTime(calculated)
                return calculated
            }
        }
        
        // 3. Last resort fallback: System time, but protected against backwards tampering
        val currentSystemTime = System.currentTimeMillis()
        return updateMaxSeenTime(currentSystemTime)
    }
    
    private fun updateMaxSeenTime(time: Long): Long {
        val maxSeen = timePrefs.getLong("max_seen_time", 0L)
        if (time > maxSeen) {
            timePrefs.edit().putLong("max_seen_time", time).apply()
            return time
        }
        return maxSeen
    }
`;
    code = code.slice(0, insertPos) + timeLogic + code.slice(insertPos);
}

// Replace System.currentTimeMillis() in validateEntitlement and isSpecificPlanActive
code = code.replace(/fun validateEntitlement\(entitlement: EntitlementEntity\?, currentTime: Long = System\.currentTimeMillis\(\)\): Boolean \{/g, 
    `fun validateEntitlement(entitlement: EntitlementEntity?, currentTime: Long = getTrustedTime()): Boolean {`);

code = code.replace(/fun isSpecificPlanActive\(plan: com\.example\.data\.local\.PlanEntity\): Boolean \{\s*val entitlement = userEntitlement\.value\s*val now = System\.currentTimeMillis\(\)/, 
    `fun isSpecificPlanActive(plan: com.example.data.local.PlanEntity): Boolean {
        val entitlement = userEntitlement.value
        val now = getTrustedTime()`);

fs.writeFileSync('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', code);
