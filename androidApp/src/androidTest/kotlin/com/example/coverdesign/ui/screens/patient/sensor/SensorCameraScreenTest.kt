package com.example.coverdesign.ui.screens.patient.sensor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

/**
 * Tests for SensorCameraScreen. Camera and audio permissions are not granted in the
 * test environment, so the permission-gate UI is shown instead of the live camera feed.
 */
class SensorCameraScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun sensorScannerTitleIsDisplayed() {
        composeTestRule.setContent {
            SensorCameraScreen()
        }
        composeTestRule.onNodeWithText("Sensor Scanner").assertIsDisplayed()
    }

    @Test
    fun permissionGateHeadlineIsDisplayedWithoutCameraPermission() {
        composeTestRule.setContent {
            SensorCameraScreen()
        }
        composeTestRule.onNodeWithText("Camera access needed").assertIsDisplayed()
    }

    @Test
    fun grantAccessButtonIsDisplayed() {
        composeTestRule.setContent {
            SensorCameraScreen()
        }
        composeTestRule.onNodeWithText("Grant access").assertIsDisplayed()
    }

    @Test
    fun grantAccessButtonIsEnabled() {
        composeTestRule.setContent {
            SensorCameraScreen()
        }
        composeTestRule.onNodeWithText("Grant access").assertIsEnabled()
    }

    @Test
    fun permissionExplanationTextIsDisplayed() {
        composeTestRule.setContent {
            SensorCameraScreen()
        }
        composeTestRule.onNodeWithText(
            "The sensor scanner uses your camera to detect body pose and " +
                "your microphone to optionally record audio alongside video."
        ).assertIsDisplayed()
    }
}
