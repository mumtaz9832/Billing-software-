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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.InvoiceItem
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.formatRupee
import com.example.ui.theme.UdhaarRed
import com.example.ui.theme.VyaparTealPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.VyaparViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: VyaparViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var barcodeInput by remember { mutableStateOf("") }
    var searchKeyword by remember { mutableStateOf("") }

    val cartItems = remember { mutableStateListOf<InvoiceItem>() }
    var showCartSheet by remember { mutableStateOf(false) }

    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct()
    }

    val filteredProducts = products.filter { prod ->
        val matchesCategory = (selectedCategory == "All" || prod.category.equals(selectedCategory, ignoreCase = true))
        val matchesSearch = prod.name.contains(searchKeyword, ignoreCase = true) ||
                prod.sku.contains(searchKeyword, ignoreCase = true) ||
                prod.barcode.contains(searchKeyword, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val cartCount = cartItems.sumOf { it.quantity.toInt() }
    val cartSubTotal = cartItems.sumOf { it.totalAmount }

    fun addProductToCart(product: ProductEntity) {
        val existingIndex = cartItems.indexOfFirst { it.productId == product.id }
        if (existingIndex >= 0) {
            val old = cartItems[existingIndex]
            val newQty = old.quantity + 1
            val net = old.unitPrice * newQty
            val tax = net * (old.gstRate / 100.0)
            cartItems[existingIndex] = old.copy(quantity = newQty, taxAmount = tax, totalAmount = net + tax)
        } else {
            val net = product.sellingPrice
            val tax = net * (product.gstRate / 100.0)
            cartItems.add(
                InvoiceItem(
                    productId = product.id,
                    productName = product.name,
                    hsn = product.hsn,
                    quantity = 1.0,
                    unit = product.unit,
                    unitPrice = product.sellingPrice,
                    gstRate = product.gstRate,
                    taxAmount = tax,
                    totalAmount = net + tax
                )
            )
        }
    }

    fun handleBarcodeScan(barcode: String) {
        if (barcode.isBlank()) return
        val match = products.find { it.barcode.equals(barcode.trim(), ignoreCase = true) || it.sku.equals(barcode.trim(), ignoreCase = true) }
        if (match != null) {
            addProductToCart(match)
            barcodeInput = ""
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("pos_screen")) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 76.dp)) {
            // POS Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                            Text(text = "🛒 Quick POS Counter", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        if (cartCount > 0) {
                            Button(
                                onClick = { showCartSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = VyaparTealPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("$cartCount Items | ${formatRupee(cartSubTotal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Barcode & Search Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = barcodeInput,
                            onValueChange = {
                                barcodeInput = it
                                handleBarcodeScan(it)
                            },
                            placeholder = { Text("Scan / Type Barcode or SKU...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = VyaparTealPrimary)
                            },
                            trailingIcon = {
                                if (barcodeInput.isNotEmpty()) {
                                    IconButton(onClick = { barcodeInput = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = searchKeyword,
                            onValueChange = { searchKeyword = it },
                            placeholder = { Text("Search...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(130.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            Surface(
                                color = if (selectedCategory == cat) VyaparTealPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCategory == cat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Product Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts) { prod ->
                    PosProductTile(
                        product = prod,
                        onAdd = { addProductToCart(prod) }
                    )
                }
            }
        }

        // Bottom Fast Bar if items in cart
        if (cartCount > 0) {
            Surface(
                color = VyaparTealPrimary,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "$cartCount Items in Cart", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                        Text(text = formatRupee(cartSubTotal), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }

                    Button(
                        onClick = { showCartSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("pos_checkout_button")
                    ) {
                        Text("Checkout / Pay", color = VyaparTealPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Cart Drawer
        if (showCartSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCartSheet = false },
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "POS Billing Cart (${cartItems.size} items)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { cartItems.clear() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Cart", tint = Color.Red)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cartItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(text = item.productName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "₹${item.unitPrice.toInt()} each", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (item.quantity > 1) {
                                                val nq = item.quantity - 1
                                                val net = item.unitPrice * nq
                                                cartItems[index] = item.copy(quantity = nq, totalAmount = net * (1 + item.gstRate / 100.0))
                                            } else {
                                                cartItems.removeAt(index)
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(16.dp))
                                    }
                                    Text(text = "${item.quantity.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                                    IconButton(
                                        onClick = {
                                            val nq = item.quantity + 1
                                            val net = item.unitPrice * nq
                                            cartItems[index] = item.copy(quantity = nq, totalAmount = net * (1 + item.gstRate / 100.0))
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                                    }
                                }

                                Text(text = formatRupee(item.totalAmount), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Payable", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = formatRupee(cartSubTotal), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = VyaparTealPrimary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instant Cash / Instant UPI Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.createInvoice(
                                    type = "SALE",
                                    party = null,
                                    partyName = "Walk-in POS Customer",
                                    partyMobile = "",
                                    partyGstin = "",
                                    partyState = "Delhi",
                                    items = cartItems.toList(),
                                    discountPercent = 0.0,
                                    discountAmount = 0.0,
                                    shippingCharge = 0.0,
                                    roundOff = 0.0,
                                    paidAmount = cartSubTotal,
                                    paymentMode = "CASH",
                                    isGst = true,
                                    notes = "POS Instant Cash Bill",
                                    onSuccess = {
                                        cartItems.clear()
                                        showCartSheet = false
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("💵 Cash Pay", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.createInvoice(
                                    type = "SALE",
                                    party = null,
                                    partyName = "Walk-in POS Customer",
                                    partyMobile = "",
                                    partyGstin = "",
                                    partyState = "Delhi",
                                    items = cartItems.toList(),
                                    discountPercent = 0.0,
                                    discountAmount = 0.0,
                                    shippingCharge = 0.0,
                                    roundOff = 0.0,
                                    paidAmount = cartSubTotal,
                                    paymentMode = "UPI",
                                    isGst = true,
                                    notes = "POS Instant UPI Bill",
                                    onSuccess = {
                                        cartItems.clear()
                                        showCartSheet = false
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("📱 UPI QR Pay", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun PosProductTile(
    product: ProductEntity,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAdd() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = if (product.currentStock <= product.minStock) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${product.currentStock.toInt()} ${product.unit}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.currentStock <= product.minStock) UdhaarRed else Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(VyaparTealPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatRupee(product.sellingPrice),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = VyaparTealPrimary
            )
        }
    }
}
