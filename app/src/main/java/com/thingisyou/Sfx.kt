package com.thingisyou

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.thingisyou.engine.Event

/**
 * Short one-shot sound effects. There is deliberately no sound for ordinary movement;
 * only things that actually happen to the world make a noise.
 */
object Sfx {

    private var pool: SoundPool? = null
    private val ids = HashMap<Int, Int>()

    private val ALL = intArrayOf(
        R.raw.sfx_rule,
        R.raw.sfx_transform,
        R.raw.sfx_sink,
        R.raw.sfx_melt,
        R.raw.sfx_tele,
        R.raw.sfx_win,
        R.raw.sfx_defeat,
        R.raw.sfx_click
    )

    fun init(context: Context) {
        if (pool != null) return
        val p = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
        pool = p
        for (res in ALL) ids[res] = p.load(context, res, 1)
    }

    fun play(res: Int) {
        val p = pool ?: return
        val id = ids[res] ?: return
        p.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun click() = play(R.raw.sfx_click)

    fun playEvents(events: Set<Event>) {
        if (events.contains(Event.WIN)) {
            play(R.raw.sfx_win)
            return
        }
        if (events.contains(Event.DEFEAT)) {
            play(R.raw.sfx_defeat)
            return
        }
        if (events.contains(Event.TRANSFORM)) play(R.raw.sfx_transform)
        if (events.contains(Event.SINK)) play(R.raw.sfx_sink)
        if (events.contains(Event.MELT)) play(R.raw.sfx_melt)
        if (events.contains(Event.TELE)) play(R.raw.sfx_tele)
        if (events.contains(Event.RULE_CHANGED)) play(R.raw.sfx_rule)
    }

    fun release() {
        pool?.release()
        pool = null
        ids.clear()
    }
}
