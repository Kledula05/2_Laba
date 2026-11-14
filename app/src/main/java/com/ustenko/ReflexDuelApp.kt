package com.ustenko

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.ustenko.data.AppPreferences

class ReflexDuelApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppPreferences(this).theme.nightMode)
    }
}
