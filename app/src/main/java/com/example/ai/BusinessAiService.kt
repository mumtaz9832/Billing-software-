package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class BusinessSnapshot(
    val todaySales: Double,
    val todayPurchases: Double,
    val todayExpenses: Double,
    val todayProfit: Double,
    val totalReceivable: Double,
    val totalPayable: Double,
    val lowStockCount: Int,
    val lowStockItems: List<String>,
    val topDebtors: List<Pair<String, Double>>,
    val recentSalesCount: Int
)

object BusinessAiService {
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    suspend fun answerBusinessQuery(
        query: String,
        snapshot: BusinessSnapshot
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        val hasValidKey = apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY")

        if (hasValidKey) {
            try {
                val response = callGeminiApi(query, snapshot, apiKey)
                if (response.isNotBlank()) return@withContext response
            } catch (e: Exception) {
                // Fallback to local intelligent analysis engine
            }
        }

        // Fast & reliable local analytics rule engine based on live database data
        generateLocalInsight(query, snapshot)
    }

    private fun callGeminiApi(
        query: String,
        snapshot: BusinessSnapshot
    , apiKey: String): String {
        val systemInstruction = """
            You are 'Vyapar AI', an expert Indian business, GST billing, and inventory advisor.
            You have real-time access to the shop's ledger database:
            - Today's Sales: ₹${"%,.2f".format(snapshot.todaySales)}
            - Today's Purchases: ₹${"%,.2f".format(snapshot.todayPurchases)}
            - Today's Expenses: ₹${"%,.2f".format(snapshot.todayExpenses)}
            - Today's Net Profit: ₹${"%,.2f".format(snapshot.todayProfit)}
            - Market Udhaar (Total Receivable): ₹${"%,.2f".format(snapshot.totalReceivable)}
            - Total Supplier Payable: ₹${"%,.2f".format(snapshot.totalPayable)}
            - Low Stock Products Count: ${snapshot.lowStockCount} (${snapshot.lowStockItems.joinToString(", ")})
            - Top Overdue Debtors: ${snapshot.topDebtors.joinToString { "${it.first}: ₹${"%,.0f".format(it.second)}" }}

            Respond helpfully, politely, and concisely in natural Hinglish or English (as prompted by user).
            Use Indian Rupee (₹) symbol, bullet points, and actionable tips for sales growth and collection.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray()
            val contentObj = JSONObject().apply {
                val partsArray = JSONArray()
                partsArray.put(JSONObject().put("text", "System: $systemInstruction\n\nUser Question: $query"))
                put("parts", partsArray)
            }
            contentsArray.put(contentObj)
            put("contents", contentsArray)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseString = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseString)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
        }
        return ""
    }

    private fun generateLocalInsight(
        query: String,
        snapshot: BusinessSnapshot
    ): String {
        val q = query.lowercase()

        return when {
            q.contains("sale") || q.contains("bikri") || q.contains("sell") -> {
                "📊 **Sales Summary**\n\n" +
                        "• **Aaj ki Total Sales:** ₹${"%,.2f".format(snapshot.todaySales)}\n" +
                        "• **Recent Invoices Created:** ${snapshot.recentSalesCount}\n\n" +
                        (if (snapshot.todaySales > 0) "✅ Counter par achha customer flow hai!" else "💡 Tip: Subah ke orders aur WhatsApp payment reminders bhejne se sales improve ho sakti hain.")
            }
            q.contains("profit") || q.contains("munafa") || q.contains("fayda") -> {
                "💰 **Profit & Loss Overview**\n\n" +
                        "• **Aaj ki Gross Sales:** ₹${"%,.2f".format(snapshot.todaySales)}\n" +
                        "• **Aaj ke Kharch (Expenses):** ₹${"%,.2f".format(snapshot.todayExpenses)}\n" +
                        "• **Estimated Net Profit:** ₹${"%,.2f".format(snapshot.todayProfit)}\n\n" +
                        "Formula: (Gross Margin - Direct Shop Expenses) = Net Profit."
            }
            q.contains("market") || q.contains("udhaar") || q.contains("receivable") || q.contains("paise") || q.contains("due") -> {
                val debtorsList = if (snapshot.topDebtors.isNotEmpty()) {
                    snapshot.topDebtors.joinToString("\n") { "  • ${it.first}: ₹${"%,.0f".format(it.second)}" }
                } else {
                    "  • Filhaal koi pending udhaar nahi hai."
                }
                "👥 **Market Udhaar (Receivables)**\n\n" +
                        "• **Kul Market Udhaar:** ₹${"%,.2f".format(snapshot.totalReceivable)}\n" +
                        "• **Sabse bade Udhaar wale Customers:**\n$debtorsList\n\n" +
                        "📲 Tip: Khata tab se in sabhi customers ko ek click mein WhatsApp payment reminder bhej sakte hain!"
            }
            q.contains("stock") || q.contains("item") || q.contains("khatam") || q.contains("inventory") || q.contains("low") -> {
                if (snapshot.lowStockCount > 0) {
                    val items = snapshot.lowStockItems.joinToString("\n") { "  ⚠️ $it" }
                    "📦 **Low Stock Alert!**\n\n" +
                            "Aapke **${snapshot.lowStockCount} items** minimum stock se kam hain:\n$items\n\n" +
                            "🔔 Inhe jald se jald Supplier se reorder karein taaki counter par customer ko mana na karna pade."
                } else {
                    "📦 **Stock Status Normal**\n\nSabhi products safe minimum level par hain. Kisi item ka out-of-stock risk nahi hai."
                }
            }
            q.contains("kharch") || q.contains("expense") -> {
                "📈 **Expenses Tracker**\n\n" +
                        "• **Aaj ke Shop Kharch:** ₹${"%,.2f".format(snapshot.todayExpenses)}\n" +
                        "Shop rent, electricity aur salaries Accounts tab mein track hoti hain."
            }
            else -> {
                "🤖 **Vyapar Business Assistant Insight**\n\n" +
                        "Aapke business ka taaza update:\n" +
                        "• **Aaj ki Sale:** ₹${"%,.2f".format(snapshot.todaySales)}\n" +
                        "• **Aaj ka Profit:** ₹${"%,.2f".format(snapshot.todayProfit)}\n" +
                        "• **Market mein Udhaar:** ₹${"%,.2f".format(snapshot.totalReceivable)}\n" +
                        "• **Supplier Payable:** ₹${"%,.2f".format(snapshot.totalPayable)}\n" +
                        "• **Low Stock Items:** ${snapshot.lowStockCount}\n\n" +
                        "Aap mujhse sales, profit, udhaar recovery, ya low stock ke baare mein kuch bhi pooch sakte hain!"
            }
        }
    }
}
