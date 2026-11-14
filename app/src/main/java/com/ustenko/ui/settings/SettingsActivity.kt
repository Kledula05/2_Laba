package com.ustenko.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ustenko.R
import com.ustenko.data.AppDatabase
import com.ustenko.data.AppPreferences
import com.ustenko.model.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferences = AppPreferences(this)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.rowLanguage).setOnClickListener { showLanguageDialog() }
        val themeValue = findViewById<TextView>(R.id.tvThemeValue)
        themeValue.setText(preferences.theme.label)
        findViewById<View>(R.id.rowTheme).setOnClickListener { showThemeDialog(themeValue) }

        val swSound = findViewById<Switch>(R.id.swSound)
        swSound.isChecked = preferences.soundEnabled
        swSound.setOnCheckedChangeListener { _, isChecked ->
            preferences.soundEnabled = isChecked
        }

        val swVibration = findViewById<Switch>(R.id.swVibration)
        swVibration.isChecked = preferences.vibrationEnabled
        swVibration.setOnCheckedChangeListener { _, isChecked ->
            preferences.vibrationEnabled = isChecked
        }

        findViewById<View>(R.id.btnClearHistory).setOnClickListener {
            confirmClearHistory()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("ru", "en")
        val labels = arrayOf(getString(R.string.language_russian), getString(R.string.language_english))
        val selected = labels.indexOf(getString(R.string.settings_language_value))

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_language)
            .setSingleChoiceItems(labels, selected) { dialog, index ->
                dialog.dismiss()
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languages[index]))
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun showThemeDialog(themeValue: TextView) {
        val themes = AppTheme.entries
        val labels = themes.map { getString(it.label) }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_theme)
            .setSingleChoiceItems(labels, themes.indexOf(preferences.theme)) { dialog, index ->
                val theme = themes[index]
                preferences.theme = theme
                themeValue.setText(theme.label)
                dialog.dismiss()
                AppCompatDelegate.setDefaultNightMode(theme.nightMode)
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun confirmClearHistory() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_clear_confirm_title)
            .setMessage(R.string.settings_clear_confirm_message)
            .setPositiveButton(R.string.settings_clear_confirm_yes) { _, _ -> clearHistory() }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun clearHistory() {
        lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance(applicationContext).matchDao().clear()
            launch(Dispatchers.Main) {
                Toast.makeText(this@SettingsActivity, R.string.settings_cleared_toast, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
