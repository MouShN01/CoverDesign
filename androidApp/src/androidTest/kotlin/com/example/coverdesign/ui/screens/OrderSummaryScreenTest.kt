package com.example.coverdesign.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.ui.viewmodel.OrderSummaryUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrderSummaryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleOrder = Order(
        id = "ord_1",
        adaptedModelId = null,
        config = ProductConfig(
            id = "conf_1",
            productId = "prod_1",
            orderCategory = OrderCategory.ACCESSORY,
            measurement = Measurement(100.0, 50.0, 10.0),
            sizeLabel = SizeLabel.M,
            color = ProductColor("Black", "#000000"),
            material = Material.METAL,
            texture = Texture("txt_1", "Brushed", null, null),
            inscription = "Custom Text",
            userPhotoUri = null,
            additionalElements = emptyList(),
            quantity = 1,
            createdAt = 0L
        ),
        priceEstimate = PriceEstimate(
            configId = "conf_1",
            basePrice = 100.0,
            subtotal = 110.0,
            total = 110.0
        ),
        status = OrderStatus.DRAFT,
        createdAt = 0L
    )

    @Test
    fun orderSections_areDisplayed() {
        composeTestRule.setContent {
            OrderSummaryScreenContent(
                uiState = OrderSummaryUiState(order = sampleOrder),
                onBack = {},
                onSubmit = {}
            )
        }

        composeTestRule.onNodeWithText("Product").assertIsDisplayed()
        composeTestRule.onNodeWithText("Measurements").assertIsDisplayed()
        composeTestRule.onNodeWithText("Order").assertIsDisplayed()
    }

    @Test
    fun materialAndColor_areDisplayed() {
        composeTestRule.setContent {
            OrderSummaryScreenContent(
                uiState = OrderSummaryUiState(order = sampleOrder),
                onBack = {},
                onSubmit = {}
            )
        }

        composeTestRule.onNodeWithText("Metal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Black").assertIsDisplayed()
    }

    @Test
    fun personalisation_shown_whenInscriptionPresent() {
        composeTestRule.setContent {
            OrderSummaryScreenContent(
                uiState = OrderSummaryUiState(order = sampleOrder),
                onBack = {},
                onSubmit = {}
            )
        }

        composeTestRule.onNodeWithText("Personalisation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom Text").assertIsDisplayed()
    }

    @Test
    fun personalisation_hidden_whenInscriptionEmpty() {
        val orderNoPersonalisation = sampleOrder.copy(
            config = sampleOrder.config.copy(inscription = "", additionalElements = emptyList())
        )
        composeTestRule.setContent {
            OrderSummaryScreenContent(
                uiState = OrderSummaryUiState(order = orderNoPersonalisation),
                onBack = {},
                onSubmit = {}
            )
        }

        composeTestRule.onNodeWithText("Personalisation").assertDoesNotExist()
    }

    @Test
    fun confirmOrder_button_enabled_whenOrderAvailable() {
        composeTestRule.setContent {
            OrderSummaryScreenContent(
                uiState = OrderSummaryUiState(order = sampleOrder),
                onBack = {},
                onSubmit = {}
            )
        }

        composeTestRule.onNodeWithText("Confirm Order").assertIsEnabled()
    }

    @Test
    fun confirmOrder_button_disabled_whenAlreadySubmitted() {
        composeTestRule.setContent {
            OrderSummaryScreenContent(
                uiState = OrderSummaryUiState(
                    order = sampleOrder,
                    submittedOrderId = "ord_1"
                ),
                onBack = {},
                onSubmit = {}
            )
        }

        composeTestRule.onNodeWithText("Confirm Order").assertIsNotEnabled()
    }

    @Test
    fun confirmOrder_click_invokesOnSubmitWithOrder() {
        var submittedOrder: Order? = null
        composeTestRule.setContent {
            OrderSummaryScreenContent(
                uiState = OrderSummaryUiState(order = sampleOrder),
                onBack = {},
                onSubmit = { submittedOrder = it }
            )
        }

        composeTestRule.onNodeWithText("Confirm Order").performClick()
        composeTestRule.waitForIdle()

        assertEquals(sampleOrder, submittedOrder)
    }
}