package vcmsa.projects.pcv1.util

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    fun saveUserId(id: Int) = prefs.edit().putInt("user_id", id).apply()
    fun getUserId(): Int = prefs.getInt("user_id", -1)
    fun clear() = prefs.edit().clear().apply()
    fun isLoggedIn() = getUserId() != -1
}