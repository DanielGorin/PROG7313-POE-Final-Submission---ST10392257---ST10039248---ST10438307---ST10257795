package vcmsa.projects.pcv1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: Budget)

    @Query("SELECT * FROM budgets WHERE userId = :userId LIMIT 1")
    suspend fun getBudgetForUser(userId: Int): Budget?

    @Query("""
    SELECT SUM(amount) FROM expenses 
    WHERE userId = :userId 
    AND strftime('%Y-%m', timestamp) = strftime('%Y-%m', 'now')
""")
    suspend fun getTotalSpentThisMonth(userId: Int): Double?

}