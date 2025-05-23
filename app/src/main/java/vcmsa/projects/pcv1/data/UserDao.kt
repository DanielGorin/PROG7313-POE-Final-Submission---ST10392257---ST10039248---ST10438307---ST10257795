package vcmsa.projects.pcv1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Insert
    suspend fun insertUser(user: User)

    @Query("UPDATE users SET profile_image_uri = :uri WHERE id = :userId")
    suspend fun updateProfileImage(userId: Int, uri: String?)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT profile_image_uri FROM users WHERE id = :userId")
    suspend fun getProfileImageUri(userId: Int): String?
}
