package com.example.coverdesign.ui.screens.config

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.coverdesign.ui.viewmodel.OrderType
import com.example.coverdesign.ui.viewmodel.ProductConfigAction
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TypePickerContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun headlineIsDisplayed() {
        composeTestRule.setContent {
            TypePickerContent(onAction = {})
        }
        composeTestRule.onNodeWithText("What would you like to order?").assertIsDisplayed()
    }

    @Test
    fun subheadingIsDisplayed() {
        composeTestRule.setContent {
            TypePickerContent(onAction = {})
        }
        composeTestRule.onNodeWithText(
            "Select a product type to start configuring your order."
        ).assertIsDisplayed()
    }

    @Test
    fun insoleCardIsDisplayed() {
        composeTestRule.setContent {
            TypePickerContent(onAction = {})
        }
        composeTestRule.onNodeWithText("Insole").assertIsDisplayed()
    }

    @Test
    fun prostheticCoverCardIsDisplayed() {
        composeTestRule.setContent {
            TypePickerContent(onAction = {})
        }
        composeTestRule.onNodeWithText("Prosthetic Cover").assertIsDisplayed()
    }

    @Test
    fun insoleCardDescriptionIsDisplayed() {
        composeTestRule.setContent {
            TypePickerContent(onAction = {})
        }
        composeTestRule.onNodeWithText(
            "Custom orthopaedic insole sized and shaped to your foot measurements."
        ).assertIsDisplayed()
    }

    @Test
    fun prostheticCoverCardDescriptionIsDisplayed() {
        composeTestRule.setContent {
            TypePickerContent(onAction = {})
        }
        composeTestRule.onNodeWithText(
            "3D-printed cosmetic cover matched to your prosthesis type and measurements."
        ).assertIsDisplayed()
    }

    @Test
    fun clickingInsoleCardDispatchesSelectInsoleAction() {
        var receivedAction: ProductConfigAction? = null
        composeTestRule.setContent {
            TypePickerContent(onAction = { receivedAction = it })
        }
        composeTestRule.onNodeWithText("Insole").performClick()
        assertEquals(ProductConfigAction.SelectOrderType(OrderType.INSOLE), receivedAction)
    }

    @Test
    fun clickingProstheticCoverCardDispatchesSelectProstheticAction() {
        var receivedAction: ProductConfigAction? = null
        composeTestRule.setContent {
            TypePickerContent(onAction = { receivedAction = it })
        }
        composeTestRule.onNodeWithText("Prosthetic Cover").performClick()
        assertEquals(
            ProductConfigAction.SelectOrderType(OrderType.PROSTHETIC_COVER),
            receivedAction
        )
    }
}
