package com.example.coverdesign.ui.screens.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.coverdesign.ui.viewmodel.AuthAction
import com.example.coverdesign.ui.viewmodel.AuthUiState
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
}