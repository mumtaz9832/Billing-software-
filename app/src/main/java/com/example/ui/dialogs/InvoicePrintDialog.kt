package com.example.ui.dialogs

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.InvoiceItemConverter
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.ui.components.formatDate
import com.example.ui.components.formatRupee
import com.example.ui.components.shareText
import com.example.ui.theme.VyaparTealPrimary

@Composable
fun InvoicePrintDialog(
    invoice: InvoiceEntity,
    profile: BusinessProfileEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFormatIndex by remember { mutableIntStateOf(0) } // 0: A4 Standard, 1: Thermal 58mm
    val items = remember(invoice.itemsJson) {
        InvoiceItemConverter.fromJson(invoice.itemsJson)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = invoice.invoiceNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${invoice.type} • ${invoice.paymentStatus}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val shareMessage = buildWhatsAppInvoiceText(invoice, profile)
                                shareText(context, shareMessage, "Share Invoice on WhatsApp")
                            },
                            modifier = Modifier.testTag("share_invoice_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = VyaparTealPrimary)
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Format Tabs (A4 Standard vs Thermal Receipt)
                TabRow(selectedTabIndex = selectedFormatIndex) {
                    Tab(
                        selected = selectedFormatIndex == 0,
                        onClick = { selectedFormatIndex = 0 },
                        text = { Text("A4 GST Invoice") }
                    )
                    Tab(
                        selected = selectedFormatIndex == 1,
                        onClick = { selectedFormatIndex = 1 },
                        text = { Text("Thermal 58mm POS") }
                    )
                }

                // Document Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (selectedFormatIndex == 0) {
                        A4InvoiceView(invoice = invoice, profile = profile, items = items)
                    } else {
                        ThermalInvoiceView(invoice = invoice, profile = profile, items = items)
                    }
                }

                // Bottom Action Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            val shareMessage = buildWhatsAppInvoiceText(invoice, profile)
                            shareText(context, shareMessage, "WhatsApp Bill & Receipt")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VyaparTealPrimary),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send WhatsApp")
                    }
                }
            }
        }
    }
}

@Composable
fun A4InvoiceView(
    invoice: InvoiceEntity,
    profile: BusinessProfileEntity,
    items: List<com.example.data.local.entity.InvoiceItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.5f)) {
                    Text(
                        text = profile.businessName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(text = profile.address, fontSize = 11.sp, color = Color(0xFF475569))
                    Text(text = "Phone: ${profile.mobile}", fontSize = 11.sp, color = Color(0xFF475569))
                    Text(text = "GSTIN: ${profile.gstin}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F766E))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Surface(
                        color = Color(0xFF0F766E),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (invoice.isGst) "TAX INVOICE" else "BILL OF SUPPLY",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Invoice #: ${invoice.invoiceNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(text = "Date: ${formatDate(invoice.date)}", fontSize = 11.sp, color = Color(0xFF475569))
                    if (invoice.dueDate > 0) {
                        Text(text = "Due Date: ${formatDate(invoice.dueDate)}", fontSize = 11.sp, color = Color(0xFFB45309))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))

            // Bill To
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "BILL TO:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(text = invoice.partyName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    if (invoice.partyMobile.isNotBlank()) {
                        Text(text = "Mobile: ${invoice.partyMobile}", fontSize = 11.sp, color = Color(0xFF475569))
                    }
                    if (invoice.partyGstin.isNotBlank()) {
                        Text(text = "GSTIN: ${invoice.partyGstin}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F766E))
                    }
                    Text(text = "State: ${invoice.partyState}", fontSize = 11.sp, color = Color(0xFF475569))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = "PAYMENT DETAILS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(text = "Mode: ${invoice.paymentMode}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    Text(text = "Status: ${invoice.paymentStatus}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (invoice.paymentStatus == "PAID") Color(0xFF16A34A) else Color(0xFFDC2626))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Items Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Item", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.weight(2f))
                Text(text = "HSN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.weight(1f))
                Text(text = "Qty", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), textAlign = TextAlign.Center, modifier = Modifier.weight(0.7f))
                Text(text = "Rate", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                Text(text = "Total", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
            }

            // Items Rows
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(2f)) {
                        Text(text = item.productName, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                        if (invoice.isGst && item.gstRate > 0) {
                            Text(text = "GST: ${item.gstRate.toInt()}%", fontSize = 9.sp, color = Color(0xFF64748B))
                        }
                    }
                    Text(text = item.hsn.ifBlank { "-" }, fontSize = 10.sp, color = Color(0xFF475569), modifier = Modifier.weight(1f))
                    Text(text = "${item.quantity.toInt()} ${item.unit}", fontSize = 11.sp, color = Color(0xFF0F172A), textAlign = TextAlign.Center, modifier = Modifier.weight(0.7f))
                    Text(text = "₹${item.unitPrice.toInt()}", fontSize = 11.sp, color = Color(0xFF0F172A), textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                    Text(text = formatRupee(item.totalAmount), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A), textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                }
                HorizontalDivider(color = Color(0xFFF1F5F9))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtotals & Tax Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bank Details & UPI QR
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(text = "Bank & UPI Details:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(text = "Bank: ${profile.bankName}", fontSize = 10.sp, color = Color(0xFF334155))
                    Text(text = "A/C: ${profile.bankAccountNo}", fontSize = 10.sp, color = Color(0xFF334155))
                    Text(text = "IFSC: ${profile.bankIfsc}", fontSize = 10.sp, color = Color(0xFF334155))
                    Text(text = "UPI: ${profile.upiId}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F766E))

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Scan & Pay\nvia UPI App", fontSize = 9.sp, color = Color(0xFF475569))
                    }
                }

                // Calculation Column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    CalcRow(label = "Sub Total", value = formatRupee(invoice.subTotal))
                    if (invoice.discountAmount > 0) {
                        CalcRow(label = "Discount", value = "- ${formatRupee(invoice.discountAmount)}", color = Color(0xFF16A34A))
                    }
                    if (invoice.isGst) {
                        if (invoice.cgstAmount > 0) CalcRow(label = "CGST", value = formatRupee(invoice.cgstAmount))
                        if (invoice.sgstAmount > 0) CalcRow(label = "SGST", value = formatRupee(invoice.sgstAmount))
                        if (invoice.igstAmount > 0) CalcRow(label = "IGST", value = formatRupee(invoice.igstAmount))
                    }
                    if (invoice.shippingCharge > 0) {
                        CalcRow(label = "Delivery", value = formatRupee(invoice.shippingCharge))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFCBD5E1))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Grand Total", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(text = formatRupee(invoice.grandTotal), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F766E))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Paid", fontSize = 11.sp, color = Color(0xFF16A34A))
                        Text(text = formatRupee(invoice.paidAmount), fontSize = 11.sp, color = Color(0xFF16A34A))
                    }
                    if (invoice.dueAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Balance Due", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                            Text(text = formatRupee(invoice.dueAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(8.dp))

            // Footer & Signatory
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1.5f)) {
                    Text(text = "Terms & Conditions:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(text = profile.termsAndConditions, fontSize = 8.sp, color = Color(0xFF64748B), lineHeight = 11.sp)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = "For ${profile.businessName}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(text = "Authorized Signatory", fontSize = 9.sp, color = Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
fun ThermalInvoiceView(
    invoice: InvoiceEntity,
    profile: BusinessProfileEntity,
    items: List<com.example.data.local.entity.InvoiceItem>
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(4.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = profile.businessName, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color.Black)
            Text(text = profile.address, fontSize = 9.sp, textAlign = TextAlign.Center, color = Color.DarkGray)
            Text(text = "Tel: ${profile.mobile}", fontSize = 9.sp, color = Color.DarkGray)
            Text(text = "GSTIN: ${profile.gstin}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)

            Text(text = "------------------------------------------", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Gray)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Bill: ${invoice.invoiceNumber}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = formatDate(invoice.date), fontSize = 10.sp, color = Color.Black)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Customer: ${invoice.partyName}", fontSize = 10.sp, color = Color.Black)
                Text(text = invoice.paymentMode, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Text(text = "------------------------------------------", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Gray)

            // Items
            items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = item.productName, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.weight(1.8f))
                    Text(text = "${item.quantity.toInt()}x${item.unitPrice.toInt()}", fontSize = 9.sp, color = Color.DarkGray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text(text = "₹${item.totalAmount.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
            }

            Text(text = "------------------------------------------", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Gray)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "TOTAL AMOUNT:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = formatRupee(invoice.grandTotal), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Paid Amount:", fontSize = 10.sp, color = Color.Black)
                Text(text = formatRupee(invoice.paidAmount), fontSize = 10.sp, color = Color.Black)
            }
            if (invoice.dueAmount > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Balance Due:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    Text(text = formatRupee(invoice.dueAmount), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color.Black, modifier = Modifier.size(54.dp))
            Text(text = "UPI: ${profile.upiId}", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Text(text = "Thank You! Visit Again", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp), color = Color.Black)
        }
    }
}

@Composable
private fun CalcRow(label: String, value: String, color: Color = Color(0xFF475569)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = color)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

fun buildWhatsAppInvoiceText(invoice: InvoiceEntity, profile: BusinessProfileEntity): String {
    val items = InvoiceItemConverter.fromJson(invoice.itemsJson)
    val itemsSummary = items.joinToString("\n") { "• ${it.productName} (${it.quantity.toInt()} ${it.unit}) = ₹${it.totalAmount.toInt()}" }

    return """
🧾 *INVOICE: ${invoice.invoiceNumber}*
From: *${profile.businessName}*
Date: ${formatDate(invoice.date)}

Dear *${invoice.partyName}*,
Here are your bill details:

$itemsSummary

*Grand Total:* ₹${invoice.grandTotal.toInt()}
*Paid:* ₹${invoice.paidAmount.toInt()}
${if (invoice.dueAmount > 0) "⚠️ *Balance Due:* ₹${invoice.dueAmount.toInt()}\nDue Date: ${formatDate(invoice.dueDate)}" else "✅ *Payment Status:* FULLY PAID"}

Pay online via UPI: *${profile.upiId}*
Bank: ${profile.bankName} | A/C: ${profile.bankAccountNo} | IFSC: ${profile.bankIfsc}

Thank you for your business! 🙏
    """.trimIndent()
}
