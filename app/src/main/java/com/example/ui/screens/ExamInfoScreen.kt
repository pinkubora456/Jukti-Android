package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ExamUpdateEntity
import com.example.ui.components.BilingualText
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

enum class HeroTab(val id: String, val titleEn: String, val titleAs: String, val icon: ImageVector) {
    SYLLABUS("Syllabus", "Syllabus", "পাঠ্যক্ৰম", Icons.Default.MenuBook),
    PATTERN("Pattern", "Exam Pattern", "পৰীক্ষাৰ আৰ্হি", Icons.Default.Assignment),
    CUTOFF("Cutoff", "Cutoff Marks", "কাট-অফ নম্বৰ", Icons.Default.BarChart)
}

@Composable
fun ExamInfoScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val updates by viewModel.examUpdates.collectAsState()
    val examsList by viewModel.examsList.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE
    val context = LocalContext.current

    var selectedHeroTab by remember { mutableStateOf(HeroTab.PATTERN) }
    var selectedExamTab by remember { mutableStateOf("All") }
    val examTabs = remember(examsList) {
        listOf("All") + examsList.map { it.title }
    }

    LaunchedEffect(examTabs) {
        if (selectedExamTab != "All" && !examTabs.contains(selectedExamTab)) {
            selectedExamTab = "All"
        }
    }

    val filteredUpdates = updates.filter { update ->
        val examNameLower = update.examName.lowercase()
        val titleLower = update.titleEn.lowercase()
        if (examNameLower.contains("dummy") || examNameLower.contains("test") ||
            titleLower.contains("dummy") || titleLower.contains("test")) {
            return@filter false
        }
        val matchesExam = (selectedExamTab == "All" || update.examName.equals(selectedExamTab, ignoreCase = true) || update.examName.contains(selectedExamTab, ignoreCase = true))
        val matchesCategory = when (selectedHeroTab) {
            HeroTab.SYLLABUS -> update.category.contains("Syllabus", ignoreCase = true) || update.titleEn.contains("Syllabus", ignoreCase = true)
            HeroTab.PATTERN -> update.category.contains("Pattern", ignoreCase = true) || update.titleEn.contains("Pattern", ignoreCase = true)
            HeroTab.CUTOFF -> update.category.contains("Cutoff", ignoreCase = true) || update.titleEn.contains("Cutoff", ignoreCase = true)
        }
        matchesExam && matchesCategory
    }

    var showExamPatternChoiceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP APP BAR WITH BACK BUTTON
        com.example.ui.components.JuktiTopAppBar(
            title = "Exam Pattern & Cutoff Hub",
            subtitle = "Syllabus, Exam Patterns & Category Cutoffs",
            onBackClick = { viewModel.navigateTo(Screen.HOME) },
            actions = {
                if (isAdminOrOwner) {
                    IconButton(
                        onClick = { showExamPatternChoiceDialog = true },
                        modifier = Modifier.testTag("admin_edit_exam_info_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Manage Syllabus & Cutoffs",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )

        if (showExamPatternChoiceDialog) {
            AlertDialog(
                onDismissRequest = { showExamPatternChoiceDialog = false },
                title = { Text("Exam Patterns, Syllabus & Cutoff") },
                text = { Text("Choose an option:") },
                confirmButton = {
                    TextButton(onClick = {
                        showExamPatternChoiceDialog = false
                        viewModel.navigateTo(Screen.MANAGE_EXAM_PATTERN_CUTOFF_UPDATE)
                    }) {
                        Text("Update (Add New)")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showExamPatternChoiceDialog = false
                        viewModel.navigateTo(Screen.MANAGE_EXAM_PATTERN_CUTOFF_VIEW)
                    }) {
                        Text("View (All, Edit & Delete)")
                    }
                }
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column {
                // HERO BAR FOR SYLLABUS, EXAM PATTERN, CUTOFF, NOTIFICATIONS
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(HeroTab.values()) { tab ->
                        val isSelected = selectedHeroTab == tab
                        val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

                        Surface(
                            onClick = { selectedHeroTab = tab },
                            shape = RoundedCornerShape(16.dp),
                            color = containerColor,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else contentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tab.titleEn,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = contentColor
                                )
                            }
                        }
                    }
                }

                // EXAM SELECTOR TABS
                ScrollableTabRow(
                    selectedTabIndex = examTabs.indexOf(selectedExamTab).coerceAtLeast(0),
                    edgePadding = 12.dp,
                    divider = {}
                ) {
                    examTabs.forEach { exam ->
                        Tab(
                            selected = (selectedExamTab == exam),
                            onClick = { selectedExamTab = exam },
                            text = {
                                Text(
                                    text = exam,
                                    fontWeight = if (selectedExamTab == exam) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }
        }

        // CONTENT SECTION BASED ON HERO TAB SELECTION
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedHeroTab) {
                HeroTab.SYLLABUS -> {
                    item {
                        SyllabusHeroHeader(isAssamese)
                    }
                }
                HeroTab.PATTERN -> {
                    item {
                        ExamPatternHeroHeader(isAssamese)
                    }
                }
                HeroTab.CUTOFF -> {
                    item {
                        CutoffHeroHeader(isAssamese)
                    }
                }
            }

            if (filteredUpdates.isNotEmpty()) {
                items(filteredUpdates, key = { it.id }) { update ->
                    ExamUpdateItemCard(
                        update = update,
                        language = language,
                        onOpenLink = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.officialLink))
                            context.startActivity(intent)
                        }
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No entries found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No syllabus, exam pattern, or cutoff details have been added yet for this filter. Admin users can add or update entries via the top-right manage icon.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/* =====================================================================
   HERO HEADERS
   ===================================================================== */
@Composable
fun SyllabusHeroHeader(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Exam Syllabus & Subject Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Comprehensive topic-wise breakdown for all Assam Govt. Exams",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ExamPatternHeroHeader(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Exam Pattern & Marking Scheme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Duration, total marks, negative marking & question distribution",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun CutoffHeroHeader(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Previous Year & Expected Cutoffs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Category-wise minimum qualifying scores (UR, OBC, SC, ST, EWS)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/* =====================================================================
   SYLLABUS CARDS
   ===================================================================== */
@Composable
fun AdreSyllabusCard(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ADRE Grade 3 & 4 Syllabus Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text("1. General Knowledge & Assam Affairs:", fontWeight = FontWeight.Bold)
            Text("• Assam History (Ahom Dynasty, Freedom Movement, Modern Assam)", style = MaterialTheme.typography.bodySmall)
            Text("• Assam Geography, Rivers, National Parks & Culture", style = MaterialTheme.typography.bodySmall)
            Text("• Indian History, Constitution, Indian Economy & Current Affairs", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))
            Text("2. General English:", fontWeight = FontWeight.Bold)
            Text("• Tenses, Prepositions, Synonyms & Antonyms, Comprehension", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))
            Text("3. General Mathematics & Reasoning:", fontWeight = FontWeight.Bold)
            Text("• Class 10th level Arithmetic, Profit & Loss, Ratio, Percentage, Logical Series", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ApscSyllabusCard(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "APSC CCE Prelims & Mains Syllabus",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text("General Studies Paper I (200 Marks):", fontWeight = FontWeight.Bold)
            Text("• History, Culture & Heritage of Assam & India", style = MaterialTheme.typography.bodySmall)
            Text("• Indian & World Geography, Polity, Panchayati Raj", style = MaterialTheme.typography.bodySmall)
            Text("• Environmental Ecology, Biodiversity & Climate Change", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))
            Text("General Studies Paper II - CSAT (200 Marks):", fontWeight = FontWeight.Bold)
            Text("• Comprehension, Analytical Ability, Data Interpretation, Basic Numeracy", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun AssamPoliceSyllabusCard(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Assam Police SI & AB/UB Constable Syllabus",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text("• Assam History, Geography & Law Awareness", style = MaterialTheme.typography.bodySmall)
            Text("• General Knowledge, Current Events & Sports", style = MaterialTheme.typography.bodySmall)
            Text("• Logical Reasoning, Mental Ability & Mathematics", style = MaterialTheme.typography.bodySmall)
            Text("• Assamese Language & English Grammar Skills", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun TetSyllabusCard(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Assam Special TET Paper I & II Syllabus",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text("• Child Development & Pedagogy (30 Marks)", style = MaterialTheme.typography.bodySmall)
            Text("• Language I (Assamese / Bodo / Bengali) (30 Marks)", style = MaterialTheme.typography.bodySmall)
            Text("• Language II (English) (30 Marks)", style = MaterialTheme.typography.bodySmall)
            Text("• Mathematics & Science / Social Studies (60 Marks)", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/* =====================================================================
   EXAM PATTERN CARDS
   ===================================================================== */
@Composable
fun AdreExamPatternCardDetailed(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ADRE Grade 3 & 4 Exam Pattern (2026)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("• Total Questions: 100 MCQs")
            Text("• Total Marks: 150 Marks")
            Text("• Duration: 90 Minutes (1.5 Hours)")
            Text("• Negative Marking: 0.25 Marks per wrong answer")
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Subject-wise Weightage:",
                fontWeight = FontWeight.Bold
            )
            Text("1. General Knowledge & Assam GK: 35 Qs (52.5 Marks)")
            Text("2. General English: 25 Qs (37.5 Marks)")
            Text("3. General Mathematics: 20 Qs (30 Marks)")
            Text("4. Logical Reasoning: 20 Qs (30 Marks)")
        }
    }
}

@Composable
fun ApscExamPatternCardDetailed(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "APSC CCE Prelims Exam Pattern",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("• Paper 1: General Studies 1 (200 Marks, 100 Qs)")
            Text("• Paper 2: GS Paper 2 - CSAT (200 Marks, 80 Qs - Qualifying 33%)")
            Text("• Duration: 2 Hours per Paper")
            Text("• Negative Marking: 0.25 Marks deducted per wrong attempt")
        }
    }
}

@Composable
fun AssamPoliceExamPatternCardDetailed(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Assam Police SI & AB/UB Constable Pattern",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("• Written Test: 100 Marks (100 Questions)")
            Text("• Physical Efficiency Test (PET): 40 Marks")
            Text("• Extra Qualification / NCC / Computer: 10 Marks")
            Text("• Total Merit Base: 150 Marks")
        }
    }
}

@Composable
fun TetExamPatternCardDetailed(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Assam Special TET Exam Pattern",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("• Total Questions: 150 MCQs")
            Text("• Total Marks: 150 Marks (No negative marking)")
            Text("• Duration: 150 Minutes")
            Text("• Minimum Passing Cutoff: 60% (90 Marks) for General, 55% (83.5 Marks) for Reserved categories")
        }
    }
}

/* =====================================================================
   CUTOFF CARDS
   ===================================================================== */
@Composable
fun AdreCutoffCard(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ADRE Grade 3 & 4 Expected Cutoff Marks (Out of 150)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            CutoffRow("Unreserved (UR General)", "118 - 124 Marks")
            CutoffRow("OBC / MOBC Category", "110 - 116 Marks")
            CutoffRow("EWS Category", "108 - 114 Marks")
            CutoffRow("SC Category", "102 - 108 Marks")
            CutoffRow("ST (Plains) / ST (Hills)", "96 - 102 Marks")
        }
    }
}

@Composable
fun ApscCutoffCard(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "APSC CCE Prelims GS Paper 1 Cutoff (Out of 200)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            CutoffRow("General (UR Male)", "112 - 118 Marks")
            CutoffRow("General (UR Female)", "106 - 112 Marks")
            CutoffRow("OBC / MOBC Category", "104 - 110 Marks")
            CutoffRow("SC / ST Category", "95 - 102 Marks")
        }
    }
}

@Composable
fun AssamPoliceCutoffCard(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Assam Police SI Written Test Cutoff (Out of 100)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            CutoffRow("UR Male Candidate", "74 - 78 / 100")
            CutoffRow("UR Female Candidate", "68 - 72 / 100")
            CutoffRow("OBC / MOBC", "69 - 73 / 100")
            CutoffRow("SC / ST Candidate", "62 - 67 / 100")
        }
    }
}

@Composable
fun TetCutoffCard(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Assam Special TET Qualifying Cutoff Marks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            CutoffRow("General / Unreserved", "90 Marks (60%)")
            CutoffRow("OBC / SC / ST / PwD", "83.5 Marks (55%)")
        }
    }
}

@Composable
fun CutoffRow(category: String, score: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(category, style = MaterialTheme.typography.bodyMedium)
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = score,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/* =====================================================================
   EXAM UPDATE CARD COMPONENT
   ===================================================================== */
@Composable
fun ExamUpdateItemCard(
    update: ExamUpdateEntity,
    language: AppLanguage,
    onOpenLink: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${update.examName} • ${update.category}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = update.updateDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            BilingualText(
                textEn = update.titleEn,
                textAs = update.titleAs,
                language = language,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            BilingualText(
                textEn = update.detailEn,
                textAs = update.detailAs,
                language = language,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenLink,
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Official Website Link")
            }
        }
    }
}
