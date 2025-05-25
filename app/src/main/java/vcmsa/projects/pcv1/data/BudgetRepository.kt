package vcmsa.projects.pcv1.data

class BudgetRepository(private val dao: BudgetDao) {
    suspend fun saveBudget(budget: Budget) = dao.upsert(budget)
    suspend fun getBudget(userId: Int): Budget? = dao.getBudgetForUser(userId)
}
