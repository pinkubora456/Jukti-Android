package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import android.net.Uri
import java.io.File
import com.example.data.local.AboutConfigEntity
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

private fun getLogoIcon(name: String): ImageVector {
    return when (name.trim()) {
        "Book" -> Icons.Default.MenuBook
        "Library" -> Icons.Default.LocalLibrary
        "Star" -> Icons.Default.Star
        "Sparkles" -> Icons.Default.AutoAwesome
        "Psychology" -> Icons.Default.Psychology
        "Award" -> Icons.Default.WorkspacePremium
        "Trophy" -> Icons.Default.EmojiEvents
        "Balance" -> Icons.Default.AccountBalance
        "Gavel" -> Icons.Default.Gavel
        "Graduation" -> Icons.Default.HistoryEdu
        "Lightbulb" -> Icons.Default.Lightbulb
        else -> Icons.Default.School
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE
    val isOwner by viewModel.isOwner.collectAsState()
    val aboutConfig by viewModel.aboutConfig.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoPickerOnly by remember { mutableStateOf(false) }
    var showGamificationInfo by remember { mutableStateOf(false) }

    val currentLogoIcon = getLogoIcon(aboutConfig.logoIconName)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAssamese) "যুক্তিৰ বিষয়ে" else "About Jukti App", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.MENU) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOwner) {
                        FilledTonalIconButton(
                            onClick = { showEditDialog = true },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit About Page (Owner Only)")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isOwner) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Owner Privilege Active",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tap logo or edit button to customize About page & logo",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = { showEditDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 12.sp)
                        }
                    }
                }
            }

            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(90.dp)
                        .clickable(enabled = isOwner) { showLogoPickerOnly = true },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = if (isOwner) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (aboutConfig.logoIconName.startsWith("custom_logo:")) {
                            val path = aboutConfig.logoIconName.removePrefix("custom_logo:")
                            AsyncImage(
                                model = File(path),
                                contentDescription = "Custom App Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = currentLogoIcon,
                                contentDescription = "App Logo",
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (isOwner) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { showLogoPickerOnly = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Logo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = aboutConfig.appTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isAssamese) aboutConfig.appSubtitleAs else aboutConfig.appSubtitleEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                AssistChip(
                    onClick = { if (isOwner) showEditDialog = true },
                    label = { Text(aboutConfig.versionText) },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAssamese) "আমাৰ উদ্দেশ্য" else "Our Mission",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isAssamese) aboutConfig.missionAs else aboutConfig.missionEn,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAssamese) "প্ৰধান সুবিধাসমূহ" else "Key Platform Features",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val features = listOf(
                        "10,000+ Subject-wise MCQs in English & Assamese",
                        "Assam History, Geography, Culture & Special GK",
                        "Full-length Timed Mock Tests with State Ranking",
                        "Daily Practice Quizzes with Explanation Sheets",
                        "Study Notes & Downloadable Revision Sheets",
                        "Detailed Progress Analytics & Weak Topic AI Tutor"
                    )

                    features.forEach { feature ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = feature, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGamificationInfo = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAssamese) "XP, স্তৰ আৰু লিডাৰব'ৰ্ড কেনেকৈ কাম কৰে" else "How XP, Levels & Leaderboard Work",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isAssamese) "পৰীক্ষা উত্তীৰ্ণৰ সম্ভাৱনা আৰু অন্যান্য এনালাইটিক্সৰ বিষয়ে জানক" else "Learn about exam clearance probability and analytics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = aboutConfig.developerTagline,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = aboutConfig.copyrightText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Owner Logo Quick Picker Dialog
    if (showLogoPickerOnly && isOwner) {
        OwnerLogoPickerDialog(
            currentIconName = aboutConfig.logoIconName,
            onSelectIcon = { newIconName ->
                viewModel.updateAboutConfig(aboutConfig.copy(logoIconName = newIconName))
                showLogoPickerOnly = false
            },
            onDismiss = { showLogoPickerOnly = false }
        )
    }

    // Comprehensive Owner Edit Dialog
    if (showEditDialog && isOwner) {
        OwnerEditAboutDialog(
            currentConfig = aboutConfig,
            onSave = { updated ->
                viewModel.updateAboutConfig(updated)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (showGamificationInfo) {
        GamificationInfoDialog(
            isAssamese = isAssamese,
            onDismiss = { showGamificationInfo = false }
        )
    }
}

@Composable
fun OwnerLogoPickerDialog(
    currentIconName: String,
    onSelectIcon: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "custom_logo_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            onSelectIcon("custom_logo:${file.absolutePath}")
        }
    }

    val iconOptions = listOf(
        "School" to "School / Education",
        "Book" to "Open Book",
        "Library" to "Library Hub",
        "Star" to "Featured Star",
        "Sparkles" to "AI Sparkles",
        "Psychology" to "Smart Brain",
        "Award" to "Premium Badge",
        "Trophy" to "Trophy Cup",
        "Balance" to "Government / Law",
        "Gavel" to "Court / Exam",
        "Graduation" to "Graduation Cap",
        "Lightbulb" to "Knowledge Bulb"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select App Logo (Owner Only)")
            }
        },
        text = {
            Column {
                Text(
                    text = "Choose an emblem to represent Jukti App across the platform:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Custom Image")
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(280.dp)
                ) {
                    items(iconOptions) { (iconKey, label) ->
                        val isSelected = iconKey == currentIconName
                        val iconVector = getLogoIcon(iconKey)

                        Surface(
                            onClick = { onSelectIcon(iconKey) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = label,
                                    modifier = Modifier.size(32.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = iconKey,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun OwnerEditAboutDialog(
    currentConfig: AboutConfigEntity,
    onSave: (AboutConfigEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(currentConfig.appTitle) }
    var subtitleEn by remember { mutableStateOf(currentConfig.appSubtitleEn) }
    var subtitleAs by remember { mutableStateOf(currentConfig.appSubtitleAs) }
    var versionText by remember { mutableStateOf(currentConfig.versionText) }
    var missionEn by remember { mutableStateOf(currentConfig.missionEn) }
    var missionAs by remember { mutableStateOf(currentConfig.missionAs) }
    var logoIconName by remember { mutableStateOf(currentConfig.logoIconName) }
    var developerTagline by remember { mutableStateOf(currentConfig.developerTagline) }
    var copyrightText by remember { mutableStateOf(currentConfig.copyrightText) }

    var selectedTab by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit About Section (Owner Only)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Logo & Branding") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("App Details") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    val context = LocalContext.current
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        if (uri != null) {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val file = File(context.filesDir, "custom_logo_${System.currentTimeMillis()}.jpg")
                            inputStream?.use { input ->
                                file.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            logoIconName = "custom_logo:${file.absolutePath}"
                        }
                    }

                    Column(
                        modifier = Modifier
                            .height(320.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("App Logo Emblem", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Select a logo symbol for the About page:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        val iconOptions = listOf(
                            "School", "Book", "Library", "Star",
                            "Sparkles", "Psychology", "Award", "Trophy",
                            "Balance", "Gavel", "Graduation", "Lightbulb"
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Surface(
                                modifier = Modifier.size(60.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (logoIconName.startsWith("custom_logo:")) {
                                        val path = logoIconName.removePrefix("custom_logo:")
                                        AsyncImage(
                                            model = File(path),
                                            contentDescription = "Custom App Logo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = getLogoIcon(logoIconName),
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload Custom Image")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            iconOptions.chunked(3).forEach { rowIcons ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowIcons.forEach { name ->
                                        val isSelected = name == logoIconName
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { logoIconName = name },
                                            label = { Text(name) },
                                            leadingIcon = {
                                                Icon(getLogoIcon(name), contentDescription = null, modifier = Modifier.size(16.dp))
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("App Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = versionText,
                            onValueChange = { versionText = it },
                            label = { Text("Version Tag (e.g. Version 2026.1.0)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .height(320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = subtitleEn,
                            onValueChange = { subtitleEn = it },
                            label = { Text("Subtitle (English)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = subtitleAs,
                            onValueChange = { subtitleAs = it },
                            label = { Text("Subtitle (Assamese)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = missionEn,
                            onValueChange = { missionEn = it },
                            label = { Text("Mission Statement (English)") },
                            minLines = 3,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = missionAs,
                            onValueChange = { missionAs = it },
                            label = { Text("Mission Statement (Assamese)") },
                            minLines = 3,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = developerTagline,
                            onValueChange = { developerTagline = it },
                            label = { Text("Developer / Team Tagline") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = copyrightText,
                            onValueChange = { copyrightText = it },
                            label = { Text("Copyright Line") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        currentConfig.copy(
                            appTitle = title,
                            appSubtitleEn = subtitleEn,
                            appSubtitleAs = subtitleAs,
                            versionText = versionText,
                            missionEn = missionEn,
                            missionAs = missionAs,
                            logoIconName = logoIconName,
                            developerTagline = developerTagline,
                            copyrightText = copyrightText
                        )
                    )
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GamificationInfoDialog(
    isAssamese: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isAssamese) "XP, স্তৰ আৰু এনালাইটিক্স কেনেকৈ কাম কৰে" else "How XP, Levels & Analytics Work",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // XP & Levels
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAssamese) "XP আৰু স্তৰ (Levels)" else "XP & Levels",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAssamese) "আপুনি মক টেষ্ট (Mock Tests) আৰু প্ৰেকটিছ কুইজ (Practice Quizzes) সম্পূৰ্ণ কৰাৰ লগে লগে XP অৰ্জন কৰে। অধিক XP য়ে আপোনাক নতুন স্তৰ (Levels) আৰু বেজ (Badges) আনলক কৰাত সহায় কৰে।" else "You earn XP by completing Mock Tests and Practice Quizzes. Gaining more XP helps you level up and unlock new Badges.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Probability of Clearing Exam
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAssamese) "উত্তীৰ্ণৰ সম্ভাৱনা" else "Exam Clearance Probability",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAssamese) "এইটো আপোনাৰ গড় স্ক'ৰ, কুইজৰ সঠিকতা, আৰু নিয়মীয়া প্ৰদৰ্শনৰ ওপৰত ভিত্তি কৰি গণনা কৰা হয়। ই দেখুৱায় যে আপুনি প্ৰকৃত পৰীক্ষাত উত্তীৰ্ণ হোৱাৰ কিমান সম্ভাৱনা আছে।" else "This is calculated based on your average mock test scores, practice accuracy, and consistency. It gives you an estimate of your chances of clearing the real exam.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Leaderboard
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAssamese) "লিডাৰব'ৰ্ড" else "Leaderboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAssamese) "লিডাৰব'ৰ্ডে আপোনাক অসমৰ অন্যান্য পৰীক্ষাৰ্থীসকলৰ সৈতে ৰেংকিং কৰে। ই আপোনাৰ XP আৰু মক টেষ্টৰ গড় স্ক'ৰ (Mock Avg) দুয়োটা ব্যৱহাৰ কৰি আপোনাৰ স্থান নিৰ্ধাৰণ কৰে।" else "The Leaderboard ranks you against other aspirants across Assam. It uses both your total XP and average mock test scores to determine your position.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isAssamese) "বুজিলো" else "Got it")
            }
        }
    )
}
