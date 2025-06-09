// Takudzwa Murwira – ST10392257, Jason Daniel Isaacs – ST10039248, Daniel Gorin – ST10438307 and Moegammad-Yaseen Salie – ST10257795
//PROG7313

//References:
//            https://medium.com/@SeanAT19/how-to-use-mpandroidchart-in-android-studio-c01a8150720f
//            https://chatgpt.com/
//            https://www.youtube.com/playlist?list=PLWz5rJ2EKKc8SmtMNw34wvYkqj45rV1d3
//            https://www.youtube.com/playlist?list=PLSrm9z4zp4mEPOfZNV9O-crOhoMa0G2-o
package vcmsa.projects.pcv1.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import vcmsa.projects.pcv1.data.ExpenseDao

// Factory to create CategoryExpensesViewModel instances with parameters
class CategoryExpensesViewModelFactory(
    private val expenseDao: ExpenseDao,  // DAO for accessing expenses in the database
    private val categoryId: Int           // The ID of the category to load expenses for
) : ViewModelProvider.Factory {

    // Called by ViewModelProvider to create a ViewModel instance
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class matches CategoryExpensesViewModel
        if (modelClass.isAssignableFrom(CategoryExpensesViewModel::class.java)) {
            // Create and return CategoryExpensesViewModel with provided parameters
            return CategoryExpensesViewModel(expenseDao, categoryId) as T
        }
        // Throw exception if an unknown ViewModel class is requested
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
