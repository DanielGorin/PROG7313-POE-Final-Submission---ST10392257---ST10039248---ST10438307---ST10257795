package vcmsa.projects.pcv1.ui.expenses

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.R
import vcmsa.projects.pcv1.data.*
import vcmsa.projects.pcv1.databinding.FragmentExpensesBinding
import vcmsa.projects.pcv1.util.SessionManager
import java.text.SimpleDateFormat
import java.util.*
/**
 * Fragment to display a list of user expenses with filtering, sorting, and navigation to expense details.
 */
class ExpensesFragment : Fragment() {
    // ViewBinding variable, nullified on view destroy to avoid memory leaks
    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    // Repository instances for accessing expense and category data
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var categoryRepository: CategoryRepository
    private var adapter: ExpenseAdapter? = null
    private var isAdapterInitialized = false

    // Use real user ID, loaded in onViewCreated
    private var currentUserId: Int = -1
    // ViewModel instance scoped to this fragment for managing UI-related data
    private val viewModel: ExpensesViewModel by viewModels()
    // Local access to currentExpenses in the ViewModel with getter/setter
    private var currentExpenses: List<Expense>
        get() = viewModel.currentExpenses
        set(value) { viewModel.currentExpenses = value }
    // Filter criteria fields
    private var selectedCategoryId: Int? = null
    private var startDate: Long? = null
    private var endDate: Long? = null
    private var minAmount: Double? = null
    private var maxAmount: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using view binding
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the current user ID from session; show error if not logged in
        currentUserId = SessionManager(requireContext()).getUserId()
        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_LONG).show()
            return
        }
        // Initialize repositories with DAOs from the singleton database instance
        val db = AppDatabase.getInstance(requireContext())
        expenseRepository = ExpenseRepository(db.expenseDao())
        categoryRepository = CategoryRepository(db.categoryDao())
        // Setup UI components and event listeners
        setupListener()
        setupSpinner()
        setupRecyclerView()
        loadExpenses()
    }
    /**
     * Set up the sorting options spinner with predefined sort orders.
     * Attach listener to apply sorting on selection changes.
     */
    private fun setupSpinner() {
        val sortOptions = arrayOf(
            "Newest First", "Oldest First",
            "Amount: Low to High", "Amount: High to Low"
        )
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sortOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = spinnerAdapter
        // Apply sorting only after adapter is initialized to avoid errors on initial selection
        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isAdapterInitialized) applySorting(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    /**
     * Initialize the RecyclerView and its adapter, and load categories to map category IDs to names.
     * Set click listener on expense items to navigate to the expense detail screen.
     */
    private fun setupRecyclerView() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Load categories to create a mapping for displaying category names in the adapter
            val categories = categoryRepository.getCategoriesForUser(currentUserId)
            val categoryMap = categories.associate { it.id to it.name }
            // Initialize adapter with empty list and category map
            adapter = ExpenseAdapter(emptyList(), categoryMap) { expense ->
                Toast.makeText(requireContext(), "Clicked: ${expense.description}", Toast.LENGTH_SHORT).show()
                // Navigate to ExpenseDetailFragment with selected expense ID
                val action = ExpensesFragmentDirections.actionExpensesFragmentToExpenseDetailFragment(expense.id)
                findNavController().navigate(action)
            }
            binding.recyclerExpenses.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerExpenses.adapter = adapter
            isAdapterInitialized = true
            // Load and display expenses after adapter setup
            loadExpenses()
        }
    }
    /**
     * Load expenses from repository applying the current filter stored in ViewModel.
     * Filter expenses locally and then apply sorting.
     */
    private fun loadExpenses() {
        if (!isAdapterInitialized) return
        // Retrieve all expenses for current user from the repository
        viewLifecycleOwner.lifecycleScope.launch {
            val allExpenses = expenseRepository.getExpensesForUser(currentUserId)
            val filter = viewModel.currentFilter
            // Filter expenses based on the filter criteria or use full list if no filter
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
            // Apply sorting based on the selected spinner position
            applySorting(binding.spinnerSort.selectedItemPosition)
            // Show or hide the "Filters Active" text based on whether a filter is applied
            binding.textFiltersActive.visibility = if (filter != null) View.VISIBLE else View.GONE
        }
    }
    /**
     * Sort current expenses based on selected sort option from spinner.
     */
    private fun applySorting(sortOption: Int) {
        if (!isAdapterInitialized) return

        val sorted = when (sortOption) {
            0 -> currentExpenses.sortedByDescending { it.timestamp }// Newest first
            1 -> currentExpenses.sortedBy { it.timestamp } // Oldest first
            2 -> currentExpenses.sortedBy { it.amount } // Amount ascending
            3 -> currentExpenses.sortedByDescending { it.amount } // Amount descending
            else -> currentExpenses
        }
        // Update the adapter data with the sorted list and existing category map
        val map = adapter?.currentCategoryMap ?: emptyMap()
        adapter?.updateData(sorted, map)
    }
    /**
     * Setup UI listeners for Add Expense button and Filter button.
     */
    private fun setupListener() {
        // Navigate to add expense screen on FAB click
        binding.fabAddExpense.setOnClickListener {
            findNavController().navigate(R.id.addExpenseFragment)
        }
        // Show filter dialog on Filter button click
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }
    /**
     * Display a dialog that allows users to select filtering criteria: category, date range, and amount range.
     * Applies or clears filters on button actions.
     */
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

        // Populate category spinner asynchronously from category repository
        lifecycleScope.launch {
            val categories = categoryRepository.getCategoriesForUser(currentUserId)
            val categoryNames = mutableListOf("All Categories")
            categoryNames.addAll(categories.map { it.name })
            val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = spinnerAdapter
            // Set spinner selection to previously selected category or default
            categorySpinner.setSelection(
                selectedCategoryId?.let { id -> categories.indexOfFirst { it.id == id } + 1 } ?: 0
            )
            // Update selectedCategoryId when user selects a category
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
        // Set onClick listeners for start and end date text views to open date pickers
        textStartDate.setOnClickListener {
            datePicker({ startDate = it }, textStartDate)
        }

        textEndDate.setOnClickListener {
            datePicker({ endDate = it }, textEndDate)
        }
        // Apply button stores filter values in ViewModel and reloads expenses
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

        // Show the dialog on screen
        alertDialog.show()
    }



    override fun onResume() {
        super.onResume()
        // Reload expenses every time fragment is resumed to reflect changes
        loadExpenses()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Nullify binding to avoid memory leaks
        _binding = null
    }
}
