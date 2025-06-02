package vcmsa.projects.pcv1.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.data.ExpenseWithCategoryName
import vcmsa.projects.pcv1.databinding.FragmentHomeBinding
import vcmsa.projects.pcv1.util.SessionManager

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val context = requireContext()
        val userId = SessionManager(context).getUserId()

        val appDatabase = AppDatabase.getInstance(context)
        viewModel = ViewModelProvider(
            this,
            HomeViewModelFactory(appDatabase.expenseDao(), appDatabase.budgetDao(), userId)
        )[HomeViewModel::class.java]

        observeViewModel()

        return binding.root
    }

    private fun observeViewModel() {
        viewModel.currentMonthExpensesWithCategory.observe(viewLifecycleOwner) { expenses ->
            val totalSpent = expenses.sumOf { it.amount }
            binding.textBudgetSummary.text = "Spent: R$totalSpent"
            viewModel.currentBudget.value?.let { budget ->
                updateProgressBar(totalSpent, budget.minAmount, budget.maxAmount)
            }
            setupPieChart(expenses)
        }

        viewModel.currentBudget.observe(viewLifecycleOwner) { budget ->
            val totalSpent = viewModel.currentMonthExpensesWithCategory.value?.sumOf { it.amount } ?: 0.0
            if (budget != null) {
                updateProgressBar(totalSpent, budget.minAmount, budget.maxAmount)
            }
        }
    }

    private fun updateProgressBar(totalSpent: Double, min: Double, max: Double) {
        val percentage = ((totalSpent / max) * 100).coerceIn(0.0, 100.0)
        binding.progressBudget.progress = percentage.toInt()

        binding.textBudgetStatus.text = when {
            totalSpent < min -> "Below budget minimum"
            totalSpent in min..max -> "Within budget"
            else -> "Over budget!"
        }
    }

    private fun setupPieChart(expenses: List<ExpenseWithCategoryName>) {
        val grouped = expenses.groupBy { it.categoryName ?: "Uncategorized" }

        val entries = grouped.map { (category, expenseList) ->
            val totalAmount = expenseList.sumOf { it.amount }.toFloat()
            PieEntry(totalAmount, category)
        }

        val dataSet = PieDataSet(entries, "Spending by Category").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        val pieData = PieData(dataSet)

        binding.pieChart.apply {
            data = pieData
            description.isEnabled = false
            setUsePercentValues(false)
            setDrawEntryLabels(true)
            setEntryLabelColor(Color.BLACK)
            centerText = "This Month"
            setCenterTextSize(14f)
            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
