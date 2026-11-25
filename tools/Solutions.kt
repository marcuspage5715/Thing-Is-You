package com.thingisyou.engine

/**
 * Authored solutions, keyed by level id.
 *
 * These exist because of how the levels are designed: the final winning position is chosen
 * first, then the board is scrambled backwards from it. The move list is therefore known
 * before the level ships, and checking a level means replaying it - not searching for it.
 *
 * A level with an entry here is *proven* solvable the moment the replay wins. That is a much
 * stronger guarantee than "the solver found something within its budget", and it is instant.
 */
object Solutions {

    val byId: Map<String, String> = mapOf(
        // Corner Office: bring FLAG and IS down to the immovable WIN in the corner.
        "21" to "URUURRURDDDDUUUURDDDDLLL"
    )

    fun of(id: String): String? = byId[id]
}
