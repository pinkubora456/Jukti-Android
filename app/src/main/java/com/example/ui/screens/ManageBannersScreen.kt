package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.BannerEntity
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

data class PresetImage(val name: String, val url: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBannersScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val banners by viewModel.allBanners.collectAsState()
    val mockTests by viewModel.mockTests.collectAsState()
    val studyNotes by viewModel.studyNotes.collectAsState()

    // Editor States
    var editingBanner by remember { mutableStateOf<BannerEntity?>(null) }
    var titleEn by remember { mutableStateOf("") }
    var titleAs by remember { mutableStateOf("") }
    var subtitleEn by remember { mutableStateOf("") }
    var subtitleAs by remember { mutableStateOf("") }
    var badgeText by remember { mutableStateOf("UPDATED") }
    var actionType by remember { mutableStateOf("Link") } // "Mock Test", "Study Notes", "Link", "None"
    var actionUrl by remember { mutableStateOf("") } // holds link, or mock test ID, or study note ID
    var imageUrl by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    var bannerType by remember { mutableStateOf("INFORMATION") } // "INFORMATION", "PROMOTIONAL", "CAROUSEL"
    var offerValidity by remember { mutableStateOf("") }
    var planPrice by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var finalPrice by remember { mutableStateOf("") }

    // Dropdowns and UI flags
    var actionTypeExpanded by remember { mutableStateOf(false) }
    var bannerTypeExpanded by remember { mutableStateOf(false) }
    var mockTestExpanded by remember { mutableStateOf(false) }
    var studyNoteExpanded by remember { mutableStateOf(false) }
    var isFormVisible by remember { mutableStateOf(false) }

    val actionTypes = listOf("Link", "Mock Test", "Study Notes", "None")
    val bannerTypes = listOf("INFORMATION", "PROMOTIONAL", "CAROUSEL")

    val presetImages = listOf(
        PresetImage("None", ""),
        PresetImage("Pass Pro", "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop"),
        PresetImage("Live Quiz", "https://images.unsplash.com/photo-1606326608606-aa0b62935f2b?w=500&auto=format&fit=crop"),
        PresetImage("Mock Exam", "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=500&auto=format&fit=crop"),
        PresetImage("Announcement", "https://images.unsplash.com/photo-1557200134-90327ee9fafa?w=500&auto=format&fit=crop")
    )

    fun clearForm() {
        editingBanner = null
        titleEn = ""
        titleAs = ""
        subtitleEn = ""
        subtitleAs = ""
        badgeText = ""
        actionType = "Link"
        actionUrl = ""
        imageUrl = ""
        isActive = true
        bannerType = "INFORMATION"
        offerValidity = ""
        planPrice = ""
        discount = ""
        finalPrice = ""
        isFormVisible = false
    }

    fun loadBanner(banner: BannerEntity) {
        editingBanner = banner
        titleEn = banner.titleEn
        titleAs = banner.titleAs
        subtitleEn = banner.subtitleEn
        subtitleAs = banner.subtitleAs
        badgeText = banner.badgeText
        actionType = banner.actionType
        actionUrl = banner.actionUrl
        imageUrl = banner.imageUrl
        isActive = banner.isActive
        bannerType = banner.type
        offerValidity = banner.offerValidity
        planPrice = banner.planPrice
        discount = banner.discount
        finalPrice = banner.finalPrice
        isFormVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Information Banners", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.WORKSPACE) },
                        modifier = Modifier.testTag("manage_banners_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isFormVisible) {
                        Button(
                            onClick = {
                                clearForm()
                                isFormVisible = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.testTag("add_new_banner_top_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Banner")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Edit / Add Form Section
            AnimatedVisibility(visible = isFormVisible) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (editingBanner == null) "Create Information Banner" else "Edit Banner Info",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { clearForm() }) {
                                Icon(Icons.Default.Cancel, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        // Title
                        SafeOutlinedTextField(
                            value = titleEn,
                            onValueChange = { titleEn = it },
                            label = { Text("Banner Title *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("banner_title_en_input"),
                            singleLine = true
                        )

                        // Notice Details
                        SafeOutlinedTextField(
                            value = subtitleEn,
                            onValueChange = { subtitleEn = it },
                            label = { Text("Notice Details *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("banner_details_en_input"),
                            minLines = 2
                        )

                        // Promotional Banner specific fields
                        if (bannerType == "PROMOTIONAL") {
                            Text("Promotional Banner pricing & validity (optional)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            
                            SafeOutlinedTextField(
                                value = planPrice,
                                onValueChange = { planPrice = it },
                                label = { Text("Plan Price (e.g., 599)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            SafeOutlinedTextField(
                                value = discount,
                                onValueChange = { discount = it },
                                label = { Text("Discount (e.g., 30% OFF)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            SafeOutlinedTextField(
                                value = finalPrice,
                                onValueChange = { finalPrice = it },
                                label = { Text("Pay Only Amount (e.g., 399)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            SafeOutlinedTextField(
                                value = offerValidity,
                                onValueChange = { offerValidity = it },
                                label = { Text("Offer Validity (e.g., Ends Tonight!)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // Action Button Dropdown Selector
                        ExposedDropdownMenuBox(
                            expanded = actionTypeExpanded,
                            onExpandedChange = { actionTypeExpanded = !actionTypeExpanded }
                        ) {
                            SafeOutlinedTextField(
                                value = actionType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Action Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionTypeExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("banner_action_type_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = actionTypeExpanded,
                                onDismissRequest = { actionTypeExpanded = false }
                            ) {
                                actionTypes.forEach { typeOption ->
                                    DropdownMenuItem(
                                        text = { Text(typeOption) },
                                        onClick = {
                                            actionType = typeOption
                                            actionTypeExpanded = false
                                            if (typeOption == "None") {
                                                actionUrl = ""
                                            }
                                        },
                                        modifier = Modifier.testTag("banner_action_type_$typeOption")
                                    )
                                }
                            }
                        }

                        // Contextual action destination option
                        when (actionType) {
                            "Link" -> {
                                SafeOutlinedTextField(
                                    value = actionUrl,
                                    onValueChange = { actionUrl = it },
                                    label = { Text("Paste Redirection Link (URL) *") },
                                    placeholder = { Text("https://example.com") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("banner_action_url_input"),
                                    singleLine = true
                                )
                            }
                            "Mock Test" -> {
                                val selectedMockName = mockTests.find { it.id.toString() == actionUrl }?.titleEn ?: "Select Mock Test"
                                ExposedDropdownMenuBox(
                                    expanded = mockTestExpanded,
                                    onExpandedChange = { mockTestExpanded = !mockTestExpanded }
                                ) {
                                    SafeOutlinedTextField(
                                        value = selectedMockName,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Select Target Mock Test *") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mockTestExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .testTag("banner_mock_test_dropdown")
                                    )
                                    ExposedDropdownMenu(
                                        expanded = mockTestExpanded,
                                        onDismissRequest = { mockTestExpanded = false }
                                    ) {
                                        mockTests.forEach { mock ->
                                            DropdownMenuItem(
                                                text = { Text(mock.titleEn) },
                                                onClick = {
                                                    actionUrl = mock.id.toString()
                                                    mockTestExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            "Study Notes" -> {
                                val selectedNoteTitle = studyNotes.find { it.id.toString() == actionUrl }?.titleEn ?: "Select Study Note"
                                ExposedDropdownMenuBox(
                                    expanded = studyNoteExpanded,
                                    onExpandedChange = { studyNoteExpanded = !studyNoteExpanded }
                                ) {
                                    SafeOutlinedTextField(
                                        value = selectedNoteTitle,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Select Target Study Note *") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studyNoteExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .testTag("banner_study_notes_dropdown")
                                    )
                                    ExposedDropdownMenu(
                                        expanded = studyNoteExpanded,
                                        onDismissRequest = { studyNoteExpanded = false }
                                    ) {
                                        studyNotes.forEach { note ->
                                            DropdownMenuItem(
                                                text = { Text(note.titleEn) },
                                                onClick = {
                                                    actionUrl = note.id.toString()
                                                    studyNoteExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Image Option Selectors
                        Text(
                            text = "Add Banner Image",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Preset Banners Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(presetImages) { preset ->
                                val isSelected = imageUrl == preset.url
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .width(100.dp)
                                        .clickable { imageUrl = preset.url }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        if (preset.url.isNotEmpty()) {
                                            AsyncImage(
                                                model = preset.url,
                                                contentDescription = preset.name,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = preset.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Custom Image URL Field
                        SafeOutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("Custom Image URL (Optional)") },
                            placeholder = { Text("https://example.com/image.jpg") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("banner_custom_image_url_input"),
                            singleLine = true
                        )

                        // Active State Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Make Banner Active", fontWeight = FontWeight.Medium)
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                modifier = Modifier.testTag("banner_active_switch"),
                                colors = SwitchDefaults.colors(
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        // Save Button
                        Button(
                            onClick = {
                                if (titleEn.isBlank() || subtitleEn.isBlank()) {
                                    Toast.makeText(context, "Please enter Banner Title and Details in English", Toast.LENGTH_SHORT).show()
                                } else if (actionType != "None" && actionUrl.isBlank()) {
                                    Toast.makeText(context, "Please select/paste action target redirection source", Toast.LENGTH_SHORT).show()
                                } else {
                                    val newBanner = BannerEntity(
                                        id = editingBanner?.id ?: 0,
                                        titleEn = titleEn.trim(),
                                        titleAs = titleAs.trim(),
                                        subtitleEn = subtitleEn.trim(),
                                        subtitleAs = subtitleAs.trim(),
                                        badgeText = badgeText.trim(),
                                        type = bannerType,
                                        actionUrl = actionUrl,
                                        isActive = isActive,
                                        imageUrl = imageUrl.trim(),
                                        actionType = actionType,
                                        offerValidity = offerValidity.trim(),
                                        planPrice = planPrice.trim(),
                                        discount = discount.trim(),
                                        finalPrice = finalPrice.trim()
                                    )

                                    if (editingBanner == null) {
                                        viewModel.addBanner(newBanner)
                                        Toast.makeText(context, "New Banner Added Successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updateBanner(newBanner)
                                        Toast.makeText(context, "Banner Updated Successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                    clearForm()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_banner_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (editingBanner == null) "Create Banner" else "Save Changes")
                        }
                    }
                }
            }

            // Headers & List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configured Banners",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (banners.isNotEmpty()) {
                    Text(
                        text = "${banners.size} items",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (banners.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewCarousel,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "No configured banners yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { isFormVisible = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create First Banner")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(banners) { banner ->
                        BannerAdminCard(
                            banner = banner,
                            onEdit = { loadBanner(banner) },
                            onDelete = {
                                viewModel.deleteBanner(banner)
                                Toast.makeText(context, "Banner Deleted!", Toast.LENGTH_SHORT).show()
                            },
                            onToggleActive = {
                                viewModel.updateBanner(banner.copy(isActive = !banner.isActive))
                                Toast.makeText(context, "Banner status updated!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BannerAdminCard(
    banner: BannerEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (banner.isActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (banner.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (banner.badgeText.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = banner.badgeText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Surface(
                            color = if (banner.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (banner.isActive) "Active" else "Inactive",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (banner.isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = banner.titleEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (banner.titleAs.isNotEmpty()) {
                        Text(
                            text = banner.titleAs,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = banner.subtitleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Redirect: ${banner.actionType} " + if (banner.actionUrl.isNotEmpty()) "(${banner.actionUrl})" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Type: ${banner.type}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    if (banner.type == "PROMOTIONAL") {
                        if (banner.planPrice.isNotBlank() || banner.discount.isNotBlank() || banner.finalPrice.isNotBlank()) {
                            Text(
                                text = "Promo: ₹${banner.planPrice} | ${banner.discount} | Pay ₹${banner.finalPrice}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (banner.offerValidity.isNotBlank()) {
                            Text(
                                text = "Validity: ${banner.offerValidity}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (banner.imageUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleActive,
                    modifier = Modifier.testTag("toggle_banner_active_${banner.id}")
                ) {
                    Icon(
                        imageVector = if (banner.isActive) Icons.Default.Cancel else Icons.Default.Check,
                        contentDescription = "Toggle Active Status",
                        tint = if (banner.isActive) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                    )
                }
                
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_banner_${banner.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Banner",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_banner_${banner.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Banner",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
