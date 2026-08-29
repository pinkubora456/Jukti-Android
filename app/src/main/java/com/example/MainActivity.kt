package com.example

import android.Manifest
import android.os.Build
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.viewmodel.LocalMessageTranslator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.JuktiTheme
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: JuktiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val showPremiumPaywall by viewModel.showPremiumPaywall.collectAsState()
            val syncToastMessage by viewModel.syncToastMessage.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current

            LaunchedEffect(syncToastMessage) {
                syncToastMessage?.let { msg ->
                    val translated = LocalMessageTranslator.translateGeneralMessage(context, msg)
                    android.widget.Toast.makeText(context, translated, android.widget.Toast.LENGTH_SHORT).show()
                    viewModel.clearSyncToastMessage()
                }
            }

            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()

            JuktiTheme(darkTheme = isDarkTheme ?: systemDark) {
                BackHandler(enabled = true) {
                    val handled = viewModel.goBack()
                    if (!handled) {
                        (context as? Activity)?.finish()
                    }
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        // Do nothing, we just asked
                    }
                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                if (showPremiumPaywall) {
                    PremiumFeatureDialog(
                        onDismiss = { viewModel.dismissPaywall() },
                        onUpgradeClick = {
                            viewModel.dismissPaywall()
                            viewModel.navigateTo(Screen.PREMIUM_PLANS)
                        }
                    )
                }
                val showBottomBar = currentScreen in listOf(
                    Screen.HOME,
                    Screen.MCQ_STUDY,
                    Screen.PRACTICE,
                    Screen.SMART_PRACTICE,
                    Screen.MOCK_TESTS,
                    Screen.LEADERBOARD,
                    Screen.MY_ANALYTICS,
                    Screen.MENU,
                    Screen.STUDY_NOTES,
                    Screen.PROFILE,
                    Screen.SETTINGS,
                    Screen.ABOUT,
                    Screen.CONTACT_US,
                    Screen.WORKSPACE,
                    Screen.USER_NOTIFICATIONS,
                    Screen.HELP_SUPPORT,
                    Screen.ABOUT_LEGAL,
                    Screen.SHARE_SUPPORT,
                    Screen.FAQ,
                    Screen.PRIVACY_POLICY,
                    Screen.TERMS_CONDITIONS
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            JuktiBottomNavigation(
                                currentScreen = currentScreen,
                                onNavigate = { screen ->
                                    if (screen == Screen.MCQ_STUDY) {
                                        viewModel.openStudyHub()
                                    } else {
                                        viewModel.navigateTo(screen)
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    var isTransitioning by remember { mutableStateOf(false) }
                    LaunchedEffect(currentScreen) {
                        isTransitioning = true
                        delay(500) // Block clicks for 500ms after navigation
                        isTransitioning = false
                    }
                    Box(modifier = Modifier
                        .padding(innerPadding)
                        .pointerInput(isTransitioning) {
                            if (isTransitioning) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            }
                        }
                    ) {
                        val screen = currentScreen
                        when (screen) {
                                Screen.SPLASH -> SplashScreen(viewModel)
                                Screen.HOME -> HomeScreen(viewModel)
                                Screen.MCQ_STUDY -> McqStudyScreen(viewModel)
                                Screen.PRACTICE -> PracticeScreen(viewModel)
                                Screen.SMART_PRACTICE -> PracticeScreen(viewModel, isSmartPractice = true)
                                Screen.MOCK_TESTS -> MockTestsScreen(viewModel)
                                Screen.MOCK_PLAYER -> MockTestPlayerScreen(viewModel)
                                Screen.MOCK_RESULT -> MockResultScreen(viewModel)
                                Screen.STUDY_NOTES, Screen.STUDY_NOTE_DETAIL -> StudyNotesScreen(viewModel)
                                Screen.LEADERBOARD -> LeaderboardAnalyticsScreen(viewModel, initialTab = 0)
                                Screen.MY_ANALYTICS -> LeaderboardAnalyticsScreen(viewModel, initialTab = 1)
                                Screen.EXAM_INFO -> ExamInfoScreen(viewModel)
                                Screen.PROFILE -> ProfileScreen(viewModel)
                                Screen.PREMIUM_PLANS -> PremiumPlansScreen(viewModel)
                                Screen.GLOBAL_SEARCH -> GlobalSearchScreen(viewModel)
                                Screen.FIREBASE_CONFIG -> SettingsScreen(viewModel)
                                Screen.AUTH -> AuthScreen(viewModel)
                                Screen.MENU -> MenuScreen(viewModel)
                                Screen.SETTINGS -> SettingsScreen(viewModel)
                                Screen.ABOUT -> AboutScreen(viewModel)
                                Screen.CONTACT_US -> ContactUsScreen(viewModel)
                                Screen.WORKSPACE -> WorkspaceScreen(viewModel)
                    Screen.CONTENT_OVERVIEW -> ContentOverviewScreen(viewModel)
                    Screen.CONTENT_QUESTIONS_OVERVIEW -> ContentQuestionsOverviewScreen(viewModel)
                    Screen.CONTENT_MOCKS_OVERVIEW -> ContentMocksOverviewScreen(viewModel)
                    Screen.CONTENT_NOTES_OVERVIEW -> ContentNotesOverviewScreen(viewModel)
                    Screen.CONTENT_CURRENT_AFFAIRS_OVERVIEW -> ContentCurrentAffairsOverviewScreen(viewModel)
                                Screen.OWNER_DASHBOARD -> OwnerDashboardScreen(viewModel)
                                Screen.MANAGE_QBANK -> ManageQBankScreen(viewModel)
                                Screen.MANAGE_MOCK -> ManageMockScreen(viewModel)
                                Screen.MANAGE_PLAN -> ManagePlanScreen(viewModel)
                                Screen.MANAGE_ADMIN -> ManageAdminScreen(viewModel)
                                Screen.ADMIN_ACTIVITY_LOG -> AdminActivityLogScreen(viewModel)
                                Screen.EXPORT_REPORTS -> ExportReportsScreen(viewModel)
                                Screen.PENDING_REQUESTS -> PendingRequestsScreen(viewModel)
                                Screen.SINGLE_QUESTION_UPLOAD -> SingleQuestionUploadScreen(viewModel)
                                Screen.BATCH_IMPORT_QUESTION -> BatchImportQuestionScreen(viewModel)
                                Screen.ALL_QUESTIONS -> AllQuestionsScreen(viewModel)
                                Screen.CREATE_PLAN -> CreatePlanScreen(viewModel)
                                Screen.EDIT_PLAN -> EditPlanScreen(viewModel)
                                Screen.MANAGE_USER_LOG -> ManageUserLogScreen(viewModel)
                                Screen.REPORTED_QUESTIONS -> ReportedQuestionsScreen(viewModel)
                                                                 Screen.MANAGE_EXAMS -> ManageExamsScreen(viewModel)
                                 Screen.CREATE_MOCK -> CreateMockScreen(viewModel)
                                 Screen.EDIT_MOCK -> EditMockScreen(viewModel)
                                 Screen.MANAGE_STUDY_NOTES -> ManageStudyNotesScreen(viewModel)
                                 Screen.MANAGE_CURRENT_AFFAIRS -> ManageCurrentAffairsScreen(viewModel)
                                 Screen.MANAGE_SUBJECTS_CHAPTERS -> ManageSubjectsChaptersScreen(viewModel)
                                 Screen.MANAGE_NOTIFICATIONS -> ManageNotificationsScreen(viewModel)
                                 Screen.STORAGE_MANAGEMENT -> StorageManagementScreen(viewModel)
                                 Screen.USER_NOTIFICATIONS -> UserNotificationsScreen(viewModel)
                                 Screen.MANAGE_EXAM_PATTERN_CUTOFF -> ManageExamPatternCutoffScreen(viewModel)
                                 Screen.MANAGE_EXAM_PATTERN_CUTOFF_UPDATE -> UpdateExamPatternCutoffScreen(viewModel)
                                 Screen.MANAGE_EXAM_PATTERN_CUTOFF_VIEW -> ViewExamPatternCutoffScreen(viewModel)
                                 Screen.MANAGE_BANNERS -> ManageBannersScreen(viewModel)
                                 Screen.PRIVACY_POLICY -> PrivacyPolicyScreen(viewModel)
                                 Screen.TERMS_CONDITIONS -> TermsConditionsScreen(viewModel)
                                 Screen.HELP_SUPPORT -> HelpSupportScreen(viewModel)
                                 Screen.ABOUT_LEGAL -> AboutLegalScreen(viewModel)
                                 Screen.SHARE_SUPPORT -> ShareSupportScreen(viewModel)
                                 Screen.FAQ -> FaqScreen(viewModel)
                            }
                    }
                }
            }
        }
    }
}

@Composable
fun JuktiBottomNavigation(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(
        BottomNavItem("Home", "গৃহ", Icons.Filled.Home, Icons.Outlined.Home, Screen.HOME),
        BottomNavItem("Study", "অধ্যয়ন", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, Screen.MCQ_STUDY),
        BottomNavItem("Mock Tests", "মক টেষ্ট", Icons.Filled.Timer, Icons.Outlined.Timer, Screen.MOCK_TESTS),
        BottomNavItem("Analyze", "বিশ্লেষণ", Icons.Filled.Analytics, Icons.Outlined.Analytics, Screen.MY_ANALYTICS),
        BottomNavItem("Menu", "মেনু", Icons.Filled.Menu, Icons.Outlined.Menu, Screen.MENU)
    )

    NavigationBar {
        items.forEach { item ->
            val isSelected = (currentScreen == item.screen)
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.titleEn
                    )
                },
                label = {
                    Text(item.titleEn)
                }
            )
        }
    }
}

data class BottomNavItem(
    val titleEn: String,
    val titleAs: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: Screen
)
