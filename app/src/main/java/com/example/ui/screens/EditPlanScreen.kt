package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.data.local.PlanEntity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.viewmodel.JuktiViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.components.PlanValiditySelector
import com.example.ui.components.PlanValidityHelper
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlanScreen(viewModel: JuktiViewModel) {
    var planToEdit by remember { mutableStateOf<PlanEntity?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success", fontWeight = FontWeight.Bold) },
            text = { Text(successMessage) },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    if (planToEdit != null) {
        EditPlanDialog(
            plan = planToEdit!!,
            onDismiss = { planToEdit = null },
            onSave = { updatedPlan -> 
                viewModel.requestOrCreatePlan(updatedPlan) { _, message ->
                    successMessage = message
                    showSuccessDialog = true
                }
                planToEdit = null
            }
        )
    }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Edit Plans",
                onBackClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_PLAN) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            val plans by viewModel.plans.collectAsState()
            
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (plans.isEmpty()) {
                    item {
                        Text("No plans created yet.")
                    }
                } else {
                    itemsIndexed(plans, key = { index, plan -> if (plan.id != 0L) plan.id else "plan_${plan.planName}_$index" }) { _, plan ->
                        PlanManageCard(
                            plan = plan,
                            onEdit = { planToEdit = plan },
                            onDelete = { viewModel.deletePlan(plan) }
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun PlanManageCard(plan: PlanEntity, onEdit: () -> Unit, onDelete: () -> Unit, onToggleArchive: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (plan.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = plan.planName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (!plan.isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "Archived",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    Text(text = "₹${plan.finalPrice}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    if (plan.offerValidity.isNotBlank()) {
                        Text(text = "Validity: ${plan.offerValidity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val benefits = plan.features.split("|").filter { it.isNotBlank() }
            if (benefits.isNotEmpty()) {
                Text(text = "Benefits:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                benefits.forEach { b ->
                    Text(text = "• $b", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onToggleArchive() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)) {
                    Icon(if (plan.isActive) Icons.Default.Archive else Icons.Default.Unarchive, contentDescription = "Archive")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (plan.isActive) "Archive" else "Unarchive")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { onEdit() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { onDelete() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun EditPlanDialog(
    plan: PlanEntity,
    onDismiss: () -> Unit,
    onSave: (PlanEntity) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var planName by remember { mutableStateOf(plan.planName) }
    var planPrice by remember { mutableStateOf(plan.planPrice) }
    var discount by remember { mutableStateOf(plan.discount) }
    var finalPrice by remember { mutableStateOf(plan.finalPrice) }
    val initialPreset = remember(plan) {
        PlanValidityHelper.detectPreset(plan.planValidity, plan.validityType, plan.validityValue, plan.isLifetime)
    }
    var selectedValidityPreset by remember { mutableStateOf(initialPreset.first) }
    var customValidityNumber by remember { mutableStateOf(initialPreset.second) }
    var customValidityUnit by remember { mutableStateOf(initialPreset.third) }
    var offerValidity by remember { mutableStateOf(plan.offerValidity) }
    var imageUrl by remember { mutableStateOf(plan.imageUrl) }
    var examTarget by remember { mutableStateOf(plan.examTarget) }
    var isActive by remember { mutableStateOf(plan.isActive) }
    var isUploading by remember { mutableStateOf(false) }
    val benefits = remember { mutableStateListOf<String>().apply { addAll(plan.features.split(",").filter { it.isNotBlank() }) } }
    var newBenefit by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val file = java.io.File(context.filesDir, "plan_banner_${System.currentTimeMillis()}.jpg")
                    java.io.FileOutputStream(file).use { it.write(bytes) }
                    imageUrl = file.absolutePath
                    Toast.makeText(context, "Photo loaded successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("EditPlanDialog", "Error loading photo", e)
            } finally {
                isUploading = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Plan", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SafeOutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text("Plan Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Photo upload and preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Promotional Banner Photo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (imageUrl.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Plan Banner Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { imageUrl = "" },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove photo",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Loading...")
                            } else {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (imageUrl.isBlank()) "Choose Photo" else "Change Photo")
                            }
                        }

                        SafeOutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("Or Image URL (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                SafeOutlinedTextField(
                    value = planPrice,
                    onValueChange = { 
                        planPrice = it 
                        val price = it.toDoubleOrNull() ?: 0.0
                        val disc = discount.toDoubleOrNull() ?: 0.0
                        if (price >= 0 && disc in 0.0..100.0) {
                            finalPrice = (price - (price * disc / 100)).toInt().toString()
                        }
                    },
                    label = { Text("Original Price") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                SafeOutlinedTextField(
                    value = discount,
                    onValueChange = { 
                        discount = it 
                        val price = planPrice.toDoubleOrNull() ?: 0.0
                        val disc = it.toDoubleOrNull() ?: 0.0
                        if (price >= 0 && disc in 0.0..100.0) {
                            finalPrice = (price - (price * disc / 100)).toInt().toString()
                        }
                    },
                    label = { Text("Discount %") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                SafeOutlinedTextField(
                    value = finalPrice,
                    onValueChange = { finalPrice = it },
                    label = { Text("Final Price") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                PlanValiditySelector(
                    selectedPreset = selectedValidityPreset,
                    onPresetChange = { selectedValidityPreset = it },
                    customNumber = customValidityNumber,
                    onCustomNumberChange = { customValidityNumber = it },
                    customUnit = customValidityUnit,
                    onCustomUnitChange = { customValidityUnit = it }
                )
                SafeOutlinedTextField(
                    value = offerValidity,
                    onValueChange = { offerValidity = it },
                    label = { Text("Offer Validity (e.g. Ends Tonight!, 2 Days Left)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                SafeOutlinedTextField(
                    value = examTarget,
                    onValueChange = { examTarget = it },
                    label = { Text("Exam Target") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("Benefits", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SafeOutlinedTextField(
                        value = newBenefit,
                        onValueChange = { newBenefit = it },
                        label = { Text("Add Benefit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        if (newBenefit.isNotBlank()) {
                            benefits.add(newBenefit.trim())
                            newBenefit = ""
                        }
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Add Benefit")
                    }
                }
                benefits.forEachIndexed { index, benefit ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(benefit)
                        IconButton(onClick = { benefits.removeAt(index) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Benefit")
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active (Visible to Users)")
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            val context = androidx.compose.ui.platform.LocalContext.current
            TextButton(onClick = {
                val (resolvedValidityType, resolvedValidityValue, isLifetime) = PlanValidityHelper.resolveValidity(
                    selectedValidityPreset,
                    customValidityNumber,
                    customValidityUnit
                )
                val resolvedValidityLabel = PlanValidityHelper.resolveLabel(
                    selectedValidityPreset,
                    customValidityNumber,
                    customValidityUnit
                )
                if (planName.isNotBlank() && finalPrice.isNotBlank() && resolvedValidityLabel.isNotBlank()) {
                    onSave(
                        plan.copy(
                            planName = planName,
                            planPrice = planPrice,
                            discount = discount,
                            finalPrice = finalPrice,
                            planValidity = resolvedValidityLabel,
                            validityType = resolvedValidityType,
                            validityValue = resolvedValidityValue,
                            validityLabel = resolvedValidityLabel,
                            isLifetime = isLifetime,
                            offerValidity = offerValidity,
                            isActive = isActive,
                            imageUrl = imageUrl,
                            examTarget = examTarget
                        )
                    )
                } else {
                    android.widget.Toast.makeText(context, "Please fill in plan name, price, and plan validity.", android.widget.Toast.LENGTH_SHORT).show()
                }
            }) {
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

