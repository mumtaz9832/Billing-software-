package com.example.data.local

import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItem
import com.example.data.local.entity.KhataTransactionEntity
import com.example.data.local.entity.ProductEntity

object SampleData {
    suspend fun seedIfEmpty(database: AppDatabase) {
        val productDao = database.productDao()
        val customerDao = database.customerDao()
        val invoiceDao = database.invoiceDao()
        val khataDao = database.khataDao()
        val expenseDao = database.expenseDao()
        val profileDao = database.businessProfileDao()

        if (profileDao.getProfile() == null) {
            profileDao.insertOrUpdate(BusinessProfileEntity())
        }

        if (productDao.getCount() == 0) {
            val products = listOf(
                ProductEntity(
                    id = 1,
                    name = "Basmati Rice 5kg Royal Premium",
                    sku = "RICE-BAS-05",
                    barcode = "8901030800101",
                    hsn = "10063020",
                    gstRate = 5.0,
                    purchasePrice = 380.0,
                    sellingPrice = 460.0,
                    mrp = 520.0,
                    wholesalePrice = 430.0,
                    minStock = 15.0,
                    currentStock = 48.0,
                    unit = "Bag",
                    category = "Groceries"
                ),
                ProductEntity(
                    id = 2,
                    name = "Fortune Sunlite Oil 1L Pouch",
                    sku = "OIL-SUN-01",
                    barcode = "8906007281012",
                    hsn = "15121910",
                    gstRate = 5.0,
                    purchasePrice = 115.0,
                    sellingPrice = 135.0,
                    mrp = 150.0,
                    wholesalePrice = 125.0,
                    minStock = 20.0,
                    currentStock = 8.0, // Low stock alert!
                    unit = "Pcs",
                    category = "Groceries"
                ),
                ProductEntity(
                    id = 3,
                    name = "Havells 9W LED Cool Day Bulb (Pack of 2)",
                    sku = "LGT-HAV-09W",
                    barcode = "8901736123456",
                    hsn = "85395000",
                    gstRate = 18.0,
                    purchasePrice = 130.0,
                    sellingPrice = 180.0,
                    mrp = 220.0,
                    wholesalePrice = 160.0,
                    minStock = 25.0,
                    currentStock = 62.0,
                    unit = "Box",
                    category = "Electricals"
                ),
                ProductEntity(
                    id = 4,
                    name = "Syska 20000mAh Power Bank Type-C",
                    sku = "ELE-PWR-20K",
                    barcode = "8904123456789",
                    hsn = "85076000",
                    gstRate = 18.0,
                    purchasePrice = 1100.0,
                    sellingPrice = 1499.0,
                    mrp = 1899.0,
                    wholesalePrice = 1350.0,
                    minStock = 10.0,
                    currentStock = 14.0,
                    unit = "Pcs",
                    category = "Electronics"
                ),
                ProductEntity(
                    id = 5,
                    name = "Tata Tea Gold 500g Jar",
                    sku = "TEA-TAT-500",
                    barcode = "8901052002345",
                    hsn = "09024020",
                    gstRate = 5.0,
                    purchasePrice = 240.0,
                    sellingPrice = 285.0,
                    mrp = 310.0,
                    wholesalePrice = 265.0,
                    minStock = 12.0,
                    currentStock = 5.0, // Low stock!
                    unit = "Pcs",
                    category = "FMCG"
                ),
                ProductEntity(
                    id = 6,
                    name = "Detergent Powder Surf Excel 3kg",
                    sku = "CLN-SRF-03K",
                    barcode = "8901030567890",
                    hsn = "34022090",
                    gstRate = 18.0,
                    purchasePrice = 390.0,
                    sellingPrice = 465.0,
                    mrp = 510.0,
                    wholesalePrice = 430.0,
                    minStock = 15.0,
                    currentStock = 32.0,
                    unit = "Pcs",
                    category = "FMCG"
                ),
                ProductEntity(
                    id = 7,
                    name = "Fastrack Casual Analog Watch",
                    sku = "WTC-FST-001",
                    barcode = "8907890123456",
                    hsn = "91021100",
                    gstRate = 18.0,
                    purchasePrice = 950.0,
                    sellingPrice = 1495.0,
                    mrp = 1795.0,
                    wholesalePrice = 1290.0,
                    minStock = 5.0,
                    currentStock = 3.0, // Low stock
                    unit = "Pcs",
                    category = "Accessories"
                ),
                ProductEntity(
                    id = 8,
                    name = "Cadbury Dairy Milk Silk 150g",
                    sku = "CNF-CAD-150",
                    barcode = "8901233020020",
                    hsn = "18063100",
                    gstRate = 18.0,
                    purchasePrice = 135.0,
                    sellingPrice = 175.0,
                    mrp = 185.0,
                    wholesalePrice = 155.0,
                    minStock = 20.0,
                    currentStock = 45.0,
                    unit = "Pcs",
                    category = "Confectionery"
                )
            )
            productDao.insertAll(products)
        }

        if (customerDao.getCount() == 0) {
            val currentTime = System.currentTimeMillis()
            val dayMillis = 86400000L
            val parties = listOf(
                CustomerEntity(
                    id = 1,
                    name = "Rahul Traders",
                    mobile = "9810123456",
                    email = "rahul.traders@gmail.com",
                    address = "Plot 12, Chandni Chowk Commercial Lane",
                    state = "Delhi",
                    gstin = "07BPAR9876Q1Z2",
                    type = "CUSTOMER",
                    openingBalance = 5000.0,
                    creditLimit = 50000.0,
                    currentBalance = 18500.0,
                    nextDueDate = currentTime + 5 * dayMillis
                ),
                CustomerEntity(
                    id = 2,
                    name = "Sharma Kirana Store",
                    mobile = "9871234567",
                    email = "sharma.kirana@yahoo.com",
                    address = "Gali No 4, Main Bazar, Karol Bagh",
                    state = "Delhi",
                    gstin = "07CPKL3456K1Z9",
                    type = "CUSTOMER",
                    openingBalance = 2000.0,
                    creditLimit = 40000.0,
                    currentBalance = 9450.0,
                    nextDueDate = currentTime - 2 * dayMillis // Overdue!
                ),
                CustomerEntity(
                    id = 3,
                    name = "Verma Electricals & Hardware",
                    mobile = "9911987654",
                    email = "verma.electric@rediffmail.com",
                    address = "Sector 18 Market, Noida",
                    state = "Uttar Pradesh",
                    gstin = "09DEPK4567M1Z1",
                    type = "CUSTOMER",
                    openingBalance = 0.0,
                    creditLimit = 60000.0,
                    currentBalance = 24800.0,
                    nextDueDate = currentTime + 10 * dayMillis
                ),
                CustomerEntity(
                    id = 4,
                    name = "Pooja Retailers",
                    mobile = "9818765432",
                    email = "pooja.retail@gmail.com",
                    address = "Laxmi Nagar Metro Pillar 44",
                    state = "Delhi",
                    gstin = "07FGHJ7890N1Z3",
                    type = "CUSTOMER",
                    openingBalance = 0.0,
                    creditLimit = 30000.0,
                    currentBalance = 3200.0,
                    nextDueDate = currentTime + 3 * dayMillis
                ),
                CustomerEntity(
                    id = 5,
                    name = "Amul & Dairy Distributors",
                    mobile = "9958012345",
                    email = "amul.agency@delhi.com",
                    address = "Okhla Phase 2 Industrial Area",
                    state = "Delhi",
                    gstin = "07AAMUL0987P1Z5",
                    type = "SUPPLIER",
                    openingBalance = 0.0,
                    creditLimit = 150000.0,
                    currentBalance = 34500.0, // Payable to supplier
                    nextDueDate = currentTime + 8 * dayMillis
                ),
                CustomerEntity(
                    id = 6,
                    name = "Havells India Authorized Wholesale",
                    mobile = "9811223344",
                    email = "sales@havellswholesale.com",
                    address = "Bhagirath Palace, Chandni Chowk",
                    state = "Delhi",
                    gstin = "07HVL9999K1Z4",
                    type = "SUPPLIER",
                    openingBalance = 0.0,
                    creditLimit = 200000.0,
                    currentBalance = 42000.0, // Payable to supplier
                    nextDueDate = currentTime + 12 * dayMillis
                )
            )
            customerDao.insertAll(parties)
        }

        if (invoiceDao.getCount() == 0) {
            val currentTime = System.currentTimeMillis()
            val dayMillis = 86400000L

            val item1 = InvoiceItem(
                productId = 1,
                productName = "Basmati Rice 5kg Royal Premium",
                hsn = "10063020",
                quantity = 10.0,
                unit = "Bag",
                unitPrice = 460.0,
                gstRate = 5.0,
                discountPercent = 2.0,
                taxAmount = 225.4,
                totalAmount = 4733.4
            )
            val item2 = InvoiceItem(
                productId = 3,
                productName = "Havells 9W LED Cool Day Bulb (Pack of 2)",
                hsn = "85395000",
                quantity = 15.0,
                unit = "Box",
                unitPrice = 180.0,
                gstRate = 18.0,
                discountPercent = 0.0,
                taxAmount = 486.0,
                totalAmount = 3186.0
            )

            val invoice1Items = listOf(item1, item2)
            val invoice1Json = InvoiceItemConverter.toJson(invoice1Items)

            val invoices = listOf(
                InvoiceEntity(
                    id = 1,
                    invoiceNumber = "INV-2026-001",
                    type = "SALE",
                    customerId = 1,
                    partyName = "Rahul Traders",
                    partyMobile = "9810123456",
                    partyGstin = "07BPAR9876Q1Z2",
                    partyState = "Delhi",
                    date = currentTime - 2 * dayMillis,
                    dueDate = currentTime + 5 * dayMillis,
                    isGst = true,
                    subTotal = 7300.0,
                    discountPercent = 0.0,
                    discountAmount = 92.0,
                    cgstAmount = 355.7,
                    sgstAmount = 355.7,
                    igstAmount = 0.0,
                    taxAmount = 711.4,
                    shippingCharge = 100.0,
                    roundOff = 0.0,
                    grandTotal = 8019.4,
                    paidAmount = 5000.0,
                    dueAmount = 3019.4,
                    paymentMode = "UPI",
                    paymentStatus = "PARTIAL",
                    notes = "Partial payment received via PhonePe. Balance next week.",
                    itemsJson = invoice1Json
                ),
                InvoiceEntity(
                    id = 2,
                    invoiceNumber = "INV-2026-002",
                    type = "SALE",
                    customerId = 3,
                    partyName = "Verma Electricals & Hardware",
                    partyMobile = "9911987654",
                    partyGstin = "09DEPK4567M1Z1",
                    partyState = "Uttar Pradesh",
                    date = currentTime - dayMillis,
                    dueDate = currentTime + 10 * dayMillis,
                    isGst = true,
                    subTotal = 14990.0,
                    discountPercent = 5.0,
                    discountAmount = 749.5,
                    cgstAmount = 0.0,
                    sgstAmount = 0.0,
                    igstAmount = 2563.3,
                    taxAmount = 2563.3,
                    shippingCharge = 250.0,
                    roundOff = 0.2,
                    grandTotal = 17054.0,
                    paidAmount = 0.0,
                    dueAmount = 17054.0,
                    paymentMode = "CREDIT",
                    paymentStatus = "UNPAID",
                    notes = "IGST inter-state supply to UP.",
                    itemsJson = invoice1Json
                ),
                InvoiceEntity(
                    id = 3,
                    invoiceNumber = "INV-2026-003",
                    type = "SALE",
                    customerId = 2,
                    partyName = "Sharma Kirana Store",
                    partyMobile = "9871234567",
                    partyGstin = "07CPKL3456K1Z9",
                    partyState = "Delhi",
                    date = currentTime,
                    dueDate = currentTime + 7 * dayMillis,
                    isGst = true,
                    subTotal = 4600.0,
                    discountPercent = 0.0,
                    discountAmount = 0.0,
                    cgstAmount = 115.0,
                    sgstAmount = 115.0,
                    igstAmount = 0.0,
                    taxAmount = 230.0,
                    shippingCharge = 0.0,
                    roundOff = 0.0,
                    grandTotal = 4830.0,
                    paidAmount = 4830.0,
                    dueAmount = 0.0,
                    paymentMode = "CASH",
                    paymentStatus = "PAID",
                    notes = "Counter sale cash payment cleared.",
                    itemsJson = invoice1Json
                ),
                InvoiceEntity(
                    id = 4,
                    invoiceNumber = "PUR-2026-001",
                    type = "PURCHASE",
                    customerId = 6,
                    partyName = "Havells India Authorized Wholesale",
                    partyMobile = "9811223344",
                    partyGstin = "07HVL9999K1Z4",
                    partyState = "Delhi",
                    date = currentTime - 3 * dayMillis,
                    dueDate = currentTime + 12 * dayMillis,
                    isGst = true,
                    subTotal = 26000.0,
                    discountPercent = 0.0,
                    discountAmount = 0.0,
                    cgstAmount = 2340.0,
                    sgstAmount = 2340.0,
                    igstAmount = 0.0,
                    taxAmount = 4680.0,
                    shippingCharge = 500.0,
                    roundOff = 0.0,
                    grandTotal = 31180.0,
                    paidAmount = 15000.0,
                    dueAmount = 16180.0,
                    paymentMode = "BANK_TRANSFER",
                    paymentStatus = "PARTIAL",
                    notes = "Stock inward batch #HV-909.",
                    itemsJson = invoice1Json
                )
            )
            invoiceDao.insertAll(invoices)
        }

        if (khataDao.getCount() == 0) {
            val currentTime = System.currentTimeMillis()
            val dayMillis = 86400000L
            val transactions = listOf(
                KhataTransactionEntity(
                    id = 1,
                    partyId = 1,
                    partyName = "Rahul Traders",
                    partyType = "CUSTOMER",
                    type = "UDHAAR_GIVE",
                    amount = 8019.0,
                    date = currentTime - 2 * dayMillis,
                    description = "Goods delivered - Bill #INV-2026-001",
                    paymentMode = "CREDIT",
                    relatedInvoiceNumber = "INV-2026-001"
                ),
                KhataTransactionEntity(
                    id = 2,
                    partyId = 1,
                    partyName = "Rahul Traders",
                    partyType = "CUSTOMER",
                    type = "JAMA_GOT",
                    amount = 5000.0,
                    date = currentTime - dayMillis,
                    description = "Received part payment via UPI PhonePe",
                    paymentMode = "UPI",
                    relatedInvoiceNumber = "INV-2026-001"
                ),
                KhataTransactionEntity(
                    id = 3,
                    partyId = 2,
                    partyName = "Sharma Kirana Store",
                    partyType = "CUSTOMER",
                    type = "UDHAAR_GIVE",
                    amount = 9450.0,
                    date = currentTime - 5 * dayMillis,
                    description = "Weekly stock delivery",
                    paymentMode = "CREDIT",
                    relatedInvoiceNumber = ""
                )
            )
            khataDao.insertAll(transactions)
        }

        if (expenseDao.getCount() == 0) {
            val currentTime = System.currentTimeMillis()
            val dayMillis = 86400000L
            val expenses = listOf(
                ExpenseEntity(
                    id = 1,
                    title = "Shop Commercial Rent (Sep 2026)",
                    category = "Rent",
                    amount = 22000.0,
                    date = currentTime - 10 * dayMillis,
                    paymentMode = "BANK",
                    notes = "Paid via NEFT to landlord"
                ),
                ExpenseEntity(
                    id = 2,
                    title = "BSES Commercial Electricity Bill",
                    category = "Electricity",
                    amount = 4850.0,
                    date = currentTime - 4 * dayMillis,
                    paymentMode = "UPI",
                    notes = "Monthly shop power bill"
                ),
                ExpenseEntity(
                    id = 3,
                    title = "Helper Staff Salary (Sohan Lal)",
                    category = "Salary",
                    amount = 14000.0,
                    date = currentTime - 8 * dayMillis,
                    paymentMode = "CASH",
                    notes = "Advance cleared"
                ),
                ExpenseEntity(
                    id = 4,
                    title = "Tempo / Goods Delivery Freight",
                    category = "Transport",
                    amount = 1200.0,
                    date = currentTime - dayMillis,
                    paymentMode = "CASH",
                    notes = "Local market delivery tempo"
                ),
                ExpenseEntity(
                    id = 5,
                    title = "Airtel Fiber High-Speed Internet",
                    category = "Internet",
                    amount = 999.0,
                    date = currentTime - 6 * dayMillis,
                    paymentMode = "UPI",
                    notes = "Billing counter WiFi"
                ),
                ExpenseEntity(
                    id = 6,
                    title = "Customer Tea, Snacks & Refreshment",
                    category = "Tea & Snacks",
                    amount = 450.0,
                    date = currentTime,
                    paymentMode = "CASH",
                    notes = "Daily market chai"
                )
            )
            expenseDao.insertAll(expenses)
        }
    }
}
