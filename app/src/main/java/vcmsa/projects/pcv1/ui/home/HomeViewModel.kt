package vcmsa.projects.pcv1.ui.home

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.data.Budget
import vcmsa.projects.pcv1.data.BudgetDao
import vcmsa.projects.pcv1.data.ExpenseDao
import vcmsa.projects.pcv1.data.ExpenseWithCategoryName

class HomeViewModel(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val userId: Int
) : ViewModel() {

    // LiveData of expenses with category names (already correct)
    val currentMonthExpensesWithCategory: LiveData<List<ExpenseWithCategoryName>> =
        expenseDao.getCurrentMonthExpensesWithCategory(userId)

    // LiveData for current budget
    private val _currentBudget = MutableLiveData<Budget?>()
    val currentBudget: LiveData<Budget?> = _currentBudget

    init {
        fetchCurrentBudget()
    }

    private fun fetchCurrentBudget() {
        viewModelScope.launch {
            val budget = budgetDao.getBudgetForUser(userId)
            _currentBudget.postValue(budget)
        }
    }
}
