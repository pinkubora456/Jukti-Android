package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.QuestionEntity

data class QuestionGroupConfig(
    val subject: String,
    val topic: String,
    val count: Int
) {
    val displayName = if (topic.isNotBlank() && subject != topic) "$subject - $topic" else subject
    val key = "$subject||$topic"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksConfigurationSection(
    testType: String,
    selectedMockSubject: String,
    selectedMockChapter: String,
    selectedQuestionIds: List<Long>,
    allQuestions: List<QuestionEntity>,
    groupMarks: MutableMap<String, String>,
    groupCustomMarks: MutableMap<String, String>
) {
    val dynamicGroups = remember(testType, selectedMockSubject, selectedMockChapter, selectedQuestionIds, allQuestions) {
        if (testType == "Subject-wise" && selectedMockSubject.isNotBlank()) {
            val count = allQuestions.count { it.subject.equals(selectedMockSubject, ignoreCase = true) }
            listOf(QuestionGroupConfig(selectedMockSubject, "", count))
        } else if (testType == "Chapter-wise" && selectedMockChapter.isNotBlank()) {
            val count = allQuestions.count { it.subject.equals(selectedMockSubject, ignoreCase = true) && it.topic.equals(selectedMockChapter, ignoreCase = true) }
            listOf(QuestionGroupConfig(selectedMockSubject, selectedMockChapter, count))
        } else {
            val selectedQs = allQuestions.filter { selectedQuestionIds.contains(it.id) }
            selectedQs.groupBy { "${it.subject}||${it.topic}" }.map { (key, list) ->
                val first = list.first()
                QuestionGroupConfig(first.subject, first.topic, list.size)
            }
        }
    }

    if (dynamicGroups.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Marks Configuration",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        var grandTotalQuestions = 0
        var grandTotalMarks = 0f

        dynamicGroups.forEach { group ->
            val markSelection = groupMarks[group.key] ?: "1.0"
            val customValue = groupCustomMarks[group.key] ?: ""
            val actualMarkFloat = if (markSelection == "Custom") {
                customValue.toFloatOrNull() ?: 0f
            } else {
                markSelection.toFloatOrNull() ?: 1.0f
            }
            
            val validMark = if (actualMarkFloat > 0f && !actualMarkFloat.isNaN() && !actualMarkFloat.isInfinite()) actualMarkFloat else 0f
            val groupTotal = group.count * validMark

            grandTotalQuestions += group.count
            grandTotalMarks += groupTotal

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = group.displayName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Questions: ${group.count}", style = MaterialTheme.typography.bodyMedium)
                        
                        var expanded by remember { mutableStateOf(false) }
                        val options = listOf("0.5", "1.0", "1.5", "2.0", "Custom")
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.width(120.dp)
                            ) {
                                OutlinedTextField(
                                    value = markSelection,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Marks") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor(),
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    options.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt) },
                                            onClick = {
                                                groupMarks[group.key] = opt
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            if (markSelection == "Custom") {
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = customValue,
                                    onValueChange = { newValue -> 
                                        // only allow numbers and decimal
                                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                            groupCustomMarks[group.key] = newValue
                                        }
                                    },
                                    label = { Text("Value") },
                                    modifier = Modifier.width(80.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    isError = validMark <= 0f
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Group Total: $groupTotal marks", 
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Summary:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Questions: $grandTotalQuestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Marks: $grandTotalMarks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

fun calculateMockQuestionMarksJson(
    testType: String,
    selectedMockSubject: String,
    selectedMockChapter: String,
    selectedQuestionIds: List<Long>,
    allQuestions: List<QuestionEntity>,
    groupMarks: Map<String, String>,
    groupCustomMarks: Map<String, String>
): String {
    val qMarks = mutableMapOf<String, Float>()
    
    val questionsToProcess = if (testType == "Subject-wise" && selectedMockSubject.isNotBlank()) {
        allQuestions.filter { it.subject.equals(selectedMockSubject, ignoreCase = true) }
    } else if (testType == "Chapter-wise" && selectedMockChapter.isNotBlank()) {
        allQuestions.filter { it.subject.equals(selectedMockSubject, ignoreCase = true) && it.topic.equals(selectedMockChapter, ignoreCase = true) }
    } else {
        allQuestions.filter { selectedQuestionIds.contains(it.id) }
    }

    questionsToProcess.forEach { q ->
        val key = if (testType == "Subject-wise") "${selectedMockSubject}||"
                  else if (testType == "Chapter-wise") "${selectedMockSubject}||${selectedMockChapter}"
                  else "${q.subject}||${q.topic}"
                  
        val markSelection = groupMarks[key] ?: "1.0"
        val customValue = groupCustomMarks[key] ?: ""
        val actualMarkFloat = if (markSelection == "Custom") {
            customValue.toFloatOrNull() ?: 1.0f
        } else {
            markSelection.toFloatOrNull() ?: 1.0f
        }
        val validMark = if (actualMarkFloat > 0f && !actualMarkFloat.isNaN() && !actualMarkFloat.isInfinite()) actualMarkFloat else 1.0f
        
        qMarks[q.id.toString()] = validMark
    }
    
    // Simple JSON serialization for Map<String, Float>
    return "{" + qMarks.entries.joinToString(",") { "\"${it.key}\":${it.value}" } + "}"
}

fun calculateTotalMarksFromConfig(
    testType: String,
    selectedMockSubject: String,
    selectedMockChapter: String,
    selectedQuestionIds: List<Long>,
    allQuestions: List<QuestionEntity>,
    groupMarks: Map<String, String>,
    groupCustomMarks: Map<String, String>
): Float {
    val questionsToProcess = if (testType == "Subject-wise" && selectedMockSubject.isNotBlank()) {
        allQuestions.filter { it.subject.equals(selectedMockSubject, ignoreCase = true) }
    } else if (testType == "Chapter-wise" && selectedMockChapter.isNotBlank()) {
        allQuestions.filter { it.subject.equals(selectedMockSubject, ignoreCase = true) && it.topic.equals(selectedMockChapter, ignoreCase = true) }
    } else {
        allQuestions.filter { selectedQuestionIds.contains(it.id) }
    }

    var total = 0f
    questionsToProcess.forEach { q ->
        val key = if (testType == "Subject-wise") "${selectedMockSubject}||"
                  else if (testType == "Chapter-wise") "${selectedMockSubject}||${selectedMockChapter}"
                  else "${q.subject}||${q.topic}"
                  
        val markSelection = groupMarks[key] ?: "1.0"
        val customValue = groupCustomMarks[key] ?: ""
        val actualMarkFloat = if (markSelection == "Custom") {
            customValue.toFloatOrNull() ?: 1.0f
        } else {
            markSelection.toFloatOrNull() ?: 1.0f
        }
        val validMark = if (actualMarkFloat > 0f && !actualMarkFloat.isNaN() && !actualMarkFloat.isInfinite()) actualMarkFloat else 1.0f
        total += validMark
    }
    return total
}
