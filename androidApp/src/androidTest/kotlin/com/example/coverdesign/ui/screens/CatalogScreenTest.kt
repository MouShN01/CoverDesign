package com.example.coverdesign.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.ui.viewmodel.CatalogAction
import com.example.coverdesign.ui.viewmodel.CatalogUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun makeProduct(
        id: String = "p1",
        name: String = "Prosthetic Cover A",
        description: String = "A high-quality cover",
        basePrice: Double = 150.0
    ) = Product.Generic(
        id = id,
        name = name,
        description = description,
        basePrice = basePrice,
        category = OrderCategory.THREE_D_PRINT,
        availableMaterials = emptyList(),
        availableTextures = emptyList(),
        imageUrl = ""
    )

    @Test
    fun productCard_displaysNameAndFormattedPrice() {
        val product = makeProduct()
        composeTestRule.setContent {
            CatalogContent(
                state = CatalogUiState(
                    products = listOf(product),
                    filtered = listOf(product)
                ),
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("Prosthetic Cover A").assertIsDisplayed()
        composeTestRule.onNodeWithText("From $150.0").assertIsDisplayed()
    }

    @Test
    fun multipleProducts_allCardsVisible() {
        val products = listOf(
            makeProduct(id = "p1", name = "Cover Alpha"),
            makeProduct(id = "p2", name = "Cover Beta"),
            makeProduct(id = "p3", name = "Cover Gamma")
        )
        composeTestRule.setContent {
            CatalogContent(
                state = CatalogUiState(products = products, filtered = products),
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("Cover Alpha").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cover Beta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cover Gamma").assertIsDisplayed()
    }

    @Test
    fun searchField_textInput_dispatchesSearchAction() {
        var lastAction: CatalogAction? = null
        composeTestRule.setContent {
            CatalogContent(
                state = CatalogUiState(),
                onAction = { lastAction = it }
            )
        }

        composeTestRule.onNodeWithText("Search products…").performTextInput("Cover")
        composeTestRule.waitForIdle()

        assertTrue(lastAction is CatalogAction.Search)
        assertEquals("Cover", (lastAction as CatalogAction.Search).query)
    }

    @Test
    fun searchField_existingQuery_displayedInField() {
        composeTestRule.setContent {
            CatalogContent(
                state = CatalogUiState(searchQuery = "lens"),
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("lens").assertIsDisplayed()
    }

    @Test
    fun productCard_click_dispatchesSelectProductAction() {
        var lastAction: CatalogAction? = null
        val product = makeProduct()
        composeTestRule.setContent {
            CatalogContent(
                state = CatalogUiState(
                    products = listOf(product),
                    filtered = listOf(product)
                ),
                onAction = { lastAction = it }
            )
        }

        composeTestRule.onNodeWithText("Prosthetic Cover A").performClick()
        composeTestRule.waitForIdle()

        assertEquals(CatalogAction.SelectProduct(product), lastAction)
    }

    @Test
    fun filteredList_empty_noProductCardsRendered() {
        val product = makeProduct()
        composeTestRule.setContent {
            CatalogContent(
                state = CatalogUiState(
                    products = listOf(product),
                    filtered = emptyList()
                ),
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("Prosthetic Cover A").assertDoesNotExist()
    }
}