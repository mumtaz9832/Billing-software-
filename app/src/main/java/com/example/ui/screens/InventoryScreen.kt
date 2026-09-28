package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.AppSearchBar
import com.example.ui.components.formatRupee
import com.example.ui.components.formatRupeeWhole
import com.example.ui.theme.PendingOrange
import com.example.ui.theme.UdhaarRed
import com.example.ui.theme.VyaparTealPrimary
import com.example.ui.viewmodel.VyaparViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: VyaparViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, LOW_STOCK, GROCERIES, FMCG, ELECTRICALS

    var showProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    var showAdjustStockDialog by remember { mutableStateOf(false) }
    var adjustingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val lowStockCount = products.count { it.currentStock <= it.minStock }
    val totalStockValuation = products.sumOf { it.purchasePrice * it.currentStock }

    val filteredProducts = products.filter { prod ->
        val matchesSearch = prod.name.contains(searchQuery, ignoreCase = true) ||
                prod.sku.contains(searchQuery, ignoreCase = true) ||
                prod.barcode.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "LOW_STOCK" -> prod.currentStock <= prod.minStock
            "ALL" -> true
            else -> prod.category.equals(selectedFilter, ignoreCase = true)
        }
        matchesSearch && matchesFilter
    }

    Box(modifier = modifier.fillMaxSize().testTag("inventory_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Valuation and summary header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Total Stock Valuation", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = formatRupeeWhole(totalStockValuation), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = VyaparTealPrimary)
                            Text(text = "${products.size} Total Products in Master", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (lowStockCount > 0) {
                            Surface(
                                color = Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { selectedFilter = "LOW_STOCK" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = UdhaarRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$lowStockCount Low Stock",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = UdhaarRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                AppSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search by item name, SKU, or barcode..."
                )
            }

            // Filters
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterPill(label = "All Items", selected = selectedFilter == "ALL") { selectedFilter = "ALL" }
                    }
                    item {
                        FilterPill(label = "⚠️ Low Stock ($lowStockCount)", selected = selectedFilter == "LOW_STOCK") { selectedFilter = "LOW_STOCK" }
                    }
                    item {
                        FilterPill(label = "Groceries", selected = selectedFilter == "Groceries") { selectedFilter = "Groceries" }
                    }
                    item {
                        FilterPill(label = "FMCG", selected = selectedFilter == "FMCG") { selectedFilter = "FMCG" }
                    }
                    item {
                        FilterPill(label = "Electricals", selected = selectedFilter == "Electricals") { selectedFilter = "Electricals" }
                    }
                }
            }

            // Product List
            if (filteredProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No products found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(filteredProducts) { prod ->
                    ProductCard(
                        product = prod,
                        onEdit = {
                            editingProduct = prod
                            showProductDialog = true
                        },
                        onAdjustStock = {
                            adjustingProduct = prod
                            showAdjustStockDialog = true
                        },
                        onDelete = {
                            viewModel.deleteProduct(prod.id)
                        }
                    )
                }
            }
        }

        // Add Product Floating Action Button
        FloatingActionButton(
            onClick = {
                editingProduct = null
                showProductDialog = true
            },
            containerColor = VyaparTealPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
                .testTag("add_product_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Product")
        }
    }

    // Add / Edit Product Sheet
    if (showProductDialog) {
        ProductMasterSheet(
            initialProduct = editingProduct,
            onDismiss = { showProductDialog = false },
            onSave = { saved ->
                viewModel.saveProduct(saved)
                showProductDialog = false
            }
        )
    }

    // Stock Adjustment Dialog
    if (showAdjustStockDialog && adjustingProduct != null) {
        StockAdjustmentDialog(
            product = adjustingProduct!!,
            onDismiss = { showAdjustStockDialog = false },
            onConfirm = { delta ->
                viewModel.adjustStock(adjustingProduct!!.id, delta)
                showAdjustStockDialog = false
            }
        )
    }
}

@Composable
fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onEdit: () -> Unit,
    onAdjustStock: () -> Unit,
    onDelete: () -> Unit
) {
    val isLow = product.currentStock <= product.minStock
    val margin = if (product.purchasePrice > 0) {
        ((product.sellingPrice - product.purchasePrice) / product.purchasePrice) * 100
    } else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1.5f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "HSN: ${product.hsn.ifBlank { "N/A" }} • GST: ${product.gstRate.toInt()}% • SKU: ${product.sku.ifBlank { "-" }}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (isLow) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${product.currentStock.toInt()} ${product.unit}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLow) UdhaarRed else Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sale: ${formatRupee(product.sellingPrice)} (MRP: ₹${product.mrp.toInt()})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cost: ${formatRupee(product.purchasePrice)} • Margin: ${margin.toInt()}%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onAdjustStock, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = "Adjust Stock", tint = VyaparTealPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StockAdjustmentDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var quantityInput by remember { mutableStateOf("") }
    var isAddition by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Adjust Stock", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = product.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Current Stock: ${product.currentStock.toInt()} ${product.unit}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isAddition = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isAddition) Color(0xFF16A34A) else Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Add Stock", color = if (isAddition) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { isAddition = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isAddition) UdhaarRed else Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("- Reduce Stock", color = if (!isAddition) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    label = { Text("Quantity (${product.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            val qty = quantityInput.toDoubleOrNull() ?: 0.0
                            val delta = if (isAddition) qty else -qty
                            onConfirm(delta)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VyaparTealPrimary)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductMasterSheet(
    initialProduct: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var sku by remember { mutableStateOf(initialProduct?.sku ?: "") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var hsn by remember { mutableStateOf(initialProduct?.hsn ?: "") }
    var gstRate by remember { mutableDoubleStateOf(initialProduct?.gstRate ?: 18.0) }
    var purchasePrice by remember { mutableStateOf(if (initialProduct != null) initialProduct.purchasePrice.toString() else "") }
    var sellingPrice by remember { mutableStateOf(if (initialProduct != null) initialProduct.sellingPrice.toString() else "") }
    var mrp by remember { mutableStateOf(if (initialProduct != null) initialProduct.mrp.toString() else "") }
    var wholesalePrice by remember { mutableStateOf(if (initialProduct != null) initialProduct.wholesalePrice.toString() else "") }
    var minStock by remember { mutableStateOf(if (initialProduct != null) initialProduct.minStock.toString() else "10") }
    var currentStock by remember { mutableStateOf(if (initialProduct != null) initialProduct.currentStock.toString() else "0") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "Pcs") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "General") }

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
                    text = if (initialProduct == null) "Add New Product" else "Edit Product",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (Pcs, Kg, Box)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hsn,
                        onValueChange = { hsn = it },
                        label = { Text("HSN / SAC Code") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = gstRate.toInt().toString(),
                        onValueChange = { gstRate = it.toDoubleOrNull() ?: 18.0 },
                        label = { Text("GST Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        label = { Text("Purchase Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it },
                        label = { Text("Selling Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mrp,
                        onValueChange = { mrp = it },
                        label = { Text("MRP (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wholesalePrice,
                        onValueChange = { wholesalePrice = it },
                        label = { Text("Wholesale (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentStock,
                        onValueChange = { currentStock = it },
                        label = { Text("Opening Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minStock,
                        onValueChange = { minStock = it },
                        label = { Text("Min Stock Alert") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU Code") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        val product = ProductEntity(
                            id = initialProduct?.id ?: 0L,
                            name = name,
                            sku = sku,
                            barcode = barcode,
                            hsn = hsn,
                            gstRate = gstRate,
                            purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                            sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                            mrp = mrp.toDoubleOrNull() ?: 0.0,
                            wholesalePrice = wholesalePrice.toDoubleOrNull() ?: 0.0,
                            minStock = minStock.toDoubleOrNull() ?: 10.0,
                            currentStock = currentStock.toDoubleOrNull() ?: 0.0,
                            unit = unit,
                            category = category
                        )
                        onSave(product)
                    },
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparTealPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Save Product", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
