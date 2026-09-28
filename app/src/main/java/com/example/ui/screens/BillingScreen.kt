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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItem
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.AppSearchBar
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatDate
import com.example.ui.components.formatRupee
import com.example.ui.components.shareText
import com.example.ui.dialogs.buildWhatsAppInvoiceText
import com.example.ui.theme.UdhaarRed
import com.example.ui.theme.VyaparTealPrimary
import com.example.ui.viewmodel.VyaparViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: VyaparViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: New Invoice, 1: History
    val parties by viewModel.parties.collectAsState()
    val products by viewModel.products.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val profile by viewModel.profile.collectAsState()

    // Form state
    var invoiceType by remember { mutableStateOf("SALE") } // SALE, PURCHASE, QUOTATION, DELIVERY_CHALLAN
    var isGst by remember { mutableStateOf(true) }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var partyName by remember { mutableStateOf("") }
    var partyMobile by remember { mutableStateOf("") }
    var partyGstin by remember { mutableStateOf("") }
    var partyState by remember { mutableStateOf("Delhi") }

    val billItems = remember { mutableStateListOf<InvoiceItem>() }
    var discountPercent by remember { mutableDoubleStateOf(0.0) }
    var shippingCharge by remember { mutableDoubleStateOf(0.0) }
    var paymentMode by remember { mutableStateOf("CASH") }
    var paidAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var historySearchQuery by remember { mutableStateOf("") }

    // Calculated amounts
    val subTotal = billItems.sumOf { it.unitPrice * it.quantity }
    val itemDiscountTotal = billItems.sumOf { (it.unitPrice * it.quantity) * (it.discountPercent / 100.0) }
    val billDiscountAmount = (subTotal - itemDiscountTotal) * (discountPercent / 100.0)
    val totalDiscount = itemDiscountTotal + billDiscountAmount

    val isInterState = partyState.isNotBlank() && !partyState.equals("Delhi", ignoreCase = true)
    var cgstTotal = 0.0
    var sgstTotal = 0.0
    var igstTotal = 0.0

    if (isGst) {
        for (item in billItems) {
            val netItemPrice = (item.unitPrice * item.quantity) * (1.0 - item.discountPercent / 100.0)
            val tax = netItemPrice * (item.gstRate / 100.0)
            if (isInterState) {
                igstTotal += tax
            } else {
                cgstTotal += tax / 2.0
                sgstTotal += tax / 2.0
            }
        }
    }

    val taxAmount = cgstTotal + sgstTotal + igstTotal
    val grandTotal = (subTotal - totalDiscount + taxAmount + shippingCharge).coerceAtLeast(0.0)
    val paidAmount = paidAmountText.toDoubleOrNull() ?: grandTotal
    val balanceDue = (grandTotal - paidAmount).coerceAtLeast(0.0)

    Column(modifier = modifier.fillMaxSize().testTag("billing_screen")) {
        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Create Bill / Invoice", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Invoice History (${invoices.size})", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            // New Bill Form
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
            ) {
                // Bill Type & GST Toggle
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    TypeChip(label = "Sale", selected = invoiceType == "SALE") { invoiceType = "SALE" }
                                    TypeChip(label = "Purchase", selected = invoiceType == "PURCHASE") { invoiceType = "PURCHASE" }
                                    TypeChip(label = "Quotation", selected = invoiceType == "QUOTATION") { invoiceType = "QUOTATION" }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "GST", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Switch(
                                        checked = isGst,
                                        onCheckedChange = { isGst = it },
                                        modifier = Modifier.testTag("gst_toggle_switch")
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(10.dp)) }

                // Party / Customer Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Customer / Party Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Party Picker
                            var expandedPartyDropdown by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expandedPartyDropdown,
                                onExpandedChange = { expandedPartyDropdown = !expandedPartyDropdown }
                            ) {
                                OutlinedTextField(
                                    value = partyName.ifBlank { "Select or Type Customer..." },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Select Existing Party") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPartyDropdown) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedPartyDropdown,
                                    onDismissRequest = { expandedPartyDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Cash / Counter Customer") },
                                        onClick = {
                                            selectedCustomer = null
                                            partyName = "Cash Customer"
                                            partyMobile = ""
                                            partyGstin = ""
                                            partyState = "Delhi"
                                            expandedPartyDropdown = false
                                        }
                                    )
                                    parties.forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text("${p.name} (${p.mobile})") },
                                            onClick = {
                                                selectedCustomer = p
                                                partyName = p.name
                                                partyMobile = p.mobile
                                                partyGstin = p.gstin
                                                partyState = p.state
                                                expandedPartyDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = partyName,
                                    onValueChange = { partyName = it },
                                    label = { Text("Party Name") },
                                    modifier = Modifier.weight(1.2f)
                                )
                                OutlinedTextField(
                                    value = partyMobile,
                                    onValueChange = { partyMobile = it },
                                    label = { Text("Mobile") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (isGst) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = partyGstin,
                                        onValueChange = { partyGstin = it },
                                        label = { Text("Party GSTIN") },
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    OutlinedTextField(
                                        value = partyState,
                                        onValueChange = { partyState = it },
                                        label = { Text("Place of Supply") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(10.dp)) }

                // Items Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Items in Bill (${billItems.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                Button(
                                    onClick = { showAddItemDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = VyaparTealPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("add_item_to_bill_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Item", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (billItems.isEmpty()) {
                                Text(
                                    text = "No items added yet. Click '+ Add Item' to select products from your inventory.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            } else {
                                billItems.forEachIndexed { index, item ->
                                    BillItemRow(
                                        item = item,
                                        onQuantityChange = { newQty ->
                                            if (newQty > 0) {
                                                val net = item.unitPrice * newQty * (1.0 - item.discountPercent / 100.0)
                                                val tax = net * (item.gstRate / 100.0)
                                                billItems[index] = item.copy(
                                                    quantity = newQty,
                                                    taxAmount = tax,
                                                    totalAmount = net + tax
                                                )
                                            }
                                        },
                                        onDelete = {
                                            billItems.removeAt(index)
                                        }
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(10.dp)) }

                // Calculations & Payment
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Bill Summary & Payment", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            Spacer(modifier = Modifier.height(8.dp))

                            SummaryRow(label = "Sub Total", value = formatRupee(subTotal))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Discount %", fontSize = 12.sp)
                                OutlinedTextField(
                                    value = if (discountPercent == 0.0) "" else discountPercent.toString(),
                                    onValueChange = { discountPercent = it.toDoubleOrNull() ?: 0.0 },
                                    placeholder = { Text("0") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(100.dp)
                                )
                            }

                            if (isGst) {
                                if (cgstTotal > 0) SummaryRow(label = "CGST (Central Tax)", value = formatRupee(cgstTotal))
                                if (sgstTotal > 0) SummaryRow(label = "SGST (State Tax)", value = formatRupee(sgstTotal))
                                if (igstTotal > 0) SummaryRow(label = "IGST (Interstate)", value = formatRupee(igstTotal))
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Grand Total", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = formatRupee(grandTotal), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = VyaparTealPrimary)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Payment mode
                            Text(text = "Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                PaymentChip("CASH", paymentMode == "CASH") { paymentMode = "CASH" }
                                PaymentChip("UPI", paymentMode == "UPI") { paymentMode = "UPI" }
                                PaymentChip("CREDIT", paymentMode == "CREDIT") {
                                    paymentMode = "CREDIT"
                                    paidAmountText = "0"
                                }
                                PaymentChip("BANK", paymentMode == "BANK") { paymentMode = "BANK" }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = paidAmountText,
                                    onValueChange = { paidAmountText = it },
                                    label = { Text("Amount Paid (₹)") },
                                    placeholder = { Text(grandTotal.toInt().toString()) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = formatRupee(balanceDue),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Balance Due (Udhaar)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    viewModel.createInvoice(
                                        type = invoiceType,
                                        party = selectedCustomer,
                                        partyName = partyName,
                                        partyMobile = partyMobile,
                                        partyGstin = partyGstin,
                                        partyState = partyState,
                                        items = billItems.toList(),
                                        discountPercent = discountPercent,
                                        discountAmount = totalDiscount,
                                        shippingCharge = shippingCharge,
                                        roundOff = 0.0,
                                        paidAmount = paidAmount,
                                        paymentMode = paymentMode,
                                        isGst = isGst,
                                        notes = notes,
                                        onSuccess = { created ->
                                            // reset form
                                            billItems.clear()
                                            partyName = ""
                                            partyMobile = ""
                                            paidAmountText = ""
                                        }
                                    )
                                },
                                enabled = billItems.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = VyaparTealPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("save_and_preview_invoice_button")
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save & Preview Invoice", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Invoice History List
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                AppSearchBar(
                    query = historySearchQuery,
                    onQueryChange = { historySearchQuery = it },
                    placeholder = "Search by invoice # or customer name..."
                )

                Spacer(modifier = Modifier.height(10.dp))

                val filtered = invoices.filter {
                    it.invoiceNumber.contains(historySearchQuery, ignoreCase = true) ||
                            it.partyName.contains(historySearchQuery, ignoreCase = true)
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No invoices matching search", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { inv ->
                            InvoiceHistoryCard(
                                invoice = inv,
                                onClick = {
                                    viewModel.selectedInvoice.value = inv
                                },
                                onWhatsApp = {
                                    profile?.let { p ->
                                        val text = buildWhatsAppInvoiceText(inv, p)
                                        shareText(context, text, "Share Invoice")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AddItemToBillDialog(
            products = products,
            onDismiss = { showAddItemDialog = false },
            onAdd = { item ->
                billItems.add(item)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun PaymentChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun BillItemRow(
    item: InvoiceItem,
    onQuantityChange: (Double) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.8f)) {
            Text(text = item.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                text = "₹${item.unitPrice.toInt()} • GST ${item.gstRate.toInt()}%",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Stepper
        Row(
            modifier = Modifier.weight(1.2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { onQuantityChange(item.quantity - 1) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
            }
            Text(
                text = "${item.quantity.toInt()} ${item.unit}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(
                onClick = { onQuantityChange(item.quantity + 1) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
            }
        }

        Text(
            text = formatRupee(item.totalAmount),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun InvoiceHistoryCard(
    invoice: InvoiceEntity,
    onClick: () -> Unit,
    onWhatsApp: () -> Unit
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
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = invoice.partyName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadge(status = invoice.paymentStatus)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "${invoice.invoiceNumber} • ${formatDate(invoice.date)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(
                modifier = Modifier.weight(1.2f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = formatRupee(invoice.grandTotal), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    if (invoice.dueAmount > 0) {
                        Text(text = "Due: ${formatRupee(invoice.dueAmount)}", fontSize = 11.sp, color = UdhaarRed, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onWhatsApp, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = VyaparTealPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AddItemToBillDialog(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onAdd: (InvoiceItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var customPrice by remember { mutableStateOf("") }
    var customGst by remember { mutableStateOf("18") }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.sku.contains(searchQuery, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Select Product", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                AppSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search inventory products..."
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredProducts) { prod ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val net = prod.sellingPrice
                                    val tax = net * (prod.gstRate / 100.0)
                                    onAdd(
                                        InvoiceItem(
                                            productId = prod.id,
                                            productName = prod.name,
                                            hsn = prod.hsn,
                                            quantity = 1.0,
                                            unit = prod.unit,
                                            unitPrice = prod.sellingPrice,
                                            gstRate = prod.gstRate,
                                            discountPercent = 0.0,
                                            taxAmount = tax,
                                            totalAmount = net + tax
                                        )
                                    )
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(text = prod.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Stock: ${prod.currentStock.toInt()} ${prod.unit} • GST ${prod.gstRate.toInt()}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = formatRupee(prod.sellingPrice),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = VyaparTealPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
