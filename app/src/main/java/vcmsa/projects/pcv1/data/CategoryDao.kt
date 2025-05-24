package vcmsa.projects.pcv1.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category)

    @Query("SELECT * FROM categories WHERE user_id = :userId")
    suspend fun getAllCategoriesForUser(userId: Int): List<Category>

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}
