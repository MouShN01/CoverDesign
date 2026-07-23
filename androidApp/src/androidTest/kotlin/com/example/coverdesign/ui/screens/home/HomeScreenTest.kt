package com.example.coverdesign.ui.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import com.example.coverdesign.ui.viewmodel.HomeAction
import com.example.coverdesign.ui.viewmodel.HomeUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

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
    fun testNewOrderClick() {
        var actionReceived: HomeAction? = null
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(),
                onAction = { actionReceived = it }
            )
        }

        composeTestRule.onNodeWithText("Create new order").performClick()
        assertEquals(HomeAction.NewOrder, actionReceived)
    }

    @Test
    fun testToolCardsClick() {
        var actionReceived: HomeAction? = null
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(),
                onAction = { actionReceived = it }
            )
        }

        composeTestRule.onNodeWithText("3D Visualisation").performClick()
        assertEquals(HomeAction.Open3D, actionReceived)

        composeTestRule.onNodeWithText("Virtual Try-On").performClick()
        assertEquals(HomeAction.OpenTryOn, actionReceived)
    }

    @Test
    fun testEmptyOrdersState() {
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(recentOrders = emptyList()),
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("No orders yet").assertIsDisplayed()
    }

    @Test
    fun testRecentOrderClick() {
        var actionReceived: HomeAction? = null
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(recentOrders = listOf(sampleOrder)),
                onAction = { actionReceived = it }
            )
        }

        composeTestRule.onNodeWithText("THREE_D_PRINT").performClick()
        assertEquals(HomeAction.OpenOrder("order_1"), actionReceived)
    }

    @Test
    fun testSeeAllOrdersClick() {
        var actionReceived: HomeAction? = null
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(recentOrders = listOf(sampleOrder)),
                onAction = { actionReceived = it }
            )
        }

        composeTestRule.onNodeWithText("See all").performClick()
        assertEquals(HomeAction.SeeAllOrders, actionReceived)
    }
}
