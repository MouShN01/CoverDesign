package com.example.coverdesign.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.domain.model.ProductCategory
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
class OrderListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleOrder = Order(
        id = "order_1",
        adaptedModelId = null,
        config = ProductConfig(
            id = "config_1",
            productId = "prod_1",
            productCategory = ProductCategory.THREE_D_PRINT,
            measurement = Measurement(10.0, 10.0, 10.0),
            sizeLabel = SizeLabel.M,
            color = ProductColor("Red", "#FF0000"),
            material = Material.PLA,
            texture = Texture("t1", "Smooth", null, null),
            inscription = "",
            userPhotoUri = null,
            additionalElements = emptyList(),
            quantity = 1,
            createdAt = 123456789L
        ),
        priceEstimate = PriceEstimate(
            configId = "config_1",
            basePrice = 100.0,
            subtotal = 100.0,
            total = 100.0
        ),
        status = OrderStatus.SUBMITTED,
        createdAt = 123456789L
    )

    @Test
    fun emptyOrders_showsNoOrdersYetMessage() {
        composeTestRule.setContent {
            OrdersListContent(
                state = OrderSummaryUiState(orderHistory = emptyList()),
                onOrderClick = {}
            )
        }

        composeTestRule.onNodeWithText("No orders yet").assertIsDisplayed()
    }

    @Test
    fun orderList_showsCategoryName() {
        composeTestRule.setContent {
            OrdersListContent(
                state = OrderSummaryUiState(orderHistory = listOf(sampleOrder)),
                onOrderClick = {}
            )
        }

        composeTestRule.onNodeWithText("THREE_D_PRINT").assertIsDisplayed()
    }

    @Test
    fun orderList_showsFormattedTotal() {
        composeTestRule.setContent {
            OrdersListContent(
                state = OrderSummaryUiState(orderHistory = listOf(sampleOrder)),
                onOrderClick = {}
            )
        }

        composeTestRule.onNodeWithText("$100.00").assertIsDisplayed()
    }

    @Test
    fun orderCard_click_invokesCallbackWithOrderId() {
        var clickedOrderId: String? = null
        composeTestRule.setContent {
            OrdersListContent(
                state = OrderSummaryUiState(orderHistory = listOf(sampleOrder)),
                onOrderClick = { clickedOrderId = it }
            )
        }

        composeTestRule.onNodeWithText("THREE_D_PRINT").performClick()
        composeTestRule.waitForIdle()

        assertEquals("order_1", clickedOrderId)
    }
}