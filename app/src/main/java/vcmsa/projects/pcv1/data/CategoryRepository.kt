package vcmsa.projects.pcv1.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository(private val categoryDao: CategoryDao) {

    suspend fun addCategory(category: Category): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                categoryDao.insert(category)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun updateCategory(category: Category): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                categoryDao.update(category)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteCategory(category: Category): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                categoryDao.delete(category)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getCategoriesForUser(userId: Int): List<Category> =
        withContext(Dispatchers.IO) {
            categoryDao.getAllCategoriesForUser(userId)
        }
}
