package vcmsa.projects.pcv1.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

class UserRepository(private val userDao: UserDao) {

    suspend fun register(username: String, rawPassword: String): Result<User> =
        withContext(Dispatchers.IO) {
            // Unique username check
            if (userDao.getUserByUsername(username) != null) {
                return@withContext Result.failure(Exception("Username already exists"))
            }
            // Hash password
            val hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt())
            val user = User(username = username, passwordHash = hash)
            userDao.insertUser(user)
            // Retrieve inserted user with ID
            val inserted = userDao.getUserByUsername(username)!!
            Result.success(inserted)
        }

    suspend fun login(username: String, rawPassword: String): Result<User> =
        withContext(Dispatchers.IO) {
            val user = userDao.getUserByUsername(username)
                ?: return@withContext Result.failure(Exception("Invalid credentials"))
            if (!BCrypt.checkpw(rawPassword, user.passwordHash)) {
                return@withContext Result.failure(Exception("Invalid credentials"))
            }
            Result.success(user)
        }
}
