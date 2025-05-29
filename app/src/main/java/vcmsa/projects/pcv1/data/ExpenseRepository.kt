package vcmsa.projects.pcv1.data

class ExpenseRepository(private val dao: ExpenseDao) {

    suspend fun addExpense(expense: Expense) = dao.addExpense(expense)

    suspend fun updateExpense(expense: Expense) = dao.updateExpense(expense)

    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)

    suspend fun getExpensesForUser(userId: Int) = dao.getExpensesForUser(userId)

    suspend fun getMonthlySpending(userId: Int, yearMonth: String): Double {
        return dao.getMonthlySpending(userId, yearMonth) ?: 0.0
    }

    suspend fun getExpensesByMonth(userId: Int, yearMonth: String): List<Expense> {
        return dao.getExpensesByMonth(userId, yearMonth)
    }

    suspend fun getFilteredExpensesForUser(
        userId: Int,
        categoryId: Int?,
        startDateMillis: Long?,
        endDateMillis: Long?,
        minAmount: Double?,
        maxAmount: Double?
    ): List<Expense> {
        return dao.getFilteredExpensesForUser(userId, categoryId, startDateMillis, endDateMillis, minAmount, maxAmount)
    }

}
