package vcmsa.projects.pcv1.ui.category

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import vcmsa.projects.pcv1.R
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.databinding.FragmentCategoryExpensesBinding
import vcmsa.projects.pcv1.ui.expenses.ExpenseAdapter

class CategoryExpensesFragment : Fragment() {

    private var _binding: FragmentCategoryExpensesBinding? = null
    private val binding get() = _binding!!

    private val args: CategoryExpensesFragmentArgs by navArgs()

    private lateinit var viewModel: CategoryExpensesViewModel
    private lateinit var adapter: ExpenseAdapter // Reuse or create new

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryExpensesBinding.inflate(inflater, container, false)

        val categoryId = args.categoryId
        val database = AppDatabase.getInstance(requireContext())

        viewModel = ViewModelProvider(
            this,
            CategoryExpensesViewModelFactory(database.expenseDao(), categoryId)
        )[CategoryExpensesViewModel::class.java]

        adapter = ExpenseAdapter(emptyList(), emptyMap()) // show date, amount, etc.

        binding.recyclerCategoryExpenses.adapter = adapter
        binding.recyclerCategoryExpenses.layoutManager = LinearLayoutManager(requireContext())

        viewModel.expenses.observe(viewLifecycleOwner) {
            adapter.updateData(it, emptyMap())
            binding.textTotalAmount.text = "Total: ${it.sumOf { expense -> expense.amount }}"
        }

        binding.btnFilter.setOnClickListener {
            showDateFilterDialog()
        }

        return binding.root
    }

    private fun showDateFilterDialog() {
        // Show a dialog with date pickers for startDate and endDate
        // Call viewModel.applyFilter(startDate, endDate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
