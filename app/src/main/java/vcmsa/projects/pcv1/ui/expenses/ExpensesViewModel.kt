package vcmsa.projects.pcv1.ui.expenses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vcmsa.projects.pcv1.data.Expense
import vcmsa.projects.pcv1.data.ExpenseFilter

class ExpensesViewModel : ViewModel() {
    var currentFilter: ExpenseFilter? = null
    var currentExpenses: List<Expense> = emptyList()
}