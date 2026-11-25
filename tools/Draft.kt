import com.thingisyou.engine.*

/**
 * Reverse drafter.
 *
 * Builds a level by starting from the winning position and stepping *backwards*. Every
 * backward step is the exact inverse of a legal forward move:
 *
 *   forward : player at A presses d, lands on B, and shoves whatever stood on B to B+d
 *   backward: player at B steps back to A, and drags the thing at B+d into B
 *
 * So any sequence of backward steps unwinds into a forward solution that is legal by
 * construction. The move list is printed alongside the board, and replaying it is the proof.
 */
class Draft(val w: Int, val h: Int) {

    val items = ArrayList<Item>()
    var px = 0
    var py = 0
    private val back = ArrayList<Dir>()

    private fun at(x: Int, y: Int) = items.indices.filter { items[it].x == x && items[it].y == y }

    fun place(word: Word, isText: Boolean, x: Int, y: Int) = items.add(Item(word, isText, x, y))

    fun setPlayer(x: Int, y: Int) {
        px = x; py = y
        val i = items.indexOfFirst { !it.isText && it.word == Word.THING }
        items[i] = items[i].copy(x = x, y = y)
    }

    fun itemAt(x: Int, y: Int): Int? = at(x, y).firstOrNull { items[it].isText }

    /** Step the player one tile in [d] backwards, optionally dragging [drag] along. */
    fun unmove(d: Dir, drag: Int? = null): Boolean {
        val ax = px - d.dx
        val ay = py - d.dy
        if (ax < 0 || ay < 0 || ax >= w || ay >= h) return false
        // In forward time the player stood on an empty tile and pushed from there.
        if (at(ax, ay).isNotEmpty()) return false
        if (drag != null) {
            val cx = px + d.dx
            val cy = py + d.dy
            if (cx < 0 || cy < 0 || cx >= w || cy >= h) return false
            val it = items[drag]
            if (it.x != cx || it.y != cy) return false
            items[drag] = it.copy(x = px, y = py)
        }
        px = ax; py = ay
        val pi = items.indexOfFirst { !it.isText && it.word == Word.THING }
        items[pi] = items[pi].copy(x = px, y = py)
        back.add(d)
        return true
    }

    /** Drag whatever text tile sits at (px+d.dx, py+d.dy) along a backward step. */
    fun unmoveDragging(d: Dir): Boolean {
        val idx = itemAt(px + d.dx, py + d.dy) ?: return false
        return unmove(d, idx)
    }

    /**
     * A backward step in direction d is undone by a forward step in the SAME direction d:
     * forward, the player stands at A, presses d, lands on B = A + d and shoves whatever was
     * on B to B + d. Backward we moved the player from B to A = B - d and pulled that tile
     * from B + d into B. So the character is d itself, and the order is reversed.
     */
    fun solution(): String = back.reversed().joinToString("") {
        when (it) {
            Dir.UP -> "U"; Dir.DOWN -> "D"; Dir.LEFT -> "L"; Dir.RIGHT -> "R"; Dir.NONE -> ""
        }
    }

    fun state(): State = State(w, h, items.toList())

    fun render(): List<String> {
        val grid = Array(h) { arrayOfNulls<String>(w) }
        for (y in 0 until h) for (x in 0 until w) {
            val cell = items.filter { it.x == x && it.y == y }
            grid[y][x] = if (cell.isEmpty()) "." else cell.joinToString("+") {
                (if (it.isText) "^" else "") + it.word.label
            }
        }
        return (0 until h).map { y -> (0 until w).joinToString(" ") { grid[y][it]!! } }
    }
}
