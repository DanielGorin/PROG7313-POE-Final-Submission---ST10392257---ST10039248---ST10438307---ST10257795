package vcmsa.projects.pcv1.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.data.Budget
import vcmsa.projects.pcv1.data.BudgetRepository
import vcmsa.projects.pcv1.databinding.FragmentBudgetBinding
import vcmsa.projects.pcv1.util.SessionManager

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: BudgetRepository
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

        loadUserBudget()

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
