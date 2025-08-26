package com.example.expensetracker.ui.budget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.data.model.Budget
import com.example.expensetracker.databinding.ItemBudgetBinding

class BudgetAdapter(
    private val onEdit: (Budget) -> Unit
) : ListAdapter<Budget, BudgetAdapter.BudgetViewHolder>(BudgetDiffCallback()) {

    private var spentAmounts: Map<String, Double> = emptyMap()

    fun setSpentAmounts(spentAmounts: Map<String, Double>) {
        this.spentAmounts = spentAmounts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = getItem(position)
        holder.bind(budget)
    }

    inner class BudgetViewHolder(private val binding: ItemBudgetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(budget: Budget) {
            val spentAmount = spentAmounts[budget.category] ?: 0.0
            val progress = (spentAmount / budget.limitAmount * 100).toInt()

            binding.textViewCategoryName.text = budget.category
            binding.textViewBudgetAmount.text = String.format("₹%.2f / ₹%.2f", spentAmount, budget.limitAmount)
            binding.progressBarBudget.progress = progress

            val context = binding.root.context
            val progressDrawable = when {
                progress < 50 -> ContextCompat.getDrawable(context, R.drawable.progress_bar_green)
                progress < 80 -> ContextCompat.getDrawable(context, R.drawable.progress_bar_yellow)
                else -> ContextCompat.getDrawable(context, R.drawable.progress_bar_red)
            }
            binding.progressBarBudget.progressDrawable = progressDrawable

            itemView.setOnClickListener { onEdit(budget) }
        }
    }

    class BudgetDiffCallback : DiffUtil.ItemCallback<Budget>() {
        override fun areItemsTheSame(oldItem: Budget, newItem: Budget): Boolean {
            return oldItem.category == newItem.category && oldItem.monthYear == newItem.monthYear
        }

        override fun areContentsTheSame(oldItem: Budget, newItem: Budget): Boolean {
            return oldItem == newItem
        }
    }
}
