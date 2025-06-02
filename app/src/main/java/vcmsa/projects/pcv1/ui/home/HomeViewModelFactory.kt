package vcmsa.projects.pcv1.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import vcmsa.projects.pcv1.data.BudgetDao
import vcmsa.projects.pcv1.data.ExpenseDao

class HomeViewModelFactory(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(expenseDao, budgetDao, userId) as T
    }
}
