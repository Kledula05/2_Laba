package com.ustenko.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ustenko.R
import com.ustenko.data.AppDatabase
import com.ustenko.data.AppPreferences
import com.ustenko.data.Match
import com.ustenko.model.GameMode
import com.ustenko.ui.game.GameActivity
import com.ustenko.ui.settings.SettingsActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "extra_mode"
        private const val STATE_SELECTED_SCORE = "selected_score"
    }

    private lateinit var mode: GameMode

    private lateinit var etPlayer1Name: EditText
    private lateinit var etPlayer2Name: EditText
    private lateinit var llPlayer2Block: View
    private lateinit var btnPlay: Button
    private lateinit var tvHistoryPlaceholder: TextView
    private lateinit var llHistoryList: LinearLayout
    private lateinit var preferences: AppPreferences

    private lateinit var chipScore3: TextView
    private lateinit var chipScore5: TextView
    private lateinit var chipScore7: TextView
    private var selectedScore = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mode = GameMode.valueOf(intent.getStringExtra(EXTRA_MODE) ?: GameMode.DUO.name)

        etPlayer1Name = findViewById(R.id.etPlayer1Name)
        etPlayer2Name = findViewById(R.id.etPlayer2Name)
        llPlayer2Block = findViewById(R.id.llPlayer2Block)
        btnPlay = findViewById(R.id.btnPlay)
        tvHistoryPlaceholder = findViewById(R.id.tvHistoryPlaceholder)
        llHistoryList = findViewById(R.id.llHistoryList)
        preferences = AppPreferences(this)

        chipScore3 = findViewById(R.id.chipScore3)
        chipScore5 = findViewById(R.id.chipScore5)
        chipScore7 = findViewById(R.id.chipScore7)

        llPlayer2Block.visibility = if (mode == GameMode.DUO) View.VISIBLE else View.GONE

        etPlayer1Name.setText(preferences.lastPlayer1Name)
        etPlayer2Name.setText(preferences.lastPlayer2Name)
        selectedScore = savedInstanceState?.getInt(STATE_SELECTED_SCORE) ?: preferences.lastTargetScore
        updateScoreChips()

        chipScore3.setOnClickListener { selectedScore = 3; updateScoreChips() }
        chipScore5.setOnClickListener { selectedScore = 5; updateScoreChips() }
        chipScore7.setOnClickListener { selectedScore = 7; updateScoreChips() }

        btnPlay.setOnClickListener {
            onPlayClicked()
        }

        findViewById<View>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SELECTED_SCORE, selectedScore)
    }

    private fun updateScoreChips() {
        setChipSelected(chipScore3, selectedScore == 3)
        setChipSelected(chipScore5, selectedScore == 5)
        setChipSelected(chipScore7, selectedScore == 7)
    }

    private fun setChipSelected(chip: TextView, selected: Boolean) {
        chip.setBackgroundResource(if (selected) R.drawable.bg_button_filled else R.drawable.bg_button_outline)
        chip.setTextColor(ContextCompat.getColor(this, if (selected) R.color.white else R.color.text_secondary))
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            val matches = AppDatabase.getInstance(applicationContext).matchDao().getRecent(5)
            llHistoryList.removeAllViews()

            if (matches.isEmpty()) {
                tvHistoryPlaceholder.visibility = View.VISIBLE
            } else {
                tvHistoryPlaceholder.visibility = View.GONE
                for (match in matches) {
                    llHistoryList.addView(createHistoryItemView(match))
                }
            }
        }
    }

    private fun createHistoryItemView(match: Match): View {
        val view = LayoutInflater.from(this).inflate(R.layout.item_match_history, llHistoryList, false)

        if (match.mode == GameMode.DUO.name) {
            view.findViewById<TextView>(R.id.tvHistoryNames).text =
                getString(R.string.history_vs, match.player1Name, match.player2Name)
            view.findViewById<TextView>(R.id.tvHistoryScore).text =
                getString(R.string.score_format, match.score1, match.score2)
        } else {
            view.findViewById<TextView>(R.id.tvHistoryNames).text =
                getString(R.string.history_single, match.player1Name)
            view.findViewById<TextView>(R.id.tvHistoryScore).text =
                if (match.bestReaction > 0) getString(R.string.ms_format, match.bestReaction.toInt()) else "—"
        }

        view.findViewById<TextView>(R.id.tvHistoryDate).text = formatPlayedAt(match.playedAt)
        return view
    }

    private fun formatPlayedAt(timestampMs: Long): String {
        val locale = resources.configuration.locales[0]
        val timeFormat = SimpleDateFormat("HH:mm", locale)
        val time = timeFormat.format(timestampMs)

        val played = Calendar.getInstance().apply { timeInMillis = timestampMs }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return when {
            isSameDay(played, today) -> getString(R.string.history_date_today, time)
            isSameDay(played, yesterday) -> getString(R.string.history_date_yesterday, time)
            else -> SimpleDateFormat("dd.MM, HH:mm", locale).format(timestampMs)
        }
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    private fun onPlayClicked() {
        val player1Name = etPlayer1Name.text.toString().ifBlank { getString(R.string.player1_hint) }
        val player2Name = etPlayer2Name.text.toString().ifBlank { getString(R.string.player2_hint) }

        preferences.lastPlayer1Name = etPlayer1Name.text.toString()
        preferences.lastPlayer2Name = etPlayer2Name.text.toString()
        preferences.lastTargetScore = selectedScore

        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra(GameActivity.EXTRA_MODE, mode.name)
            putExtra(GameActivity.EXTRA_PLAYER1_NAME, player1Name)
            putExtra(GameActivity.EXTRA_PLAYER2_NAME, player2Name)
            putExtra(GameActivity.EXTRA_TARGET_SCORE, selectedScore)
        }
        startActivity(intent)
    }
}
