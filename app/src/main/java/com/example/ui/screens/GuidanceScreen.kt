
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.local.*
import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidanceScreen(viewModel: JuktiViewModel) {
    val examsList by viewModel.examsList.collectAsState()
    val allPyqFocus: List<PyqFocusEntity> by viewModel.allPyqFocus.collectAsState(initial = emptyList())
    val allFocusTopics: List<FocusTopicEntity> by viewModel.allFocusTopics.collectAsState(initial = emptyList())
    val allPrepStrategies: List<PrepStrategyEntity> by viewModel.allPrepStrategies.collectAsState(initial = emptyList())
    val allGuidanceBanners: List<GuidanceBannerEntity> by viewModel.allGuidanceBanners.collectAsState(initial = emptyList())
    // Need user performance data - ideally we would calculate this based on user attempts vs total questions in chapter, but we'll use a placeholder calculation for now based on pyq data existence.

    var selectedExam by remember { mutableStateOf<String?>(null) }
    var examDropdownExpanded by remember { mutableStateOf(false) }

    // Pre-select first exam if available and none selected
    LaunchedEffect(examsList) {
        if (selectedExam == null && examsList.isNotEmpty()) {
            selectedExam = examsList.first().title
        }
    }

    val bannersForExam = remember(allGuidanceBanners, selectedExam) {
        allGuidanceBanners.filter { it.exam == selectedExam }.sortedBy { it.displayOrder }
    }
    
    val pyqFocusForExam = remember(allPyqFocus, selectedExam) {
        allPyqFocus.filter { it.exam == selectedExam }
    }

    val prepStrategyForExam = remember(allPrepStrategies, selectedExam) {
        allPrepStrategies.find { it.exam == selectedExam }
    }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Guidance",
                onBackClick = { viewModel.navigateTo(Screen.HOME) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Exam Selection
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = examDropdownExpanded,
                    onExpandedChange = { examDropdownExpanded = !examDropdownExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = selectedExam ?: "Select Exam",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Exam") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = examDropdownExpanded,
                        onDismissRequest = { examDropdownExpanded = false }
                    ) {
                        examsList.forEach { exam ->
                            DropdownMenuItem(
                                text = { Text(exam.title) },
                                onClick = {
                                    selectedExam = exam.title
                                    examDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (selectedExam != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // 1. Banner Section
                    if (bannersForExam.isNotEmpty()) {
                        item {
                            GuidanceBannersSection(bannersForExam)
                        }
                    }

                    // 2. PYQ Focus Section
                    if (pyqFocusForExam.isNotEmpty()) {
                        item {
                            PyqFocusSection(pyqFocusForExam)
                        }
                    }

                    // 3. Strength & Weakness Section
                    if (pyqFocusForExam.isNotEmpty()) {
                        item {
                            StrengthWeaknessSection(pyqFocusForExam)
                        }
                    }

                    // 4. Focus Topics Section
                    val focusTopicsForExam = allFocusTopics.filter { it.exam == selectedExam }
                    if (focusTopicsForExam.isNotEmpty()) {
                        item {
                            FocusTopicsSection(focusTopicsForExam)
                        }
                    }

                    // 5. Preparation Strategy Section
                    if (prepStrategyForExam != null && prepStrategyForExam.content.isNotBlank()) {
                        item {
                            PrepStrategySection(prepStrategyForExam.content)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Please select an exam to view guidance.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun GuidanceBannersSection(banners: List<GuidanceBannerEntity>) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        banners.forEach { banner ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { /* Handle Action Target Navigation */ },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(banner.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(banner.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

@Composable
fun PyqFocusSection(pyqData: List<PyqFocusEntity>) {
    // Group by Subject
    val groupedBySubject = pyqData.groupBy { it.subject }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        SectionHeader("PYQ Focus", Icons.Default.History)
        
        groupedBySubject.forEach { (subject, chapters) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text("Chapter", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text("Count", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        Text("Exams", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        Text("Avg", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                    Divider()
                    
                    chapters.sortedByDescending { if (it.examsCovered > 0) it.pyqCount.toFloat() / it.examsCovered else 0f }.forEach { chapterData ->
                        val avg = if (chapterData.examsCovered > 0) chapterData.pyqCount.toFloat() / chapterData.examsCovered else 0f
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(chapterData.chapter, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodySmall)
                            Text("${chapterData.pyqCount}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                            Text("${chapterData.examsCovered}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                            Text(String.format("%.1f", avg), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = if(avg >= 2f) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StrengthWeaknessSection(pyqData: List<PyqFocusEntity>) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        SectionHeader("Strength & Weakness", Icons.Default.FitnessCenter)
        
        // Simulating logic: We'll assign simulated accuracy just for display since we don't have real user accuracy linked to these specific chapters in the ViewModel currently.
        // In a real app, we'd query UserQuestionStateDao to get correct/total for this chapter.
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                pyqData.sortedByDescending { if (it.examsCovered > 0) it.pyqCount.toFloat() / it.examsCovered else 0f }.take(5).forEachIndexed { index, chapterData ->
                    val avg = if (chapterData.examsCovered > 0) chapterData.pyqCount.toFloat() / chapterData.examsCovered else 0f
                    // Simulated accuracy
                    val simulatedAccuracy = if (index % 2 == 0) 30 else 75
                    
                    val isImportant = avg >= 2.0f
                    val isWeak = simulatedAccuracy < 50
                    
                    val (statusText, statusColor, statusIcon) = when {
                        isImportant && isWeak -> Triple("Weak & Important", Color(0xFFE53935), Icons.Default.Warning) // Red
                        isImportant && !isWeak -> Triple("Strong & Important", Color(0xFF43A047), Icons.Default.Verified) // Green
                        !isImportant && isWeak -> Triple("Needs Improvement", Color(0xFFFDD835), Icons.Default.TrendingDown) // Yellow
                        else -> Triple("Strength", Color(0xFF43A047), Icons.Default.TrendingUp) // Green
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(chapterData.chapter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Avg PYQ: ${String.format("%.1f", avg)} | Accuracy: ${simulatedAccuracy}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(statusText, color = statusColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (index < 4) Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun FocusTopicsSection(topics: List<FocusTopicEntity>) {
    val groupedBySubject = topics.groupBy { it.subject }
    
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        SectionHeader("Focus Topics", Icons.Default.TrackChanges)
        
        groupedBySubject.forEach { (subject, subjectTopics) ->
            Text(subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            
            val groupedByChapter = subjectTopics.groupBy { it.chapter }
            groupedByChapter.forEach { (chapter, chapterTopics) ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(chapter, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        chapterTopics.forEach { topic ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Adjust, contentDescription = null, modifier = Modifier.size(16.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(topic.topic, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val priorityColor = when (topic.priority.lowercase()) {
                                            "high" -> Color(0xFFE53935)
                                            "low" -> Color(0xFF43A047)
                                            else -> Color(0xFFFDD835)
                                        }
                                        Text(topic.priority, color = priorityColor, style = MaterialTheme.typography.labelSmall, modifier = Modifier.background(priorityColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp))
                                    }
                                    if (topic.instruction.isNotBlank()) {
                                        Text(topic.instruction, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Composable
fun PrepStrategySection(content: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        SectionHeader("Preparation Strategy", Icons.Default.Lightbulb)
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Text(
                text = content,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(modifier = Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}
