package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import com.example.data.local.ExamEntity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.viewmodel.JuktiViewModel
import androidx.compose.ui.text.style.TextDecoration
import com.example.ui.components.PlanDisplayHelper

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
    val examsList by viewModel.examsList.collectAsState()
    
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
            val sortedPlans = remember(plans) { plans.sortedWith(compareBy({ it.displayOrder }, { it.id })) }
            
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (sortedPlans.isEmpty()) {
                    item {
                        Text("No plans created yet.")
                    }
                } else {
                    itemsIndexed(sortedPlans, key = { index, plan -> if (plan.id != 0L) plan.id else "plan_${plan.planName}_$index" }) { _, plan ->
                        PlanManageCard(
                            plan = plan,
                            onEdit = {
                                viewModel.planToEditForScreen = plan
                                viewModel.navigateTo(com.example.ui.viewmodel.Screen.CREATE_PLAN)
                            },
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
    val cleanFinalPrice = PlanDisplayHelper.formatPrice(plan.finalPrice)
    val cleanOriginalPrice = PlanDisplayHelper.formatPrice(plan.planPrice)
    val cleanDiscount = PlanDisplayHelper.formatDiscount(plan.discount)
    val cleanValidity = PlanDisplayHelper.formatValidity(plan)
    val benefits = PlanDisplayHelper.parseFeatures(plan.features)

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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Active",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (cleanFinalPrice.isNotBlank()) {
                            Text(text = cleanFinalPrice, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        if (cleanOriginalPrice.isNotBlank() && cleanOriginalPrice != cleanFinalPrice) {
                            Text(
                                text = cleanOriginalPrice,
                                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (cleanDiscount.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = cleanDiscount,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Validity: $cleanValidity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (plan.offerValidity.isNotBlank() && !plan.offerValidity.equals(cleanValidity, ignoreCase = true)) {
                        Text(text = "Offer: ${plan.offerValidity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }

                    if (plan.examTarget.isNotBlank()) {
                        Text(text = "Target: ${plan.examTarget}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }

                    if (plan.googlePlayProductId.isNotBlank()) {
                        Text(text = "Play ID: ${plan.googlePlayProductId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }

                    if (plan.guidanceEnabled) {
                        Text(text = "💡 Guidance: Enabled", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (benefits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Benefits Included:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                benefits.forEach { b ->
                    Text(text = "• $b", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
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
