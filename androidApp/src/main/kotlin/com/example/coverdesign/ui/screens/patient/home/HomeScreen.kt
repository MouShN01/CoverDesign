package com.example.coverdesign.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.BluetoothDisabled
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FaceRetouchingNatural
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ShoppingBag
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.R
import com.example.coverdesign.domain.model.BodyPosition
import com.example.coverdesign.domain.model.CopPoint
import com.example.coverdesign.domain.model.GaitSession
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.OrderListItem
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.theme.ChartEndRed
import com.example.coverdesign.ui.theme.ChartStartGreen
import com.example.coverdesign.ui.theme.NeutralMid
import com.example.coverdesign.ui.theme.StatusBlue
import com.example.coverdesign.ui.theme.StatusBlueBg
import com.example.coverdesign.ui.theme.StatusGreen
import com.example.coverdesign.ui.theme.StatusGreenBg
import com.example.coverdesign.ui.theme.StatusPurple
import com.example.coverdesign.ui.theme.StatusPurpleBg
import com.example.coverdesign.ui.theme.StatusViolet
import com.example.coverdesign.ui.components.OrderStatusChip
import com.example.coverdesign.ui.viewmodel.HomeAction
import com.example.coverdesign.ui.viewmodel.HomeUiEvent
import com.example.coverdesign.ui.viewmodel.HomeUiState
import com.example.coverdesign.ui.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onNewOrder: () -> Unit,
    on3DVisualize: () -> Unit,
    onVirtualTryOn: () -> Unit,
    onOrderClick: (String) -> Unit,
    onSeeAllOrders: () -> Unit,
    onOpenRehabHistory: () -> Unit,
    onOrderInsoles: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    EventHandler(viewModel.events) { event ->
        when (event) {
            is HomeUiEvent.NavigateToNewOrder    -> onNewOrder()
            is HomeUiEvent.NavigateTo3D          -> on3DVisualize()
            is HomeUiEvent.NavigateToTryOn       -> onVirtualTryOn()
            is HomeUiEvent.NavigateToAllOrders   -> onSeeAllOrders()
            is HomeUiEvent.NavigateToOrder       -> onOrderClick(event.orderId)
            is HomeUiEvent.NavigateToRehabHistory -> onOpenRehabHistory()
            is HomeUiEvent.NavigateToInsoleOrder -> onOrderInsoles()
        }
    }

    HomeContent(
        state = state, onAction = viewModel::onAction
    )
}

@Composable
internal fun HomeContent(
    state: HomeUiState, onAction: (HomeAction) -> Unit
) {
    Scaffold(
        topBar = {
            HomeTopBar(onInfoClick = { /* show app info */ })
        }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            // ── New order CTA ─────────────────────────────────────────────
            NewOrderBanner(
                onClick = { onAction(HomeAction.NewOrder) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // ── Tools section ─────────────────────────────────────────────
            HomeSectionHeader(title = "Tools")
            ToolCard(
                icon = Icons.Outlined.FaceRetouchingNatural,
                label = "Virtual Try-On",
                tint = StatusViolet,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                onClick = { onAction(HomeAction.OpenTryOn) })

            Spacer(Modifier.height(24.dp))

            // ── My Rehabilitation ─────────────────────────────────────────
            HomeSectionHeader(
                title = "My Rehabilitation",
                action = "View History",
                onAction = { onAction(HomeAction.OpenRehabHistory) })
            RehabSummaryCard(
                latestSession = state.rehabSessions.firstOrNull(),
                hasInsoles = state.hasInsoles,
                onOrderInsoles = { onAction(HomeAction.OrderInsoles) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(24.dp))

            // ── Recent orders ─────────────────────────────────────────────
            HomeSectionHeader(
                title = "Recent Orders",
                action = "See all",
                onAction = { onAction(HomeAction.SeeAllOrders) })

            if (state.recentOrders.isEmpty()) {
                EmptyOrdersCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                state.recentOrders.forEach { item ->
                    RecentOrderCard(
                        item    = item,
                        onClick = { onAction(HomeAction.OpenOrder(item.orderId)) },
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

// ── Rehabilitation summary card ───────────────────────────────────────────────
@Composable
private fun RehabSummaryCard(
    latestSession: GaitSession?,
    hasInsoles: Boolean,
    onOrderInsoles: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (latestSession != null) {
                // ── Score chips ───────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScoreChip(
                        "Posture", latestSession.gaitAnalysis.postureScore, Modifier.weight(1f)
                    )
                    ScoreChip(
                        "Balance", latestSession.gaitAnalysis.balanceScore, Modifier.weight(1f)
                    )
                    ScoreChip(
                        "Symmetry", latestSession.gaitAnalysis.symmetryScore, Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ── Mini charts ───────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left: stabilogram (COP trajectory) — only if insoles connected
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "COP Trajectory",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        if (hasInsoles && latestSession.pressureData != null) {
                            StabilogramMiniChart(
                                copPoints = latestSession.pressureData!!.copTrajectory,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                        } else {
                            NoInsolesMiniCard(
                                onOrderInsoles = onOrderInsoles,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                        }
                    }

                    // Right: body deviation chart
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Body Deviation",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        DeviationMiniChart(
                            series = latestSession.gaitAnalysis.positionSeries,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    }
                }
            } else {
                // No sessions yet
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "No sessions recorded yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Complete a rehabilitation session to see your progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreChip(label: String, score: Float, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                "${score.toInt()}%",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// COP trajectory on a 2-D plane — lissajous ellipse in primary color
@Composable
private fun StabilogramMiniChart(copPoints: List<CopPoint>, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val axisColor = NeutralMid.copy(alpha = 0.35f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        drawRect(color = surfaceVariant, size = size)

        // axis lines
        val axisStroke = 1.dp.toPx()
        drawLine(axisColor, Offset(0f, cy), Offset(w, cy), strokeWidth = axisStroke)
        drawLine(axisColor, Offset(cx, 0f), Offset(cx, h), strokeWidth = axisStroke)

        if (copPoints.size < 2) return@Canvas

        // scale: ±35mm → width, ±25mm → height
        val scaleX = w / 70f
        val scaleY = h / 50f

        val path = Path()
        copPoints.forEachIndexed { index, pt ->
            val px = cx + pt.x * scaleX
            val py = cy - pt.y * scaleY   // flip Y so +y = up
            if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        drawPath(
            path = path,
            color = primaryColor.copy(alpha = 0.8f),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // start dot (green) and end dot (red)
        val first = copPoints.first()
        val last = copPoints.last()
        drawCircle(
            ChartStartGreen,
            radius = 3.dp.toPx(),
            center = Offset(cx + first.x * scaleX, cy - first.y * scaleY)
        )
        drawCircle(
            ChartEndRed,
            radius = 3.dp.toPx(),
            center = Offset(cx + last.x * scaleX, cy - last.y * scaleY)
        )
    }
}

// Lateral (red) and AP (blue) body deviation over time
@Composable
private fun DeviationMiniChart(series: List<BodyPosition>, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2f

        drawRect(color = surfaceColor, size = size)

        // zero line
        drawLine(
            color = ChartStartGreen,
            start = Offset(0f, midY),
            end = Offset(w, midY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
        )

        if (series.size < 2) return@Canvas

        val maxDev = 12f  // cm range
        val scaleY = (h / 2f) / maxDev
        val scaleX = w / (series.size - 1).toFloat().coerceAtLeast(1f)

        // lateral deviation (X, red)
        val pathX = Path()
        series.forEachIndexed { i, pt ->
            val px = i * scaleX
            val py = midY - pt.deviationX * scaleY
            if (i == 0) pathX.moveTo(px, py) else pathX.lineTo(px, py)
        }
        drawPath(
            pathX,
            errorColor.copy(alpha = 0.85f),
            style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // AP deviation (Y, blue/primary)
        val pathY = Path()
        series.forEachIndexed { i, pt ->
            val px = i * scaleX
            val py = midY - pt.deviationY * scaleY
            if (i == 0) pathY.moveTo(px, py) else pathY.lineTo(px, py)
        }
        drawPath(
            pathY,
            primary.copy(alpha = 0.85f),
            style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun NoInsolesMiniCard(onOrderInsoles: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.BluetoothDisabled,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "No insoles connected",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            TextButton(
                onClick = onOrderInsoles,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Order Insoles", style = MaterialTheme.typography.labelSmall)
            }
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
                text = stringResource(R.string.title_custom),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.slogan),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }, actions = {
        IconButton(onClick = onInfoClick) {
            Icon(Icons.Outlined.Info, contentDescription = "Info")
        }
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background
    )
    )
}

// ── New order banner ──────────────────────────────────────────────────────────
@Composable
private fun NewOrderBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Create new order",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Start designing your cover",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────
@Composable
private fun HomeSectionHeader(
    title: String, action: String? = null, onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (action != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(0.dp)) {
                Text(
                    text = action,
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
    icon: ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp), color = tint.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(28.dp)
                )
            }
            Text(
                text = label, style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// ── Recent order card ─────────────────────────────────────────────────────────
@Composable
private fun RecentOrderCard(
    item: OrderListItem, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.category.label, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    formatDate(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OrderStatusChip(status = item.status)
        }
    }
}

private val OrderCategory.label: String get() = when (this) {
    OrderCategory.PROSTHETIC_COVER -> "Prosthetic Cover"
    OrderCategory.INSOLE         -> "Insole"
    else                         -> name
}

// ── Empty orders ──────────────────────────────────────────────────────────────
@Composable
private fun EmptyOrdersCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Outlined.ShoppingBag,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No orders yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Create your first custom prosthetic cover",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.consultation_question) + stringResource(R.string.our_team_is_always_ready_to_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            ContactRow(
                icon = Icons.Outlined.Phone,
                tint = StatusGreen,
                label = stringResource(R.string.phone),
                value = stringResource(R.string.contact_number),
                bgColor = StatusGreenBg
            )
            ContactRow(
                icon = Icons.Outlined.Email,
                tint = StatusBlue,
                label = "Email",
                value = stringResource(R.string.info_email),
                bgColor = StatusBlueBg
            )
            ContactRow(
                icon = Icons.Outlined.Chat,
                tint = StatusPurple,
                label = stringResource(R.string.online_chat),
                value = stringResource(R.string.available_24_7),
                bgColor = StatusPurpleBg
            )
        }
    }
}

@Composable
private fun ContactRow(
    icon: ImageVector, tint: Color, label: String, value: String, bgColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp), color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium)
                Text(
                    value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Date formatter ────────────────────────────────────────────────────────────
fun formatDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
//
//@Preview(showBackground = true)
//@Composable
//private fun HomeScreenPreview() {
//    AppTheme {
//        HomeContent(
//            state = HomeUiState(
//                rehabSessions = RehabMockData.generateSessions(),
//                hasInsoles = true,
//                recentOrders = listOf(
//                    Order(
//                        id = "1",
//                        adaptedModelId = null,
//                        config = ProductConfig(
//                            id = "c1",
//                            productId = "p1",
//                            productCategory = ProductCategory.THREE_D_PRINT,
//                            measurement = Measurement(100.0, 50.0, 30.0),
//                            sizeLabel = SizeLabel.M,
//                            color = ProductColor("Cyan", "#00FFFF"),
//                            material = Material.PLA,
//                            texture = Texture("t1", "Smooth", null, null),
//                            inscription = "Sample",
//                            userPhotoUri = null,
//                            additionalElements = emptyList(),
//                            quantity = 1,
//                            createdAt = System.currentTimeMillis()
//                        ),
//                        priceEstimate = PriceEstimate(
//                            configId = "c1",
//                            basePrice = 50.0,
//                            subtotal = 55.0,
//                            total = 55.0
//                        ),
//                        status = OrderStatus.SUBMITTED,
//                        createdAt = System.currentTimeMillis()
//                    )
//                )
//            ),
//            onAction = {}
//        )
//    }
//}
