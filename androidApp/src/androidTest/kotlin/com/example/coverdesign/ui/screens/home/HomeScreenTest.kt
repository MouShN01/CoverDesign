package com.example.coverdesign.ui.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.OrderListItem
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.ui.screens.patient.home.HomeContent
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
                state = HomeUiState(),
                onAction = { actionReceived = it }
            )
        }

        composeTestRule.onNodeWithText("Create new order").performClick()
        assertEquals(HomeAction.NewOrder, actionReceived)
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
    fun testEmptyRehabState() {
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(rehabSessions = emptyList()),
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
                state = HomeUiState(recentOrders = listOf(sampleOrderItem)),
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
                state = HomeUiState(recentOrders = listOf(sampleOrderItem)),
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
                state = HomeUiState(),
                onAction = { actionReceived = it }
            )
        }

        composeTestRule.onNodeWithText("View History").performClick()
        assertEquals(HomeAction.OpenRehabHistory, actionReceived)
    }

    @Test
    fun testWelcomeCoachMarkShownForNewUser() {
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(isNewUser = true),
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("Welcome aboard!").assertIsDisplayed()
    }

    @Test
    fun testWelcomeCoachMarkHiddenForExistingUser() {
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(isNewUser = false),
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("Welcome aboard!").assertDoesNotExist()
    }

    @Test
    fun testSkipDismissesWelcome() {
        var actionReceived: HomeAction? = null
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(isNewUser = true),
                onAction = { actionReceived = it }
            )
        }

        composeTestRule.onNodeWithText("Skip").performClick()
        assertEquals(HomeAction.DismissWelcome, actionReceived)
    }

    @Test
    fun testStartTourOpensQuickTour() {
        var actionReceived: HomeAction? = null
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(isNewUser = true),
                onAction = { actionReceived = it }
            )
        }

        composeTestRule.onNodeWithText("Start tour").performClick()

        // Starting the tour dismisses the coach-mark and shows the first tour step.
        assertEquals(HomeAction.DismissWelcome, actionReceived)
        composeTestRule.onNodeWithText("Create an order").assertIsDisplayed()
    }
}