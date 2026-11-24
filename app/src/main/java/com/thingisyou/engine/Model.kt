package com.thingisyou.engine

/** One thing standing on one tile. [isText] false means it is the object, true means it is the word tile. */
data class Item(
    val word: Word,
    val isText: Boolean,
    val x: Int,
    val y: Int,
    val dir: Dir = Dir.NONE
) {
    fun at(nx: Int, ny: Int): Item = copy(x = nx, y = ny)

    /** Canonical sort key, so two boards that differ only in list order hash the same. */
    fun key(): String = "$x,$y,${word.label},${if (isText) 1 else 0},${dir.ordinal}"
}

/** An immutable board. Every turn produces a new State, which makes undo and search trivial. */
data class State(val w: Int, val h: Int, val items: List<Item>) {

    fun itemsAt(x: Int, y: Int): List<Int> {
        val out = ArrayList<Int>(2)
        for (i in items.indices) if (items[i].x == x && items[i].y == y) out.add(i)
        return out
    }

    fun firstTextAt(x: Int, y: Int): Word? {
        for (i in items.indices) {
            val it = items[i]
            if (it.x == x && it.y == y && it.isText) return it.word
        }
        return null
    }

    fun hasNoun(x: Int, y: Int, noun: Word): Boolean {
        for (i in items.indices) {
            val it = items[i]
            if (it.x == x && it.y == y && matches(it, noun)) return true
        }
        return false
    }

    fun inside(x: Int, y: Int): Boolean = x in 0 until w && y in 0 until h

    /** Stable hash key for the solver. */
    fun key(): String = items.map { it.key() }.sorted().joinToString("|")

    companion object {
        /** TEXT matches every word tile; a noun matches only the real object of that name. */
        fun matches(item: Item, noun: Word): Boolean =
            if (noun == Word.TEXT) item.isText else (!item.isText && item.word == noun)
    }
}

/** A level as authored: a token grid, one string per row. */
data class LevelDef(
    val id: String,
    val name: String,
    val rows: List<String>,
    val hint: String = ""
) {
    fun build(): State = LevelParser.parse(rows)
}

object LevelParser {

    fun parse(rows: List<String>): State {
        val grid = rows.map { it.trim().split(Regex("\\s+")).filter { t -> t.isNotEmpty() } }
        val h = grid.size
        val w = grid.maxOf { it.size }
        val items = ArrayList<Item>()
        for (y in 0 until h) {
            val row = grid[y]
            for (x in 0 until row.size) {
                val token = row[x]
                if (token == "." || token == "_" || token == "-") continue
                // "BABA+GRASS" puts two things on one tile.
                for (part in token.split("+")) {
                    if (part.isEmpty()) continue
                    items.add(parseToken(part, x, y))
                }
            }
        }
        return State(w, h, items)
    }

    private fun parseToken(token: String, x: Int, y: Int): Item {
        var body = token
        val isText = body.startsWith("^")
        if (isText) body = body.substring(1)

        var dir = Dir.NONE
        if (body.length > 1) {
            val suffix = Dir.ofSuffix(body.last())
            if (suffix != null) {
                dir = suffix
                body = body.dropLast(1)
            }
        }
        val word = Word.of(body)
            ?: throw IllegalArgumentException("Unknown token '$token' at ($x,$y)")
        return Item(word, isText, x, y, dir)
    }
}

/** A level plus the running state of a play session. */
class GameSession(val level: LevelDef) {
    val initial: State = level.build()

    var state: State = initial
        private set

    private val history = ArrayList<State>()

    val canUndo: Boolean get() = history.isNotEmpty()

    var won: Boolean = false
        private set
    var lost: Boolean = false
        private set

    /** Last step's side effects, for the UI and for sound cues. */
    var lastEvents: Set<Event> = emptySet()
        private set

    fun step(dir: Dir): Set<Event> {
        if (won) return emptySet()
        val before = Rules.evaluate(state)
        val outcome = Engine.step(state, dir)
        val after = Rules.evaluate(outcome.state)

        val events = HashSet<Event>()
        events.addAll(outcome.events)
        if (!before.ruleSignature().equals(after.ruleSignature())) {
            if (after.rules.isNotEmpty() && before.ruleSignature() != after.ruleSignature()) {
                events.add(Event.RULE_CHANGED)
            }
        }
        if (outcome.state != state) {
            history.add(state)
            state = outcome.state
        }
        if (outcome.win) {
            won = true
            events.add(Event.WIN)
        }
        if (outcome.defeat) {
            lost = true
            events.add(Event.DEFEAT)
        }
        lastEvents = events
        return events
    }

    fun undo() {
        if (history.isEmpty()) return
        state = history.removeAt(history.size - 1)
        won = false
        lost = false
    }

    fun restart() {
        state = initial
        history.clear()
        won = false
        lost = false
    }

    /** True when nothing on the board is under player control any more. */
    fun stuck(): Boolean = !won && Rules.evaluate(state).hasYou.not()
}

enum class Event { RULE_CHANGED, TRANSFORM, SINK, MELT, WIN, DEFEAT, TELE }

/**
 * Order-independent 64-bit fingerprint of a board, used by the build-time solver.
 * Two boards that differ only in list order produce the same value.
 */
fun State.fingerprint(): Long {
    var h = 0L
    for (item in items) {
        var k = 1125899906842597L
        k = k * 31 + item.x
        k = k * 31 + item.y
        k = k * 31 + item.word.ordinal
        k = k * 31 + (if (item.isText) 1 else 0)
        k = k * 31 + item.dir.ordinal
        k *= -7046029254386353131L
        k = k xor (k ushr 29)
        h += k
    }
    return h
}
