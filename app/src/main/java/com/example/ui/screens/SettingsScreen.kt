package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.entity.BusinessProfileEntity
import com.example.ui.theme.VyaparTealPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.VyaparViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: VyaparViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentProfile by viewModel.profile.collectAsState()
    val profile = currentProfile ?: BusinessProfileEntity()

    var businessName by remember(profile) { mutableStateOf(profile.businessName) }
    var ownerName by remember(profile) { mutableStateOf(profile.ownerName) }
    var mobile by remember(profile) { mutableStateOf(profile.mobile) }
    var email by remember(profile) { mutableStateOf(profile.email) }
    var address by remember(profile) { mutableStateOf(profile.address) }
    var state by remember(profile) { mutableStateOf(profile.state) }
    var gstin by remember(profile) { mutableStateOf(profile.gstin) }
    var pan by remember(profile) { mutableStateOf(profile.pan) }

    var bankName by remember(profile) { mutableStateOf(profile.bankName) }
    var bankAccountNo by remember(profile) { mutableStateOf(profile.bankAccountNo) }
    var bankIfsc by remember(profile) { mutableStateOf(profile.bankIfsc) }
    var upiId by remember(profile) { mutableStateOf(profile.upiId) }
    var terms by remember(profile) { mutableStateOf(profile.termsAndConditions) }

    var isPinLockEnabled by remember(profile) { mutableStateOf(profile.isPinLockEnabled) }
    var pinCode by remember(profile) { mutableStateOf(profile.pinCode) }
    var currentRole by remember(profile) { mutableStateOf(profile.currentRole) }
    var preferredPrinter by remember(profile) { mutableStateOf(profile.preferredPrinter) }

    val roles = listOf("Owner", "Manager", "Cashier", "Salesman", "Accountant")
    val printers = listOf("A4", "A5", "Thermal 58mm", "Thermal 80mm")

    Column(modifier = modifier.fillMaxSize().testTag("settings_screen")) {
        // Settings Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Business Profile & Settings",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Business Details Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = VyaparTealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Company & Store Details", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Business / Firm Name *") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = ownerName,
                                onValueChange = { ownerName = it },
                                label = { Text("Owner Name") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = mobile,
                                onValueChange = { mobile = it },
                                label = { Text("Phone Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Complete Shop Address") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = gstin,
                                onValueChange = { gstin = it },
                                label = { Text("GSTIN") },
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = state,
                                onValueChange = { state = it },
                                label = { Text("State") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Bank & UPI Details Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = VyaparTealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Bank & UPI Details (Printed on Invoices)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = { Text("UPI ID (e.g. store@sbi)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = bankName,
                                onValueChange = { bankName = it },
                                label = { Text("Bank Name") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = bankIfsc,
                                onValueChange = { bankIfsc = it },
                                label = { Text("IFSC Code") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = bankAccountNo,
                            onValueChange = { bankAccountNo = it },
                            label = { Text("Bank Account Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Role & Security Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = VyaparTealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Staff Role & App Security", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Role Selector
                        var expandedRole by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedRole,
                            onExpandedChange = { expandedRole = !expandedRole }
                        ) {
                            OutlinedTextField(
                                value = currentRole,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Active User Role") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedRole,
                                onDismissRequest = { expandedRole = false }
                            ) {
                                roles.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role) },
                                        onClick = { currentRole = role; expandedRole = false }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // PIN Lock Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "4-Digit PIN App Lock", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Require PIN to open app", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isPinLockEnabled,
                                onCheckedChange = { isPinLockEnabled = it }
                            )
                        }

                        if (isPinLockEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = pinCode,
                                onValueChange = { if (it.length <= 4) pinCode = it },
                                label = { Text("Security PIN (4 digits)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Invoice Terms & Print Layout
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = VyaparTealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Invoice Template & Terms", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Printer Layout selector
                        var expandedPrinter by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedPrinter,
                            onExpandedChange = { expandedPrinter = !expandedPrinter }
                        ) {
                            OutlinedTextField(
                                value = preferredPrinter,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Default Print Format") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPrinter) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedPrinter,
                                onDismissRequest = { expandedPrinter = false }
                            ) {
                                printers.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p) },
                                        onClick = { preferredPrinter = p; expandedPrinter = false }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = terms,
                            onValueChange = { terms = it },
                            label = { Text("Invoice Terms & Conditions") },
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Save Settings Button
            item {
                Button(
                    onClick = {
                        val updated = profile.copy(
                            businessName = businessName,
                            ownerName = ownerName,
                            mobile = mobile,
                            email = email,
                            address = address,
                            state = state,
                            gstin = gstin,
                            pan = pan,
                            bankName = bankName,
                            bankAccountNo = bankAccountNo,
                            bankIfsc = bankIfsc,
                            upiId = upiId,
                            termsAndConditions = terms,
                            isPinLockEnabled = isPinLockEnabled,
                            pinCode = pinCode,
                            currentRole = currentRole,
                            preferredPrinter = preferredPrinter
                        )
                        viewModel.updateBusinessProfile(updated)
                        Toast.makeText(context, "Business settings saved successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparTealPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Settings", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
