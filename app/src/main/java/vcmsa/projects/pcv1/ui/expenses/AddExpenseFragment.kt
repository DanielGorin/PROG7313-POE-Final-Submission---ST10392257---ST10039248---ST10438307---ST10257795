package vcmsa.projects.pcv1.ui.expenses

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
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
    private var selectedCategoryId: Int? = null

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

    private fun setupCategorySpinner() {
        lifecycleScope.launch {
            val categories = categoryRepository.getCategoriesForUser(currentUserId)
            val names = categories.map { it.icon + " " + it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter

            binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedCategoryId = categories[position].id
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedCategoryId = null
                }
            }
        }
    }

    private fun setupDateTimePickers() {
        val calendar = Calendar.getInstance()

        binding.editDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                updateTimestamp(calendar)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.editTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                updateTimestamp(calendar)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
    }

    private fun updateTimestamp(calendar: Calendar) {
        selectedTimestamp = calendar.timeInMillis
        binding.editDate.setText(android.text.format.DateFormat.format("yyyy-MM-dd", calendar))
        binding.editTime.setText(android.text.format.DateFormat.format("HH:mm", calendar))
    }

    private fun setupImagePicker() {
        binding.btnAttachPhoto.setOnClickListener {
            imagePicker.launch("image/*")
        }
    }

    private fun saveExpense() {
        val amount = binding.editAmount.text.toString().toDoubleOrNull()
        val description = binding.editDescription.text.toString()

        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            userId = currentUserId,
            categoryId = selectedCategoryId,
            amount = amount,
            description = description.ifBlank { null },
            timestamp = selectedTimestamp,
            photoUri = selectedPhotoUri
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
