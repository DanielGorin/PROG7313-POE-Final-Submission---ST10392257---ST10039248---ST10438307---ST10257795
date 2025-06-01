package vcmsa.projects.pcv1.ui.expenses

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.R
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.data.Expense
import vcmsa.projects.pcv1.data.ExpenseRepository
import vcmsa.projects.pcv1.databinding.FragmentExpenseDetailBinding


class ExpenseDetailFragment : Fragment() {

    private var _binding: FragmentExpenseDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var expenseRepository: ExpenseRepository
    private var expenseId: Int = -1
    private var currentExpense: Expense? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            expenseId = it.getInt("expenseId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getInstance(requireContext())
        expenseRepository = ExpenseRepository(db.expenseDao())

        viewLifecycleOwner.lifecycleScope.launch {
            val expenses = expenseRepository.getExpensesForUser(1) // Replace with actual user logic
            currentExpense = expenses.find { it.id == expenseId }
            currentExpense?.let { displayExpenseDetails(it) }
        }

        binding.btnDelete.setOnClickListener {
            currentExpense?.let {
                viewLifecycleOwner.lifecycleScope.launch {
                    expenseRepository.deleteExpense(it)
                    findNavController().navigateUp()
                }
            }
        }

        binding.btnEdit.setOnClickListener {
            // Navigate to EditExpenseFragment or re-use AddExpenseFragment with args
        }
    }

    private fun displayExpenseDetails(expense: Expense) {
        binding.textAmount.text = "R%.2f".format(expense.amount)
        binding.textDate.text = expense.date
        binding.textTime.text = "${expense.startTime} - ${expense.endTime ?: "N/A"}"
        binding.textDescription.text = expense.description ?: "No description"

        if (!expense.photoUri.isNullOrEmpty()) {
            binding.imagePhoto.setImageURI(Uri.parse(expense.photoUri))
        } else {
            binding.imagePhoto.setImageResource(R.drawable.placeholder)
        }
    }
}
