package com.example.data.local

import com.example.data.local.entity.InvoiceItem
import org.json.JSONArray
import org.json.JSONObject

object InvoiceItemConverter {
    fun toJson(items: List<InvoiceItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("productId", item.productId)
                put("productName", item.productName)
                put("hsn", item.hsn)
                put("quantity", item.quantity)
                put("unit", item.unit)
                put("unitPrice", item.unitPrice)
                put("gstRate", item.gstRate)
                put("discountPercent", item.discountPercent)
                put("taxAmount", item.taxAmount)
                put("totalAmount", item.totalAmount)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun fromJson(jsonStr: String?): List<InvoiceItem> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        val list = mutableListOf<InvoiceItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    InvoiceItem(
                        productId = obj.optLong("productId", 0L),
                        productName = obj.optString("productName", ""),
                        hsn = obj.optString("hsn", ""),
                        quantity = obj.optDouble("quantity", 1.0),
                        unit = obj.optString("unit", "Pcs"),
                        unitPrice = obj.optDouble("unitPrice", 0.0),
                        gstRate = obj.optDouble("gstRate", 0.0),
                        discountPercent = obj.optDouble("discountPercent", 0.0),
                        taxAmount = obj.optDouble("taxAmount", 0.0),
                        totalAmount = obj.optDouble("totalAmount", 0.0)
                    )
                )
            }
        } catch (_: Exception) {
            // Safe fallback
        }
        return list
    }
}
