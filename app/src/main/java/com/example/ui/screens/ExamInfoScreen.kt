package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
    ALL("All", "All Updates", "সকলো জাননী", Icons.Default.AllInclusive),
    SYLLABUS("Syllabus", "Syllabus", "পাঠ্যক্ৰম", Icons.Default.MenuBook),
    PATTERN("Pattern", "Exam Pattern", "পৰীক্ষাৰ আৰ্হি", Icons.Default.Assignment),
    CUTOFF("Cutoff", "Cutoff Marks", "কাট-অফ নম্বৰ", Icons.Default.BarChart),
    NOTICES("Notification", "Official Notices", "চৰকাৰী জাননী", Icons.Default.NotificationsActive),
    ADMIT_CARD("Admit Card", "Admit Cards", "এডমিট কাৰ্ড", Icons.Default.ConfirmationNumber)
}

@Composable
fun ExamInfoScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val updates by viewModel.examUpdates.collectAsState()
    val examsList by viewModel.examsList.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE
    val context = LocalContext.current

    var selectedHeroTab by remember { mutableStateOf(HeroTab.ALL) }
    var selectedExamTab by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val examTabs = remember(examsList) {
        listOf("All") + examsList.map { it.title }
    }

    LaunchedEffect(examTabs) {
        if (selectedExamTab != "All" && !examTabs.contains(selectedExamTab)) {
            selectedExamTab = "All"
        }
    }

    val filteredUpdates = remember(updates, selectedExamTab, selectedHeroTab, searchQuery) {
        updates.filter { update ->
            val matchesExam = (selectedExamTab == "All" ||
                    update.examName.equals(selectedExamTab, ignoreCase = true) ||
                    update.examName.contains(selectedExamTab, ignoreCase = true))

            val matchesCategory = when (selectedHeroTab) {
                HeroTab.ALL -> true
                HeroTab.SYLLABUS -> update.category.contains("Syllabus", ignoreCase = true) ||
                        update.titleEn.contains("Syllabus", ignoreCase = true)
                HeroTab.PATTERN -> update.category.contains("Pattern", ignoreCase = true) ||
                        update.titleEn.contains("Pattern", ignoreCase = true)
                HeroTab.CUTOFF -> update.category.contains("Cutoff", ignoreCase = true) ||
                        update.titleEn.contains("Cutoff", ignoreCase = true)
                HeroTab.NOTICES -> update.category.contains("Notification", ignoreCase = true) ||
                        update.category.contains("Notice", ignoreCase = true) ||
                        update.titleEn.contains("Notice", ignoreCase = true) ||
                        update.titleEn.contains("Notification", ignoreCase = true)
                HeroTab.ADMIT_CARD -> update.category.contains("Admit Card", ignoreCase = true) ||
                        update.titleEn.contains("Admit Card", ignoreCase = true)
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                update.titleEn.lowercase().contains(q) ||
                        update.titleAs.lowercase().contains(q) ||
                        update.detailEn.lowercase().contains(q) ||
                        update.detailAs.lowercase().contains(q) ||
                        update.examName.lowercase().contains(q)
            }

            matchesExam && matchesCategory && matchesSearch
        }
    }

    var showExamPatternChoiceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP APP BAR WITH BACK BUTTON
        com.example.ui.components.JuktiTopAppBar(
            title = if (isAssamese) "পৰীক্ষাৰ জাননী আৰু নিৰ্দেশনা" else "Exam Info, Pattern & Syllabus",
            subtitle = if (isAssamese) "পাঠ্যক্ৰম, পৰীক্ষাৰ আৰ্হি আৰু কাট-অফ নম্বৰ" else "Official Syllabus, Pattern, Cutoffs & Notifications",
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
                text = { Text("Choose an action:") },
                confirmButton = {
                    TextButton(onClick = {
                        showExamPatternChoiceDialog = false
                        viewModel.navigateTo(Screen.MANAGE_EXAM_PATTERN_CUTOFF_UPDATE)
                    }) {
                        Text("Add New Update")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showExamPatternChoiceDialog = false
                        viewModel.navigateTo(Screen.MANAGE_EXAM_PATTERN_CUTOFF_VIEW)
                    }) {
                        Text("Manage Existing")
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
                                    text = if (isAssamese) tab.titleAs else tab.titleEn,
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

        // SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isAssamese) "পৰীক্ষাৰ জাননী সন্ধান কৰক..." else "Search exam info, syllabus, cutoff...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // CONTENT SECTION BASED ON HERO TAB SELECTION
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedHeroTab) {
                HeroTab.ALL -> {
                    item {
                        AllUpdatesHeroHeader(isAssamese)
                    }
                }
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
                HeroTab.NOTICES -> {
                    item {
                        NoticesHeroHeader(isAssamese)
                    }
                }
                HeroTab.ADMIT_CARD -> {
                    item {
                        AdmitCardHeroHeader(isAssamese)
                    }
                }
            }

            if (filteredUpdates.isNotEmpty()) {
                items(filteredUpdates, key = { it.id }) { update ->
                    ExamUpdateItemCard(
                        update = update,
                        language = language,
                        onOpenLink = {
                            if (update.officialLink.isNotBlank()) {
                                try {
                                    val url = if (update.officialLink.startsWith("http://") || update.officialLink.startsWith("https://")) {
                                        update.officialLink
                                    } else {
                                        "https://${update.officialLink}"
                                    }
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "No official link provided for this entry.", Toast.LENGTH_SHORT).show()
                            }
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
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isAssamese) "কোনো তথ্য পোৱা নগ'ল" else "No entries found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isAssamese)
                                    "নিৰ্বাচিত ফিল্টাৰৰ বাবে কোনো তথ্য উপলব্ধ নহয়। এডমিনে ওপৰৰ এডিট বুটামৰ সহায়ত নতুন তথ্য যোগ কৰিব পাৰে।"
                                else
                                    "No syllabus, exam pattern, or cutoff details are available for this selection yet.",
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
fun AllUpdatesHeroHeader(isAssamese: Boolean) {
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
                imageVector = Icons.Default.AllInclusive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isAssamese) "পৰীক্ষাৰ সম্পূৰ্ণ জাননী আৰু তথ্য" else "All Exam Information & Updates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (isAssamese) "পাঠ্যক্ৰম, নম্বৰ বিভাজন আৰু আনুষ্ঠানিক জাননী" else "Official syllabus, exam pattern, cutoffs & notices from government portals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

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
                    text = if (isAssamese) "পাঠ্যক্ৰম আৰু বিষয়বস্তু" else "Exam Syllabus & Subject Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (isAssamese) "অসমৰ চৰকাৰী পৰীক্ষাৰ অধ্যায়ভিত্তিক পাঠ্যক্ৰম" else "Comprehensive topic-wise breakdown for all Assam Govt. Exams",
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
                    text = if (isAssamese) "পৰীক্ষাৰ আৰ্হি আৰু নম্বৰ প্ৰণালী" else "Exam Pattern & Marking Scheme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = if (isAssamese) "সময়, মুঠ নম্বৰ, ঋণাত্মক নম্বৰ আৰু প্ৰশ্নৰ গাঁথনি" else "Duration, total marks, negative marking & question distribution",
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
                    text = if (isAssamese) "কাট-অফ নম্বৰ আৰু প্ৰত্যাশিত নম্বৰ" else "Previous Year & Expected Cutoffs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = if (isAssamese) "শ্ৰেণী অনুসৰি ন্যূনতম নম্বৰ (UR, OBC, SC, ST, EWS)" else "Category-wise minimum qualifying scores (UR, OBC, SC, ST, EWS)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun NoticesHeroHeader(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isAssamese) "অফিচিয়েল জাননী আৰু ঘোষণা" else "Official Notices & Announcements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isAssamese) "চৰকাৰী বিভাগসমূহৰ পৰা শেহতীয়া ঘোষণা" else "Latest notifications published by recruitment commissions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun AdmitCardHeroHeader(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isAssamese) "এডমিট কাৰ্ড আৰু ডাউনলোড লিংক" else "Admit Cards & Hall Tickets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isAssamese) "পৰীক্ষাৰ এডমিট কাৰ্ড ডাউনলোডৰ প্ৰত্যক্ষ লিংক" else "Direct portal links to download examination admit cards",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = update.examName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = update.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                if (update.updateDate.isNotBlank()) {
                    Text(
                        text = update.updateDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BilingualText(
                textEn = update.titleEn,
                textAs = update.titleAs,
                language = language,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (update.detailEn.isNotBlank() || update.detailAs.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                BilingualText(
                    textEn = update.detailEn,
                    textAs = update.detailAs,
                    language = language,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (update.officialLink.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onOpenLink,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.ASSAMESE) "অফিচিয়েল লিংক খোলক" else "Official Portal Link",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

