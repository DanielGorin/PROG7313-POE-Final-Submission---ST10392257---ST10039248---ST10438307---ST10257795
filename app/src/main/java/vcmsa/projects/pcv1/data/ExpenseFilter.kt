package vcmsa.projects.pcv1.data


data class ExpenseFilter(
    val categoryId: Int? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
)

