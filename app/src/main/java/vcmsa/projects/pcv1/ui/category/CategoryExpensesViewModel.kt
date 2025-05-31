package vcmsa.projects.pcv1.ui.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.data.Expense
import vcmsa.projects.pcv1.data.ExpenseDao

class CategoryExpensesViewModel(
    private val expenseDao: ExpenseDao,
    private val categoryId: Int
) : ViewModel() {

    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> = _expenses

    private var startDate: Long? = null
    private var endDate: Long? = null

    init {
        loadExpenses()
    }

    fun applyFilter(start: Long?, end: Long?) {
        startDate = start
        endDate = end
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            val list = when {
                startDate != null && endDate != null ->
                    expenseDao.getExpensesByCategoryAndDateRange(categoryId, startDate!!, endDate!!)
                else ->
                    expenseDao.getExpensesByCategory(categoryId)
            }
            _expenses.value = list
        }
    }
}
