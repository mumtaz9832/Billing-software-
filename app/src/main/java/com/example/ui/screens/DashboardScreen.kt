package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.MetricStatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatDate
import com.example.ui.components.formatRupee
import com.example.ui.components.formatRupeeWhole
import com.example.ui.components.shareText
import com.example.ui.dialogs.buildWhatsAppInvoiceText
import com.example.ui.theme.JamaGreen
import com.example.ui.theme.PendingOrange
import com.example.ui.theme.UdhaarRed
import com.example.ui.theme.VyaparTealPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.VyaparViewModel

@Composable
fun DashboardScreen(
    viewModel: VyaparViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val products by viewModel.products.collectAsState()

    val recentInvoices = invoices.take(5)
    val topProducts = products.sortedByDescending { it.sellingPrice * it.currentStock }.take(4)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Business Header
        item {
            HeaderCard(
                businessName = profile?.businessName ?: "Bharat Traders",
                gstin = profile?.gstin ?: "",
                role = profile?.currentRole ?: "Owner",
                onAiClick = { viewModel.navigateTo(AppScreen.AI_ASSISTANT) },
                onSettingsClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
            )
        }

        // Today's Highlights Matrix
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Today's Business Snapshot",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Today Sales",
                        value = formatRupeeWhole(metrics.todaySales),
                        subtitle = "Gross billings",
                        icon = Icons.Default.TrendingUp,
                        containerColor = Color(0xFFE6F4EA),
                        contentColor = Color(0xFF137333),
                        modifier = Modifier.weight(1f)
                    )

                    MetricStatCard(
                        title = "Est. Profit",
                        value = formatRupeeWhole(metrics.todayProfit),
                        subtitle = "Margin - Expenses",
                        icon = Icons.Default.AccountBalanceWallet,
                        containerColor = Color(0xFFE8F0FE),
                        contentColor = Color(0xFF1A73E8),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Purchases",
                        value = formatRupeeWhole(metrics.todayPurchases),
                        subtitle = "Inward Stock",
                        icon = Icons.Default.ArrowDownward,
                        containerColor = Color(0xFFF3E8FD),
                        contentColor = Color(0xFF8430CE),
                        modifier = Modifier.weight(1f)
                    )

                    MetricStatCard(
                        title = "Expenses",
                        value = formatRupeeWhole(metrics.todayExpenses),
                        subtitle = "Daily shop kharch",
                        icon = Icons.Default.MoneyOff,
                        containerColor = Color(0xFFFCE8E6),
                        contentColor = Color(0xFFC5221F),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.REPORTS) }
                    )
                }
            }
        }

        // Balance & Khata Overview (Market Udhaar vs Supplier Payable)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Accounts & Khata Ledger",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.KHATA) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "View Khata",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Total Receivable", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = formatRupeeWhole(metrics.totalReceivable),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UdhaarRed
                                )
                                Text(text = "Market Udhaar (Lena hai)", fontSize = 10.sp, color = UdhaarRed)
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(44.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )

                            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(text = "Total Payable", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = formatRupeeWhole(metrics.totalPayable),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PendingOrange
                                )
                                Text(text = "Supplier Dues (Dena hai)", fontSize = 10.sp, color = PendingOrange)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(text = "Cash in Counter", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = formatRupeeWhole(metrics.cashBalance), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(text = "Bank / UPI Balance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = formatRupeeWhole(metrics.bankUpiBalance), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Smart Alert Badges
        if (metrics.lowStockCount > 0 || metrics.overdueDebtorsCount > 0) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (metrics.lowStockCount > 0) {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.navigateTo(AppScreen.INVENTORY) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "${metrics.lowStockCount} Items Low Stock", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                                    Text(text = "Reorder from suppliers", fontSize = 10.sp, color = Color(0xFFB91C1C))
                                }
                            }
                        }
                    }

                    if (metrics.overdueDebtorsCount > 0) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.navigateTo(AppScreen.KHATA) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "${metrics.overdueDebtorsCount} Overdue Udhaar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                    Text(text = "Send WhatsApp reminder", fontSize = 10.sp, color = Color(0xFF92400E))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        title = "+ Sale Bill",
                        icon = Icons.Default.Receipt,
                        color = Color(0xFF0F766E),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.BILLING) }
                    )
                    QuickActionButton(
                        title = "🛒 Quick POS",
                        icon = Icons.Default.PointOfSale,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.POS) }
                    )
                    QuickActionButton(
                        title = "+ Customer",
                        icon = Icons.Default.PersonAdd,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.KHATA) }
                    )
                    QuickActionButton(
                        title = "+ Product",
                        icon = Icons.Default.Inventory2,
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.INVENTORY) }
                    )
                }
            }
        }

        // Recent Invoices
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Invoices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "See All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.BILLING) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (recentInvoices.isEmpty()) {
                    Text(text = "No invoices yet. Create your first bill above!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    recentInvoices.forEach { invoice ->
                        InvoiceItemCard(
                            invoice = invoice,
                            onClick = {
                                viewModel.selectedInvoice.value = invoice
                            },
                            onWhatsAppClick = {
                                profile?.let {
                                    val text = buildWhatsAppInvoiceText(invoice, it)
                                    shareText(context, text, "Send Invoice via WhatsApp")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Top Selling Products Banner
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Inventory Highlights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage Stock",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.INVENTORY) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(topProducts) { prod ->
                        TopProductCard(product = prod)
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderCard(
    businessName: String,
    gstin: String,
    role: String,
    onAiClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = businessName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = role,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (gstin.isNotBlank()) {
                        Text(
                            text = "GSTIN: $gstin",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Row {
                    // AI Assistant Trigger
                    IconButton(
                        onClick = onAiClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .testTag("ai_assistant_header_button")
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Vyapar AI",
                            tint = Color(0xFFFDE047)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun InvoiceItemCard(
    invoice: InvoiceEntity,
    onClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = invoice.partyName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = invoice.paymentStatus)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${invoice.invoiceNumber} • ${formatDate(invoice.date)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatRupee(invoice.grandTotal),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (invoice.dueAmount > 0) {
                    Text(
                        text = "Due: ${formatRupee(invoice.dueAmount)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UdhaarRed
                    )
                }
            }
        }
    }
}

@Composable
fun TopProductCard(product: ProductEntity) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatRupee(product.sellingPrice),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = VyaparTealPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Stock: ${product.currentStock.toInt()} ${product.unit}",
                fontSize = 11.sp,
                color = if (product.currentStock <= product.minStock) UdhaarRed else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
