package vcmsa.projects.pcv1.ui.budget

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.EditText
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.R
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.data.Budget
import vcmsa.projects.pcv1.data.BudgetRepository
import vcmsa.projects.pcv1.data.Expense
import vcmsa.projects.pcv1.data.ExpenseRepository
import vcmsa.projects.pcv1.databinding.FragmentBudgetBinding
import vcmsa.projects.pcv1.util.SessionManager
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
            showSplurgePrompt()
        }
    }

    private fun updateUIBasedOnBudget() {
        lifecycleScope.launch {
            val budget = repository.getBudget(currentUserId)
            val hasBudget = budget != null

            if (isEditing || !hasBudget) {
                binding.editMinBudget.visibility = View.VISIBLE
                binding.editMaxBudget.visibility = View.VISIBLE
                binding.btnSaveBudget.visibility = View.VISIBLE
                binding.textMinValue.visibility = View.GONE
                binding.textMaxValue.visibility = View.GONE
                binding.textBudgetUsage.visibility = View.GONE
                binding.progressBudget.visibility = View.GONE
                binding.textBudgetAmount.visibility = View.GONE
            } else {
                binding.editMinBudget.visibility = View.GONE
                binding.editMaxBudget.visibility = View.GONE
                binding.btnSaveBudget.visibility = View.GONE
                binding.textMinValue.visibility = View.VISIBLE
                binding.textMaxValue.visibility = View.VISIBLE
                binding.textBudgetUsage.visibility = View.VISIBLE
                binding.progressBudget.visibility = View.VISIBLE
                binding.textBudgetAmount.visibility = View.VISIBLE
                if (budget != null) {
                    binding.textMinValue.text = String.format("R%.2f", budget.minAmount)
                    binding.textMaxValue.text = String.format("R%.2f", budget.maxAmount)
                }
            }

            binding.btnEditBudget.visibility = if (hasBudget && !isEditing) View.VISIBLE else View.GONE
            binding.fabBudgetAction.visibility = View.VISIBLE

            if (budget != null && isEditing) {
                binding.editMinBudget.setText(budget.minAmount.toString())
                binding.editMaxBudget.setText(budget.maxAmount.toString())
            }

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

                // 👇 Add this block
                val expenses = expenseRepository.getExpensesByMonth(currentUserId, yearMonth)
                updateGraph(expenses, budget.maxAmount)

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

    private fun updateGraph(expenses: List<Expense>, maxBudget: Double) {
        // 1. Filter expenses to current month
        val calendar = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
        val startOfMonth = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = calendar.timeInMillis

        val monthlyExpenses = expenses.filter {
            it.timestamp in startOfMonth until endOfMonth
        }

        // 2. Prepare daily totals
        val dailyTotals = mutableMapOf<String, Double>()
        val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())

        for (expense in monthlyExpenses) {
            val date = dateFormatter.format(Date(expense.timestamp))
            dailyTotals[date] = dailyTotals.getOrDefault(date, 0.0) + expense.amount
        }

        // 3. Generate full date range for the month
        val labels = mutableListOf<String>()
        val start = Calendar.getInstance().apply {
            timeInMillis = startOfMonth
        }
        while (start.timeInMillis < endOfMonth) {
            labels.add(dateFormatter.format(start.time))
            start.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 4. Calculate remaining budget per day
        val entries = mutableListOf<Entry>()
        var runningTotal = 0.0
        for ((index, date) in labels.withIndex()) {
            runningTotal += dailyTotals[date] ?: 0.0
            val remaining = maxBudget - runningTotal
            entries.add(Entry(index + 1f, remaining.toFloat()))
        }

        // 5. Create data set with cubic smoothing and fill
        val dataSet = LineDataSet(entries, "Remaining Budget").apply {
            color = ContextCompat.getColor(requireContext(), R.color.teal_700)
            valueTextColor = Color.TRANSPARENT
            lineWidth = 2f
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.graph_gradient)
            //mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircles(true)
            circleRadius = 3f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.teal_700))
        }

        // 6. Setup chart with marker view
        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.description.isEnabled = false
        binding.lineChart.setTouchEnabled(true)
        binding.lineChart.setPinchZoom(true)
        binding.lineChart.setScaleEnabled(true)

        // 7. MarkerView for tap-to-tooltip
        val marker = BudgetMarkerView(requireContext(), R.layout.marker_view, labels)
        binding.lineChart.marker = marker

        // 8. X-axis formatting
        val xAxis = binding.lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.GRAY
        xAxis.textSize = 10f
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt() - 1
                return if (index in labels.indices && index % 5 == 0) labels[index] else ""
            }
        }

        // 9. Y-axis limit line for max budget
        val leftAxis = binding.lineChart.axisLeft
        leftAxis.removeAllLimitLines()
        val limitLine = LimitLine(maxBudget.toFloat(), "Max Budget").apply {
            lineColor = Color.RED
            lineWidth = 2f
            textColor = Color.RED
            textSize = 12f
        }
        leftAxis.addLimitLine(limitLine)
        leftAxis.textColor = Color.GRAY

        binding.lineChart.axisRight.isEnabled = false

        // 10. Refresh chart
        binding.lineChart.invalidate()
    }






    private fun showSplurgePrompt() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "e.g. 150.00"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("How much would you like to splurge?")
            .setView(input)
            .setPositiveButton("Flip") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    lifecycleScope.launch {
                        val budget = repository.getBudget(currentUserId)
                        val spent = expenseRepository.getMonthlySpending(currentUserId, getCurrentYearMonth())
                        val remaining = (budget?.maxAmount ?: 0.0) - spent

                        if (amount > remaining) {
                            Toast.makeText(requireContext(), "You don't have enough budget left!", Toast.LENGTH_LONG).show()
                        } else {
                            startCoinFlipAnimation(amount, remaining)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startCoinFlipAnimation(amount: Double, remaining: Double) {
        val coinImage = binding.coinFlipImage
        val choice = binding.coinTextView
        coinImage.visibility = View.VISIBLE
        //choice.visibility = View.VISIBLE




        val animator = ObjectAnimator.ofFloat(coinImage, View.ROTATION_Y, 0f, 1800f).apply {
            duration = 5000
            interpolator = LinearInterpolator()
        }

        animator.doOnEnd {
            val result = if (shouldSpend(amount, remaining)) {
                choice.visibility = View.VISIBLE
                choice.text = "Spend!"
                coinImage.setImageResource(R.drawable.coin_head)
                "Spend!"
            } else {
                choice.text = "Save!"
                choice.visibility = View.VISIBLE
                coinImage.setImageResource(R.drawable.coin_tail)
                "Save!"
            }

            Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show()
            coinImage.postDelayed({
                choice.visibility = View.GONE
                coinImage.visibility = View.GONE
            }, 3000)
        }

        animator.start()
    }

    private fun shouldSpend(amount: Double, remaining: Double): Boolean {
        val ratio = amount / remaining
        val chanceToSpend = (1.0 - ratio).coerceIn(0.05, 0.95)
        return Math.random() < chanceToSpend
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
