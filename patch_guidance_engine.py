import re

with open('app/src/main/java/com/example/data/util/GuidanceEngine.kt', 'r') as f:
    content = f.read()

replacement = """        // Priority Topics
        val priorityTopics = filteredPerformances.map { perf ->
            val pyqScore = (perf.pyqAvg.coerceAtMost(5f) / 5f) * 100f
            
            val weaknessScore = if (perf.accuracy != null) {
                100f - perf.accuracy
            } else {
                50f
            }
            
            val priorityScore = (pyqScore * 0.6f) + (weaknessScore * 0.4f)
            
            val label: String
            val reason: String
            
            if (perf.pyqAvg >= 2.0f) {
                label = "High Priority"
                reason = "Avg PYQ >= 2.0 (High Importance)"
            } else if (perf.pyqAvg >= 1.0f) {
                label = "Medium Priority"
                reason = "Avg PYQ 1.0 - 2.0 (Medium Importance)"
            } else {
                label = "Low Priority"
                reason = "Avg PYQ < 1.0 (Low Importance)"
            }
            
            PriorityTopicItem(
                subject = perf.subject,
                chapter = perf.chapter,
                priorityScore = priorityScore,
                priorityLabel = label,
                reason = reason,
                pyqAvg = perf.pyqAvg,
                accuracy = perf.accuracy
            )
        }.sortedByDescending { it.priorityScore }"""

pattern = r'\s*// Priority Topics\s*val priorityTopics = filteredPerformances\.map \{ perf ->.*?(?=PriorityTopicItem\()PriorityTopicItem\(\s*subject = perf\.subject,\s*chapter = perf\.chapter,\s*priorityScore = priorityScore,\s*priorityLabel = label,\s*reason = reason,\s*pyqAvg = perf\.pyqAvg,\s*accuracy = perf\.accuracy\s*\)\s*\}\.sortedByDescending \{ it\.priorityScore \}'

new_content = re.sub(pattern, replacement, content, flags=re.MULTILINE | re.DOTALL)

with open('app/src/main/java/com/example/data/util/GuidanceEngine.kt', 'w') as f:
    f.write(new_content)
