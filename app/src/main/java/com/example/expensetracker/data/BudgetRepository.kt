package com.example.expensetracker.data

import androidx.lifecycle.LiveData
import com.example.expensetracker.data.model.Budget

class BudgetRepository(private val budgetDao: BudgetDao) {

    val allBudgets: LiveData<List<Budget>> = budgetDao.getAllBudgets()

    suspend fun insert(budget: Budget) {
        budgetDao.insert(budget)
    }

    fun getBudget(category: String, monthYear: String): LiveData<Budget> {
        return budgetDao.getBudget(category, monthYear)
    }
}
