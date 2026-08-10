package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.data.local.PlanEntity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.JuktiViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.example.ui.components.SafeOutlinedTextField
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
            TopAppBar(
                title = { Text("Edit Plans", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_PLAN) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
fun PlanManageCard(plan: PlanEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = plan.planName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
    var planName by remember { mutableStateOf(plan.planName) }
    var planPrice by remember { mutableStateOf(plan.planPrice) }
    var discount by remember { mutableStateOf(plan.discount) }
    var finalPrice by remember { mutableStateOf(plan.finalPrice) }
    var planValidity by remember { mutableStateOf(plan.planValidity) }
    var offerValidity by remember { mutableStateOf(plan.offerValidity) }
    var isActive by remember { mutableStateOf(plan.isActive) }

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
                SafeOutlinedTextField(
                    value = planValidity,
                    onValueChange = { planValidity = it },
                    label = { Text("Plan Validity (e.g. 1 Month, 1 Year) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                SafeOutlinedTextField(
                    value = offerValidity,
                    onValueChange = { offerValidity = it },
                    label = { Text("Offer Validity (e.g. Ends Tonight!, 2 Days Left)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Is Active")
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            val context = androidx.compose.ui.platform.LocalContext.current
            TextButton(onClick = {
                if (planName.isNotBlank() && finalPrice.isNotBlank() && planValidity.isNotBlank()) {
                    onSave(
                        plan.copy(
                            planName = planName,
                            planPrice = planPrice,
                            discount = discount,
                            finalPrice = finalPrice,
                            planValidity = planValidity,
                            offerValidity = offerValidity,
                            isActive = isActive
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

