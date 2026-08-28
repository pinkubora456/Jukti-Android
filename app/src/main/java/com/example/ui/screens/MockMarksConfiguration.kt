package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.QuestionEntity
import com.example.ui.components.SafeOutlinedTextField

fun isComprehensionQuestion(q: QuestionEntity): Boolean {
    val sub = q.subject.trim().lowercase()
    val top = q.topic.trim().lowercase()
    val qType = q.questionType.trim().lowercase()
    return top.contains("comprehension") || 
           sub.contains("comprehension") || 
           qType.contains("comprehension") || 
           top.contains("passage") || 
           qType.contains("passage")
}

fun getQuestionSubject(q: QuestionEntity): String {
    return if (isComprehensionQuestion(q)) {
        "Reading Comprehension"
    } else {
        com.example.data.repository.normalizeSubjectName(q.subject)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksConfigurationSection(
    subjectMarks: Map<String, String>,
    onSubjectMarkChange: (String, String) -> Unit,
    selectedQuestions: List<QuestionEntity>,
    individualMarks: Map<Long, String>,
    onIndividualMarkChange: (Long, String) -> Unit,
    onResetIndividualMark: (Long) -> Unit
) {
    var showOverrides by remember { mutableStateOf(false) }

    val groupedBySubject = remember(selectedQuestions) {
        selectedQuestions.groupBy { getQuestionSubject(it) }
    }
    val canonicalOrder = com.example.data.repository.SampleData.CANONICAL_SUBJECTS
    val presentSubjects = remember(groupedBySubject) {
        groupedBySubject.keys.sortedBy { subj ->
            val idx = canonicalOrder.indexOf(subj)
            if (idx >= 0) idx else 999
        }
    }

    val totalQuestions = selectedQuestions.size

    val (totalMarks, overriddenCount) = remember(selectedQuestions, subjectMarks.toMap(), individualMarks.toMap()) {
        var sum = 0f
        var overrideCount = 0
        selectedQuestions.forEach { q ->
            val subj = getQuestionSubject(q)
            val subjMarkInput = subjectMarks[subj] ?: "1"
            val subjMarkVal = subjMarkInput.toFloatOrNull() ?: 1.0f
            val expectedSubjMark = if (subjMarkVal > 0f) subjMarkVal else 1.0f

            val customStr = individualMarks[q.id]
            val customVal = customStr?.toFloatOrNull()
            if (customVal != null && customVal > 0f && customVal != expectedSubjMark) {
                sum += customVal
                overrideCount++
            } else {
                sum += expectedSubjMark
            }
        }
        Pair(sum, overrideCount)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Subject-Wise Marks Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (presentSubjects.isEmpty()) {
                Text(
                    text = "No questions selected. Add questions to configure marks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presentSubjects.forEach { subjectName ->
                        val count = groupedBySubject[subjectName]?.size ?: 0
                        val currentVal = subjectMarks[subjectName] ?: "1"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = subjectName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Questions: $count",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                SafeOutlinedTextField(
                                    value = currentVal,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                            onSubjectMarkChange(subjectName, newValue)
                                        }
                                    },
                                    label = { Text("Marks/Q") },
                                    placeholder = { Text("1") },
                                    modifier = Modifier.width(96.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                }
            }

            if (totalQuestions > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Mark Calculation Breakdown:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        presentSubjects.forEach { subj ->
                            val qCount = groupedBySubject[subj]?.size ?: 0
                            val markVal = (subjectMarks[subj] ?: "1").toFloatOrNull() ?: 1.0f
                            val effectiveMark = if (markVal > 0f) markVal else 1.0f
                            val formattedMark = if (effectiveMark % 1f == 0f) effectiveMark.toInt().toString() else effectiveMark.toString()
                            val subjTotal = qCount * effectiveMark
                            val formattedSubjTotal = if (subjTotal % 1f == 0f) subjTotal.toInt().toString() else "%.1f".format(subjTotal)
                            Text(
                                text = "• $subj: $qCount × ${formattedMark}m = ${formattedSubjTotal}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (overriddenCount > 0) {
                            Text(
                                text = "Includes $overriddenCount individual question mark override(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Questions",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "$totalQuestions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Calculated Total Marks",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        val formattedMarks = if (totalMarks % 1f == 0f) totalMarks.toInt().toString() else "%.1f".format(totalMarks)
                        Text(
                            text = "$formattedMarks Marks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (selectedQuestions.isNotEmpty()) {
                TextButton(
                    onClick = { showOverrides = !showOverrides },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (showOverrides) "Hide Question Mark Overrides" else "Configure Individual Question Marks (${if (overriddenCount > 0) "$overriddenCount Overridden" else "Optional"})",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (showOverrides) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(visible = showOverrides) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        selectedQuestions.forEachIndexed { index, q ->
                            val subjKey = getQuestionSubject(q)
                            val subjMarkInput = subjectMarks[subjKey] ?: "1"
                            val subjMarkVal = subjMarkInput.toFloatOrNull() ?: 1.0f
                            val expectedDefault = if (subjMarkVal > 0f) subjMarkVal else 1.0f

                            val customStr = individualMarks[q.id] ?: ""
                            val customVal = customStr.toFloatOrNull()
                            val isGenuineOverride = customVal != null && customVal > 0f && customVal != expectedDefault

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isGenuineOverride) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                                     else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Q${index + 1}. ",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = subjKey,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = q.questionEn,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val displayPlaceholder = if (expectedDefault % 1f == 0f) expectedDefault.toInt().toString() else expectedDefault.toString()
                                        SafeOutlinedTextField(
                                            value = if (isGenuineOverride) customStr else displayPlaceholder,
                                            onValueChange = { input ->
                                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                                    val inputVal = input.toFloatOrNull()
                                                    if (inputVal != null && inputVal == expectedDefault) {
                                                        onResetIndividualMark(q.id)
                                                    } else {
                                                        onIndividualMarkChange(q.id, input)
                                                    }
                                                }
                                            },
                                            label = { Text("Mark") },
                                            modifier = Modifier.width(72.dp),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )

                                        if (isGenuineOverride) {
                                            IconButton(
                                                onClick = { onResetIndividualMark(q.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Reset to Default",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun calculateTotalMarksFromSubjectConfig(
    selectedQuestions: List<QuestionEntity>,
    subjectMarks: Map<String, String>,
    individualMarks: Map<Long, String>
): Float {
    var sum = 0f
    selectedQuestions.forEach { q ->
        val subj = getQuestionSubject(q)
        val subjMarkStr = subjectMarks[subj] ?: "1"
        val subjMarkVal = subjMarkStr.toFloatOrNull() ?: 1.0f
        val effectiveSubjMark = if (subjMarkVal > 0f) subjMarkVal else 1.0f

        val customStr = individualMarks[q.id]
        val customVal = customStr?.toFloatOrNull()
        if (customVal != null && customVal > 0f && customVal != effectiveSubjMark) {
            sum += customVal
        } else {
            sum += effectiveSubjMark
        }
    }
    return sum
}

fun calculateSubjectMarksJson(
    selectedQuestions: List<QuestionEntity>,
    subjectMarks: Map<String, String>
): String {
    val presentSubjects = selectedQuestions.map { getQuestionSubject(it) }.distinct()
    if (presentSubjects.isEmpty()) return "{}"
    val map = mutableMapOf<String, Float>()
    presentSubjects.forEach { subj ->
        val str = subjectMarks[subj] ?: "1"
        val floatVal = str.toFloatOrNull() ?: 1.0f
        val effective = if (floatVal > 0f) floatVal else 1.0f
        map[subj] = effective
    }
    val jsonObj = org.json.JSONObject()
    map.forEach { (k, v) -> jsonObj.put(k, v.toDouble()) }
    return jsonObj.toString()
}

fun calculateMockQuestionMarksJson(
    selectedQuestions: List<QuestionEntity>,
    subjectMarks: Map<String, String>,
    individualMarks: Map<Long, String>
): String {
    val qMarks = mutableMapOf<String, Float>()
    selectedQuestions.forEach { q ->
        val subj = getQuestionSubject(q)
        val subjMarkStr = subjectMarks[subj] ?: "1"
        val subjMarkVal = subjMarkStr.toFloatOrNull() ?: 1.0f
        val effectiveSubjMark = if (subjMarkVal > 0f) subjMarkVal else 1.0f

        val customVal = individualMarks[q.id]?.toFloatOrNull()
        if (customVal != null && customVal > 0f && customVal != effectiveSubjMark) {
            qMarks[q.id.toString()] = customVal
        }
    }
    if (qMarks.isEmpty()) return "{}"
    val jsonObj = org.json.JSONObject()
    qMarks.forEach { (k, v) -> jsonObj.put(k, v.toDouble()) }
    return jsonObj.toString()
}

fun getQuestionEffectiveMark(
    q: QuestionEntity,
    subjectMarksJsonStr: String,
    questionMarksJsonStr: String,
    defaultMark: Float = 1.0f
): Float {
    if (questionMarksJsonStr.isNotBlank() && questionMarksJsonStr != "{}") {
        try {
            val qObj = org.json.JSONObject(questionMarksJsonStr)
            val qIdStr = q.id.toString()
            if (qObj.has(qIdStr)) {
                val override = qObj.optDouble(qIdStr, -1.0)
                if (override > 0) return override.toFloat()
            }
        } catch (e: Exception) {}
    }

    val subjKey = getQuestionSubject(q)
    if (subjectMarksJsonStr.isNotBlank() && subjectMarksJsonStr != "{}") {
        try {
            val sObj = org.json.JSONObject(subjectMarksJsonStr)
            if (sObj.has(subjKey)) {
                val mark = sObj.optDouble(subjKey, -1.0)
                if (mark > 0) return mark.toFloat()
            }
            if (sObj.has(q.subject)) {
                val mark = sObj.optDouble(q.subject, -1.0)
                if (mark > 0) return mark.toFloat()
            }
        } catch (e: Exception) {}
    }

    return if (defaultMark > 0f) defaultMark else 1.0f
}
