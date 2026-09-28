package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfileEntity(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "Bharat Traders & Enterprises",
    val ownerName: String = "Rajesh Sharma",
    val mobile: String = "+91 98765 43210",
    val email: String = "contact@bharattraders.in",
    val address: String = "Shop No. 42, Main Commercial Market, Connaught Place",
    val city: String = "New Delhi",
    val state: String = "Delhi",
    @ColumnInfo(name = "postal_code") val postalZip: String = "110001",
    val gstin: String = "07AAAAA0000A1Z5",
    val pan: String = "AAAAA0000A",
    val bankName: String = "State Bank of India",
    val bankAccountNo: String = "302918273645",
    val bankIfsc: String = "SBIN0001234",
    val upiId: String = "bharattraders@sbi",
    val termsAndConditions: String = "1. Goods once sold will be exchanged within 7 days with valid bill.\n2. Interest @ 18% p.a. will be charged if bill is unpaid after due date.\n3. Subject to Delhi Jurisdiction.",
    val invoicePrefix: String = "INV-2026-",
    val isPinLockEnabled: Boolean = false,
    @ColumnInfo(name = "security_pin") val pinCode: String = "1234",
    val currentRole: String = "Owner", // Owner, Manager, Cashier, Salesman
    val preferredPrinter: String = "A4" // A4, A5, Thermal 58mm, Thermal 80mm
)
