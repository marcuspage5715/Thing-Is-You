package com.thingisyou

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import com.thingisyou.engine.Levels

/** Plain grid of puzzles. No islands, no map, no cutscenes. */
class LevelSelectActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels)
        Sfx.init(this)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            Sfx.click()
            finish()
        }

        val grid = findViewById<GridLayout>(R.id.grid)
        val dm = resources.displayMetrics
        val gap = (5 * dm.density).toInt()
        val side = (12 * dm.density).toInt()
        val tile = ((dm.widthPixels - side * 2 - gap * 8) / 4)

        for (lvl in Levels.all) {
            val tv = TextView(this)
            tv.text = lvl.id
            tv.gravity = Gravity.CENTER
            tv.setTextColor(getColor(R.color.text))
            tv.textSize = 18f
            tv.typeface = Typeface.create("sans-serif", Typeface.BOLD)
            tv.setBackgroundResource(R.drawable.tile_bg)
            tv.isSelected = Progress.isSolved(this, lvl.id)

            val lp = GridLayout.LayoutParams()
            lp.width = tile
            lp.height = tile
            lp.setMargins(gap, gap, gap, gap)
            tv.layoutParams = lp

            tv.setOnClickListener {
                Sfx.click()
                startActivity(
                    Intent(this, GameActivity::class.java).putExtra("levelId", lvl.id)
                )
            }
            grid.addView(tv)
        }
    }
}
