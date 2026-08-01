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

    val isAssamese = language == AppLanguage.ASSAMESE

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isAssamese) "মেনু আৰু সেৱাসমূহ" else "Menu & Services",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            // User Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.PROFILE) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile?.name ?: if (isAssamese) "অসম শিক্ষাৰ্থী" else "Assam Scholar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = userProfile?.email ?: "scholar@jukti.in",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AssistChip(
                                onClick = { viewModel.navigateTo(Screen.PROFILE) },
                                label = { Text(if (isAssamese) "প্ৰফাইল চাওক" else "View Profile") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = if (isAssamese) "মুখ্য মেনু বিকল্পসমূহ" else "Main Menu Options",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Primary Navigation Options requested by user:
            // Profile, Settings, About App, Contact Us, Notes

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MenuItemCard(
                    title = if (isAssamese) "প্ৰফাইল" else "Profile",
                    description = if (isAssamese) "ব্যক্তিগত তথ্য, স্তৰ আৰু ব্যৱহাৰকাৰী পৰিসংখ্যা" else "Personal details, levels & user stats",
                    icon = Icons.Default.Person,
                    badge = if (isAssamese) "লেভেল ${userProfile?.level ?: 7}" else "Lvl ${userProfile?.level ?: 7}",
                    badgeColor = MaterialTheme.colorScheme.primary,
                    onClick = { viewModel.navigateTo(Screen.PROFILE) }
                )

                MenuItemCard(
                    title = if (isAssamese) "কৰ্মস্থান (Workspace)" else "Workspace",
                    description = if (isAssamese) "প্ৰশাসক আৰু ওনাৰৰ বাবে ৱৰ্কস্পেছ" else "Workspace & Dashboard for Admins & Owners",
                    icon = Icons.Default.Work,
                    badge = if (isAssamese) "এডমিন / ওনাৰ" else "Admin / Owner",
                    badgeColor = MaterialTheme.colorScheme.primary,
                    onClick = { viewModel.navigateTo(Screen.WORKSPACE) }
                )

                MenuItemCard(
                    title = if (isAssamese) "ছেটিংছ (Settings)" else "Settings",
                    description = if (isAssamese) "ভাষা, থিম, নোটিফিকেশন আৰু ফেচিলিটি ছেটিংছ" else "Language, theme, notifications & preferences",
                    icon = Icons.Default.Settings,
                    onClick = { viewModel.navigateTo(Screen.SETTINGS) }
                )

                MenuItemCard(
                    title = if (isAssamese) "যোগাযোগ কৰক (Contact Us)" else "Contact Us",
                    description = if (isAssamese) "সাহায্য ডেঙ্ক, টেলিগ্ৰাম, হোৱাটছএপ আৰু মতামত" else "Helpdesk, Telegram, WhatsApp & direct feedback",
                    icon = Icons.Default.SupportAgent,
                    onClick = { viewModel.navigateTo(Screen.CONTACT_US) }
                )

                MenuItemCard(
                    title = if (isAssamese) "যুক্তি সম্পৰ্কে (About)" else "About",
                    description = if (isAssamese) "যুক্তি পৰ্টেল সংৰচনা, সংস্কৰণ v2026.1 আৰু পৰীক্ষা সূচী" else "Jukti info, version v2026.1 & exam info",
                    icon = Icons.Default.Info,
                    onClick = { viewModel.navigateTo(Screen.ABOUT) }
                )

                MenuItemCard(
                    title = if (isAssamese) "ৰিফাণ্ড পলিচি (Refund Policy)" else "Refund Policy",
                    description = if (isAssamese) "ক্ৰয় আৰু ফি ৰিফাণ্ড সংক্ৰান্তিয় নিয়মাৱলী" else "Subscription refund window & guidelines",
                    icon = Icons.Default.ReceiptLong,
                    onClick = { viewModel.navigateTo(Screen.REFUND_POLICY) }
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
                            text = if (isAssamese) "প্ৰিমিয়াম সদস্যতা পাওক" else "Get Jukti Premium",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = if (isAssamese) "অসীমিত মক টেষ্ট আৰু পিডিএফ ডাউনলোড কৰক" else "Unlock all APSC & ADRE mock tests",
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
                onClick = { viewModel.navigateTo(Screen.AUTH) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isAssamese) "লগ আউট / একাউণ্ট সলনি কৰক" else "Log Out / Switch Account")
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
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
