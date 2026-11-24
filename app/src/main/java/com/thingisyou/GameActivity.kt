package com.thingisyou

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.thingisyou.engine.GameSession
import com.thingisyou.engine.LevelDef
import com.thingisyou.engine.Levels

/** One puzzle, filling the screen. Nothing else. */
class GameActivity : Activity() {

    private lateinit var view: GameView
    private lateinit var session: GameSession
    private lateinit var level: LevelDef

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        Sfx.init(this)

        val id = intent.getStringExtra("levelId") ?: Levels.all.first().id
        level = Levels.byId(id) ?: Levels.all.first()
        session = GameSession(level)

        view = findViewById(R.id.gameView)
        view.bind(session)
        view.onEvents = { Sfx.playEvents(it) }
        view.onEnd = { won -> showOverlay(won) }

        findViewById<TextView>(R.id.levelName).text = "${level.id}   ${level.name}"

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            Sfx.click()
            finish()
        }

        findViewById<Button>(R.id.btnUndo).setOnClickListener {
            Sfx.click()
            session.undo()
            view.refresh(false)
            hideOverlay()
        }

        findViewById<Button>(R.id.btnRestart).setOnClickListener {
            Sfx.click()
            session.restart()
            view.refresh(false)
            hideOverlay()
        }

        findViewById<Button>(R.id.btnRetry).setOnClickListener {
            Sfx.click()
            session.restart()
            view.refresh(false)
            hideOverlay()
        }

        findViewById<Button>(R.id.btnMenu).setOnClickListener {
            Sfx.click()
            startActivity(Intent(this, LevelSelectActivity::class.java))
            finish()
        }
    }

    private fun hideOverlay() {
        findViewById<View>(R.id.overlay).visibility = View.GONE
    }

    private fun showOverlay(won: Boolean) {
        if (won) Progress.markSolved(this, level.id)

        findViewById<View>(R.id.overlay).visibility = View.VISIBLE

        val title = findViewById<TextView>(R.id.overlayTitle)
        title.text = getString(if (won) R.string.solved else R.string.defeated)
        title.setTextColor(getColor(if (won) R.color.good else R.color.bad))

        findViewById<TextView>(R.id.overlayHint).text =
            getString(if (won) R.string.solved_hint else R.string.defeated_hint)

        val nextId = Levels_order.nextAfter(level.id)
        val btnNext = findViewById<Button>(R.id.btnNext)
        if (won && nextId != null) {
            btnNext.visibility = View.VISIBLE
            btnNext.setOnClickListener {
                Sfx.click()
                startActivity(
                    Intent(this, GameActivity::class.java).putExtra("levelId", nextId)
                )
                finish()
            }
        } else {
            btnNext.visibility = View.GONE
        }
    }
}
