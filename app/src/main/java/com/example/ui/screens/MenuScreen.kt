package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val aboutConfig by viewModel.aboutConfig.collectAsState()
    val context = LocalContext.current

    val isAssamese = language == AppLanguage.ASSAMESE

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Menu & Services"
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {




            // Primary Navigation Options requested by user:
            // Profile, Settings, About App, Contact Us, Notes

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MenuItemCard(
                    title = "Profile",
                    description = "Personal details, levels & user stats",
                    icon = Icons.Default.Person,
                    badge = "Lvl ${userProfile?.level ?: 7}",
                    badgeColor = MaterialTheme.colorScheme.primary,
                    onClick = { viewModel.navigateTo(Screen.PROFILE) }
                )

                if (isAdminOrOwner) {
                    MenuItemCard(
                        title = "Workspace",
                        description = "Workspace & Dashboard for Admins & Owners",
                        icon = Icons.Default.Work,
                        badge = "Admin / Owner",
                        badgeColor = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.navigateTo(Screen.WORKSPACE) }
                    )
                }

                MenuItemCard(
                    title = "Settings",
                    description = "Language, theme, notifications & preferences",
                    icon = Icons.Default.Settings,
                    onClick = { viewModel.navigateTo(Screen.SETTINGS) }
                )

                MenuItemCard(
                    title = "Contact Us",
                    description = "Helpdesk, Telegram, WhatsApp & direct feedback",
                    icon = Icons.Default.SupportAgent,
                    onClick = { viewModel.navigateTo(Screen.CONTACT_US) }
                )

                MenuItemCard(
                    title = "About",
                    description = "Jukti info, version v2026.1 & exam info",
                    icon = Icons.Default.Info,
                    onClick = { viewModel.navigateTo(Screen.ABOUT) }
                )

                MenuItemCard(
                    title = "Rate Us",
                    description = "Rate Jukti 5 stars on Google Play",
                    icon = Icons.Default.Star,
                    onClick = {
                        val link = if (aboutConfig.playStoreUrl.isNotBlank()) aboutConfig.playStoreUrl else "https://ais-dev-mbq2e6ge5z4qs5wk3gkstx-397582032913.asia-southeast1.run.app"
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(link))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                            context.startActivity(intent)
                        }
                    }
                )

                MenuItemCard(
                    title = "Share App",
                    description = "Share app with friends & aspirants",
                    icon = Icons.Default.Share,
                    onClick = {
                        val link = if (aboutConfig.playStoreUrl.isNotBlank()) aboutConfig.playStoreUrl else "https://ais-dev-mbq2e6ge5z4qs5wk3gkstx-397582032913.asia-southeast1.run.app"
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, "Prepare for APSC, ADRE, and Assam Competitive Exams with Jukti! Download now: $link")
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, "Share App via")
                        context.startActivity(shareIntent)
                    }
                )

                MenuItemCard(
                    title = "Privacy Policy",
                    description = "Data collection, security & privacy protection",
                    icon = Icons.Default.Security,
                    onClick = { viewModel.navigateTo(Screen.PRIVACY_POLICY) }
                )

                MenuItemCard(
                    title = "Terms & Conditions",
                    description = "Terms of service, payments & refund policy",
                    icon = Icons.Default.Description,
                    onClick = { viewModel.navigateTo(Screen.TERMS_CONDITIONS) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Upgrade Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.PREMIUM_PLANS) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Get Jukti Premium",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Unlock all APSC & ADRE mock tests",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Logout Button
            OutlinedButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Jukti • Test Your Knowledge",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "v2026.1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MenuItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    badge: String? = null,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
