package com.example.coverdesign.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FaceRetouchingNatural
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.R
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.components.OrderStatusChip
import com.example.coverdesign.ui.viewmodel.HomeAction
import com.example.coverdesign.ui.viewmodel.HomeUiEvent
import com.example.coverdesign.ui.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel


// androidApp/ui/screens/home/HomeScreen.kt
@Composable
fun HomeScreen(
    viewModel      : HomeViewModel = koinViewModel(),
    onNewOrder     : () -> Unit,
    on3DVisualize  : () -> Unit,
    onVirtualTryOn : () -> Unit,
    onOrderClick   : (String) -> Unit,
    onSeeAllOrders : () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    EventHandler(viewModel.events) { event ->
        when (event) {
            is HomeUiEvent.NavigateToNewOrder  -> onNewOrder()
            is HomeUiEvent.NavigateTo3D        -> on3DVisualize()
            is HomeUiEvent.NavigateToTryOn     -> onVirtualTryOn()
            is HomeUiEvent.NavigateToAllOrders -> onSeeAllOrders()
            is HomeUiEvent.NavigateToOrder     -> onOrderClick(event.orderId)
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(onInfoClick = { /* show app info */ })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── New order CTA ─────────────────────────────────────────────
            NewOrderBanner(
                onClick  = { viewModel.onAction(HomeAction.NewOrder) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // ── Tools section ─────────────────────────────────────────────
            HomeSectionHeader(title = "Tools")
            Row(
                modifier              = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ToolCard(
                    icon     = Icons.Outlined.ViewInAr,
                    label    = "3D Visualisation",
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick  = { viewModel.onAction(HomeAction.Open3D) }
                )
                ToolCard(
                    icon     = Icons.Outlined.FaceRetouchingNatural,
                    label    = "Virtual Try-On",
                    tint     = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f),
                    onClick  = { viewModel.onAction(HomeAction.OpenTryOn) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Recent orders ─────────────────────────────────────────────
            HomeSectionHeader(
                title    = "Recent Orders",
                action   = "See all",
                onAction = { viewModel.onAction(HomeAction.SeeAllOrders) }
            )

            if (state.recentOrders.isEmpty()) {
                EmptyOrdersCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                state.recentOrders.forEach { order ->
                    RecentOrderCard(
                        order    = order,
                        onClick  = { viewModel.onAction(HomeAction.OpenOrder(order.id)) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Contact section ───────────────────────────────────────────
            HomeSectionHeader(title = "Contact Us")
            ContactCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(onInfoClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text  = "ProsCover",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text  = "Cosmetic prosthetic covers",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            IconButton(onClick = onInfoClick) {
                Icon(Icons.Outlined.Info, contentDescription = "Info")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ── New order banner ──────────────────────────────────────────────────────────
@Composable
private fun NewOrderBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier          = Modifier
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // plus icon in rounded square
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector        = Icons.Default.Add,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimary,
                    modifier           = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "Create new order",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text  = "Start designing your cover",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector        = Icons.Default.ArrowForward,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────
@Composable
private fun HomeSectionHeader(
    title    : String,
    action   : String? = null,
    onAction : (() -> Unit)? = null
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (action != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(0.dp)) {
                Text(
                    text  = action,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ── Tool card ─────────────────────────────────────────────────────────────────
@Composable
private fun ToolCard(
    icon     : ImageVector,
    label    : String,
    tint     : Color,
    modifier : Modifier = Modifier,
    onClick  : () -> Unit
) {
    Card(
        onClick   = onClick,
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border    = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tint.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = tint,
                    modifier           = Modifier
                        .padding(12.dp)
                        .size(28.dp)
                )
            }
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Recent order card ─────────────────────────────────────────────────────────
@Composable
private fun RecentOrderCard(
    order    : Order,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier
) {
    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border    = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier          = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = order.config.productType.displayName,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = formatDate(order.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                // 3D badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier          = Modifier.padding(
                            horizontal = 6.dp, vertical = 2.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.ViewInAr,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint     = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "3D",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                OrderStatusChip(status = order.status)
            }
        }
    }
}


// ── Empty orders ──────────────────────────────────────────────────────────────
@Composable
private fun EmptyOrdersCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Outlined.ShoppingBag,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint     = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = "No orders yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text      = "Create your first custom prosthetic cover",
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Contact card ──────────────────────────────────────────────────────────────
@Composable
private fun ContactCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text  = stringResource(R.string.consultation_question) +
                        stringResource(R.string.our_team_is_always_ready_to_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            ContactRow(
                icon    = Icons.Outlined.Phone,
                tint    = Color(0xFF2E7D32),
                label   = stringResource(R.string.phone),
                value   = stringResource(R.string.contact_number),
                bgColor = Color(0xFFE8F5E9)
            )
            ContactRow(
                icon    = Icons.Outlined.Email,
                tint    = Color(0xFF1565C0),
                label   = "Email",
                value   = stringResource(R.string.info_email),
                bgColor = Color(0xFFE3F2FD)
            )
            ContactRow(
                icon    = Icons.Outlined.Chat,
                tint    = Color(0xFF6A1B9A),
                label   = stringResource(R.string.online_chat),
                value   = stringResource(R.string.available_24_7),
                bgColor = Color(0xFFF3E5F5)
            )
        }
    }
}

@Composable
private fun ContactRow(
    icon    : ImageVector,
    tint    : Color,
    label   : String,
    value   : String,
    bgColor : Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = tint,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium)
                Text(value, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Date formatter ────────────────────────────────────────────────────────────
fun formatDate(epochMillis: Long): String {
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(epochMillis))
}