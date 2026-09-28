package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    val hsn: String = "",
    val gstRate: Double = 18.0, // 0, 5, 12, 18, 28
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val mrp: Double = 0.0,
    val wholesalePrice: Double = 0.0,
    val minStock: Double = 10.0,
    val currentStock: Double = 0.0,
    val unit: String = "Pcs", // Pcs, Box, Kg, Ltr, Meter
    val category: String = "General",
    val batchNumber: String = "",
    val expiryDate: String = ""
)
