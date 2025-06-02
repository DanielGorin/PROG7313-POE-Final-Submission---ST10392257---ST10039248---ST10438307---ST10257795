package vcmsa.projects.pcv1.ui.home

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.drawable.DrawableCompat
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
        val sessionManager = SessionManager(context)
        val userId = sessionManager.getUserId()
        val username = sessionManager.getUsername()

        binding.textWelcome.text = "Welcome, $username!"

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

            val budget = viewModel.currentBudget.value
            if (budget != null) {
                updateProgressBar(totalSpent, budget.minAmount, budget.maxAmount)
            }

            setupPieChart(expenses)
        }

        viewModel.currentBudget.observe(viewLifecycleOwner) { budget ->
            val expenses = viewModel.currentMonthExpensesWithCategory.value
            val totalSpent = expenses?.sumOf { it.amount } ?: 0.0

            if (budget != null) {
                updateProgressBar(totalSpent, budget.minAmount, budget.maxAmount)
            }
        }
    }

    private fun updateProgressBar(totalSpent: Double, min: Double, max: Double) {
        val percentage = ((totalSpent / max) * 100).coerceIn(0.0, 100.0).toInt()

        // ✅ Ensure max is set for proper scaling
        binding.progressBudget.max = 100

        // ✅ Animate progress change
        ObjectAnimator.ofInt(binding.progressBudget, "progress", percentage).apply {
            duration = 500
            interpolator = DecelerateInterpolator()
            start()
        }

        // ✅ Set dynamic color
        val color = getBudgetColor(totalSpent, min, max)
        val drawable = DrawableCompat.wrap(binding.progressBudget.progressDrawable.mutate())
        DrawableCompat.setTint(drawable, color)
        binding.progressBudget.progressDrawable = drawable

        // ✅ Budget status text
        binding.textBudgetStatus.text = when {
            totalSpent < min -> "Below budget minimum"
            totalSpent in min..max -> "Within budget"
            else -> "Over budget!"
        }
    }

    private fun getBudgetColor(totalSpent: Double, min: Double, max: Double): Int {
        return when {
            totalSpent < min -> {
                val fraction = (totalSpent / min).coerceIn(0.0, 1.0)
                interpolateColor(Color.parseColor("#4CAF50"), Color.parseColor("#FFA500"), fraction)
            }
            totalSpent in min..max -> {
                val fraction = ((totalSpent - min) / (max - min)).coerceIn(0.0, 1.0)
                interpolateColor(Color.parseColor("#FFA500"), Color.parseColor("#FF8C00"), fraction)
            }
            else -> Color.parseColor("#8B0000") // Over budget: Dark Red
        }
    }

    private fun interpolateColor(startColor: Int, endColor: Int, fraction: Double): Int {
        val startA = Color.alpha(startColor)
        val startR = Color.red(startColor)
        val startG = Color.green(startColor)
        val startB = Color.blue(startColor)

        val endA = Color.alpha(endColor)
        val endR = Color.red(endColor)
        val endG = Color.green(endColor)
        val endB = Color.blue(endColor)

        val resultA = (startA + ((endA - startA) * fraction)).toInt()
        val resultR = (startR + ((endR - startR) * fraction)).toInt()
        val resultG = (startG + ((endG - startG) * fraction)).toInt()
        val resultB = (startB + ((endB - startB) * fraction)).toInt()

        return Color.argb(resultA, resultR, resultG, resultB)
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
