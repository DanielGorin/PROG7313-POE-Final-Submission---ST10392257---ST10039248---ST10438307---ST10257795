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
import vcmsa.projects.pcv1.data.ExpenseRepository
//import vcmsa.projects.pcv1.databinding.FragmentExpenseBinding
import vcmsa.projects.pcv1.databinding.FragmentExpensesBinding
import vcmsa.projects.pcv1.util.SessionManager

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var adapter: ExpenseAdapter
    private lateinit var session: SessionManager
    private var currentUserId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        //_binding = FragmentExpensesBinding.inflate(inflater, container, false)
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)

        session = SessionManager(requireContext())
        currentUserId = session.getUserId()
        expenseRepository = ExpenseRepository(AppDatabase.getInstance(requireContext()).expenseDao())

        adapter = ExpenseAdapter(emptyList())
        binding.recyclerExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerExpenses.adapter = adapter
        //binding.recyclerViewExpenses.layoutManager = LinearLayoutManager(requireContext())
        //binding.recyclerViewExpenses.adapter = adapter

        binding.fabAddExpense.setOnClickListener {
            findNavController().navigate(R.id.addExpenseFragment)

            // Navigate to AddExpenseFragment (you may use Navigation Component or FragmentTransaction)
//            parentFragmentManager.beginTransaction()
//                .replace(this.id, AddExpenseFragment() )
//                .addToBackStack(null)
//                .commit()
        }

        loadExpenses()

        return binding.root
    }

    private fun loadExpenses() {
        lifecycleScope.launch {
            val expenses = expenseRepository.getExpensesForUser(currentUserId)
            adapter.updateData(expenses)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
