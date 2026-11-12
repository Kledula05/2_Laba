package com.ustenko.model

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import com.ustenko.R

enum class AppTheme(val key: String, val nightMode: Int, @get:StringRes val label: Int) {
    LIGHT("light", AppCompatDelegate.MODE_NIGHT_NO, R.string.settings_theme_light),
    DARK("dark", AppCompatDelegate.MODE_NIGHT_YES, R.string.settings_theme_dark),
    SYSTEM("system", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, R.string.settings_theme_system)
}
