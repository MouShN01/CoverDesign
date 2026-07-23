package com.example.coverdesign.ui.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.coverdesign.R
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.components.ErrorScreen
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.components.LoadingScreen
import com.example.coverdesign.ui.viewmodel.CatalogAction
import com.example.coverdesign.ui.viewmodel.CatalogUiEvent
import com.example.coverdesign.ui.viewmodel.CatalogUiState
import com.example.coverdesign.ui.viewmodel.CatalogViewModel
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun CatalogScreen(
    viewModel         : CatalogViewModel = koinViewModel(),
    onProductSelected : (Product) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // handle one-time navigation events
    EventHandler(viewModel.events) { event ->
        when (event) {
            is CatalogUiEvent.NavigateToConfig -> onProductSelected(event.product)
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Catalog") }
    ) { padding ->
        when {
            state.isLoading -> LoadingScreen()
            state.error != null -> ErrorScreen(
                message = state.error!!,
                onRetry = { viewModel.onAction(CatalogAction.Refresh) }
            )
            else -> CatalogContent(
                state     = state,
                onAction  = viewModel::onAction,
                modifier  = Modifier.padding(padding)
            )
        }
    }
}

@Composable
internal fun CatalogContent(
    state    : CatalogUiState,
    onAction : (CatalogAction) -> Unit,
    modifier : Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // search bar
        OutlinedTextField(
            value         = state.searchQuery,
            onValueChange = { onAction(CatalogAction.Search(it)) },
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder   = { Text("Search products…") },
            leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine    = true,
            shape         = RoundedCornerShape(12.dp)
        )

        // product grid
        LazyVerticalGrid(
            columns          = GridCells.Fixed(2),
            modifier         = Modifier.fillMaxSize(),
            contentPadding   = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement   = Arrangement.spacedBy(12.dp)
        ) {
            items(state.filtered, key = { it.id }) { product ->
                ProductCard(
                    product  = product,
                    onClick  = { onAction(CatalogAction.SelectProduct(product)) }
                )
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(product.imageUrl)
                    .crossfade(true).build() ,
                contentDescription = product.name,
                modifier          = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale      = ContentScale.Crop,
                placeholder = painterResource( R.drawable.ic_launcher_foreground)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text  = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "From $${product.basePrice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}