package com.example.coverdesign.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun staticContent_isDisplayed() {
        composeTestRule.setContent {
            ProfileContent(onSignOut = {})
        }

        composeTestRule.onNodeWithText("User").assertIsDisplayed()
        composeTestRule.onNodeWithText("user@example.com").assertIsDisplayed()
    }

    @Test
    fun menuItems_areDisplayed() {
        composeTestRule.setContent {
            ProfileContent(onSignOut = {})
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Notifications").assertIsDisplayed()
        composeTestRule.onNodeWithText("Help & Support").assertIsDisplayed()
        composeTestRule.onNodeWithText("Privacy Policy").assertIsDisplayed()
    }

    @Test
    fun signOut_click_invokesCallback() {
        var signOutCalled = false
        composeTestRule.setContent {
            ProfileContent(onSignOut = { signOutCalled = true })
        }

        composeTestRule.onNodeWithText("Sign Out").performClick()
        composeTestRule.waitForIdle()

        assertTrue(signOutCalled)
    }
}