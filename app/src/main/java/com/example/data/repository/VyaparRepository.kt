package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.InvoiceItemConverter
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItem
import com.example.data.local.entity.KhataTransactionEntity
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

data class DashboardMetrics(
    val todaySales: Double = 0.0,
    val todayPurchases: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val todayProfit: Double = 0.0,
    val totalReceivable: Double = 0.0,
    val totalPayable: Double = 0.0,
    val cashBalance: Double = 0.0,
    val bankUpiBalance: Double = 0.0,
    val lowStockCount: Int = 0,
    val pendingInvoiceCount: Int = 0,
    val overdueDebtorsCount: Int = 0
)

class VyaparRepository(private val database: AppDatabase) {
    private val productDao = database.productDao()
    private val customerDao = database.customerDao()
    private val invoiceDao = database.invoiceDao()
    private val khataDao = database.khataDao()
    private val expenseDao = database.expenseDao()
    private val profileDao = database.businessProfileDao()

    // Products
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    suspend fun getProductByBarcode(barcode: String): ProductEntity? =
        productDao.getProductByBarcode(barcode)

    suspend fun insertProduct(product: ProductEntity): Long =
        productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) =
        productDao.updateProduct(product)

    suspend fun adjustProductStock(id: Long, delta: Double) =
        productDao.adjustStock(id, delta)

    suspend fun deleteProduct(id: Long) =
        productDao.deleteProduct(id)

    // Parties & Khata
    val allParties: Flow<List<CustomerEntity>> = customerDao.getAllParties()
    val customers: Flow<List<CustomerEntity>> = customerDao.getPartiesByType("CUSTOMER")
    val suppliers: Flow<List<CustomerEntity>> = customerDao.getPartiesByType("SUPPLIER")
    val debtors: Flow<List<CustomerEntity>> = customerDao.getDebtors()

    suspend fun getPartyById(id: Long): CustomerEntity? =
        customerDao.getPartyById(id)

    suspend fun insertParty(customer: CustomerEntity): Long =
        customerDao.insertParty(customer)

    suspend fun updateParty(customer: CustomerEntity) =
        customerDao.updateParty(customer)

    suspend fun deleteParty(id: Long) =
        customerDao.deleteParty(id)

    // Invoices
    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()
    val salesInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getInvoicesByType("SALE")
    val pendingInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getPendingInvoices()

    suspend fun getInvoiceById(id: Long): InvoiceEntity? =
        invoiceDao.getInvoiceById(id)

    suspend fun deleteInvoice(id: Long) =
        invoiceDao.deleteInvoice(id)

    suspend fun generateNextInvoiceNumber(type: String): String {
        val count = invoiceDao.getCountByType(type) + 1
        val prefix = if (type == "PURCHASE") "PUR-2026-" else if (type == "QUOTATION") "QT-2026-" else "INV-2026-"
        return "$prefix%03d".format(count)
    }

    suspend fun createInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItem>
    ): Long {
        val invoiceId = invoiceDao.insertInvoice(invoice)

        // Automatic Stock adjustment
        for (item in items) {
            if (item.productId > 0) {
                if (invoice.type == "SALE" || invoice.type == "DELIVERY_CHALLAN") {
                    productDao.adjustStock(item.productId, -item.quantity)
                } else if (invoice.type == "PURCHASE") {
                    productDao.adjustStock(item.productId, item.quantity)
                }
            }
        }

        // Automatic Khata / Ledger ledgering if customer linked
        if (invoice.customerId > 0) {
            if (invoice.type == "SALE") {
                // If there's an unpaid amount, add to customer udhaar balance
                if (invoice.dueAmount > 0) {
                    customerDao.updateBalance(invoice.customerId, invoice.dueAmount)
                    khataDao.insertTransaction(
                        KhataTransactionEntity(
                            partyId = invoice.customerId,
                            partyName = invoice.partyName,
                            partyType = "CUSTOMER",
                            type = "UDHAAR_GIVE",
                            amount = invoice.dueAmount,
                            date = invoice.date,
                            description = "Bill #${invoice.invoiceNumber} Pending Due",
                            paymentMode = invoice.paymentMode,
                            relatedInvoiceNumber = invoice.invoiceNumber
                        )
                    )
                }
            } else if (invoice.type == "PURCHASE") {
                if (invoice.dueAmount > 0) {
                    customerDao.updateBalance(invoice.customerId, invoice.dueAmount)
                    khataDao.insertTransaction(
                        KhataTransactionEntity(
                            partyId = invoice.customerId,
                            partyName = invoice.partyName,
                            partyType = "SUPPLIER",
                            type = "UDHAAR_GIVE",
                            amount = invoice.dueAmount,
                            date = invoice.date,
                            description = "Purchase Bill #${invoice.invoiceNumber} Payable",
                            paymentMode = invoice.paymentMode,
                            relatedInvoiceNumber = invoice.invoiceNumber
                        )
                    )
                }
            }
        }

        return invoiceId
    }

    // Khata entries
    val allKhataTransactions: Flow<List<KhataTransactionEntity>> = khataDao.getAllTransactions()

    fun getTransactionsForParty(partyId: Long): Flow<List<KhataTransactionEntity>> =
        khataDao.getTransactionsForParty(partyId)

    suspend fun recordKhataPayment(
        party: CustomerEntity,
        type: String, // "JAMA_GOT" or "UDHAAR_GIVE"
        amount: Double,
        mode: String,
        notes: String
    ) {
        khataDao.insertTransaction(
            KhataTransactionEntity(
                partyId = party.id,
                partyName = party.name,
                partyType = party.type,
                type = type,
                amount = amount,
                date = System.currentTimeMillis(),
                description = notes.ifBlank { if (type == "JAMA_GOT") "Payment Received (Jama)" else "Credit Given (Udhaar)" },
                paymentMode = mode
            )
        )

        // If Jama received from customer: decreases customer's due balance
        // If Udhaar given to customer: increases customer's due balance
        // If Jama paid to supplier: decreases supplier's payable balance
        // If Udhaar taken from supplier: increases supplier's payable balance
        val delta = if (type == "JAMA_GOT") -amount else amount
        customerDao.updateBalance(party.id, delta)
    }

    // Expenses
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    suspend fun insertExpense(expense: ExpenseEntity): Long =
        expenseDao.insertExpense(expense)

    suspend fun deleteExpense(id: Long) =
        expenseDao.deleteExpense(id)

    // Business Profile
    val profileFlow: Flow<BusinessProfileEntity?> = profileDao.getProfileListFlow().map { it.firstOrNull() }

    suspend fun getProfile(): BusinessProfileEntity {
        return profileDao.getProfile() ?: run {
            val defaultProfile = BusinessProfileEntity()
            profileDao.insertOrUpdate(defaultProfile)
            defaultProfile
        }
    }

    suspend fun updateProfile(profile: BusinessProfileEntity) =
        profileDao.updateProfile(profile)
}
