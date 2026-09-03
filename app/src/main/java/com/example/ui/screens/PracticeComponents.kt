package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BilingualText
import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.theme.success
import com.example.ui.theme.successContainer
import com.example.ui.viewmodel.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeSubjectBannerCard(
    banner: BannerConfig,
    availableChapters: List<String>,
    selectedChapters: Set<String>,
    onChaptersChanged: (Set<String>) -> Unit,
    totalQuestionsCount: Int,
    actionButtonTextEn: String,
    actionButtonTextAs: String,
    onStartClick: () -> Unit,
    isAssamese: Boolean,
    chapterCounts: Map<String, Int> = emptyMap()
) {
    var expanded by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()
    val actualContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else banner.containerColor
    val actualTitleColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF1C1B1F)
    val actualSubtitleColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF49454F)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = actualContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Icon + Title + Subtitle + Total Count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = banner.iconColor,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = banner.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = banner.titleEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = actualTitleColor
                        )
                        Surface(
                            color = banner.iconColor,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$totalQuestionsCount Qs",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = banner.subtitleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = actualSubtitleColor
                    )
                }
            }

            // Chapter Dropdown (Single / Multiple selection)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                val labelText = when {
                    selectedChapters.isEmpty() -> "All Chapters Selected (Mix All)"
                    selectedChapters.size == 1 -> selectedChapters.first()
                    else -> "${selectedChapters.size} Chapters Selected"
                }

                SafeOutlinedTextField(
                    value = labelText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Chapters (Single/Multiple)", fontSize = 12.sp, color = actualSubtitleColor) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
                        focusedContainerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
                        unfocusedBorderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color(0xFFC4C6D0),
                        focusedBorderColor = banner.iconColor,
                        unfocusedLabelColor = actualSubtitleColor,
                        focusedLabelColor = banner.iconColor,
                        unfocusedTextColor = actualTitleColor,
                        focusedTextColor = actualTitleColor,
                        unfocusedTrailingIconColor = actualSubtitleColor,
                        focusedTrailingIconColor = banner.iconColor
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.heightIn(max = 280.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedChapters.isEmpty(),
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "All Chapters (Mix All)",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        onClick = {
                            onChaptersChanged(emptySet())
                            expanded = false
                        }
                    )

                    if (availableChapters.isNotEmpty()) {
                        HorizontalDivider()
                        availableChapters.forEach { chapter ->
                            val isChecked = selectedChapters.contains(chapter)
                            val count = chapterCounts[chapter] ?: 0
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "$chapter ($count Qs)")
                                    }
                                },
                                onClick = {
                                    val updated = if (isChecked) selectedChapters - chapter else selectedChapters + chapter
                                    onChaptersChanged(updated)
                                }
                            )
                        }
                    }
                }
            }

            // Display Chips if chapters selected
            if (selectedChapters.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectedChapters.forEach { ch ->
                        InputChip(
                            selected = true,
                            onClick = { onChaptersChanged(selectedChapters - ch) },
                            label = { Text(ch, style = MaterialTheme.typography.labelSmall, color = Color.White) },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = banner.iconColor,
                                selectedLabelColor = Color.White,
                                selectedTrailingIconColor = Color.White
                            ),
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(12.dp), tint = Color.White)
                            }
                        )
                    }
                }
            }

            // Start Option Button
            Button(
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = banner.iconColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = actionButtonTextEn,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun PracticeSummaryView(
    questions: List<com.example.data.local.QuestionEntity>,
    userAnswers: Map<Long, Int>,
    totalTimeSeconds: Int,
    onFinish: () -> Unit,
    onPracticeAgain: () -> Unit,
    questionLanguage: AppLanguage,
    isSmartPractice: Boolean = false
) {
    var correctCount = 0
    var incorrectCount = 0
    val unattemptedCount = (questions.size - userAnswers.size).coerceAtLeast(0)

    val answeredQuestions = mutableListOf<Pair<com.example.data.local.QuestionEntity, Boolean>>()

    questions.forEach { q ->
        val answerIndex = userAnswers[q.id]
        if (answerIndex != null) {
            val isCorrect = answerIndex == q.correctOptionIndex
            if (isCorrect) correctCount++ else incorrectCount++
            answeredQuestions.add(q to isCorrect)
        }
    }

    val totalAttempted = correctCount + incorrectCount
    val accuracy = if (totalAttempted > 0) (correctCount.toFloat() / totalAttempted * 100).toInt() else 0
    val score = correctCount * 10
    val avgSpeedPerMcq = if (totalAttempted > 0) (totalTimeSeconds.toFloat() / totalAttempted).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isSmartPractice) "Smart Practice Complete 🎯" else "Practice Summary 🎉",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "${questions.size} Questions in Session • $totalAttempted Attempted",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Key Performance Cards Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SummaryStatCard(title = "Score", count = score, subtitle = "pts", color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
            }
            Box(modifier = Modifier.weight(1f)) {
                SummaryStatCard(title = "Accuracy", count = accuracy, subtitle = "%", color = if (accuracy >= 60) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error)
            }
        }

        // Breakdown Cards Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SummaryStatCard(title = "Correct", count = correctCount, subtitle = "", color = MaterialTheme.colorScheme.success)
            }
            Box(modifier = Modifier.weight(1f)) {
                SummaryStatCard(title = "Incorrect", count = incorrectCount, subtitle = "", color = MaterialTheme.colorScheme.error)
            }
            Box(modifier = Modifier.weight(1f)) {
                SummaryStatCard(title = "Skipped", count = unattemptedCount, subtitle = "", color = MaterialTheme.colorScheme.outline)
            }
        }

        // Time & Solving Speed Row 3
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val m = totalTimeSeconds / 60
                    val s = totalTimeSeconds % 60
                    Text(
                        text = if (m > 0) "${m}m ${s}s" else "${s}s",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = "Time Spent", style = MaterialTheme.typography.labelSmall)
                }
                HorizontalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (avgSpeedPerMcq > 0) "${avgSpeedPerMcq}s" else "-",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = "Speed / MCQ", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onPracticeAgain,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Practice Again")
            }
            Button(
                onClick = onFinish,
                modifier = Modifier.weight(1f)
            ) {
                Text("Finish Practice")
            }
        }

        if (answeredQuestions.isNotEmpty()) {
            Text(
                text = "Detailed Question Review",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 16.dp, bottom = 8.dp)
            )

            answeredQuestions.forEachIndexed { index, (q, isCorrect) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) MaterialTheme.colorScheme.successContainer.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isCorrect) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        com.example.ui.components.QuestionTypeBadge(
                            questionType = q.questionType,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isCorrect) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp).padding(end = 8.dp)
                            )
                            BilingualText(
                                textEn = "${index + 1}. ${q.questionEn}",
                                textAs = "${index + 1}. ${q.questionAs}",
                                language = questionLanguage,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val selectedAnsIndex = userAnswers[q.id] ?: -1
                        val selectedAnsEn = when (selectedAnsIndex) {
                            0 -> q.optionAEn; 1 -> q.optionBEn; 2 -> q.optionCEn; 3 -> q.optionDEn; else -> ""
                        }
                        val correctAnsEn = when (q.correctOptionIndex) {
                            0 -> q.optionAEn; 1 -> q.optionBEn; 2 -> q.optionCEn; 3 -> q.optionDEn; else -> ""
                        }

                        Text(
                            text = "Your Answer: $selectedAnsEn",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCorrect) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
                        )
                        if (!isCorrect) {
                            Text(
                                text = "Correct Answer: $correctAnsEn",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.success
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStatCard(title: String, count: Int, subtitle: String = "", color: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (subtitle.isNotBlank()) "$count$subtitle" else count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}
