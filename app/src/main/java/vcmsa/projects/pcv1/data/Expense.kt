package vcmsa.projects.pcv1.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(index = true) val userId: Int, // must match parent column name
    val amount: Double,
    val categoryId: Int,
    val description: String?,
    val photoUri: String? = null,
    val date: String, // e.g., "2025-05-27"
    val startTime: String, // e.g., "14:00"
    val endTime: String?,  // nullable, e.g., "16:00"
    val timestamp: Long // Unix timestamp
)
