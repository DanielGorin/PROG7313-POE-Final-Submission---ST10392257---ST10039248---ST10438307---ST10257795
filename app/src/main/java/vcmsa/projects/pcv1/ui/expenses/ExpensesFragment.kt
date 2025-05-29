package vcmsa.projects.pcv1.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.R
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.data.CategoryRepository
import vcmsa.projects.pcv1.data.Expense
import vcmsa.projects.pcv1.data.ExpenseRepository
import vcmsa.projects.pcv1.databinding.FragmentExpensesBinding

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var categoryRepository: CategoryRepository

    private var adapter: ExpenseAdapter? = null
    private var isAdapterInitialized = false

    private var currentExpenses: List<Expense> = emptyList()
    private val currentUserId: Int = 1 // Replace with actual user logic

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
            currentExpenses = expenseRepository.getExpensesForUser(currentUserId)
            applySorting(binding.spinnerSort.selectedItemPosition)
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
