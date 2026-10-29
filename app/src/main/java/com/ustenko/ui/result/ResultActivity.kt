package com.ustenko.ui.result

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ustenko.R
import com.ustenko.data.AppDatabase
import com.ustenko.data.Match
import com.ustenko.model.GameMode
import com.ustenko.ui.game.GameActivity
import com.ustenko.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResultActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_PLAYER1_NAME = "extra_player1_name"
        const val EXTRA_PLAYER2_NAME = "extra_player2_name"
        const val EXTRA_SCORE1 = "extra_score1"
        const val EXTRA_SCORE2 = "extra_score2"
        const val EXTRA_AVG1 = "extra_avg1"
        const val EXTRA_AVG2 = "extra_avg2"
        const val EXTRA_BEST1 = "extra_best1"
        const val EXTRA_BEST2 = "extra_best2"
        const val EXTRA_FALSESTARTS1 = "extra_falsestarts1"
        const val EXTRA_FALSESTARTS2 = "extra_falsestarts2"
        const val EXTRA_TARGET_SCORE = "extra_target_score"

        const val NO_DATA = -1L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val mode = GameMode.valueOf(intent.getStringExtra(EXTRA_MODE) ?: GameMode.DUO.name)
        val player1Name = intent.getStringExtra(EXTRA_PLAYER1_NAME) ?: ""
        val player2Name = intent.getStringExtra(EXTRA_PLAYER2_NAME) ?: ""
        val score1 = intent.getIntExtra(EXTRA_SCORE1, 0)
        val score2 = intent.getIntExtra(EXTRA_SCORE2, 0)
        val avg1 = intent.getLongExtra(EXTRA_AVG1, NO_DATA)
        val avg2 = intent.getLongExtra(EXTRA_AVG2, NO_DATA)
        val best1 = intent.getLongExtra(EXTRA_BEST1, NO_DATA)
        val best2 = intent.getLongExtra(EXTRA_BEST2, NO_DATA)
        val falseStarts1 = intent.getIntExtra(EXTRA_FALSESTARTS1, 0)
        val falseStarts2 = intent.getIntExtra(EXTRA_FALSESTARTS2, 0)
        val targetScore = intent.getIntExtra(EXTRA_TARGET_SCORE, 5)

        val winnerName = if (mode == GameMode.SINGLE || score1 >= score2) player1Name else player2Name

        findViewById<TextView>(R.id.tvWinnerName).text = winnerName
        findViewById<TextView>(R.id.tvSubtitle).text =
            if (mode == GameMode.SINGLE) getString(R.string.result_practice_label) else getString(R.string.result_win_label)
        findViewById<TextView>(R.id.tvScore).text =
            if (mode == GameMode.SINGLE) score1.toString() else getString(R.string.score_format, score1, score2)

        findViewById<TextView>(R.id.tvPlayer1Header).text = player1Name
        findViewById<TextView>(R.id.tvAvg1).text = formatMs(avg1)
        findViewById<TextView>(R.id.tvBest1).text = formatMs(best1)
        findViewById<TextView>(R.id.tvFalseStarts1).text = falseStarts1.toString()

        if (mode == GameMode.DUO) {
            findViewById<TextView>(R.id.tvPlayer2Header).text = player2Name
            findViewById<TextView>(R.id.tvAvg2).text = formatMs(avg2)
            findViewById<TextView>(R.id.tvBest2).text = formatMs(best2)
            findViewById<TextView>(R.id.tvFalseStarts2).text = falseStarts2.toString()

            highlightBetter(avg1, avg2, R.id.tvAvg1, R.id.tvAvg2, R.id.starAvg1, R.id.starAvg2)
            highlightBetter(best1, best2, R.id.tvBest1, R.id.tvBest2, R.id.starBest1, R.id.starBest2)
            highlightBetter(
                falseStarts1.toLong(), falseStarts2.toLong(),
                R.id.tvFalseStarts1, R.id.tvFalseStarts2, R.id.starFalseStarts1, R.id.starFalseStarts2
            )

            saveMatch(player1Name, player2Name, score1, score2, targetScore, avg1, avg2, best1, best2, falseStarts1, falseStarts2)
        } else {
            findViewById<View>(R.id.tvPlayer2Header).visibility = View.GONE
            findViewById<View>(R.id.avgContainer2).visibility = View.GONE
            findViewById<View>(R.id.bestContainer2).visibility = View.GONE
            findViewById<View>(R.id.falseStartsContainer2).visibility = View.GONE
        }

        findViewById<View>(R.id.btnRematch).setOnClickListener {
            val intent = Intent(this, GameActivity::class.java).apply {
                putExtra(GameActivity.EXTRA_MODE, mode.name)
                putExtra(GameActivity.EXTRA_PLAYER1_NAME, player1Name)
                putExtra(GameActivity.EXTRA_PLAYER2_NAME, player2Name)
                putExtra(GameActivity.EXTRA_TARGET_SCORE, targetScore)
            }
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.btnMenu).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun saveMatch(
        player1Name: String,
        player2Name: String,
        score1: Int,
        score2: Int,
        targetScore: Int,
        avg1: Long,
        avg2: Long,
        best1: Long,
        best2: Long,
        falseStarts1: Int,
        falseStarts2: Int
    ) {
        val match = Match(
            player1Name = player1Name,
            player2Name = player2Name,
            score1 = score1,
            score2 = score2,
            targetScore = targetScore,
            avgReaction1 = avg1.takeIf { it != NO_DATA } ?: 0L,
            avgReaction2 = avg2.takeIf { it != NO_DATA } ?: 0L,
            bestReaction = minOf(best1.takeIf { it != NO_DATA } ?: Long.MAX_VALUE, best2.takeIf { it != NO_DATA } ?: Long.MAX_VALUE)
                .takeIf { it != Long.MAX_VALUE } ?: 0L,
            falseStarts = "$falseStarts1,$falseStarts2",
            playedAt = System.currentTimeMillis()
        )
        lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance(applicationContext).matchDao().insert(match)
        }
    }

    private fun formatMs(value: Long): String {
        return if (value == NO_DATA) "—" else getString(R.string.ms_format, value.toInt())
    }

    private fun highlightBetter(v1: Long, v2: Long, textId1: Int, textId2: Int, starId1: Int, starId2: Int) {
        if (v1 == v2 || v1 == NO_DATA || v2 == NO_DATA) return

        val betterTextId = if (v1 < v2) textId1 else textId2
        val betterStarId = if (v1 < v2) starId1 else starId2

        val betterText = findViewById<TextView>(betterTextId)
        betterText.setTextColor(ContextCompat.getColor(this, R.color.blue_primary))
        betterText.setTypeface(null, android.graphics.Typeface.BOLD)
        findViewById<View>(betterStarId).visibility = View.VISIBLE
    }
}
