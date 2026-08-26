package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.ui.theme.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MockTestEntity
import com.example.ui.components.BilingualText
import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MockTestsScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val mockTests by viewModel.mockTests.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE

    // Interactive Selection Banner state ("All", "Full-Length", "Subject-wise", "Chapter-wise")
    var selectedTestType by remember { mutableStateOf("All") }

    // Search state
    var searchQuery by remember { mutableStateOf("") }

    // Stats calculations
    val totalAvailable = mockTests.size
    val completedTests = mockTests.filter { it.isCompleted }
    val completedCount = completedTests.size
    val bestScorePct = if (completedTests.isNotEmpty()) {
        completedTests.maxOf { (it.userScore.toFloat() / it.totalMarks.toFloat() * 100f) }
    } else 0f
    val avgScorePct = if (completedTests.isNotEmpty()) {
        completedTests.map { (it.userScore.toFloat() / it.totalMarks.toFloat() * 100f) }.average().toFloat()
    } else 0f

    // Find last test in progress (if any)
    val unfinishedTest = mockTests.firstOrNull { it.inProgress && !it.isCompleted }

    // Filter logic
    val filteredMocks = mockTests.filter { mock ->
        val matchesType = when (selectedTestType) {
            "Full-Length" -> mock.testType == "Full-Length"
            "Subject-wise" -> mock.testType == "Subject-wise"
            "Chapter-wise" -> mock.testType == "Chapter-wise"
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                mock.titleEn.contains(searchQuery, ignoreCase = true) ||
                mock.titleAs.contains(searchQuery, ignoreCase = true) ||
                mock.subjectOrChapter.contains(searchQuery, ignoreCase = true) ||
                mock.category.contains(searchQuery, ignoreCase = true)

        matchesType && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Header Bar
        com.example.ui.components.JuktiTopAppBar(
            title = "Mock Test",
            onBackClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.HOME) }
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. TOP 2x2 STATISTICS GRID
            item {
                Column {
                    Text(
                        text = "Mock Performance Summary",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MockStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Available Mocks",
                            value = "$totalAvailable",
                            icon = Icons.Default.MenuBook,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        MockStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Completed Mocks",
                            value = "$completedCount",
                            icon = Icons.Default.CheckCircle,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MockStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Best Score",
                            value = if (completedCount > 0) String.format("%.1f%%", bestScorePct) else "N/A",
                            icon = Icons.Default.EmojiEvents,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        MockStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Average Score",
                            value = if (completedCount > 0) String.format("%.1f%%", avgScorePct) else "N/A",
                            icon = Icons.Default.BarChart,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. LARGE INTERACTIVE SELECTION BANNER (Replaces promotional hero banner)
            item {
                Column {
                    Text(
                        text = "Select Mock Test Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Horizontally Scrollable Hero Bar
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // All Mock Tests
                        item {
                            HeroCategoryChip(
                                title = "All Mocks",
                                count = mockTests.size,
                                icon = Icons.Default.GridView,
                                isSelected = selectedTestType == "All",
                                onClick = { selectedTestType = "All" }
                            )
                        }
                        // Full Length
                        item {
                            HeroCategoryChip(
                                title = "Full Length",
                                count = mockTests.count { it.testType == "Full-Length" },
                                icon = Icons.Default.Assignment,
                                isSelected = selectedTestType == "Full-Length",
                                onClick = { selectedTestType = "Full-Length" }
                            )
                        }
                        // Subject
                        item {
                            HeroCategoryChip(
                                title = "Subject",
                                count = mockTests.count { it.testType == "Subject-wise" },
                                icon = Icons.Default.MenuBook,
                                isSelected = selectedTestType == "Subject-wise",
                                onClick = { selectedTestType = "Subject-wise" }
                            )
                        }
                        // Chapter
                        item {
                            HeroCategoryChip(
                                title = "Chapter",
                                count = mockTests.count { it.testType == "Chapter-wise" },
                                icon = Icons.Default.FormatListBulleted,
                                isSelected = selectedTestType == "Chapter-wise",
                                onClick = { selectedTestType = "Chapter-wise" }
                            )
                        }
                    }
                }
            }

            // 3. "CONTINUE LAST MOCK" CARD (if unfinished test exists)
            if (unfinishedTest != null) {
                item {
                    ContinueLastMockCard(
                        mock = unfinishedTest,
                        language = language,
                        onResume = { viewModel.selectMockTest(unfinishedTest) }
                    )
                }
            }

            // 4. SEARCH BAR
            item {
                SafeOutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search mock test name or subject...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // 5. MOCK TEST CARDS LIST
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Mock Tests (${filteredMocks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (searchQuery.isNotEmpty() || selectedTestType != "All") {
                        TextButton(onClick = {
                            searchQuery = ""
                            selectedTestType = "All"
                        }) {
                            Text(
                                text = "Clear Search",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            if (filteredMocks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        com.example.ui.components.EmptyStateIllustration(
                            type = com.example.ui.components.EmptyStateType.STUDENT_JAAPI,
                            title = "No Mock Tests Found",
                            message = "Try clearing your search query or filters",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                items(filteredMocks, key = { it.id }) { mock ->
                    MockTestCardDetailed(
                        mock = mock,
                        language = language,
                        onStart = { viewModel.selectMockTest(mock) },
                        onViewResult = { viewModel.viewMockResultForTest(mock) }
                    )
                }
            }
        }
    }
}

/* =====================================================================
   COMPONENTS
   ===================================================================== */

@Composable
fun MockStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = contentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MockTypeBannerCard(
    modifier: Modifier = Modifier,
    title: String,
    countText: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bannerScale"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = countText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ContinueLastMockCard(
    mock: MockTestEntity,
    language: AppLanguage,
    onResume: () -> Unit
) {
    val isAssamese = language == AppLanguage.ASSAMESE
    val progressRatio = if (mock.totalQuestions > 0) mock.questionsAnswered.toFloat() / mock.totalQuestions.toFloat() else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CONTINUE LAST MOCK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                val remH = (mock.timeRemainingSeconds / 3600).coerceAtLeast(0)
                val remM = ((mock.timeRemainingSeconds % 3600) / 60).coerceAtLeast(0)
                val remS = (mock.timeRemainingSeconds % 60).coerceAtLeast(0)
                Text(
                    text = "%02d:%02d:%02d remaining".format(remH, remM, remS),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            BilingualText(
                textEn = mock.titleEn,
                textAs = mock.titleAs,
                language = language,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${mock.questionsAnswered} of ${mock.totalQuestions} questions answered (${(progressRatio * 100).toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Resume Test",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MockTestCardDetailed(
    mock: MockTestEntity,
    language: AppLanguage,
    onStart: () -> Unit,
    onViewResult: (() -> Unit)? = null
) {
    val isAssamese = language == AppLanguage.ASSAMESE

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Test Type Badge
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = mock.testType,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    // Difficulty Badge
                    val (diffBg, diffFg) = when (mock.difficulty.lowercase()) {
                        "easy" -> MaterialTheme.colorScheme.successContainer to MaterialTheme.colorScheme.onSuccessContainer
                        "hard" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.warningContainer to MaterialTheme.colorScheme.onWarningContainer
                    }
                    Surface(
                        color = diffBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = mock.difficulty,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = diffFg
                        )
                    }
                }

                // Free vs Premium Badge
                if (mock.isPremium) {
                    Surface(
                        color = MaterialTheme.colorScheme.warningContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.warning),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Premium",
                                tint = MaterialTheme.colorScheme.warning,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "PREMIUM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onWarningContainer
                            )
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "FREE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Test Name
            BilingualText(
                textEn = mock.titleEn,
                textAs = mock.titleAs,
                language = language,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )



            Spacer(modifier = Modifier.height(12.dp))

            // 4 Specs Grid (Questions, Time, Marks, Negative Marking)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecItem(
                            icon = Icons.Default.Quiz,
                            label = "${mock.totalQuestions} Questions"
                        )
                        SpecItem(
                            icon = Icons.Default.Timer,
                            label = "${mock.durationMinutes} Mins"
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecItem(
                            icon = Icons.Default.EmojiEvents,
                            label = "${mock.totalMarks} Marks"
                        )
                        SpecItem(
                            icon = Icons.Default.Warning,
                            label = "Neg: ${mock.negativeMarking}"
                        )
                    }
                }
            }

            // Completed score banner (if completed)
            if (mock.isCompleted) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Score: ${mock.userScore}/${mock.totalMarks} (${mock.userAccuracy.toInt()}%)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Rank #${mock.userRank}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            if (mock.isCompleted && onViewResult != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewResult,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "View Result",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Button(
                        onClick = onStart,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Re-attempt",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            } else {
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (mock.inProgress) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (mock.inProgress) {
                            "Resume Test"
                        } else {
                            "Start Mock Test"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SpecItem(
    icon: ImageVector,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MockTestListItem(
    mock: MockTestEntity,
    language: AppLanguage,
    onStart: () -> Unit
) {
    MockTestCardDetailed(
        mock = mock,
        language = language,
        onStart = onStart
    )
}

@Composable
fun HeroCategoryChip(
    title: String,
    count: Int,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val iconContainerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val iconColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderStroke = if (isSelected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "chipScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(28.dp),
        color = containerColor,
        contentColor = contentColor,
        border = borderStroke,
        shadowElevation = if (isSelected) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconContainerColor,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$count ${if (count == 1) "Test" else "Tests"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.75f),
                    maxLines = 1
                )
            }
        }
    }
}

