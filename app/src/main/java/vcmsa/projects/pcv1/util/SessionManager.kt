package vcmsa.projects.pcv1.util

import android.content.Context
// Manages user session using SharedPreferences
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    // Save user ID to preferences
    fun saveUserId(id: Int) = prefs.edit().putInt("user_id", id).apply()
    // Retrieves stored user ID
    fun getUserId(): Int = prefs.getInt("user_id", -1)
    // Save username to preferences
    fun saveUsername(username: String) = prefs.edit().putString("username", username).apply()
    // Retrieve stored username
    fun getUsername(): String? = prefs.getString("username", null)
    // Clear all stored session data
    fun clear() = prefs.edit().clear().apply()
    // Check if a user is logged in
    fun isLoggedIn() = getUserId() != -1
}
