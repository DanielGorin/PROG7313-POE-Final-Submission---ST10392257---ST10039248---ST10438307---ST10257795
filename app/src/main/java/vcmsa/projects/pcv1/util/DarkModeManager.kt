package vcmsa.projects.pcv1.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
// Handles app-wide dark mode preference
object DarkModeManager {
    private const val PREF_NAME = "settings"
    private const val KEY_DARK_MODE = "dark_mode"
    // Check if dark mode is enabled
    fun isDarkModeEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }
    // Enable or disable dark mode and apply the change
    fun setDarkMode(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()

        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}

