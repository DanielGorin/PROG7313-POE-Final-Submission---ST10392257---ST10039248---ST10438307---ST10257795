package vcmsa.projects.pcv1.ui.home

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AlertDialog
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
import androidx.core.graphics.toColorInt
import androidx.navigation.fragment.findNavController
import vcmsa.projects.pcv1.R

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var budgetStatus: String

    private val greenTips = listOf(
        "Remember things at home are often cheaper, try making your own coffee and lunch this week.",
        "Use a shopping list, this helps stop impulse purchases.",
        "Try a cash-only day, it will make you rethink your expenses.",
        "Take time to think about big expenses, impulses often go away after 10 minutes.",
        "Try making a lift club for your common trips, this can save on petrol and transport."
    )

    private val yellowTips = listOf(
        "Subscriptions and automated payments can build up. Review yours to see where you can save.",
        "Try only eating at home this week.",
        "Buying in bulk can lead to big savings.",
        "Try meal prepping this week to reduce costs and improve nutrition.",
        "Go through your pantry—cooking with staples can reduce food costs."
    )

    private val redTips = listOf(
        "Consider selling unused items online for extra cash.",
        "Try a free entertainment week—no spending on fun.",
        "Uninstall apps that tempt you to spend, like food delivery.",
        "Think about delaying non-urgent expenses.",
        "Tell someone you trust about your goal to spend less—it helps accountability."
    )

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

        setupButtonClickListeners()
        observeViewModel()


        binding.btnTips.setOnClickListener {
            val tip = getRandomTip(budgetStatus)
            AlertDialog.Builder(requireContext())
                .setTitle("Budget Tip")
                .setMessage(tip)
                .setPositiveButton("OK", null)
                .show()
        }

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

    private fun setupButtonClickListeners() {
        binding.btnTips.setOnClickListener {
            val tip = getRandomTip(budgetStatus)
            AlertDialog.Builder(requireContext())
                .setTitle("Budget Tip")
                .setMessage(tip)
                .setPositiveButton("OK", null)
                .show()
        }

        binding.btnAddExpense.setOnClickListener {

            findNavController().navigate(R.id.action_homeFragment_to_addExpenseFragment)
            Log.d("HomeFragment", "Add Expense button clicked")

        }

        binding.btnViewCategories.setOnClickListener {

            findNavController().navigate(R.id.action_homeFragment_to_categoriesFragment)
            Log.d("HomeFragment", "View Categories button clicked")
        }

        binding.btnViewExpenses.setOnClickListener {

            findNavController().navigate(R.id.action_homeFragment_to_allExpensesFragment)
            Log.d("HomeFragment", "View All Expenses button clicked")
        }

        binding.btnBudgetSettings.setOnClickListener {

            findNavController().navigate(R.id.action_homeFragment_to_budgetSettingsFragment)
            Log.d("HomeFragment", "Budget Settings button clicked")
        }}

    private fun updateProgressBar(totalSpent: Double, min: Double, max: Double) {
        val percentage = ((totalSpent / max) * 100).coerceIn(0.0, 100.0).toInt()

        val currentProgress = binding.progressBudget.progress
        binding.textProgressPercentage.text = "$percentage%"

        binding.progressBudget.max = 100

        // Animate the circular progress
        ObjectAnimator.ofInt(binding.progressBudget, "progress", currentProgress, percentage).apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
            start()
        }




            // Update progress color
        val color = getBudgetColor(totalSpent, min, max)
        binding.progressBudget.setIndicatorColor(color)

        binding.textBudgetSummary.text = "Spent: R${String.format("%.2f", totalSpent)} of R${String.format("%.2f", max)}"

        binding.textBudgetStatus.text = when {
            totalSpent < min -> {
                budgetStatus = "green"
                "Below budget minimum"
            }
            totalSpent in min..max -> {
                budgetStatus = "yellow"
                "Within budget"
            }
            else -> {
                budgetStatus = "red"
                "Over budget!"
            }
        }
    }



    private fun getBudgetColor(totalSpent: Double, min: Double, max: Double): Int {
        return when {
            totalSpent < min -> {
                val fraction = (totalSpent / min).coerceIn(0.0, 1.0)
                interpolateColor("#4CAF50".toColorInt(), "#FFA500".toColorInt(), fraction)
            }
            totalSpent in min..max -> {
                val fraction = ((totalSpent - min) / (max - min)).coerceIn(0.0, 1.0)
                interpolateColor("#FFA500".toColorInt(), "#FF8C00".toColorInt(), fraction)
            }
            else -> "#8B0000".toColorInt()
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

    private fun getRandomTip(status: String): String {
        return when (status.lowercase()) {
            "green" -> greenTips.random()
            "yellow" -> yellowTips.random()
            "red" -> redTips.random()
            else -> "Track your expenses daily to stay on top of your budget!"
        }
    }

    private fun animateProgressSmoothly(from: Int, to: Int) {
        val animator = ObjectAnimator.ofInt(binding.progressBudget, "progress", from, to)
        animator.duration = 1000 // Slower for loader feel
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }

    private fun animateBudgetText(from: Int, to: Int) {
        val animator = ValueAnimator.ofInt(from, to)
        animator.duration = 1000
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            binding.textBudgetSummary.text = "Spent: R$value"
        }
        animator.start()
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
