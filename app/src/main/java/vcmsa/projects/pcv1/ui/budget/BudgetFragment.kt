package vcmsa.projects.pcv1.ui.budget

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.data.Budget
import vcmsa.projects.pcv1.data.BudgetRepository
import vcmsa.projects.pcv1.data.ExpenseRepository
import vcmsa.projects.pcv1.databinding.FragmentBudgetBinding
import vcmsa.projects.pcv1.util.SessionManager

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: BudgetRepository
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var session: SessionManager
    private var currentUserId: Int = -1
    private var isEditing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
        currentUserId = session.getUserId()

        val db = AppDatabase.getInstance(requireContext())
        repository = BudgetRepository(db.budgetDao())
        expenseRepository = ExpenseRepository(db.expenseDao())

        setupListeners()
        updateUIBasedOnBudget()

        return binding.root
    }

    private fun setupListeners() {
        binding.btnSaveBudget.setOnClickListener {
            val min = binding.editMinBudget.text.toString().toDoubleOrNull()
            val max = binding.editMaxBudget.text.toString().toDoubleOrNull()

            if (min != null && max != null && min <= max) {
                lifecycleScope.launch {
                    val user = AppDatabase.getInstance(requireContext()).userDao().getUserById(currentUserId)
                    if (user != null) {
                        val budget = Budget(userId = currentUserId, minAmount = min, maxAmount = max)
                        repository.saveBudget(budget)
                        Toast.makeText(requireContext(), "Budget saved!", Toast.LENGTH_SHORT).show()
                        isEditing = false
                        updateUIBasedOnBudget()
                    } else {
                        Toast.makeText(requireContext(), "User not found. Cannot save budget.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Enter valid amounts (min ≤ max)", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnEditBudget.setOnClickListener {
            isEditing = true
            updateUIBasedOnBudget()
        }

        binding.fabBudgetAction.setOnClickListener {
            // You can add functionality here later
            Toast.makeText(requireContext(), "FAB clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUIBasedOnBudget() {
        lifecycleScope.launch {
            val budget = repository.getBudget(currentUserId)
            val hasBudget = budget != null

            if (isEditing || !hasBudget) {
                // Show input fields
                binding.editMinBudget.visibility = View.VISIBLE
                binding.editMaxBudget.visibility = View.VISIBLE
                binding.btnSaveBudget.visibility = View.VISIBLE

                // Hide value TextViews
                binding.textMinValue.visibility = View.GONE
                binding.textMaxValue.visibility = View.GONE

                // Hide progress views
                binding.textBudgetUsage.visibility = View.GONE
                binding.progressBudget.visibility = View.GONE
                binding.textBudgetAmount.visibility = View.GONE
            } else {
                // Hide input fields
                binding.editMinBudget.visibility = View.GONE
                binding.editMaxBudget.visibility = View.GONE
                binding.btnSaveBudget.visibility = View.GONE

                // Show value TextViews
                binding.textMinValue.visibility = View.VISIBLE
                binding.textMaxValue.visibility = View.VISIBLE

                // Show budget info
                binding.textBudgetUsage.visibility = View.VISIBLE
                binding.progressBudget.visibility = View.VISIBLE
                binding.textBudgetAmount.visibility = View.VISIBLE

                // Set values
                if (budget != null) {
                    binding.textMinValue.text = String.format("R%.2f", budget.minAmount)
                }
                if (budget != null) {
                    binding.textMaxValue.text = String.format("R%.2f", budget.maxAmount)
                }
            }

            // Always show edit button and FAB
            binding.btnEditBudget.visibility = if (hasBudget && !isEditing) View.VISIBLE else View.GONE
            binding.fabBudgetAction.visibility = View.VISIBLE

            // Pre-fill inputs if editing
            if (budget != null && isEditing) {
                binding.editMinBudget.setText(budget.minAmount.toString())
                binding.editMaxBudget.setText(budget.maxAmount.toString())
            }

            // Load progress if not editing
            if (hasBudget) {
                loadCurrentSpending()
            }
        }
    }

    private fun loadCurrentSpending() {
        lifecycleScope.launch {
            val yearMonth = getCurrentYearMonth()
            val spending = expenseRepository.getMonthlySpending(currentUserId, yearMonth)
            val budget = repository.getBudget(currentUserId)

            if (budget != null && budget.maxAmount > 0) {
                val percent = ((spending / budget.maxAmount) * 100).toInt().coerceAtMost(100)

                binding.progressBudget.progress = percent
                binding.textBudgetUsage.text = "Budget Usage"
                binding.textBudgetAmount.text = String.format("R%.2f spent of R%.2f", spending, budget.maxAmount)

                val color = when {
                    percent >= 80 -> Color.RED
                    percent >= 50 -> Color.YELLOW
                    else -> Color.GREEN
                }

                DrawableCompat.setTint(binding.progressBudget.progressDrawable, color)
            } else {
                binding.progressBudget.progress = 0
                binding.textBudgetAmount.text = "No budget set"
            }
        }
    }

    private fun getCurrentYearMonth(): String {
        val now = java.util.Calendar.getInstance()
        val year = now.get(java.util.Calendar.YEAR)
        val month = now.get(java.util.Calendar.MONTH) + 1
        return String.format("%04d-%02d", year, month)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
