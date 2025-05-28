package vcmsa.projects.pcv1.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.R
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.data.CategoryRepository
import vcmsa.projects.pcv1.data.ExpenseRepository
import vcmsa.projects.pcv1.databinding.FragmentExpensesBinding
import vcmsa.projects.pcv1.util.SessionManager

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var adapter: ExpenseAdapter
    private lateinit var session: SessionManager
    private var categoryMap: Map<Int, String> = emptyMap()
    private var currentUserId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)

        session = SessionManager(requireContext())
        currentUserId = session.getUserId()

        setupListeners()
        setupRecyclerView()

        return binding.root
    }

    private fun setupRecyclerView() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            expenseRepository = ExpenseRepository(db.expenseDao())
            val categoryRepository = CategoryRepository(db.categoryDao())

            val categories = categoryRepository.getCategoriesForUser(currentUserId)
            categoryMap = categories.associateBy({ it.id }, { "${it.icon ?: ""} ${it.name}" })

            adapter = ExpenseAdapter(emptyList(), categoryMap)
            binding.recyclerExpenses.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerExpenses.adapter = adapter

            loadExpenses()
        }
    }

    private fun setupListeners() {
        binding.fabAddExpense.setOnClickListener {
            findNavController().navigate(R.id.addExpenseFragment)
        }
    }

    private fun loadExpenses() {
        lifecycleScope.launch {
            val expenses = expenseRepository.getExpensesForUser(currentUserId)
            adapter.updateData(expenses, categoryMap)
        }
    }

    override fun onResume() {
        super.onResume()
        loadExpenses() // Reload when returning from AddExpenseFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
