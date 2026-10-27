package com.ustenko.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ustenko.R
import com.ustenko.model.GameMode
import com.ustenko.ui.game.GameActivity

class   MainActivity : AppCompatActivity() {

    private lateinit var rgMode: RadioGroup
    private lateinit var etPlayer1Name: EditText
    private lateinit var etPlayer2Name: EditText
    private lateinit var rgScore: RadioGroup
    private lateinit var btnPlay: Button

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

        rgMode.setOnCheckedChangeListener { _, checkedId ->
            etPlayer2Name.visibility = if (checkedId == R.id.rbDuo) View.VISIBLE else View.GONE
        }

        btnPlay.setOnClickListener {
            onPlayClicked()
        }
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
