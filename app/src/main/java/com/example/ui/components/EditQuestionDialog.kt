package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.QuestionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuestionDialog(
    question: QuestionEntity,
    onDismiss: () -> Unit,
    onSave: (QuestionEntity) -> Unit
) {
    var subject by remember { mutableStateOf(question.subject) }
    var topic by remember { mutableStateOf(question.topic) }
    var difficulty by remember { mutableStateOf(question.difficulty) }
    var questionEn by remember { mutableStateOf(question.questionEn) }
    var questionAs by remember { mutableStateOf(question.questionAs) }
    
    var optionAEn by remember { mutableStateOf(question.optionAEn) }
    var optionBEn by remember { mutableStateOf(question.optionBEn) }
    var optionCEn by remember { mutableStateOf(question.optionCEn) }
    var optionDEn by remember { mutableStateOf(question.optionDEn) }

    var optionAAs by remember { mutableStateOf(question.optionAAs) }
    var optionBAs by remember { mutableStateOf(question.optionBAs) }
    var optionCAs by remember { mutableStateOf(question.optionCAs) }
    var optionDAs by remember { mutableStateOf(question.optionDAs) }

    var correctIndex by remember { mutableStateOf(question.correctOptionIndex) }
    var explanationEn by remember { mutableStateOf(question.explanationEn) }
    var explanationAs by remember { mutableStateOf(question.explanationAs) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Question", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SafeOutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
                SafeOutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic") },
                    modifier = Modifier.fillMaxWidth()
                )
                SafeOutlinedTextField(
                    value = difficulty,
                    onValueChange = { difficulty = it },
                    label = { Text("Difficulty (Easy/Medium/Hard)") },
                    modifier = Modifier.fillMaxWidth()
                )
                SafeOutlinedTextField(
                    value = questionEn,
                    onValueChange = { questionEn = it },
                    label = { Text("Question (English)") },
                    modifier = Modifier.fillMaxWidth()
                )
                SafeOutlinedTextField(
                    value = questionAs,
                    onValueChange = { questionAs = it },
                    label = { Text("Question (Assamese)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Options (English & Assamese)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SafeOutlinedTextField(
                        value = optionAEn,
                        onValueChange = { optionAEn = it },
                        label = { Text("Option A (En)") },
                        modifier = Modifier.weight(1f)
                    )
                    SafeOutlinedTextField(
                        value = optionAAs,
                        onValueChange = { optionAAs = it },
                        label = { Text("Option A (As)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SafeOutlinedTextField(
                        value = optionBEn,
                        onValueChange = { optionBEn = it },
                        label = { Text("Option B (En)") },
                        modifier = Modifier.weight(1f)
                    )
                    SafeOutlinedTextField(
                        value = optionBAs,
                        onValueChange = { optionBAs = it },
                        label = { Text("Option B (As)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SafeOutlinedTextField(
                        value = optionCEn,
                        onValueChange = { optionCEn = it },
                        label = { Text("Option C (En)") },
                        modifier = Modifier.weight(1f)
                    )
                    SafeOutlinedTextField(
                        value = optionCAs,
                        onValueChange = { optionCAs = it },
                        label = { Text("Option C (As)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SafeOutlinedTextField(
                        value = optionDEn,
                        onValueChange = { optionDEn = it },
                        label = { Text("Option D (En)") },
                        modifier = Modifier.weight(1f)
                    )
                    SafeOutlinedTextField(
                        value = optionDAs,
                        onValueChange = { optionDAs = it },
                        label = { Text("Option D (As)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Correct Answer", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("A" to 0, "B" to 1, "C" to 2, "D" to 3).forEach { (label, idx) ->
                        FilterChip(
                            selected = correctIndex == idx,
                            onClick = { correctIndex = idx },
                            label = { Text("Option $label") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                SafeOutlinedTextField(
                    value = explanationEn,
                    onValueChange = { explanationEn = it },
                    label = { Text("Explanation (English)") },
                    modifier = Modifier.fillMaxWidth()
                )
                SafeOutlinedTextField(
                    value = explanationAs,
                    onValueChange = { explanationAs = it },
                    label = { Text("Explanation (Assamese)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        question.copy(
                            subject = subject,
                            topic = topic,
                            difficulty = difficulty,
                            questionEn = questionEn,
                            questionAs = questionAs,
                            optionAEn = optionAEn,
                            optionBEn = optionBEn,
                            optionCEn = optionCEn,
                            optionDEn = optionDEn,
                            optionAAs = optionAAs,
                            optionBAs = optionBAs,
                            optionCAs = optionCAs,
                            optionDAs = optionDAs,
                            correctOptionIndex = correctIndex,
                            explanationEn = explanationEn,
                            explanationAs = explanationAs,
                            isReported = false
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
