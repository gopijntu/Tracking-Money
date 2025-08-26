package com.example.expensetracker.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.data.model.Budget
import com.example.expensetracker.databinding.FragmentBudgetsBinding
import com.example.expensetracker.ui.transaction.TransactionViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BudgetsFragment : Fragment() {

    private var _binding: FragmentBudgetsBinding? = null
    private val binding get() = _binding!!

    private val budgetViewModel: BudgetViewModel by viewModels()
    private val transactionViewModel: TransactionViewModel by viewModels()
    private lateinit var budgetAdapter: BudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModels()
        setupAddBudgetButton()
    }

    private fun setupRecyclerView() {
        budgetAdapter = BudgetAdapter { budget -> showSetBudgetDialog(budget) }
        binding.recyclerViewBudgets.apply {
            adapter = budgetAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private val alertedCategories = mutableSetOf<String>()

    private fun observeViewModels() {
        budgetViewModel.allBudgets.observe(viewLifecycleOwner) { budgets ->
            budgetAdapter.submitList(budgets)
            checkBudgetThresholds(budgets, transactionViewModel.allTransactions.value ?: emptyList())
        }

        transactionViewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            val spentAmounts = transactions.groupBy { it.category }
                .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            budgetAdapter.setSpentAmounts(spentAmounts)
            checkBudgetThresholds(budgetViewModel.allBudgets.value ?: emptyList(), transactions)
        }
    }

    private fun checkBudgetThresholds(budgets: List<Budget>, transactions: List<com.example.expensetracker.data.model.Transaction>) {
        val spentAmounts = transactions.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }

        for (budget in budgets) {
            val spentAmount = spentAmounts[budget.category] ?: 0.0
            val percentage = (spentAmount / budget.limitAmount * 100)

            if (percentage >= 100 && !alertedCategories.contains(budget.category + "_100")) {
                showAlert(budget.category, "100%")
                alertedCategories.add(budget.category + "_100")
            } else if (percentage >= 80 && !alertedCategories.contains(budget.category + "_80")) {
                showSnackbar(budget.category, "80%")
                alertedCategories.add(budget.category + "_80")
            }
        }
    }

    private fun showSnackbar(category: String, threshold: String) {
        Snackbar.make(binding.root, "⚠️ You've spent $threshold of your $category budget.", Snackbar.LENGTH_LONG).show()
    }

    private fun showAlert(category: String, threshold: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Budget Alert")
            .setMessage("You have exceeded your $category budget.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupAddBudgetButton() {
        binding.buttonAddBudget.setOnClickListener {
            showSetBudgetDialog(null)
        }
    }

    private fun showSetBudgetDialog(budget: Budget?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_set_budget, null)
        val categoryEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_category)
        val amountEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_amount)

        if (budget != null) {
            categoryEditText.setText(budget.category)
            categoryEditText.isEnabled = false
            amountEditText.setText(budget.limitAmount.toString())
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (budget == null) "Set New Budget" else "Adjust Budget")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val category = categoryEditText.text.toString()
                val amount = amountEditText.text.toString().toDoubleOrNull()
                if (category.isNotBlank() && amount != null) {
                    val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                    budgetViewModel.insert(Budget(category, amount, monthYear))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
