package vcmsa.projects.pcv1.ui.expenses

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.R
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.data.CategoryRepository
import vcmsa.projects.pcv1.data.Expense
import vcmsa.projects.pcv1.data.ExpenseFilter
import vcmsa.projects.pcv1.data.ExpenseRepository
import vcmsa.projects.pcv1.databinding.FragmentExpensesBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var categoryRepository: CategoryRepository

    private var adapter: ExpenseAdapter? = null
    private var isAdapterInitialized = false


    private val viewModel: ExpensesViewModel by viewModels()
    private var currentExpenses: List<Expense>
        get() = viewModel.currentExpenses
        set(value) { viewModel.currentExpenses = value }
    private val currentUserId: Int = 1 // Replace with actual user logic


    private var selectedCategoryId: Int? = null
    private var startDate: Long? = null
    private var endDate: Long? = null
    private var minAmount: Double? = null
    private var maxAmount: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        expenseRepository = ExpenseRepository(db.expenseDao())
        categoryRepository = CategoryRepository(db.categoryDao())




        setupListener()
        setupSpinner()
        setupRecyclerView()

    }

    private fun setupSpinner() {
        val sortOptions = arrayOf("Newest First", "Oldest First", "Amount: Low to High", "Amount: High to Low")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = spinnerAdapter

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isAdapterInitialized) {
                    applySorting(position)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupRecyclerView() {
        viewLifecycleOwner.lifecycleScope.launch {
            val categories = categoryRepository.getCategoriesForUser(currentUserId)
            val categoryMap = categories.associate { it.id to it.name }

            adapter = ExpenseAdapter(emptyList(), categoryMap)
            binding.recyclerExpenses.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerExpenses.adapter = adapter
            isAdapterInitialized = true

            loadExpenses()
        }
    }

    private fun loadExpenses() {
        if (!isAdapterInitialized) return

        lifecycleScope.launch {
            val allExpenses = expenseRepository.getExpensesForUser(currentUserId)
            val filter = viewModel.currentFilter

            currentExpenses = if (filter == null) {
                allExpenses
            } else {
                allExpenses.filter { expense ->
                    (filter.categoryId == null || expense.categoryId == filter.categoryId) &&
                            (filter.startDate == null || expense.timestamp >= filter.startDate) &&
                            (filter.endDate == null || expense.timestamp <= filter.endDate) &&
                            (filter.minAmount == null || expense.amount >= filter.minAmount) &&
                            (filter.maxAmount == null || expense.amount <= filter.maxAmount)
                }
            }

            applySorting(binding.spinnerSort.selectedItemPosition)

            // Show/hide "Filters Active" message
            binding.textFiltersActive.visibility = if (filter != null) View.VISIBLE else View.GONE
        }
    }

    private fun applySorting(sortOption: Int) {
        if (!isAdapterInitialized) return

        val sortedExpenses = when (sortOption) {
            0 -> currentExpenses.sortedByDescending { it.timestamp }       // Newest First
            1 -> currentExpenses.sortedBy { it.timestamp }                 // Oldest First
            2 -> currentExpenses.sortedBy { it.amount }                    // Amount: Low to High
            3 -> currentExpenses.sortedByDescending { it.amount }         // Amount: High to Low
            else -> currentExpenses
        }

        val map = adapter?.currentCategoryMap ?: emptyMap()
        adapter?.updateData(sortedExpenses, map)

    }

    private fun setupListener(){
        binding.fabAddExpense.setOnClickListener {
            findNavController().navigate(R.id.addExpenseFragment)
        }

        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_expense_filter, null)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val textStartDate = dialogView.findViewById<TextView>(R.id.textStartDate)
        val textEndDate = dialogView.findViewById<TextView>(R.id.textEndDate)
        val editMinAmount = dialogView.findViewById<EditText>(R.id.editMinAmount)
        val editMaxAmount = dialogView.findViewById<EditText>(R.id.editMaxAmount)
        val btnApply = dialogView.findViewById<Button>(R.id.btnApplyFilter)
        val btnClear = dialogView.findViewById<Button>(R.id.btnClearFilter)

        val alertDialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Populate category spinner
        lifecycleScope.launch {
            val categories = categoryRepository.getCategoriesForUser(currentUserId)
            val categoryNames = mutableListOf("All Categories")
            categoryNames.addAll(categories.map { it.name })
            val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = spinnerAdapter

            categorySpinner.setSelection(
                selectedCategoryId?.let { id -> categories.indexOfFirst { it.id == id } + 1 } ?: 0
            )

            categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedCategoryId = if (position == 0) null else categories[position - 1].id
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        // Handle date pickers
        val datePicker = { setDateText: (Long) -> Unit, targetView: TextView ->
            val calendar = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar.set(year, month, day, 0, 0, 0)
                val timestamp = calendar.timeInMillis
                setDateText(timestamp)
                targetView.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
            }
            DatePickerDialog(requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        textStartDate.setOnClickListener {
            datePicker({ startDate = it }, textStartDate)
        }

        textEndDate.setOnClickListener {
            datePicker({ endDate = it }, textEndDate)
        }

        btnApply.setOnClickListener {
            minAmount = editMinAmount.text.toString().toDoubleOrNull()
            maxAmount = editMaxAmount.text.toString().toDoubleOrNull()

            // Store in ViewModel to persist filter across lifecycles
            viewModel.currentFilter = ExpenseFilter(
                categoryId = selectedCategoryId,
                startDate = startDate,
                endDate = endDate,
                minAmount = minAmount,
                maxAmount = maxAmount
            )

            binding.textFiltersActive.visibility = View.VISIBLE
            loadExpenses()  // This will re-filter based on ViewModel state
            alertDialog.dismiss()
        }


        btnClear.setOnClickListener {
            selectedCategoryId = null
            startDate = null
            endDate = null
            minAmount = null
            maxAmount = null
            viewModel.currentFilter = null  // Clear persistent filter

            binding.textFiltersActive.visibility = View.GONE
            loadExpenses()
            alertDialog.dismiss()
        }


        alertDialog.show()
    }

    private fun applyFilters() {
        val filtered = currentExpenses.filter { expense ->
            val matchCategory = selectedCategoryId == null || expense.categoryId == selectedCategoryId
            val matchDate = (startDate == null || expense.timestamp >= startDate!!) &&
                    (endDate == null || expense.timestamp <= endDate!!)
            val matchAmount = (minAmount == null || expense.amount >= minAmount!!) &&
                    (maxAmount == null || expense.amount <= maxAmount!!)
            matchCategory && matchDate && matchAmount
        }

        val sorted = when (binding.spinnerSort.selectedItemPosition) {
            0 -> filtered.sortedByDescending { it.timestamp }
            1 -> filtered.sortedBy { it.timestamp }
            2 -> filtered.sortedBy { it.amount }
            3 -> filtered.sortedByDescending { it.amount }
            else -> filtered
        }

        val map = adapter?.currentCategoryMap ?: emptyMap()
        adapter?.updateData(sorted, map)
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
