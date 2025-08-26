package com.example.expensetracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.TransactionRepository
import java.util.Calendar

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    val todaySpend: LiveData<Double>
    val monthlySpend: LiveData<Double>

    init {
        // TODO: Replace hardcoded passphrase
        val database = AppDatabase.getDatabase(application, "test_password")
        val transactionDao = database.transactionDao()
        repository = TransactionRepository(transactionDao)

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        todaySpend = repository.getSpendSince(todayStart)

        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        monthlySpend = repository.allTransactions.map { transactions ->
            transactions.filter { it.date >= monthStart }.sumOf { it.amount }
        }
    }
}
