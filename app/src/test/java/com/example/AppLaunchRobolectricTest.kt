package com.example

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.JuktiDatabase
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppLaunchRobolectricTest {

    @Test
    fun testDatabaseInitialization() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = JuktiDatabase.getDatabase(context)
        assertNotNull(db)
        assertNotNull(db.questionDao())
        assertNotNull(db.userProfileDao())
    }

    @Test
    fun testViewModelInitializationAndTransitions() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = JuktiViewModel(context)
        assertNotNull(viewModel)
        assertNotNull(viewModel.effectiveEntitlement)
        assertNotNull(viewModel.questions)
        assertNotNull(viewModel.mockTests)
        assertNotNull(viewModel.studyNotes)
        
        // Test finish splash transition
        viewModel.finishSplash()
        
        // Test navigation across core screens
        listOf(
            Screen.HOME,
            Screen.MCQ_STUDY,
            Screen.PRACTICE,
            Screen.MOCK_TESTS,
            Screen.STUDY_NOTES,
            Screen.LEADERBOARD,
            Screen.PROFILE,
            Screen.SETTINGS,
            Screen.ABOUT
        ).forEach { screen ->
            viewModel.navigateTo(screen)
        }
    }

    @Test
    fun testActivityScenarioLaunch() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity)
            }
        }
    }
}
