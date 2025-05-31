package vcmsa.projects.pcv1.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import vcmsa.projects.pcv1.data.ExpenseDao

class CategoryExpensesViewModelFactory(
    private val expenseDao: ExpenseDao,
    private val categoryId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryExpensesViewModel::class.java)) {
            return CategoryExpensesViewModel(expenseDao, categoryId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
