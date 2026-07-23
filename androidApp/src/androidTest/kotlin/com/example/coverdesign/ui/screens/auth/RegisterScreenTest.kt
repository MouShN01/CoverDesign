package com.example.coverdesign.ui.screens.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.coverdesign.ui.viewmodel.AuthUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

//    @Test
//    fun header_isDisplayed() {
//        composeTestRule.setContent {
//            RegisterContent(
//                state = AuthUiState(),
//                onAction = {},
//                onNavigateBack = {}
//            )
//        }
//
//        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
//        composeTestRule.onNodeWithText(
//            "We need a few details to process your orders"
//        ).assertIsDisplayed()
//    }

 //   @Test
//    fun signIn_link_click_invokesNavigationCallback() {
//        var navigateCalled = false
//        composeTestRule.setContent {
//            RegisterContent(
//                state = AuthUiState(),
//                onAction = {},
//                onNavigateBack = { navigateCalled = true }
//            )
//        }
//
//        composeTestRule.onNodeWithText("Sign In").performClick()
//        composeTestRule.waitForIdle()
//
//        assertTrue(navigateCalled)
//    }

    @Test
    fun createAccount_button_disabled_withEmptyFields() {
        composeTestRule.setContent {
            RegisterContent(
                state = AuthUiState(),
                onAction = {},
                onNavigateBack = {}
            )
        }

        // Finds the "Create Account" Button specifically (not the heading Text)
        // by combining text match with isNotEnabled — only the disabled Button has Disabled=true
        composeTestRule.onNode(hasText("Create Account") and isNotEnabled())
            .assertIsNotEnabled()
    }

    @Test
    fun passwordMismatch_showsValidationError() {
        composeTestRule.setContent {
            RegisterContent(
                state = AuthUiState(),
                onAction = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Password *").performTextInput("password123")
        composeTestRule.onNodeWithText("Confirm password *").performTextInput("different")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Passwords do not match").assertIsDisplayed()
    }
}