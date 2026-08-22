package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

object PlanValidityHelper {
    val PRESETS = listOf("1 Week", "1 Month", "6 Months", "1 Year", "Lifetime", "Custom")
    val UNITS = listOf("Days", "Months", "Years")

    fun resolveValidity(preset: String, customNumber: String, customUnit: String): Triple<String, Int, Boolean> {
        return when (preset) {
            "1 Week" -> Triple("DAYS", 7, false)
            "1 Month" -> Triple("MONTHS", 1, false)
            "6 Months" -> Triple("MONTHS", 6, false)
            "1 Year" -> Triple("YEARS", 1, false)
            "Lifetime" -> Triple("LIFETIME", 0, true)
            "Custom" -> {
                val num = customNumber.toIntOrNull()?.coerceAtLeast(1) ?: 1
                val type = when (customUnit) {
                    "Days" -> "DAYS"
                    "Years" -> "YEARS"
                    else -> "MONTHS"
                }
                Triple(type, num, false)
            }
            else -> Triple("MONTHS", 1, false)
        }
    }

    fun resolveLabel(preset: String, customNumber: String, customUnit: String): String {
        return when (preset) {
            "1 Week" -> "1 Week"
            "1 Month" -> "1 Month"
            "6 Months" -> "6 Months"
            "1 Year" -> "1 Year"
            "Lifetime" -> "Lifetime"
            "Custom" -> {
                val num = customNumber.toIntOrNull()?.coerceAtLeast(1) ?: 1
                val unitStr = if (num == 1) {
                    when (customUnit) {
                        "Days" -> "Day"
                        "Years" -> "Year"
                        else -> "Month"
                    }
                } else {
                    when (customUnit) {
                        "Days" -> "Days"
                        "Years" -> "Years"
                        else -> "Months"
                    }
                }
                "$num $unitStr"
            }
            else -> preset
        }
    }

    fun detectPreset(planValidity: String, validityType: String, validityValue: Int, isLifetime: Boolean): Triple<String, String, String> {
        if (isLifetime || validityType.equals("LIFETIME", ignoreCase = true) || planValidity.equals("Lifetime", ignoreCase = true)) {
            return Triple("Lifetime", "1", "Months")
        }
        if (validityType.equals("DAYS", ignoreCase = true) && validityValue == 7) {
            return Triple("1 Week", "7", "Days")
        }
        if (validityType.equals("MONTHS", ignoreCase = true) && validityValue == 1) {
            return Triple("1 Month", "1", "Months")
        }
        if (validityType.equals("MONTHS", ignoreCase = true) && validityValue == 6) {
            return Triple("6 Months", "6", "Months")
        }
        if ((validityType.equals("MONTHS", ignoreCase = true) && validityValue == 12) ||
            (validityType.equals("YEARS", ignoreCase = true) && validityValue == 1)) {
            return Triple("1 Year", "1", "Years")
        }

        // Check planValidity string matching
        val trimmed = planValidity.trim()
        if (trimmed.equals("1 Week", ignoreCase = true) || trimmed.equals("7 Days", ignoreCase = true)) {
            return Triple("1 Week", "7", "Days")
        }
        if (trimmed.equals("1 Month", ignoreCase = true)) {
            return Triple("1 Month", "1", "Months")
        }
        if (trimmed.equals("6 Months", ignoreCase = true)) {
            return Triple("6 Months", "6", "Months")
        }
        if (trimmed.equals("1 Year", ignoreCase = true) || trimmed.equals("12 Months", ignoreCase = true)) {
            return Triple("1 Year", "1", "Years")
        }

        // Custom
        val unit = when (validityType.uppercase()) {
            "DAYS" -> "Days"
            "YEARS" -> "Years"
            else -> "Months"
        }
        val num = if (validityValue > 0) validityValue.toString() else "30"
        return Triple("Custom", num, unit)
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlanValiditySelector(
    selectedPreset: String,
    onPresetChange: (String) -> Unit,
    customNumber: String,
    onCustomNumberChange: (String) -> Unit,
    customUnit: String,
    onCustomUnitChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val resolvedLabel = PlanValidityHelper.resolveLabel(selectedPreset, customNumber, customUnit)
    val (resolvedType, resolvedValue, isLifetime) = PlanValidityHelper.resolveValidity(selectedPreset, customNumber, customUnit)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timelapse,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Plan Validity (Select Duration) *",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isLifetime) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = resolvedLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLifetime) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text(
                text = "Select how long user gets access upon purchase or assignment:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Presets grid/flow
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PlanValidityHelper.PRESETS.forEach { preset ->
                    val isSelected = selectedPreset == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPresetChange(preset) },
                        label = {
                            Text(
                                text = preset,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Custom inputs if "Custom" is selected
            if (selectedPreset == "Custom") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Custom Validity Duration",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SafeOutlinedTextField(
                                value = customNumber,
                                onValueChange = onCustomNumberChange,
                                label = { Text("Number") },
                                placeholder = { Text("e.g. 45") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            var expandedUnitDropdown by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expandedUnitDropdown,
                                onExpandedChange = { expandedUnitDropdown = !expandedUnitDropdown },
                                modifier = Modifier.weight(1.2f)
                            ) {
                                SafeOutlinedTextField(
                                    value = customUnit,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Unit") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnitDropdown) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedUnitDropdown,
                                    onDismissRequest = { expandedUnitDropdown = false }
                                ) {
                                    PlanValidityHelper.UNITS.forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text(unit) },
                                            onClick = {
                                                onCustomUnitChange(unit)
                                                expandedUnitDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expiry explanatory hint
            val hintText = if (isLifetime) {
                "✨ User will have permanent Lifetime access. This entitlement never expires."
            } else {
                "⏳ When activated, user will have access for exactly $resolvedLabel from the date of purchase/assignment."
            }
            Text(
                text = hintText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}
