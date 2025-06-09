// Takudzwa Murwira – ST10392257, Jason Daniel Isaacs – ST10039248, Daniel Gorin – ST10438307 and Moegammad-Yaseen Salie – ST10257795
//PROG7313

//References:
//            https://medium.com/@SeanAT19/how-to-use-mpandroidchart-in-android-studio-c01a8150720f
//            https://chatgpt.com/
//            https://www.youtube.com/playlist?list=PLWz5rJ2EKKc8SmtMNw34wvYkqj45rV1d3
//            https://www.youtube.com/playlist?list=PLSrm9z4zp4mEPOfZNV9O-crOhoMa0G2-o
package vcmsa.projects.pcv1.ui.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.data.Expense
import vcmsa.projects.pcv1.data.ExpenseDao

// ViewModel to manage expenses filtered by category and optional date range
class CategoryExpensesViewModel(
    private val expenseDao: ExpenseDao,  // DAO to access expense data
    private val categoryId: Int          // Category ID to filter expenses
) : ViewModel() {

    // LiveData holding the list of expenses to be observed by the UI
    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> = _expenses

    // Optional start and end dates for filtering expenses by date range
    private var startDate: Long? = null
    private var endDate: Long? = null

    init {
        // Load expenses on initialization
        loadExpenses()
    }

    // Apply a date range filter and reload expenses accordingly
    fun applyFilter(start: Long?, end: Long?) {
        startDate = start
        endDate = end
        loadExpenses()
    }

    // Load expenses from the database applying category and optional date filters
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
