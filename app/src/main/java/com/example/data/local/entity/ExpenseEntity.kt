package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "Miscellaneous", // Rent, Electricity, Salary, Transport, Internet, Maintenance, Tea/Snacks, Miscellaneous
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMode: String = "CASH", // CASH, UPI, BANK
    val notes: String = ""
)
