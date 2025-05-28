package vcmsa.projects.pcv1.ui.expenses

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.data.*
import vcmsa.projects.pcv1.databinding.FragmentAddExpenseBinding
import vcmsa.projects.pcv1.util.SessionManager
import java.util.*

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var session: SessionManager
    private var currentUserId: Int = -1
    private var selectedTimestamp: Long = System.currentTimeMillis()
    private var selectedPhotoUri: String? = null
    private var selectedCategoryId: Int = -1

    private var currentCategories: List<Category> = listOf()

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedPhotoUri = it.toString()
            binding.imagePreview.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
        currentUserId = session.getUserId()

        val db = AppDatabase.getInstance(requireContext())
        expenseRepository = ExpenseRepository(db.expenseDao())
        categoryRepository = CategoryRepository(db.categoryDao())

        setupCategorySpinner()
        setupDateTimePickers()
        setupImagePicker()

        binding.btnSaveExpense.setOnClickListener { saveExpense() }

        return binding.root
    }

    private fun setupCategorySpinner(preselectId: Int? = null) {
        lifecycleScope.launch {
            currentCategories = categoryRepository.getCategoriesForUser(currentUserId)

            val categoryNames = currentCategories.map { "${it.icon} ${it.name}" }.toMutableList()
            categoryNames.add("➕ Add New Category")

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter

            if (preselectId != null) {
                val index = currentCategories.indexOfFirst { it.id == preselectId }
                if (index != -1) binding.spinnerCategory.setSelection(index)
            }

            binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (position == currentCategories.size) {
                        showAddCategoryDialog()
                    } else {
                        selectedCategoryId = currentCategories[position].id
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun showAddCategoryDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val inputName = EditText(requireContext()).apply {
            hint = "Category name"
        }

        val inputIcon = EditText(requireContext()).apply {
            hint = "Optional emoji/icon"
        }

        layout.addView(inputName)
        layout.addView(inputIcon)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Category")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = inputName.text.toString().trim()
                val icon = inputIcon.text.toString().trim()

                if (name.isNotEmpty()) {
                    val newCategory = Category(userId = currentUserId, name = name, icon = icon.ifEmpty { null })

                    lifecycleScope.launch {
                        val result = categoryRepository.addCategory(newCategory)
                        if (result.isSuccess) {
                            setupCategorySpinner() // Reload spinner
                            Toast.makeText(requireContext(), "Category added", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Error adding category", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun setupDateTimePickers() {
        val calendar = Calendar.getInstance()

        binding.editDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                binding.editDate.setText(android.text.format.DateFormat.format("yyyy-MM-dd", calendar))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.editStartTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val timeStr = String.format("%02d:%02d", hour, minute)
                binding.editStartTime.setText(timeStr)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        binding.editEndTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val timeStr = String.format("%02d:%02d", hour, minute)
                binding.editEndTime.setText(timeStr)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
    }

    private fun calculateTimestamp(date: String, time: String): Long {
        return try {
            val parts = date.split("-")
            val timeParts = time.split(":")

            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val day = parts[2].toInt()
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val calendar = Calendar.getInstance().apply {
                set(year, month, day, hour, minute, 0)
                set(Calendar.MILLISECOND, 0)
            }

            calendar.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun setupImagePicker() {
        binding.btnAttachPhoto.setOnClickListener {
            imagePicker.launch("image/*")
        }
    }

    private fun saveExpense() {
        val amount = binding.editAmount.text.toString().toDoubleOrNull()
        val description = binding.editDescription.text.toString().ifBlank { null }
        val date = binding.editDate.text.toString()
        val startTime = binding.editStartTime.text.toString()
        val endTime = binding.editEndTime.text.toString().ifBlank { null }

        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (date.isBlank() || startTime.isBlank()) {
            Toast.makeText(requireContext(), "Date and start time are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCategoryId == -1) {
            Toast.makeText(requireContext(), "Please select or add a category", Toast.LENGTH_SHORT).show()
            return
        }

        val calculatedTimestamp = calculateTimestamp(date, startTime)

        val expense = Expense(
            userId = currentUserId,
            categoryId = selectedCategoryId,
            amount = amount,
            description = description,
            photoUri = selectedPhotoUri,
            date = date,
            startTime = startTime,
            endTime = endTime,
            timestamp = calculatedTimestamp
        )

        lifecycleScope.launch {
            expenseRepository.addExpense(expense)
            Toast.makeText(requireContext(), "Expense saved", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
