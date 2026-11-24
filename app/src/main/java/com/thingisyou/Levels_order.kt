package com.thingisyou

import android.content.Context
import com.thingisyou.engine.Levels

/** Tiny helper so the main screen knows which puzzle to jump into. */
object Levels_order {

    fun firstUnfinished(context: Context): String {
        for (lvl in Levels.all) {
            if (!Progress.isSolved(context, lvl.id)) return lvl.id
        }
        return Levels.all.first().id
    }

    fun nextAfter(id: String): String? {
        val idx = Levels.all.indexOfFirst { it.id == id }
        if (idx < 0) return null
        return Levels.all.getOrNull(idx + 1)?.id
    }
}
