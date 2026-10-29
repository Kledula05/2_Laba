package com.ustenko.ui.result

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ustenko.R
import com.ustenko.model.GameMode
import com.ustenko.ui.game.GameActivity
import com.ustenko.ui.main.MainActivity

class ResultActivity : AppCompatActivity() {

    companion object {
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

        val player1Name = intent.getStringExtra(EXTRA_PLAYER1_NAME) ?: ""
        val player2Name = intent.getStringExtra(EXTRA_PLAYER2_NAME) ?: ""
        val score1 = intent.getIntExtra(EXTRA_SCORE1, 0)
        val score2 = intent.getIntExtra(EXTRA_SCORE2, 0)
        val avg1 = intent.getLongExtra(EXTRA_AVG1, 0)
        val avg2 = intent.getLongExtra(EXTRA_AVG2, 0)
        val best1 = intent.getLongExtra(EXTRA_BEST1, 0)
        val best2 = intent.getLongExtra(EXTRA_BEST2, 0)
        val falseStarts1 = intent.getIntExtra(EXTRA_FALSESTARTS1, 0)
        val falseStarts2 = intent.getIntExtra(EXTRA_FALSESTARTS2, 0)
        val targetScore = intent.getIntExtra(EXTRA_TARGET_SCORE, 5)

        val winnerName = if (score1 >= score2) player1Name else player2Name

        findViewById<TextView>(R.id.tvWinnerName).text = winnerName
        findViewById<TextView>(R.id.tvScore).text = getString(R.string.score_format, score1, score2)
        findViewById<TextView>(R.id.tvPlayer1Header).text = player1Name
        findViewById<TextView>(R.id.tvPlayer2Header).text = player2Name

        findViewById<TextView>(R.id.tvAvg1).text = formatMs(avg1)
        findViewById<TextView>(R.id.tvAvg2).text = formatMs(avg2)
        highlightBetter(avg1, avg2, R.id.tvAvg1, R.id.tvAvg2, R.id.starAvg1, R.id.starAvg2)

        findViewById<TextView>(R.id.tvBest1).text = formatMs(best1)
        findViewById<TextView>(R.id.tvBest2).text = formatMs(best2)
        highlightBetter(best1, best2, R.id.tvBest1, R.id.tvBest2, R.id.starBest1, R.id.starBest2)

        findViewById<TextView>(R.id.tvFalseStarts1).text = falseStarts1.toString()
        findViewById<TextView>(R.id.tvFalseStarts2).text = falseStarts2.toString()
        highlightBetter(
            falseStarts1.toLong(), falseStarts2.toLong(),
            R.id.tvFalseStarts1, R.id.tvFalseStarts2, R.id.starFalseStarts1, R.id.starFalseStarts2
        )

        findViewById<View>(R.id.btnRematch).setOnClickListener {
            val intent = Intent(this, GameActivity::class.java).apply {
                putExtra(GameActivity.EXTRA_MODE, GameMode.DUO.name)
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
