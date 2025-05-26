package vcmsa.projects.pcv1.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val categoryId: Int?, // Nullable if no category is selected
    val amount: Double,
    val description: String?,
    val timestamp: Long, // Store as Unix timestamp
    val photoUri: String? = null
)
