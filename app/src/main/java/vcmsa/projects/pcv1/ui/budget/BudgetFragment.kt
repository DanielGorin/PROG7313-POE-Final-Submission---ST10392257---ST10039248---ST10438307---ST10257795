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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
        currentUserId = session.getUserId()

        val db = AppDatabase.getInstance(requireContext())
        repository = BudgetRepository(db.budgetDao())
        expenseRepository = ExpenseRepository(db.expenseDao())

        loadUserBudget()
        loadCurrentSpending()

        binding.btnSaveBudget.setOnClickListener {
            val min = binding.editMinBudget.text.toString().toDoubleOrNull()
            val max = binding.editMaxBudget.text.toString().toDoubleOrNull()

            if (min != null && max != null && min <= max) {
                lifecycleScope.launch {
                    val user = db.userDao().getUserById(currentUserId)
                    if (user != null) {
                        val budget = Budget(userId = currentUserId, minAmount = min, maxAmount = max)
                        repository.saveBudget(budget)
                        Toast.makeText(requireContext(), "Budget saved!", Toast.LENGTH_SHORT).show()
                        loadUserBudget()
                        loadCurrentSpending()
                    } else {
                        Toast.makeText(requireContext(), "User not found. Cannot save budget.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Enter valid amounts (min ≤ max)", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun loadUserBudget() {
        lifecycleScope.launch {
            val budget = repository.getBudget(currentUserId)
            budget?.let {
                binding.editMinBudget.setText(it.minAmount.toString())
                binding.editMaxBudget.setText(it.maxAmount.toString())
            }
        }
    }

    private fun loadCurrentSpending() {
        lifecycleScope.launch {
            val yearMonth = getCurrentYearMonth()
            val spending = expenseRepository.getMonthlySpending(currentUserId, yearMonth)
            val budget = repository.getBudget(currentUserId)

            //binding.textCurrentSpending.text = "Current Spending: R%.2f".format(spending)
            binding.textBudgetUsage.text = "Budget Usage: R%.2f".format(spending)


            if (budget != null && budget.maxAmount > 0) {
                val percent = ((spending / budget.maxAmount) * 100).toInt().coerceAtMost(100)

                binding.progressBudget.progress = percent

                // Update display text
                binding.textBudgetAmount.text = String.format("R%.2f spent of R%.2f", spending, budget.maxAmount)

                // Update progress bar color
                val color = when {
                    percent >= 80 -> Color.RED
                    percent >= 50 -> Color.YELLOW
                    else -> Color.GREEN
                }
                //binding.progressBarBudget.progressDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
                val progressDrawable = binding.progressBudget.progressDrawable
                DrawableCompat.setTint(progressDrawable, color)

            } else {
                //binding.progressBarBudget.progress = 0
                binding.progressBudget.progress =0
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
