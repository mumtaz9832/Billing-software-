package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mobile: String = "",
    val email: String = "",
    val address: String = "",
    val state: String = "Delhi",
    val gstin: String = "",
    val type: String = "CUSTOMER", // "CUSTOMER" or "SUPPLIER"
    val openingBalance: Double = 0.0,
    val creditLimit: Double = 50000.0,
    val currentBalance: Double = 0.0, // Positive: Receivable (Customer owes) or Payable (To supplier)
    val nextDueDate: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
