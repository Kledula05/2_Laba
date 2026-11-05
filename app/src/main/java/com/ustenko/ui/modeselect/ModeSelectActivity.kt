package com.ustenko.ui.modeselect

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ustenko.R
import com.ustenko.model.GameMode
import com.ustenko.ui.main.MainActivity

class ModeSelectActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "extra_mode"
    }

    private var selectedMode = GameMode.DUO

    private lateinit var cardSingle: View
    private lateinit var cardDuo: View
    private lateinit var checkSingle: View
    private lateinit var checkDuo: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mode_select)

        cardSingle = findViewById(R.id.cardSingle)
        cardDuo = findViewById(R.id.cardDuo)
        checkSingle = findViewById(R.id.checkSingle)
        checkDuo = findViewById(R.id.checkDuo)

        cardSingle.setOnClickListener { selectMode(GameMode.SINGLE) }
        cardDuo.setOnClickListener { selectMode(GameMode.DUO) }

        findViewById<View>(R.id.btnContinue).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_MODE, selectedMode.name)
            }
            startActivity(intent)
        }

        updateSelection()
    }

    private fun selectMode(mode: GameMode) {
        selectedMode = mode
        updateSelection()
    }

    private fun updateSelection() {
        val isSingle = selectedMode == GameMode.SINGLE
        cardSingle.background = getDrawable(if (isSingle) R.drawable.bg_card_selected else R.drawable.bg_card)
        cardDuo.background = getDrawable(if (!isSingle) R.drawable.bg_card_selected else R.drawable.bg_card)
        checkSingle.visibility = if (isSingle) View.VISIBLE else View.GONE
        checkDuo.visibility = if (!isSingle) View.VISIBLE else View.GONE
    }
}
