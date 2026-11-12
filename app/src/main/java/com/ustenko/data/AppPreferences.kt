package com.ustenko.data

import android.content.Context
import com.ustenko.model.AppTheme

class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var theme: AppTheme
        get() {
            val key = prefs.getString(KEY_THEME, AppTheme.SYSTEM.key)
            return AppTheme.entries.firstOrNull { it.key == key } ?: AppTheme.SYSTEM
        }
        set(value) = prefs.edit().putString(KEY_THEME, value.key).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, false)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    var lastPlayer1Name: String
        get() = prefs.getString(KEY_PLAYER1_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PLAYER1_NAME, value).apply()

    var lastPlayer2Name: String
        get() = prefs.getString(KEY_PLAYER2_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PLAYER2_NAME, value).apply()

    var lastTargetScore: Int
        get() = prefs.getInt(KEY_TARGET_SCORE, 5)
        set(value) = prefs.edit().putInt(KEY_TARGET_SCORE, value).apply()

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_THEME = "theme"
        private const val KEY_SOUND = "sound"
        private const val KEY_VIBRATION = "vibration"
        private const val KEY_PLAYER1_NAME = "last_player1_name"
        private const val KEY_PLAYER2_NAME = "last_player2_name"
        private const val KEY_TARGET_SCORE = "last_target_score"
    }
}
