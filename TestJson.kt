fun main() {
    val obj = org.json.JSONObject("{\"1\": 1.0}")
    obj.keys().forEach { k -> println(k) }
}
