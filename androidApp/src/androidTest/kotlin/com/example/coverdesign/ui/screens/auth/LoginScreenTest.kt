package com.example.coverdesign.ui.screens.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.coverdesign.ui.viewmodel.AuthAction
import com.example.coverdesign.ui.viewmodel.AuthUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

//    @Test
//    fun header_isDisplayed() {
//        composeTestRule.setContent {
//            LoginContent(
//                state = AuthUiState(),
//                onAction = {},
//                onNavigateToRegister = {}
//            )
//        }
//
//        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Welcome back to ProsCover").assertIsDisplayed()
//    }

    @Test
    fun forgotPassword_click_dispatchesResetPasswordAction() {
        var lastAction: AuthAction? = null
        composeTestRule.setContent {
            LoginContent(
                state = AuthUiState(),
                onAction = { lastAction = it },
                onNavigateToRegister = {}
            )
        }

        composeTestRule.onNodeWithText("Forgot password?").performClick()

        assertTrue(lastAction is AuthAction.ResetPassword)
    }

    @Test
    fun createAccount_click_invokesNavigationCallback() {
        var navigateCalled = false
        composeTestRule.setContent {
            LoginContent(
                state = AuthUiState(),
                onAction = {},
                onNavigateToRegister = { navigateCalled = true }
            )
        }

        composeTestRule.onNodeWithText("Create Account").performClick()
        composeTestRule.waitForIdle()

        assertTrue(navigateCalled)
    }

    @Test
    fun resetEmailSent_showsConfirmationBanner() {
        composeTestRule.setContent {
            LoginContent(
                state = AuthUiState(resetEmailSent = true),
                onAction = {},
                onNavigateToRegister = {}
            )
        }

        composeTestRule.onNodeWithText(
            "Password reset email sent — check your inbox."
        ).assertIsDisplayed()
    }

    @Test
    fun signIn_click_withCredentials_dispatchesSignInWithEmailAction() {
        var lastAction: AuthAction? = null
        composeTestRule.setContent {
            LoginContent(
                state = AuthUiState(),
                onAction = { lastAction = it },
                onNavigateToRegister = {}
            )
        }

        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        // "Sign In" appears as both the screen heading and the button label,
        // so filter to the one with a click action (the button).
        composeTestRule.onAllNodesWithText("Sign In")
            .filterToOne(hasClickAction())
            .performClick()
        composeTestRule.waitForIdle()

        assertTrue(lastAction is AuthAction.SignInWithEmail)
        val action = lastAction as AuthAction.SignInWithEmail
        assertEquals("test@example.com", action.email)
        assertEquals("password123", action.password)
    }
}