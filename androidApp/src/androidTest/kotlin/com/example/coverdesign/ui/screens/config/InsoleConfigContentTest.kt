package com.example.coverdesign.ui.screens.config

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.coverdesign.ui.viewmodel.InsoleFootConfig
import com.example.coverdesign.ui.viewmodel.InsoleOrderUiState
import com.example.coverdesign.ui.viewmodel.LegSide
import com.example.coverdesign.ui.viewmodel.ProductConfigAction
import com.example.coverdesign.ui.viewmodel.ProductConfigUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class InsoleConfigContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val defaultState = ProductConfigUiState()

    @Test
    fun primaryFootSectionTitleIsDisplayed() {
        composeTestRule.setContent {
            InsoleConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Primary Foot").assertIsDisplayed()
    }

    @Test
    fun leftLegSideChipIsDisplayed() {
        composeTestRule.setContent {
            InsoleConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Left").assertIsDisplayed()
    }

    @Test
    fun rightLegSideChipIsDisplayed() {
        composeTestRule.setContent {
            InsoleConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Right").assertIsDisplayed()
    }

    @Test
    fun addSecondFootButtonIsDisplayed() {
        composeTestRule.setContent {
            InsoleConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("+ Add other foot measurements").assertIsDisplayed()
    }

    @Test
    fun clickingAddSecondFootButtonDispatchesAction() {
        var receivedAction: ProductConfigAction? = null
        composeTestRule.setContent {
            InsoleConfigContent(
                state = defaultState,
                onAction = { receivedAction = it }
            )
        }
        composeTestRule.onNodeWithText("+ Add other foot measurements").performClick()
        assertEquals(ProductConfigAction.AddInsoleSecondFoot, receivedAction)
    }

    @Test
    fun secondFootSectionIsDisplayedWhenConfigured() {
        val stateWithSecondFoot = defaultState.copy(
            insoleConfig = InsoleOrderUiState(
                primaryFoot = InsoleFootConfig(side = LegSide.LEFT),
                secondFoot = InsoleFootConfig(side = LegSide.RIGHT)
            )
        )
        composeTestRule.setContent {
            InsoleConfigContent(state = stateWithSecondFoot, onAction = {})
        }
        composeTestRule.onNodeWithText("Second Foot").assertIsDisplayed()
    }

    @Test
    fun addSecondFootButtonIsHiddenWhenSecondFootAlreadyConfigured() {
        val stateWithSecondFoot = defaultState.copy(
            insoleConfig = InsoleOrderUiState(
                primaryFoot = InsoleFootConfig(side = LegSide.LEFT),
                secondFoot = InsoleFootConfig(side = LegSide.RIGHT)
            )
        )
        composeTestRule.setContent {
            InsoleConfigContent(state = stateWithSecondFoot, onAction = {})
        }
        composeTestRule.onNodeWithText("+ Add other foot measurements").assertDoesNotExist()
    }

    @Test
    fun appearanceSectionTitleIsPresent() {
        composeTestRule.setContent {
            InsoleConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
    }

//    @Test
//    fun quantityLabelIsPresent() {
//        composeTestRule.setContent {
//            InsoleConfigContent(state = defaultState, onAction = {})
//        }
//        composeTestRule.onNodeWithText("Quantity").assertIsDisplayed()
//    }

    @Test
    fun materialDropdownLabelIsPresent() {
        composeTestRule.setContent {
            InsoleConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Material").assertIsDisplayed()
    }

    @Test
    fun clickingRightChipForPrimaryFootDispatchesUpdateAction() {
        var receivedAction: ProductConfigAction? = null
        composeTestRule.setContent {
            InsoleConfigContent(
                state = defaultState,
                onAction = { receivedAction = it }
            )
        }
        composeTestRule.onNodeWithText("Right").performClick()
        val expected = ProductConfigAction.UpdateInsolePrimaryFoot(
            defaultState.insoleConfig.primaryFoot.copy(side = LegSide.RIGHT)
        )
        assertEquals(expected, receivedAction)
    }
}
