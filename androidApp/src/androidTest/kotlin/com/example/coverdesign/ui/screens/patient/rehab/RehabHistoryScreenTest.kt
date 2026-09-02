package com.example.coverdesign.ui.screens.patient.rehab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.coverdesign.ui.viewmodel.RehabAction
import com.example.coverdesign.ui.viewmodel.RehabMockData
import com.example.coverdesign.ui.viewmodel.RehabUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RehabHistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sessions = RehabMockData.generateSessions()

    @Test
    fun titleIsDisplayed() {
        composeTestRule.setContent {
            RehabHistoryContent(state = RehabUiState(), onAction = {}, onBack = {})
        }
        composeTestRule.onNodeWithText("Rehabilitation History").assertIsDisplayed()
    }

    @Test
    fun scoreTrendCardIsDisplayed() {
        composeTestRule.setContent {
            RehabHistoryContent(state = RehabUiState(), onAction = {}, onBack = {})
        }
        composeTestRule.onNodeWithText("Score Trend").assertIsDisplayed()
    }

    @Test
    fun sessionsHeaderIsDisplayed() {
        composeTestRule.setContent {
            RehabHistoryContent(
                state = RehabUiState(sessions = sessions),
                onAction = {},
                onBack = {}
            )
        }
        composeTestRule.onNodeWithText("Sessions").assertIsDisplayed()
    }

    @Test
    fun emptySessionsShowsPlaceholder() {
        composeTestRule.setContent {
            RehabHistoryContent(
                state = RehabUiState(sessions = emptyList()),
                onAction = {},
                onBack = {}
            )
        }
        composeTestRule.onNodeWithText("No sessions recorded yet.").assertIsDisplayed()
    }

    @Test
    fun backButtonInvokesCallback() {
        var backCalled = false
        composeTestRule.setContent {
            RehabHistoryContent(
                state = RehabUiState(),
                onAction = {},
                onBack = { backCalled = true }
            )
        }
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(backCalled)
    }

    @Test
    fun sessionCardRendersSessionData() {
        val session = sessions.first()
        composeTestRule.setContent {
            RehabHistoryContent(
                state = RehabUiState(sessions = listOf(session)),
                onAction = {},
                onBack = {}
            )
        }
        val stepCount = session.pressureData?.stepCount ?: 0
        val durationMin = session.durationSeconds / 60
        composeTestRule.onNodeWithText("$durationMin min session · $stepCount steps")
            .assertIsDisplayed()
    }

    @Test
    fun clickingSessionCardDispatchesToggleAction() {
        val session = sessions.first()
        var receivedAction: RehabAction? = null
        composeTestRule.setContent {
            RehabHistoryContent(
                state = RehabUiState(sessions = listOf(session)),
                onAction = { receivedAction = it },
                onBack = {}
            )
        }
        val stepCount = session.pressureData?.stepCount ?: 0
        val durationMin = session.durationSeconds / 60
        composeTestRule.onNodeWithText("$durationMin min session · $stepCount steps").performClick()
        assertEquals(RehabAction.ToggleSession(session.id), receivedAction)
    }

    @Test
    fun expandedSessionShowsCopTrajectoryLabel() {
        val session = sessions.first()
        composeTestRule.setContent {
            RehabHistoryContent(
                state = RehabUiState(
                    sessions = listOf(session),
                    expandedSessionId = session.id,
                    hasInsoles = true
                ),
                onAction = {},
                onBack = {}
            )
        }
        composeTestRule.onNodeWithText("COP Trajectory").assertIsDisplayed()
    }

    @Test
    fun expandedSessionWithNoInsolesShowsPlaceholder() {
        val session = sessions.first()
        composeTestRule.setContent {
            RehabHistoryContent(
                state = RehabUiState(
                    sessions = listOf(session),
                    expandedSessionId = session.id,
                    hasInsoles = false
                ),
                onAction = {},
                onBack = {}
            )
        }
        composeTestRule.onNodeWithText("No smart insoles connected").assertIsDisplayed()
    }

    @Test
    fun expandedSessionShowsFootLoadDistribution() {
        val session = sessions.first()
        composeTestRule.setContent {
            RehabHistoryContent(
                state = RehabUiState(
                    sessions = listOf(session),
                    expandedSessionId = session.id,
                    hasInsoles = true
                ),
                onAction = {},
                onBack = {}
            )
        }
        composeTestRule.onNodeWithText("Foot Load Distribution").assertIsDisplayed()
    }
}
