package com.thingisyou

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            Sfx.click()
            val next = Levels_order.firstUnfinished(this)
            startActivity(Intent(this, GameActivity::class.java).putExtra("levelId", next))
        }

        findViewById<Button>(R.id.btnLevels).setOnClickListener {
            Sfx.click()
            startActivity(Intent(this, LevelSelectActivity::class.java))
        }

        findViewById<Button>(R.id.btnExit).setOnClickListener {
            Sfx.click()
            finishAffinity()
        }
    }

    override fun onResume() {
        super.onResume()
        Sfx.init(this)
    }
}
