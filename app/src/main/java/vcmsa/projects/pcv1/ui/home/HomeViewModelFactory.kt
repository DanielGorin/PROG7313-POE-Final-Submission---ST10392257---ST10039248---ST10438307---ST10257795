package vcmsa.projects.pcv1.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import vcmsa.projects.pcv1.data.BudgetDao
import vcmsa.projects.pcv1.data.ExpenseDao
// Factory to create HomeViewModel with required dependencies
class HomeViewModelFactory(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val userId: Int
) : ViewModelProvider.Factory {
    // Create an instance of HomeViewModel with DAOs and userId
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(expenseDao, budgetDao, userId) as T
    }
}
