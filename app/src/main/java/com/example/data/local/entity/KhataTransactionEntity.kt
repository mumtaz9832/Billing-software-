package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "khata_transactions")
data class KhataTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partyId: Long,
    val partyName: String,
    val partyType: String = "CUSTOMER", // CUSTOMER or SUPPLIER
    val type: String, // "UDHAAR_GIVE" (Debit / You Gave) or "JAMA_GOT" (Credit / You Received)
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val paymentMode: String = "CASH", // CASH, UPI, BANK, CHEQUE
    val relatedInvoiceNumber: String = ""
)
