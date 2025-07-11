package vcmsa.projects.pcv1.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface ExpenseDao {

    @Insert
    suspend fun addExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getExpensesForUser(userId: Int): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND strftime('%Y-%m', datetime(timestamp / 1000, 'unixepoch')) = :yearMonth")
    suspend fun getMonthlySpending(userId: Int, yearMonth: String): Double?

    @Query("SELECT * FROM expenses WHERE userId = :userId AND strftime('%Y-%m', datetime(timestamp / 1000, 'unixepoch')) = :yearMonth ORDER BY timestamp ASC")
    suspend fun getExpensesByMonth(userId: Int, yearMonth: String): List<Expense>

    @Query("""
    SELECT * FROM expenses 
    WHERE userId = :userId
    AND (:categoryId IS NULL OR categoryId = :categoryId)
    AND (:startDateMillis IS NULL OR timestamp >= :startDateMillis)
    AND (:endDateMillis IS NULL OR timestamp <= :endDateMillis)
    AND (:minAmount IS NULL OR amount >= :minAmount)
    AND (:maxAmount IS NULL OR amount <= :maxAmount)
    ORDER BY timestamp DESC
""")
    suspend fun getFilteredExpensesForUser(
        userId: Int,
        categoryId: Int?,
        startDateMillis: Long?,
        endDateMillis: Long?,
        minAmount: Double?,
        maxAmount: Double?
    ): List<Expense>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId")
    suspend fun getExpensesByCategory(categoryId: Int): List<Expense>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId AND date BETWEEN :start AND :end")
    suspend fun getExpensesByCategoryAndDateRange(
        categoryId: Int,
        start: Long,
        end: Long
    ): List<Expense>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND strftime('%m-%Y', date) = strftime('%m-%Y', 'now')")
    fun getExpensesForCurrentMonth(userId: Int): LiveData<List<Expense>>

    @Query("""
    SELECT e.amount, c.name AS categoryName
    FROM expenses AS e
    LEFT JOIN categories AS c ON e.categoryId = c.id
    WHERE e.userId = :userId AND strftime('%m', e.date) = strftime('%m', 'now') AND strftime('%Y', e.date) = strftime('%Y', 'now')
""")
    fun getCurrentMonthExpensesWithCategory(userId: Int): LiveData<List<ExpenseWithCategoryName>>



}
