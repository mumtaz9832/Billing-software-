package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.KhataTransactionEntity
import com.example.ui.components.AppSearchBar
import com.example.ui.components.formatDate
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatRupee
import com.example.ui.components.formatRupeeWhole
import com.example.ui.components.shareText
import com.example.ui.theme.JamaGreen
import com.example.ui.theme.PendingOrange
import com.example.ui.theme.UdhaarRed
import com.example.ui.theme.VyaparTealPrimary
import com.example.ui.viewmodel.VyaparViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerKhataScreen(
    viewModel: VyaparViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Customers, 1: Suppliers
    val parties by viewModel.parties.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val selectedParty by viewModel.selectedParty.collectAsState()
    val partyTransactions by viewModel.partyTransactions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddPartyDialog by remember { mutableStateOf(false) }

    var showKhataEntryDialog by remember { mutableStateOf(false) }
    var entryType by remember { mutableStateOf("JAMA_GOT") } // "JAMA_GOT" or "UDHAAR_GIVE"

    val currentType = if (selectedTab == 0) "CUSTOMER" else "SUPPLIER"
    val filteredParties = parties.filter {
        it.type == currentType && (it.name.contains(searchQuery, ignoreCase = true) || it.mobile.contains(searchQuery))
    }

    val totalUdhaar = parties.filter { it.type == "CUSTOMER" && it.currentBalance > 0 }.sumOf { it.currentBalance }
    val totalPayable = parties.filter { it.type == "SUPPLIER" && it.currentBalance > 0 }.sumOf { it.currentBalance }

    if (selectedParty != null) {
        // Detailed Ledger View
        PartyLedgerView(
            party = selectedParty!!,
            profile = profile ?: BusinessProfileEntity(),
            transactions = partyTransactions,
            onBack = { viewModel.selectedParty.value = null },
            onAddEntry = { type ->
                entryType = type
                showKhataEntryDialog = true
            },
            onSendReminder = {
                profile?.let { p ->
                    val text = buildReminderText(selectedParty!!, p)
                    shareText(context, text, "WhatsApp Payment Reminder")
                }
            }
        )
    } else {
        // List View
        Box(modifier = modifier.fillMaxSize().testTag("khata_screen")) {
            Column(modifier = Modifier.fillMaxSize().padding(bottom = 76.dp)) {
                // Tab Row
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Customers (Udhaar)", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Suppliers (Payable)", fontWeight = FontWeight.Bold) }
                    )
                }

                // Balance summary banner
                Surface(
                    color = if (selectedTab == 0) Color(0xFFFEF2F2) else Color(0xFFFFFBEB),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTab == 0) "Total Market Udhaar (You'll receive)" else "Total Supplier Dues (You'll pay)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedTab == 0) UdhaarRed else PendingOrange
                        )
                        Text(
                            text = formatRupeeWhole(if (selectedTab == 0) totalUdhaar else totalPayable),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (selectedTab == 0) UdhaarRed else PendingOrange
                        )
                    }
                }

                // Search Bar
                Box(modifier = Modifier.padding(14.dp)) {
                    AppSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = if (selectedTab == 0) "Search customer by name or phone..." else "Search supplier..."
                    )
                }

                // Parties List
                if (filteredParties.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (selectedTab == 0) "No customers found. Click + to add." else "No suppliers found. Click + to add.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredParties) { party ->
                            PartyCard(
                                party = party,
                                onClick = { viewModel.selectParty(party) },
                                onCall = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${party.mobile}"))
                                    context.startActivity(intent)
                                },
                                onWhatsApp = {
                                    profile?.let { p ->
                                        val text = buildReminderText(party, p)
                                        shareText(context, text, "WhatsApp Reminder")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // FAB to Add Customer/Supplier
            FloatingActionButton(
                onClick = { showAddPartyDialog = true },
                containerColor = VyaparTealPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 80.dp)
                    .testTag("add_party_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Party")
            }
        }
    }

    // Add Party Sheet
    if (showAddPartyDialog) {
        AddPartySheet(
            defaultType = currentType,
            onDismiss = { showAddPartyDialog = false },
            onSave = { newParty ->
                viewModel.saveParty(newParty)
                showAddPartyDialog = false
            }
        )
    }

    // Khata Entry Dialog
    if (showKhataEntryDialog && selectedParty != null) {
        RecordKhataEntryDialog(
            party = selectedParty!!,
            initialType = entryType,
            onDismiss = { showKhataEntryDialog = false },
            onSave = { type, amount, mode, notes ->
                viewModel.recordKhataEntry(selectedParty!!, type, amount, mode, notes)
                showKhataEntryDialog = false
            }
        )
    }
}

@Composable
fun PartyCard(
    party: CustomerEntity,
    onClick: () -> Unit,
    onCall: () -> Unit,
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
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(text = party.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (party.mobile.isNotBlank()) {
                    Text(text = party.mobile, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (party.gstin.isNotBlank()) {
                    Text(text = "GSTIN: ${party.gstin}", fontSize = 10.sp, color = Color(0xFF0F766E))
                }
            }

            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatRupee(party.currentBalance),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (party.currentBalance > 0) (if (party.type == "CUSTOMER") UdhaarRed else PendingOrange) else JamaGreen
                )
                Text(
                    text = if (party.currentBalance > 0) (if (party.type == "CUSTOMER") "Due (Lena hai)" else "Due (Dena hai)") else "No Dues",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    if (party.mobile.isNotBlank()) {
                        IconButton(onClick = onCall, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (party.currentBalance > 0) {
                        IconButton(onClick = onWhatsApp, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Reminder", tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PartyLedgerView(
    party: CustomerEntity,
    profile: BusinessProfileEntity,
    transactions: List<KhataTransactionEntity>,
    onBack: () -> Unit,
    onAddEntry: (String) -> Unit,
    onSendReminder: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(bottom = 70.dp)) {
        // Party Details Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = party.name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${party.mobile} • ${party.state}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (party.currentBalance > 0 && party.type == "CUSTOMER") {
                        Button(
                            onClick = onSendReminder,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reminder", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Net Balance", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(
                            text = formatRupee(party.currentBalance),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (party.currentBalance > 0) UdhaarRed else JamaGreen
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Credit Limit", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(text = formatRupee(party.creditLimit), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // + GAVE UDHAAR & + GOT JAMA action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onAddEntry("UDHAAR_GIVE") },
                        colors = ButtonDefaults.buttonColors(containerColor = UdhaarRed),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (party.type == "CUSTOMER") "+ GAVE UDHAAR" else "+ PURCHASE BILL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onAddEntry("JAMA_GOT") },
                        colors = ButtonDefaults.buttonColors(containerColor = JamaGreen),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (party.type == "CUSTOMER") "+ RECEIVED JAMA" else "+ PAID SUPPLIER", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        HorizontalDivider()

        // Transaction History List
        Text(
            text = "Ledger History (${transactions.size} entries)",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(14.dp)
        )

        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No khata transactions recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { txn ->
                    val isUdhaar = txn.type == "UDHAAR_GIVE"
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
                                Text(
                                    text = if (isUdhaar) "Udhaar Given" else "Jama Received (${txn.paymentMode})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isUdhaar) UdhaarRed else JamaGreen
                                )
                                Text(text = txn.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = formatDateTime(txn.date), fontSize = 10.sp, color = Color.Gray)
                            }

                            Text(
                                text = "${if (isUdhaar) "+" else "-"} ${formatRupee(txn.amount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = if (isUdhaar) UdhaarRed else JamaGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordKhataEntryDialog(
    party: CustomerEntity,
    initialType: String,
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Double, mode: String, notes: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("CASH") }
    var notes by remember { mutableStateOf("") }

    val isUdhaar = type == "UDHAAR_GIVE"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Khata Entry: ${party.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { type = "UDHAAR_GIVE" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isUdhaar) UdhaarRed else Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Udhaar (Gave)", color = if (isUdhaar) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { type = "JAMA_GOT" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isUdhaar) JamaGreen else Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Jama (Received)", color = if (!isUdhaar) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Bill Details") },
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
                            if (amt > 0) {
                                onSave(type, amt, paymentMode, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isUdhaar) UdhaarRed else JamaGreen)
                    ) {
                        Text("Save Entry")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartySheet(
    defaultType: String,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("Delhi") }
    var gstin by remember { mutableStateOf("") }
    var partyType by remember { mutableStateOf(defaultType) }
    var openingBalance by remember { mutableStateOf("0") }
    var creditLimit by remember { mutableStateOf("50000") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = if (partyType == "CUSTOMER") "Add New Customer" else "Add New Supplier",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Party / Business Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Mobile Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = gstin,
                    onValueChange = { gstin = it },
                    label = { Text("GSTIN (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Shop / Office Address") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = openingBalance,
                        onValueChange = { openingBalance = it },
                        label = { Text("Opening Udhaar (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = creditLimit,
                        onValueChange = { creditLimit = it },
                        label = { Text("Credit Limit (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        val ob = openingBalance.toDoubleOrNull() ?: 0.0
                        val party = CustomerEntity(
                            name = name,
                            mobile = mobile,
                            email = email,
                            address = address,
                            state = state,
                            gstin = gstin,
                            type = partyType,
                            openingBalance = ob,
                            currentBalance = ob,
                            creditLimit = creditLimit.toDoubleOrNull() ?: 50000.0,
                            nextDueDate = System.currentTimeMillis() + 7L * 86400000L
                        )
                        onSave(party)
                    },
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparTealPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Save Party", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

fun buildReminderText(party: CustomerEntity, profile: BusinessProfileEntity): String {
    return """
Namaste *${party.name}* ji,

This is a gentle payment reminder from *${profile.businessName}*.
Your current outstanding due balance is *₹${party.currentBalance.toInt()}*.

Kindly clear the balance at your earliest convenience.
Pay via UPI: *${profile.upiId}*
Bank Transfer: ${profile.bankName} | A/C: ${profile.bankAccountNo} | IFSC: ${profile.bankIfsc}

Thank you! 🙏
${profile.ownerName} (${profile.mobile})
    """.trimIndent()
}
