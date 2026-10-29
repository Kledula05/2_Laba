package com.ustenko.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ustenko.R
import com.ustenko.data.AppDatabase
import com.ustenko.data.Match
import com.ustenko.model.GameMode
import com.ustenko.ui.game.GameActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var rgMode: RadioGroup
    private lateinit var etPlayer1Name: EditText
    private lateinit var etPlayer2Name: EditText
    private lateinit var rgScore: RadioGroup
    private lateinit var btnPlay: Button
    private lateinit var tvHistoryPlaceholder: TextView
    private lateinit var llHistoryList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rgMode = findViewById(R.id.rgMode)
        etPlayer1Name = findViewById(R.id.etPlayer1Name)
        etPlayer2Name = findViewById(R.id.etPlayer2Name)
        rgScore = findViewById(R.id.rgScore)
        btnPlay = findViewById(R.id.btnPlay)
        tvHistoryPlaceholder = findViewById(R.id.tvHistoryPlaceholder)
        llHistoryList = findViewById(R.id.llHistoryList)

        rgMode.setOnCheckedChangeListener { _, checkedId ->
            etPlayer2Name.visibility = if (checkedId == R.id.rbDuo) View.VISIBLE else View.GONE
        }

        btnPlay.setOnClickListener {
            onPlayClicked()
        }
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
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
        view.findViewById<TextView>(R.id.tvHistoryNames).text =
            getString(R.string.history_vs, match.player1Name, match.player2Name)
        view.findViewById<TextView>(R.id.tvHistoryScore).text =
            getString(R.string.score_format, match.score1, match.score2)
        view.findViewById<TextView>(R.id.tvHistoryDate).text = formatPlayedAt(match.playedAt)
        return view
    }

    private fun formatPlayedAt(timestampMs: Long): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = timeFormat.format(timestampMs)

        val played = Calendar.getInstance().apply { timeInMillis = timestampMs }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return when {
            isSameDay(played, today) -> getString(R.string.history_date_today, time)
            isSameDay(played, yesterday) -> getString(R.string.history_date_yesterday, time)
            else -> SimpleDateFormat("dd.MM, HH:mm", Locale.getDefault()).format(timestampMs)
        }
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    private fun onPlayClicked() {
        val mode = if (rgMode.checkedRadioButtonId == R.id.rbDuo) GameMode.DUO else GameMode.SINGLE

        val player1Name = etPlayer1Name.text.toString().ifBlank { getString(R.string.player1_hint) }
        val player2Name = etPlayer2Name.text.toString().ifBlank { getString(R.string.player2_hint) }

        val targetScore = when (rgScore.checkedRadioButtonId) {
            R.id.rbScore3 -> 3
            R.id.rbScore7 -> 7
            else -> 5
        }

        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra(GameActivity.EXTRA_MODE, mode.name)
            putExtra(GameActivity.EXTRA_PLAYER1_NAME, player1Name)
            putExtra(GameActivity.EXTRA_PLAYER2_NAME, player2Name)
            putExtra(GameActivity.EXTRA_TARGET_SCORE, targetScore)
        }
        startActivity(intent)
    }
}
