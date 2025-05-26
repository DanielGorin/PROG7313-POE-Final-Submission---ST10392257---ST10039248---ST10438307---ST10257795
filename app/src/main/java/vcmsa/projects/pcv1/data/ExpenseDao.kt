package vcmsa.projects.pcv1.data

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
}
