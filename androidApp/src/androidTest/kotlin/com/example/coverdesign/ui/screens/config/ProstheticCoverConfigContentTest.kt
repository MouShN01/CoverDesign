package com.example.coverdesign.ui.screens.config

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.coverdesign.ui.viewmodel.LegSide
import com.example.coverdesign.ui.viewmodel.LimbLevel
import com.example.coverdesign.ui.viewmodel.ProductConfigAction
import com.example.coverdesign.ui.viewmodel.ProductConfigUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProstheticCoverConfigContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val defaultState = ProductConfigUiState()

    @Test
    fun prosthesisTypeSectionTitleIsDisplayed() {
        composeTestRule.setContent {
            ProstheticCoverConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Prosthesis Type").assertIsDisplayed()
    }

    @Test
    fun limbSideLabelIsDisplayed() {
        composeTestRule.setContent {
            ProstheticCoverConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Limb side").assertIsDisplayed()
    }

    @Test
    fun leftLimbSideChipIsDisplayed() {
        composeTestRule.setContent {
            ProstheticCoverConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Left").assertIsDisplayed()
    }

    @Test
    fun rightLimbSideChipIsDisplayed() {
        composeTestRule.setContent {
            ProstheticCoverConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Right").assertIsDisplayed()
    }

    @Test
    fun levelLabelIsDisplayed() {
        composeTestRule.setContent {
            ProstheticCoverConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Level").assertIsDisplayed()
    }

    @Test
    fun aboveKneeLimbLevelChipIsDisplayed() {
        composeTestRule.setContent {
            ProstheticCoverConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Above Knee").assertIsDisplayed()
    }

    @Test
    fun belowKneeLimbLevelChipIsDisplayed() {
        composeTestRule.setContent {
            ProstheticCoverConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Below Knee").assertIsDisplayed()
    }

    @Test
    fun commonMeasurementsSectionIsDisplayed() {
        composeTestRule.setContent {
            ProstheticCoverConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Common Measurements").assertIsDisplayed()
    }

    @Test
    fun circumferenceFieldIsDisplayed() {
        composeTestRule.setContent {
            ProstheticCoverConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Circumference").assertIsDisplayed()
    }

    @Test
    fun fasteningTypeSectionIsDisplayed() {
        composeTestRule.setContent {
            ProstheticCoverConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Fastening").assertIsDisplayed()
    }

    @Test
    fun strapFasteningOptionIsDisplayed() {
        composeTestRule.setContent {
            ProstheticCoverConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Strap").assertIsDisplayed()
    }

    @Test
    fun magneticFasteningOptionIsDisplayed() {
        composeTestRule.setContent {
            ProstheticCoverConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Magnetic").assertIsDisplayed()
    }

    @Test
    fun appearanceSectionIsDisplayed() {
        composeTestRule.setContent {
            ProstheticCoverConfigContent(state = defaultState, onAction = {})
        }
        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
    }

//    @Test
//    fun additionalNotesSectionIsDisplayed() {
//        composeTestRule.setContent {
//            ProstheticCoverConfigContent(state = defaultState, onAction = {})
//        }
//        composeTestRule.onNodeWithText("Additional Notes").assertIsDisplayed()
//    }

    @Test
    fun clickingRightLimbSideDispatchesUpdateProstheticSideAction() {
        var receivedAction: ProductConfigAction? = null
        composeTestRule.setContent {
            ProstheticCoverConfigContent(
                state = defaultState,
                onAction = { receivedAction = it }
            )
        }
        composeTestRule.onNodeWithText("Right").performClick()
        assertEquals(ProductConfigAction.UpdateProstheticSide(LegSide.RIGHT), receivedAction)
    }

    @Test
    fun clickingAboveKneeLevelDispatchesSelectLimbLevelAction() {
        var receivedAction: ProductConfigAction? = null
        composeTestRule.setContent {
            ProstheticCoverConfigContent(
                state = defaultState,
                onAction = { receivedAction = it }
            )
        }
        composeTestRule.onNodeWithText("Above Knee").performClick()
        assertEquals(ProductConfigAction.SelectLimbLevel(LimbLevel.ABOVE_KNEE), receivedAction)
    }

    @Test
    fun clickingBelowKneeLevelDispatchesSelectLimbLevelAction() {
        var receivedAction: ProductConfigAction? = null
        composeTestRule.setContent {
            ProstheticCoverConfigContent(
                state = defaultState,
                onAction = { receivedAction = it }
            )
        }
        composeTestRule.onNodeWithText("Below Knee").performClick()
        assertEquals(ProductConfigAction.SelectLimbLevel(LimbLevel.BELOW_KNEE), receivedAction)
    }
}
