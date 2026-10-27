package com.ustenko.ui.game

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ustenko.R
import com.ustenko.model.GameMode
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_PLAYER1_NAME = "extra_player1_name"
        const val EXTRA_PLAYER2_NAME = "extra_player2_name"
        const val EXTRA_TARGET_SCORE = "extra_target_score"

        private const val ROUND_PAUSE_MS = 1200L
        private const val MIN_DELAY_MS = 1500L
        private const val MAX_DELAY_MS = 10000L
    }

    private enum class RoundPhase { WAITING, SIGNAL, ROUND_OVER }

    private lateinit var mode: GameMode
    private lateinit var player1Name: String
    private lateinit var player2Name: String
    private var targetScore = 5

    private lateinit var topHalf: FrameLayout
    private lateinit var bottomHalf: FrameLayout
    private lateinit var tvPlayer1Name: TextView
    private lateinit var tvPlayer1Score: TextView
    private lateinit var tvPlayer1Status: TextView
    private lateinit var tvPlayer2Name: TextView
    private lateinit var tvPlayer2Score: TextView
    private lateinit var tvPlayer2Status: TextView

    private var score1 = 0
    private var score2 = 0
    private var phase = RoundPhase.WAITING
    private var signalTimeMs = 0L

    private val handler = Handler(Looper.getMainLooper())
    private val signalRunnable = Runnable { showSignal() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        mode = GameMode.valueOf(intent.getStringExtra(EXTRA_MODE) ?: GameMode.DUO.name)
        player1Name = intent.getStringExtra(EXTRA_PLAYER1_NAME) ?: getString(R.string.player1_hint)
        player2Name = intent.getStringExtra(EXTRA_PLAYER2_NAME) ?: getString(R.string.player2_hint)
        targetScore = intent.getIntExtra(EXTRA_TARGET_SCORE, 5)

        topHalf = findViewById(R.id.topHalf)
        bottomHalf = findViewById(R.id.bottomHalf)
        tvPlayer1Name = findViewById(R.id.tvPlayer1Name)
        tvPlayer1Score = findViewById(R.id.tvPlayer1Score)
        tvPlayer1Status = findViewById(R.id.tvPlayer1Status)
        tvPlayer2Name = findViewById(R.id.tvPlayer2Name)
        tvPlayer2Score = findViewById(R.id.tvPlayer2Score)
        tvPlayer2Status = findViewById(R.id.tvPlayer2Status)

        tvPlayer1Name.text = player1Name
        tvPlayer1Score.text = score1.toString()

        bottomHalf.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) onHalfTouched(1)
            true
        }

        if (mode == GameMode.DUO) {
            tvPlayer2Name.text = player2Name
            tvPlayer2Score.text = score2.toString()
            topHalf.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) onHalfTouched(2)
                true
            }
        } else {
            topHalf.visibility = View.GONE
        }

        startRound()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun startRound() {
        phase = RoundPhase.WAITING
        setHalfColor(1, R.color.round_waiting)
        setStatus(1, getString(R.string.round_ready))
        if (mode == GameMode.DUO) {
            setHalfColor(2, R.color.round_waiting)
            setStatus(2, getString(R.string.round_ready))
        }
        handler.postDelayed(signalRunnable, Random.nextLong(MIN_DELAY_MS, MAX_DELAY_MS + 1))
    }

    private fun showSignal() {
        phase = RoundPhase.SIGNAL
        signalTimeMs = SystemClock.elapsedRealtime()
        setHalfColor(1, R.color.round_signal)
        setStatus(1, getString(R.string.round_signal))
        if (mode == GameMode.DUO) {
            setHalfColor(2, R.color.round_signal)
            setStatus(2, getString(R.string.round_signal))
        }
    }

    private fun onHalfTouched(player: Int) {
        when (phase) {
            RoundPhase.WAITING -> handleFalseStart(player)
            RoundPhase.SIGNAL -> handleScore(player)
            RoundPhase.ROUND_OVER -> Unit
        }
    }

    private fun handleFalseStart(player: Int) {
        handler.removeCallbacks(signalRunnable)
        phase = RoundPhase.ROUND_OVER
        setHalfColor(player, R.color.round_falsestart)
        setStatus(player, getString(R.string.round_falsestart))

        if (mode == GameMode.DUO) {
            addScore(otherPlayer(player))
            handler.postDelayed({ nextRoundOrFinish() }, ROUND_PAUSE_MS)
        } else {
            handler.postDelayed({ startRound() }, ROUND_PAUSE_MS)
        }
    }

    private fun handleScore(player: Int) {
        phase = RoundPhase.ROUND_OVER
        val reactionMs = SystemClock.elapsedRealtime() - signalTimeMs
        addScore(player)
        setHalfColor(player, R.color.round_win)
        setStatus(player, getString(R.string.round_point, reactionMs.toInt()))

        if (mode == GameMode.DUO) {
            setHalfColor(otherPlayer(player), R.color.round_waiting)
            setStatus(otherPlayer(player), "")
        }

        handler.postDelayed({ nextRoundOrFinish() }, ROUND_PAUSE_MS)
    }

    private fun nextRoundOrFinish() {
        if (score1 >= targetScore || score2 >= targetScore) {
            finishMatch()
        } else {
            startRound()
        }
    }

    private fun finishMatch() {
        val winnerName = if (score1 >= targetScore) player1Name else player2Name
        Toast.makeText(this, getString(R.string.match_over, winnerName, score1, score2), Toast.LENGTH_LONG).show()
    }

    private fun addScore(player: Int) {
        if (player == 1) {
            score1++
            tvPlayer1Score.text = score1.toString()
        } else {
            score2++
            tvPlayer2Score.text = score2.toString()
        }
    }

    private fun otherPlayer(player: Int) = if (player == 1) 2 else 1

    private fun setHalfColor(player: Int, colorRes: Int) {
        val color = ContextCompat.getColor(this, colorRes)
        if (player == 1) bottomHalf.setBackgroundColor(color) else topHalf.setBackgroundColor(color)
    }

    private fun setStatus(player: Int, text: String) {
        if (player == 1) tvPlayer1Status.text = text else tvPlayer2Status.text = text
    }
}
