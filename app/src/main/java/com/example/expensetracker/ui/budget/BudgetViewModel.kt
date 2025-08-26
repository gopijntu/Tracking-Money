package com.example.expensetracker.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.BudgetRepository
import com.example.expensetracker.data.model.Budget
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetRepository
    val allBudgets: LiveData<List<Budget>>

    init {
        // TODO: Replace hardcoded passphrase with one from user's master password
        val database = AppDatabase.getDatabase(application, "test_password")
        val budgetDao = database.budgetDao()
        repository = BudgetRepository(budgetDao)
        allBudgets = repository.allBudgets
    }

    fun insert(budget: Budget) = viewModelScope.launch {
        repository.insert(budget)
    }

    fun getBudget(category: String, monthYear: String): LiveData<Budget> {
        return repository.getBudget(category, monthYear)
    }
}
