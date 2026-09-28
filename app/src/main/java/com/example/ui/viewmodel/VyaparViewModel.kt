package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.BusinessAiService
import com.example.ai.BusinessSnapshot
import com.example.data.local.AppDatabase
import com.example.data.local.InvoiceItemConverter
import com.example.data.local.SampleData
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItem
import com.example.data.local.entity.KhataTransactionEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.repository.DashboardMetrics
import com.example.data.repository.VyaparRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen {
    DASHBOARD,
    BILLING,
    POS,
    INVENTORY,
    KHATA,
    REPORTS,
    AI_ASSISTANT,
    SETTINGS
}

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class VyaparViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = VyaparRepository(database)

    // Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Screen navigation history for BackHandler
    private val screenStack = mutableListOf(AppScreen.DASHBOARD)

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
            _currentScreen.value = screenStack.last()
            return true
        }
        return false
    }

    // PIN lock state
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    fun unlockApp(pin: String, correctPin: String): Boolean {
        return if (pin == correctPin) {
            _isUnlocked.value = true
            true
        } else {
            false
        }
    }

    // Reactive Data
    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val parties: StateFlow<List<CustomerEntity>> = repository.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<BusinessProfileEntity?> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Selection for detail views
    val selectedInvoice = MutableStateFlow<InvoiceEntity?>(null)
    val selectedParty = MutableStateFlow<CustomerEntity?>(null)
    val partyTransactions = MutableStateFlow<List<KhataTransactionEntity>>(emptyList())

    // AI Chat
    private val _aiMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "AI",
                text = "Namaste! Main hoon Vyapar AI, aapka digital business assistant. Aap mujhse aaj ki sale, profit, pending udhaar, ya stock updates ke baare mein kuch bhi pooch sakte hain."
            )
        )
    )
    val aiMessages: StateFlow<List<ChatMessage>> = _aiMessages.asStateFlow()
    val isAiThinking = MutableStateFlow(false)

    // Live Metrics computation
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        products,
        parties,
        invoices,
        expenses
    ) { prods, pts, invs, exps ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis

        var todaySales = 0.0
        var todayPurchases = 0.0
        var todayExpenses = 0.0
        var cashBalance = 0.0
        var bankUpiBalance = 0.0
        var pendingInvoicesCount = 0

        for (inv in invs) {
            val isToday = inv.date >= todayStart
            if (inv.type == "SALE") {
                if (isToday) todaySales += inv.grandTotal
                if (inv.paymentStatus != "PAID") pendingInvoicesCount++

                // Cash / Bank split
                if (inv.paymentMode == "CASH") cashBalance += inv.paidAmount
                else bankUpiBalance += inv.paidAmount
            } else if (inv.type == "PURCHASE") {
                if (isToday) todayPurchases += inv.grandTotal
                if (inv.paymentMode == "CASH") cashBalance -= inv.paidAmount
                else bankUpiBalance -= inv.paidAmount
            }
        }

        for (exp in exps) {
            if (exp.date >= todayStart) todayExpenses += exp.amount
            if (exp.paymentMode == "CASH") cashBalance -= exp.amount
            else bankUpiBalance -= exp.amount
        }

        // Customer udhaar vs Supplier payable
        var totalReceivable = 0.0
        var totalPayable = 0.0
        var overdueCount = 0
        val now = System.currentTimeMillis()

        for (party in pts) {
            if (party.type == "CUSTOMER") {
                if (party.currentBalance > 0) {
                    totalReceivable += party.currentBalance
                    if (party.nextDueDate in 1..<now) overdueCount++
                }
            } else {
                if (party.currentBalance > 0) {
                    totalPayable += party.currentBalance
                }
            }
        }

        val lowStockCount = prods.count { it.currentStock <= it.minStock }
        // Rough estimate profit margin ~20% of sales minus expenses
        val estimatedGrossProfit = todaySales * 0.20
        val todayProfit = (estimatedGrossProfit - todayExpenses).coerceAtLeast(0.0)

        DashboardMetrics(
            todaySales = todaySales,
            todayPurchases = todayPurchases,
            todayExpenses = todayExpenses,
            todayProfit = todayProfit,
            totalReceivable = totalReceivable,
            totalPayable = totalPayable,
            cashBalance = cashBalance.coerceAtLeast(0.0) + 12500.0, // baseline shop cash float
            bankUpiBalance = bankUpiBalance.coerceAtLeast(0.0) + 48200.0,
            lowStockCount = lowStockCount,
            pendingInvoiceCount = pendingInvoicesCount,
            overdueDebtorsCount = overdueCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    init {
        viewModelScope.launch {
            SampleData.seedIfEmpty(database)
        }
    }

    // Party selection
    fun selectParty(party: CustomerEntity) {
        selectedParty.value = party
        viewModelScope.launch {
            repository.getTransactionsForParty(party.id).collect {
                partyTransactions.value = it
            }
        }
    }

    // Product actions
    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            if (product.id == 0L) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
        }
    }

    fun adjustStock(id: Long, delta: Double) {
        viewModelScope.launch {
            repository.adjustProductStock(id, delta)
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    // Party actions
    fun saveParty(party: CustomerEntity) {
        viewModelScope.launch {
            if (party.id == 0L) {
                repository.insertParty(party)
            } else {
                repository.updateParty(party)
            }
        }
    }

    fun recordKhataEntry(
        party: CustomerEntity,
        type: String,
        amount: Double,
        mode: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordKhataPayment(party, type, amount, mode, notes)
            // Refresh selected party
            val updated = repository.getPartyById(party.id)
            if (updated != null) {
                selectedParty.value = updated
            }
        }
    }

    // Invoice actions
    fun createInvoice(
        type: String,
        party: CustomerEntity?,
        partyName: String,
        partyMobile: String,
        partyGstin: String,
        partyState: String,
        items: List<InvoiceItem>,
        discountPercent: Double,
        discountAmount: Double,
        shippingCharge: Double,
        roundOff: Double,
        paidAmount: Double,
        paymentMode: String,
        isGst: Boolean,
        notes: String,
        onSuccess: (InvoiceEntity) -> Unit
    ) {
        viewModelScope.launch {
            val invoiceNumber = repository.generateNextInvoiceNumber(type)
            val subTotal = items.sumOf { it.unitPrice * it.quantity }
            var cgst = 0.0
            var sgst = 0.0
            var igst = 0.0

            val isInterState = partyState.isNotBlank() && !partyState.equals("Delhi", ignoreCase = true)

            for (item in items) {
                val itemSub = item.unitPrice * item.quantity * (1.0 - item.discountPercent / 100.0)
                val itemTax = itemSub * (item.gstRate / 100.0)
                if (isInterState) {
                    igst += itemTax
                } else {
                    cgst += itemTax / 2.0
                    sgst += itemTax / 2.0
                }
            }

            val totalTax = if (isGst) (cgst + sgst + igst) else 0.0
            val grandTotal = (subTotal - discountAmount + totalTax + shippingCharge + roundOff).coerceAtLeast(0.0)
            val due = (grandTotal - paidAmount).coerceAtLeast(0.0)
            val status = when {
                due <= 0.5 -> "PAID"
                paidAmount > 0.0 -> "PARTIAL"
                else -> "UNPAID"
            }

            val invoice = InvoiceEntity(
                invoiceNumber = invoiceNumber,
                type = type,
                customerId = party?.id ?: 0L,
                partyName = if (partyName.isNotBlank()) partyName else party?.name ?: "Cash Customer",
                partyMobile = if (partyMobile.isNotBlank()) partyMobile else party?.mobile ?: "",
                partyGstin = if (partyGstin.isNotBlank()) partyGstin else party?.gstin ?: "",
                partyState = if (partyState.isNotBlank()) partyState else party?.state ?: "Delhi",
                date = System.currentTimeMillis(),
                dueDate = System.currentTimeMillis() + (7L * 86400000L),
                isGst = isGst,
                subTotal = subTotal,
                discountPercent = discountPercent,
                discountAmount = discountAmount,
                cgstAmount = if (isGst) cgst else 0.0,
                sgstAmount = if (isGst) sgst else 0.0,
                igstAmount = if (isGst) igst else 0.0,
                taxAmount = totalTax,
                shippingCharge = shippingCharge,
                roundOff = roundOff,
                grandTotal = grandTotal,
                paidAmount = paidAmount,
                dueAmount = due,
                paymentMode = paymentMode,
                paymentStatus = status,
                notes = notes,
                itemsJson = InvoiceItemConverter.toJson(items)
            )

            val id = repository.createInvoice(invoice, items)
            val savedInvoice = invoice.copy(id = id)
            selectedInvoice.value = savedInvoice
            onSuccess(savedInvoice)
        }
    }

    // Expense actions
    fun addExpense(
        title: String,
        category: String,
        amount: Double,
        mode: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntity(
                    title = title,
                    category = category,
                    amount = amount,
                    paymentMode = mode,
                    notes = notes
                )
            )
        }
    }

    // Profile actions
    fun updateBusinessProfile(updated: BusinessProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(updated)
        }
    }

    // AI Assistant Query
    fun askAiAssistant(query: String) {
        if (query.isBlank()) return
        val userMsg = ChatMessage(sender = "USER", text = query)
        _aiMessages.value = _aiMessages.value + userMsg
        isAiThinking.value = true

        viewModelScope.launch {
            val metrics = dashboardMetrics.value
            val prods = products.value
            val debtorsList = parties.value
                .filter { it.type == "CUSTOMER" && it.currentBalance > 0 }
                .sortedByDescending { it.currentBalance }
                .take(5)
                .map { Pair(it.name, it.currentBalance) }

            val lowStockNames = prods.filter { it.currentStock <= it.minStock }.map { it.name }.take(5)

            val snapshot = BusinessSnapshot(
                todaySales = metrics.todaySales,
                todayPurchases = metrics.todayPurchases,
                todayExpenses = metrics.todayExpenses,
                todayProfit = metrics.todayProfit,
                totalReceivable = metrics.totalReceivable,
                totalPayable = metrics.totalPayable,
                lowStockCount = metrics.lowStockCount,
                lowStockItems = lowStockNames,
                topDebtors = debtorsList,
                recentSalesCount = invoices.value.count { it.type == "SALE" }
            )

            val aiAnswer = BusinessAiService.answerBusinessQuery(query, snapshot)
            isAiThinking.value = false
            _aiMessages.value = _aiMessages.value + ChatMessage(sender = "AI", text = aiAnswer)
        }
    }
}
