package com.example.coverdesign.ui.screens.config
//
//
//import androidx.compose.ui.test.assertIsDisplayed
//import androidx.compose.ui.test.assertIsNotEnabled
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.compose.ui.test.onNodeWithText
//import androidx.compose.ui.test.performClick
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.example.coverdesign.domain.model.ProductCategory
//import org.junit.Assert.assertTrue
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//class HumanPlaceholderScreenTest {
//
//    @get:Rule
//    val composeTestRule = createComposeRule()
//
//    @Test
//    fun modelNotReady_showsGeneratingText() {
//        composeTestRule.setContent {
//            HumanPlaceholderScreen(
//                productCategory = ProductCategory.THREE_D_PRINT,
//                isModelReady = false,
//                onProceedToAr = {},
//                onBack = {}
//            )
//        }
//
//        composeTestRule.onNodeWithText("Generating 3D model…").assertIsDisplayed()
//    }
//
//    @Test
//    fun modelNotReady_proceedButton_isDisabled() {
//        composeTestRule.setContent {
//            HumanPlaceholderScreen(
//                productCategory = ProductCategory.THREE_D_PRINT,
//                isModelReady = false,
//                onProceedToAr = {},
//                onBack = {}
//            )
//        }
//
//        composeTestRule.onNodeWithText("Waiting for 3D model…").assertIsNotEnabled()
//    }
//
//    @Test
//    fun modelNotReady_showsContinueEditingButton() {
//        composeTestRule.setContent {
//            HumanPlaceholderScreen(
//                productCategory = ProductCategory.THREE_D_PRINT,
//                isModelReady = false,
//                onProceedToAr = {},
//                onBack = {}
//            )
//        }
//
//        composeTestRule.onNodeWithText("Continue editing configuration").assertIsDisplayed()
//    }
//
//    @Test
//    fun modelReady_showsStartLiveTryOnButton() {
//        composeTestRule.setContent {
//            HumanPlaceholderScreen(
//                productCategory = ProductCategory.THREE_D_PRINT,
//                isModelReady = true,
//                onProceedToAr = {},
//                onBack = {}
//            )
//        }
//
//        composeTestRule.onNodeWithText("Start Live Try-On").assertIsDisplayed()
//    }
//
//    @Test
//    fun modelReady_hidesContinueEditingButton() {
//        composeTestRule.setContent {
//            HumanPlaceholderScreen(
//                productCategory = ProductCategory.THREE_D_PRINT,
//                isModelReady = true,
//                onProceedToAr = {},
//                onBack = {}
//            )
//        }
//
//        composeTestRule.onNodeWithText("Continue editing configuration").assertDoesNotExist()
//    }
//
//    @Test
//    fun startLiveTryOn_click_invokesProceedToArCallback() {
//        var callbackInvoked = false
//        composeTestRule.setContent {
//            HumanPlaceholderScreen(
//                productCategory = ProductCategory.THREE_D_PRINT,
//                isModelReady = true,
//                onProceedToAr = { callbackInvoked = true },
//                onBack = {}
//            )
//        }
//
//        composeTestRule.onNodeWithText("Start Live Try-On").performClick()
//        composeTestRule.waitForIdle()
//
//        assertTrue(callbackInvoked)
//    }
//
//    @Test
//    fun continueEditing_click_invokesOnBackCallback() {
//        var callbackInvoked = false
//        composeTestRule.setContent {
//            HumanPlaceholderScreen(
//                productCategory = ProductCategory.CLOTHING,
//                isModelReady = false,
//                onProceedToAr = {},
//                onBack = { callbackInvoked = true }
//            )
//        }
//
//        composeTestRule.onNodeWithText("Continue editing configuration").performClick()
//        composeTestRule.waitForIdle()
//
//        assertTrue(callbackInvoked)
//    }
//
//    @Test
//    fun bodyLabel_clothing_isDisplayed() {
//        composeTestRule.setContent {
//            HumanPlaceholderScreen(
//                productCategory = ProductCategory.CLOTHING,
//                isModelReady = true,
//                onProceedToAr = {},
//                onBack = {}
//            )
//        }
//
//        composeTestRule.onNodeWithText("Full body — clothing fitting mode").assertIsDisplayed()
//    }
//
//    @Test
//    fun bodyLabel_footwear_isDisplayed() {
//        composeTestRule.setContent {
//            HumanPlaceholderScreen(
//                productCategory = ProductCategory.FOOTWEAR,
//                isModelReady = true,
//                onProceedToAr = {},
//                onBack = {}
//            )
//        }
//
//        composeTestRule.onNodeWithText("Lower legs — footwear fitting highlighted").assertIsDisplayed()
//    }
//}