package com.ustenko.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ustenko.R
import com.ustenko.data.AppDatabase
import com.ustenko.data.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferences = AppPreferences(this)

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

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

        findViewById<TextView>(R.id.btnClearHistory).setOnClickListener {
            confirmClearHistory()
        }
    }

    private fun confirmClearHistory() {
        AlertDialog.Builder(this)
            .setTitle(R.string.settings_clear_confirm_title)
            .setMessage(R.string.settings_clear_confirm_message)
            .setPositiveButton(R.string.settings_clear_confirm_yes) { _, _ -> clearHistory() }
            .setNegativeButton(R.string.settings_clear_confirm_cancel, null)
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
