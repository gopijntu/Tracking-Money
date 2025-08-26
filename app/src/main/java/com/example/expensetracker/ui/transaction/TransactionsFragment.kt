package com.example.expensetracker.ui.transaction

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.data.model.Transaction
import com.example.expensetracker.databinding.FragmentTransactionsBinding
import com.example.expensetracker.util.SmsReader
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter

    private val createFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                exportTransactionsToCsv(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupFab()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_transactions, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export_csv -> {
                launchCreateFile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun launchCreateFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "transactions_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv")
        }
        createFileLauncher.launch(intent)
    }

    private fun exportTransactionsToCsv(uri: android.net.Uri) {
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.bufferedWriter().use { writer ->
                    writer.appendLine("ID,Date,Amount,Category,Merchant,SMS Snippet")
                    viewModel.allTransactions.value?.forEach { transaction ->
                        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(transaction.date))
                        writer.appendLine("${transaction.id},${formattedDate},${transaction.amount},${transaction.category},\"${transaction.merchant}\",\"${transaction.smsSnippet.replace("\"", "\"\"")}\"")
                    }
                }
                Snackbar.make(binding.root, "Exported to CSV successfully.", Snackbar.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            Snackbar.make(binding.root, "Failed to export to CSV.", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onEdit = { /* Handle edit */ },
            onDelete = { viewModel.deleteById(it.id) }
        )
        binding.recyclerViewTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(context)
        }
        setupSwipeToDelete()
    }

    private fun observeViewModel() {
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            transactionAdapter.submitList(transactions)
        }
    }

    private fun setupFab() {
        binding.fabRefresh.setOnClickListener {
            scanSms()
        }
    }

    private fun scanSms() {
        val smsReader = SmsReader(requireActivity().contentResolver)
        val transactions = smsReader.readSms()
        transactions.forEach { viewModel.insert(it) }
        Snackbar.make(binding.root, "Scanned ${transactions.size} new transactions.", Snackbar.LENGTH_LONG).show()
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val transaction = transactionAdapter.currentList[position]
                viewModel.deleteById(transaction.id)
                Snackbar.make(binding.root, "Transaction deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") { viewModel.insert(transaction) }
                    .show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewTransactions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
