package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ExpenseEntity
import com.example.ui.components.MetricStatCard
import com.example.ui.components.formatDate
import com.example.ui.components.formatRupee
import com.example.ui.components.formatRupeeWhole
import com.example.ui.components.shareText
import com.example.ui.theme.JamaGreen
import com.example.ui.theme.PendingOrange
import com.example.ui.theme.UdhaarRed
import com.example.ui.theme.VyaparTealPrimary
import com.example.ui.viewmodel.VyaparViewModel

@Composable
fun ReportsScreen(
    viewModel: VyaparViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: P&L, 1: GST, 2: Expenses, 3: Sales/Purchases
    val invoices by viewModel.invoices.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val products by viewModel.products.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var showAddExpenseDialog by remember { mutableStateOf(false) }

    // Aggregate Analytics
    val totalSales = invoices.filter { it.type == "SALE" }.sumOf { it.grandTotal }
    val totalPurchases = invoices.filter { it.type == "PURCHASE" }.sumOf { it.grandTotal }
    val totalExpenses = expenses.sumOf { it.amount }

    // COGS estimate ~ 75% of sales
    val estimatedCogs = totalPurchases.coerceAtLeast(totalSales * 0.70)
    val grossProfit = (totalSales - estimatedCogs).coerceAtLeast(0.0)
    val netProfit = (grossProfit - totalExpenses).coerceAtLeast(0.0)

    // GST Aggregates
    val saleInvoices = invoices.filter { it.type == "SALE" && it.isGst }
    val purchaseInvoices = invoices.filter { it.type == "PURCHASE" && it.isGst }

    val outwardTaxable = saleInvoices.sumOf { it.subTotal - it.discountAmount }
    val outputCgst = saleInvoices.sumOf { it.cgstAmount }
    val outputSgst = saleInvoices.sumOf { it.sgstAmount }
    val outputIgst = saleInvoices.sumOf { it.igstAmount }
    val totalOutputTax = outputCgst + outputSgst + outputIgst

    val inputTaxCredit = purchaseInvoices.sumOf { it.taxAmount }
    val netGstPayable = (totalOutputTax - inputTaxCredit).coerceAtLeast(0.0)

    Column(modifier = modifier.fillMaxSize().testTag("reports_screen")) {
        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("P&L Statement", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("GST Returns", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Expenses", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Overview", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedTab == 0) {
                // Profit & Loss Tab
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Estimated Net Profit", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Healthy Margin",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JamaGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = formatRupeeWhole(netProfit),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = JamaGreen
                            )

                            Text(text = "Sales – Cost of Goods – Shop Expenses", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            PnLRow("Gross Revenue (Sales)", formatRupee(totalSales), Color(0xFF0F766E))
                            PnLRow("Cost of Stock Inward (COGS)", "- ${formatRupee(estimatedCogs)}", Color(0xFF64748B))
                            PnLRow("Operating Expenses (Shop Kharch)", "- ${formatRupee(totalExpenses)}", UdhaarRed)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            PnLRow("Estimated Net Profit", formatRupee(netProfit), JamaGreen, isBold = true)
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            val text = """
📊 *PROFIT & LOSS REPORT*
Shop: *${profile?.businessName}*
Date: ${formatDate(System.currentTimeMillis())}

• Total Sales: ${formatRupee(totalSales)}
• Stock Inward / COGS: ${formatRupee(estimatedCogs)}
• Shop Expenses: ${formatRupee(totalExpenses)}
--------------------------
• *Estimated Net Profit:* ${formatRupee(netProfit)}
                            """.trimIndent()
                            shareText(context, text, "Share Profit & Loss Report")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VyaparTealPrimary),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share P&L Summary")
                    }
                }
            } else if (selectedTab == 1) {
                // GST Summary Tab
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "GSTR-3B / Tax Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = "GSTIN: ${profile?.gstin}", fontSize = 12.sp, color = VyaparTealPrimary, fontWeight = FontWeight.SemiBold)

                            Spacer(modifier = Modifier.height(12.dp))

                            PnLRow("Total Outward Taxable Value", formatRupee(outwardTaxable))
                            PnLRow("CGST (Central Tax)", formatRupee(outputCgst))
                            PnLRow("SGST (State Tax)", formatRupee(outputSgst))
                            PnLRow("IGST (Integrated Tax)", formatRupee(outputIgst))

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            PnLRow("Total Output GST Liability", formatRupee(totalOutputTax), isBold = true)
                            PnLRow("Input Tax Credit (ITC on Purchases)", "- ${formatRupee(inputTaxCredit)}", Color(0xFF16A34A))

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            PnLRow("Net Tax Payable in Cash", formatRupee(netGstPayable), UdhaarRed, isBold = true)
                        }
                    }
                }
            } else if (selectedTab == 2) {
                // Expenses Tracker Tab
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Total Expenses", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = formatRupeeWhole(totalExpenses), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = UdhaarRed)
                        }

                        Button(
                            onClick = { showAddExpenseDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = VyaparTealPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Expense")
                        }
                    }
                }

                item {
                    Text(text = "Recent Shop Expenses (${expenses.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                items(expenses) { exp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(text = exp.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "${exp.category} • ${exp.paymentMode} • ${formatDate(exp.date)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(text = formatRupee(exp.amount), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = UdhaarRed)
                        }
                    }
                }
            } else {
                // Sales & Purchases Overview
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            title = "Total Sales",
                            value = formatRupeeWhole(totalSales),
                            icon = Icons.Default.TrendingUp,
                            containerColor = Color(0xFFDCFCE7),
                            contentColor = Color(0xFF15803D),
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Total Purchases",
                            value = formatRupeeWhole(totalPurchases),
                            icon = Icons.Default.ArrowDownward,
                            containerColor = Color(0xFFEDE9FE),
                            contentColor = Color(0xFF6D28D9),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Sales vs Purchase Ratio", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            val progress = if (totalSales + totalPurchases > 0) {
                                (totalSales / (totalSales + totalPurchases)).toFloat()
                            } else 0.5f

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                color = VyaparTealPrimary,
                                trackColor = Color(0xFF6D28D9)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Sales: ${(progress * 100).toInt()}%", fontSize = 11.sp, color = VyaparTealPrimary, fontWeight = FontWeight.Bold)
                                Text(text = "Purchases: ${((1 - progress) * 100).toInt()}%", fontSize = 11.sp, color = Color(0xFF6D28D9), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onAdd = { title, category, amount, mode, notes ->
                viewModel.addExpense(title, category, amount, mode, notes)
                showAddExpenseDialog = false
            }
        )
    }
}

@Composable
fun PnLRow(label: String, value: String, color: Color = Color.Unspecified, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, fontSize = 13.sp, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, category: String, amount: Double, mode: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Rent") }
    var amountText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("CASH") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("Rent", "Electricity", "Salary", "Transport", "Internet", "Tea & Snacks", "Maintenance", "Marketing", "Miscellaneous")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Record Shop Expense", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g. Shop Rent) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && amt > 0) {
                                onAdd(title, category, amt, mode, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VyaparTealPrimary)
                    ) {
                        Text("Save Expense")
                    }
                }
            }
        }
    }
}
