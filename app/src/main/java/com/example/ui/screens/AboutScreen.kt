package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import com.example.ui.components.getLogoIcon


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: JuktiViewModel) {
    val localContext = androidx.compose.ui.platform.LocalContext.current
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
            com.example.ui.components.JuktiTopAppBar(
                title = "About Jukti App",
                onBackClick = { viewModel.navigateTo(Screen.MENU) },
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
                    shape = RoundedCornerShape(12.dp),
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
                Box(contentAlignment = Alignment.Center, modifier = Modifier.height(90.dp).padding(12.dp).clickable(enabled = isOwner) { showLogoPickerOnly = true }) {
                    val imageModifier = Modifier.fillMaxHeight().widthIn(max = 200.dp)
                    val localLogo = java.io.File(LocalContext.current.filesDir, "cached_logo.png")
                    if (localLogo.exists() && localLogo.length() > 0) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(LocalContext.current)
                                .data(localLogo)
                                .crossfade(true)
                                .error(com.example.R.drawable.jukti_logo)
                                .fallback(com.example.R.drawable.jukti_logo)
                                .build(),
                            contentDescription = "App Logo",
                            modifier = imageModifier,
                            contentScale = ContentScale.Fit
                        )
                    } else if (aboutConfig.logoUrl.isNotBlank() && aboutConfig.logoUrl.startsWith("http")) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(LocalContext.current)
                                .data(aboutConfig.logoUrl)
                                .crossfade(true)
                                .error(com.example.R.drawable.jukti_logo)
                                .fallback(com.example.R.drawable.jukti_logo)
                                .build(),
                            contentDescription = "App Logo",
                            modifier = imageModifier,
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        coil.compose.AsyncImage(
                            model = com.example.R.drawable.jukti_logo,
                            contentDescription = "App Logo",
                            modifier = imageModifier,
                            contentScale = ContentScale.Fit
                        )
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
                    text = aboutConfig.appSubtitleEn,
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
                        text = "Our Mission",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = aboutConfig.missionEn,
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
                        text = "Key Platform Features",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val features = listOf(
                        "Subject & Chapter-Wise Practice MCQs in English & Assamese",
                        "Assam History, Geography, Polity, Economy, Culture & Special GK",
                        "Full-Length Timed Mock Tests with Instant Answer Key & Solutions",
                        "State-Wide Leaderboard Ranking, Levels, Badges & XP Rewards",
                        "Daily Quiz Practice Challenges with Detailed Explanation Sheets",
                        "Comprehensive Study Notes & Downloadable PDF Revision Sheets",
                        "Detailed Performance Analytics & Exam Clearance Probability",
                        "Weak Topic AI Tutor & Subject-Wise Accuracy Breakdown",
                        "Bookmarked MCQs & Offline Practice Support",
                        "Real-Time Exam Updates, Notifications & Instant Cloud Sync"
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
                            text = "How XP, Levels & Leaderboard Work",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Learn about exam clearance probability and analytics",
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

            FounderCard(
                aboutConfig = aboutConfig,
                isOwner = isOwner,
                onEditFounder = { showEditDialog = true },
                onUpdateFounderPhoto = { newUrl ->
                    viewModel.updateAboutConfig(aboutConfig.copy(founderPhotoUrl = newUrl))
                }
            )

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
            onSelectIcon = { newIconName, uri ->
                if (uri != null) {
                    viewModel.uploadLogoAndSaveConfig(uri, aboutConfig, localContext)
                } else {
                    viewModel.updateAboutConfig(aboutConfig.copy(logoIconName = newIconName))
                }
                showLogoPickerOnly = false
            },
            onDismiss = { showLogoPickerOnly = false }
        )
    }

    // Comprehensive Owner Edit Dialog
    if (showEditDialog && isOwner) {
        OwnerEditAboutDialog(
            currentConfig = aboutConfig,
            onSave = { updated, uri ->
                if (uri != null) {
                    viewModel.uploadLogoAndSaveConfig(uri, updated, localContext)
                } else {
                    viewModel.updateAboutConfig(updated) { success, msg ->
                        val confirmText = if (success) "About section saved & synced to Firebase!" else "Saved locally. Firebase sync queued: $msg"
                        android.widget.Toast.makeText(localContext, confirmText, android.widget.Toast.LENGTH_LONG).show()
                    }
                }
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
    onSelectIcon: (String, Uri?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onSelectIcon(uri.toString(), uri)
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
                            onClick = { onSelectIcon(iconKey, null) },
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
    onSave: (AboutConfigEntity, Uri?) -> Unit,
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
    var founderName by remember { mutableStateOf(currentConfig.founderName) }
    var founderTitle by remember { mutableStateOf(currentConfig.founderTitle) }
    var founderCredential by remember { mutableStateOf(currentConfig.founderCredential) }
    var founderDescription by remember { mutableStateOf(currentConfig.founderDescription) }
    var founderTagline by remember { mutableStateOf(currentConfig.founderTagline) }
    var founderPhotoUrl by remember { mutableStateOf(currentConfig.founderPhotoUrl) }
    var playStoreUrl by remember { mutableStateOf(currentConfig.playStoreUrl) }

    var selectedTab by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    var selectedLogoUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedLogoUri = uri
            logoIconName = uri.toString()
        }
    }

    val founderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "founder_photo_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            founderPhotoUrl = "custom_founder:${file.absolutePath}"
        }
    }

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
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Founder") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
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

                        SafeOutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("App Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SafeOutlinedTextField(
                            value = versionText,
                            onValueChange = { versionText = it },
                            label = { Text("Version Tag (e.g. Version 2026.1.0)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (selectedTab == 1) {
                    Column(
                        modifier = Modifier
                            .height(320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SafeOutlinedTextField(
                            value = subtitleEn,
                            onValueChange = { subtitleEn = it },
                            label = { Text("Subtitle (English)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        SafeOutlinedTextField(
                            value = subtitleAs,
                            onValueChange = { subtitleAs = it },
                            label = { Text("Subtitle (Assamese)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        SafeOutlinedTextField(
                            value = missionEn,
                            onValueChange = { missionEn = it },
                            label = { Text("Mission Statement (English)") },
                            minLines = 3,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )

                        SafeOutlinedTextField(
                            value = missionAs,
                            onValueChange = { missionAs = it },
                            label = { Text("Mission Statement (Assamese)") },
                            minLines = 3,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )

                        SafeOutlinedTextField(
                            value = developerTagline,
                            onValueChange = { developerTagline = it },
                            label = { Text("Developer / Team Tagline") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        SafeOutlinedTextField(
                            value = copyrightText,
                            onValueChange = { copyrightText = it },
                            label = { Text("Copyright Line") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        SafeOutlinedTextField(
                            value = playStoreUrl,
                            onValueChange = { playStoreUrl = it },
                            label = { Text("App Link / Play Store URL (for Share & Rate Us)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (selectedTab == 2) {
                    Column(
                        modifier = Modifier
                            .height(320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(72.dp)
                                .clickable { founderLauncher.launch("image/*") },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (founderPhotoUrl.startsWith("custom_founder:")) {
                                    val path = founderPhotoUrl.removePrefix("custom_founder:")
                                    AsyncImage(
                                        model = File(path),
                                        contentDescription = "Founder Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        OutlinedButton(onClick = { founderLauncher.launch("image/*") }) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Founder Photo")
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        SafeOutlinedTextField(
                            value = founderName,
                            onValueChange = { founderName = it },
                            label = { Text("Founder Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        SafeOutlinedTextField(
                            value = founderTitle,
                            onValueChange = { founderTitle = it },
                            label = { Text("Founder Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        SafeOutlinedTextField(
                            value = founderCredential,
                            onValueChange = { founderCredential = it },
                            label = { Text("Credential / Achievement") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        SafeOutlinedTextField(
                            value = founderDescription,
                            onValueChange = { founderDescription = it },
                            label = { Text("Founder Bio / Description") },
                            minLines = 4,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                        SafeOutlinedTextField(
                            value = founderTagline,
                            onValueChange = { founderTagline = it },
                            label = { Text("Founder Tagline") },
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
                    val updatedConfig = currentConfig.copy(
                            appTitle = title,
                            appSubtitleEn = subtitleEn,
                            appSubtitleAs = subtitleAs,
                            versionText = versionText,
                            missionEn = missionEn,
                            missionAs = missionAs,
                            logoIconName = if (selectedLogoUri != null) currentConfig.logoIconName else logoIconName,
                            developerTagline = developerTagline,
                            copyrightText = copyrightText,
                            founderName = founderName,
                            founderTitle = founderTitle,
                            founderCredential = founderCredential,
                            founderDescription = founderDescription,
                            founderTagline = founderTagline,
                            founderPhotoUrl = founderPhotoUrl,
                            playStoreUrl = playStoreUrl
                        )
                    onSave(updatedConfig, selectedLogoUri)
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
                text = "How XP, Levels & Analytics Work",
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
                            text = "XP & Levels",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You earn XP by completing Mock Tests and Practice Quizzes. Gaining more XP helps you level up and unlock new Badges.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Probability of Clearing Exam
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Exam Clearance Probability (Estimate)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This is an automated in-app estimate calculated from your mock test scores, practice question accuracy, and daily study consistency. It is provided for personal practice tracking only and does NOT guarantee or predict that you will pass, clear, qualify, or fail any examination. Real exam results depend on multiple external factors, and Jukti is not responsible for official examination outcomes.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Leaderboard
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Leaderboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The Leaderboard ranks you against other aspirants across Assam. It uses both your total XP and average mock test scores to determine your position.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

@Composable
fun FounderCard(
    aboutConfig: AboutConfigEntity,
    isOwner: Boolean,
    onEditFounder: () -> Unit,
    onUpdateFounderPhoto: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val founderPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "founder_photo_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            onUpdateFounderPhoto("custom_founder:${file.absolutePath}")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable(enabled = isOwner) { founderPhotoLauncher.launch("image/*") },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val photoUrl = aboutConfig.founderPhotoUrl
                            if (photoUrl.startsWith("custom_founder:")) {
                                val path = photoUrl.removePrefix("custom_founder:")
                                AsyncImage(
                                    model = File(path),
                                    contentDescription = "Founder Profile Photo - Pinku Bora",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Founder Profile Photo - Pinku Bora",
                                    modifier = Modifier.size(44.dp),
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
                                .clickable { founderPhotoLauncher.launch("image/*") }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change Founder Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = aboutConfig.founderName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = aboutConfig.founderTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = aboutConfig.founderCredential,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val desc = aboutConfig.founderDescription
            val displayDesc = if (isExpanded || desc.length < 100) {
                desc
            } else {
                desc.take(100) + "..."
            }

            Text(
                text = displayDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            if (desc.length >= 100) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Read Less" else "Read More",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (isOwner) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onEditFounder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Founder Details", fontSize = 12.sp)
                }
            }
        }
    }
}
