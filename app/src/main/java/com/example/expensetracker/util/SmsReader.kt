package com.example.expensetracker.util

import android.content.ContentResolver
import android.provider.Telephony
import com.example.expensetracker.data.model.Transaction
import java.util.regex.Pattern

class SmsReader(private val contentResolver: ContentResolver) {

    private val transactionKeywords = listOf("debit", "debited", "spent", "charged", "paid")
    private val amountPattern = Pattern.compile("Rs\\.?\\s?([0-9,]+\\.?[0-9]*)")
    private val keywordMapping = mapOf(
        "MEDICAL" to listOf("pharmacy", "apollo", "medplus", "netmeds", "practo", "hospital", "clinic", "medicine"),
        "MOVIES" to listOf("bookmyshow", "inox", "pvr", "carnival", "paytm movies", "ticketnew"),
        "GROCERIES" to listOf("dmart", "reliance fresh", "zepto", "blinkit", "bigbasket", "amazon", "flipkart", "myntra", "meesho", "ajio")
    )

    fun readSms(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            null,
            null,
            Telephony.Sms.DEFAULT_SORT_ORDER
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                do {
                    val smsBody = it.getString(bodyIndex).lowercase()
                    val smsDate = it.getLong(dateIndex)

                    if (transactionKeywords.any { keyword -> smsBody.contains(keyword) }) {
                        val amountMatcher = amountPattern.matcher(smsBody)
                        if (amountMatcher.find()) {
                            val amount = amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                            val (category, merchant) = categorize(smsBody)
                            transactions.add(
                                Transaction(
                                    date = smsDate,
                                    amount = amount,
                                    category = category,
                                    merchant = merchant,
                                    smsSnippet = smsBody.take(100)
                                )
                            )
                        }
                    }
                } while (it.moveToNext())
            }
        }
        return transactions.distinctBy { it.smsSnippet } // Handle duplicate SMS
    }

    private fun categorize(smsBody: String): Pair<String, String> {
        for ((category, keywords) in keywordMapping) {
            for (keyword in keywords) {
                if (smsBody.contains(keyword)) {
                    return Pair(category, keyword)
                }
            }
        }
        return Pair("OTHERS", "Unknown")
    }
}
