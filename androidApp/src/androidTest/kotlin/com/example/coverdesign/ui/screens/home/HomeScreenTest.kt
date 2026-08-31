package com.example.coverdesign.ui.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.OrderListItem
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.ui.viewmodel.HomeAction
import com.example.coverdesign.ui.viewmodel.HomeUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleOrderItem = OrderListItem(
        orderId    = "order_1",
        category   = OrderCategory.PROSTHETIC_COVER,
        createdAt  = 123456789L,
        totalPrice = 100.0,
        status     = OrderStatus.SUBMITTED
    )

    @Test
    fun testNewOrderClick() {
        var actionReceived: HomeAction? = null
        composeTestRule.setContent {
            HomeContent(
                state    = HomeUiState(),
                onAction = { actionReceived = it }
            )
        }

        composeTestRule.onNodeWithText("Create new order").performClick()
        assertEquals(HomeAction.NewOrder, actionReceived)
    }

    @Test
    fun testVirtualTryOnClick() {
        var actionReceived: HomeAction? = null
        composeTestRule.setContent {
            HomeContent(
                state    = HomeUiState(),
                onAction = { actionReceived = it }
            )
        }

        composeTestRule.onNodeWithText("Virtual Try-On").performClick()
        assertEquals(HomeAction.OpenTryOn, actionReceived)
    }

    @Test
    fun testEmptyOrdersState() {
        composeTestRule.setContent {
            HomeContent(
                state    = HomeUiState(recentOrders = emptyList()),
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("No orders yet").assertIsDisplayed()
    }

    @Test
    fun testEmptyRehabState() {
        composeTestRule.setContent {
            HomeContent(
                state    = HomeUiState(rehabSessions = emptyList()),
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("No sessions recorded yet").assertIsDisplayed()
    }

    @Test
    fun testRecentOrderClick() {
        var actionReceived: HomeAction? = null
        composeTestRule.setContent {
            HomeContent(
                state    = HomeUiState(recentOrders = listOf(sampleOrderItem)),
                onAction = { actionReceived = it }
            )
        }

        // RecentOrderCard shows category.label — PROSTHETIC_COVER → "Prosthetic Cover"
        composeTestRule.onNodeWithText("Prosthetic Cover").performClick()
        assertEquals(HomeAction.OpenOrder("order_1"), actionReceived)
    }

    @Test
    fun testSeeAllOrdersClick() {
        var actionReceived: HomeAction? = null
        composeTestRule.setContent {
            HomeContent(
                state    = HomeUiState(recentOrders = listOf(sampleOrderItem)),
                onAction = { actionReceived = it }
            )
        }

        composeTestRule.onNodeWithText("See all").performClick()
        assertEquals(HomeAction.SeeAllOrders, actionReceived)
    }

    @Test
    fun testViewRehabHistoryClick() {
        var actionReceived: HomeAction? = null
        composeTestRule.setContent {
            HomeContent(
                state    = HomeUiState(),
                onAction = { actionReceived = it }
            )
        }

        composeTestRule.onNodeWithText("View History").performClick()
        assertEquals(HomeAction.OpenRehabHistory, actionReceived)
    }
}