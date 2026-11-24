package com.thingisyou

import android.content.Context

/** Remembers which levels have been finished. */
object Progress {

    private const val FILE = "thing_is_you_progress"
    private const val KEY = "solved"

    private fun load(context: Context): MutableSet<String> {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, "") ?: ""
        return raw.split(",").filter { it.isNotEmpty() }.toMutableSet()
    }

    fun isSolved(context: Context, id: String): Boolean = load(context).contains(id)

    fun markSolved(context: Context, id: String) {
        val set = load(context)
        if (set.add(id)) {
            context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
                .edit().putString(KEY, set.joinToString(",")).apply()
        }
    }

    fun solvedCount(context: Context): Int = load(context).size
}
