package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

data class InvoiceItem(
    val productId: Long = 0,
    val productName: String,
    val hsn: String = "",
    val quantity: Double = 1.0,
    val unit: String = "Pcs",
    val unitPrice: Double = 0.0,
    val gstRate: Double = 0.0,
    val discountPercent: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val type: String = "SALE", // SALE, PURCHASE, QUOTATION, PROFORMA, DELIVERY_CHALLAN, CREDIT_NOTE
    val customerId: Long = 0,
    val partyName: String = "Cash Customer",
    val partyMobile: String = "",
    val partyGstin: String = "",
    val partyState: String = "Delhi",
    val date: Long = System.currentTimeMillis(),
    val dueDate: Long = 0L,
    val isGst: Boolean = true,
    val subTotal: Double = 0.0,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val shippingCharge: Double = 0.0,
    val roundOff: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val paymentMode: String = "CASH", // CASH, UPI, CARD, BANK_TRANSFER, CHEQUE, CREDIT
    val paymentStatus: String = "PAID", // PAID, PARTIAL, UNPAID
    val notes: String = "",
    val itemsJson: String = "[]" // JSON representation of List<InvoiceItem>
)
