package com.example

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
            val language by viewModel.language.collectAsState()
            val showPremiumPaywall by viewModel.showPremiumPaywall.collectAsState()

            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            JuktiTheme(darkTheme = isDarkTheme ?: systemDark) {
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
                    Screen.MOCK_TESTS,
                    Screen.LEADERBOARD,
                    Screen.MENU,
                    Screen.STUDY_NOTES,
                    Screen.PROFILE,
                    Screen.SETTINGS,
                    Screen.ABOUT,
                    Screen.CONTACT_US,
                    Screen.WORKSPACE,
                    Screen.USER_NOTIFICATIONS
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            JuktiBottomNavigation(
                                currentScreen = currentScreen,
                                language = language,
                                onNavigate = { screen -> viewModel.navigateTo(screen) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                            when (screen) {
                                Screen.HOME -> HomeScreen(viewModel)
                                Screen.MCQ_STUDY -> McqStudyScreen(viewModel)
                                Screen.PRACTICE -> PracticeScreen(viewModel)
                                Screen.MOCK_TESTS -> MockTestsScreen(viewModel)
                                Screen.MOCK_PLAYER -> MockTestPlayerScreen(viewModel)
                                Screen.MOCK_RESULT -> MockResultScreen(viewModel)
                                Screen.STUDY_NOTES, Screen.STUDY_NOTE_DETAIL -> StudyNotesScreen(viewModel)
                                Screen.LEADERBOARD -> LeaderboardAnalyticsScreen(viewModel)
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
                                 Screen.MANAGE_SUBJECTS_CHAPTERS -> ManageSubjectsChaptersScreen(viewModel)
                                 Screen.MANAGE_NOTIFICATIONS -> ManageNotificationsScreen(viewModel)
                                 Screen.USER_NOTIFICATIONS -> UserNotificationsScreen(viewModel)
                                  Screen.MANAGE_EXAM_PATTERN_CUTOFF -> ManageExamPatternCutoffScreen(viewModel)
                                   Screen.MANAGE_BANNERS -> ManageBannersScreen(viewModel)
                                    Screen.REFUND_POLICY -> RefundPolicyScreen(viewModel)
                            }
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
    language: AppLanguage,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(
        BottomNavItem("Home", "গৃহ", Icons.Filled.Home, Icons.Outlined.Home, Screen.HOME),
        BottomNavItem("Study", "অধ্যয়ন", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, Screen.MCQ_STUDY),
        BottomNavItem("Mock Tests", "মক টেষ্ট", Icons.Filled.Timer, Icons.Outlined.Timer, Screen.MOCK_TESTS),
        BottomNavItem("Analyze", "বিশ্লেষণ", Icons.Filled.Analytics, Icons.Outlined.Analytics, Screen.LEADERBOARD),
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
                    Text(if (language == AppLanguage.ASSAMESE) item.titleAs else item.titleEn)
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
