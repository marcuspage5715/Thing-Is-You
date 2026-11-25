import com.thingisyou.engine.*

/**
 * Route-planning scrambler.
 *
 * Unwinds from the winning position, but instead of a fixed script it plans: the player is
 * routed around obstacles with a BFS, and each word tile is dragged to a chosen cell. Every
 * step is still the exact inverse of a legal forward move, so the derived solution is legal
 * by construction and replaying it is the proof.
 */

private fun opposite(d: Dir) = when (d) {
    Dir.UP -> Dir.DOWN; Dir.DOWN -> Dir.UP
    Dir.LEFT -> Dir.RIGHT; Dir.RIGHT -> Dir.LEFT; Dir.NONE -> Dir.NONE
}

// The player's own cell is not an obstacle - it is where the player already stands.
fun Draft.occupied(x: Int, y: Int) =
    items.any { it.x == x && it.y == y && !(!it.isText && it.word == Word.THING) }

/** Walk the player to (tx,ty) through empty cells. Each step is one forward move. */
fun Draft.walkTo(tx: Int, ty: Int): Boolean {
    if (px == tx && py == ty) return true
    val start = py * w + px
    val prev = HashMap<Int, Int>()
    val q = ArrayDeque<Int>()
    prev[start] = -1
    q.add(start)
    val target = ty * w + tx
    while (q.isNotEmpty()) {
        val c = q.removeFirst()
        if (c == target) break
        val cx = c % w; val cy = c / w
        for ((dx, dy) in listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)) {
            val nx = cx + dx; val ny = cy + dy
            if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue
            val k = ny * w + nx
            if (prev.containsKey(k)) continue
            if (occupied(nx, ny)) continue
            prev[k] = c
            q.add(k)
        }
    }
    if (!prev.containsKey(target)) return false
    val path = ArrayList<Int>()
    var c = target
    while (c != -1) { path.add(c); c = prev[c]!! }
    path.reverse()
    for (i in 1 until path.size) {
        val from = path[i - 1]; val to = path[i]
        val fx = from % w; val fy = from / w
        val tx2 = to % w; val ty2 = to / w
        val d = when {
            tx2 > fx -> Dir.LEFT
            tx2 < fx -> Dir.RIGHT
            ty2 > fy -> Dir.UP
            else -> Dir.DOWN
        }
        if (!unmove(d)) return false
    }
    return true
}

/** Drag tile [idx] to (tx,ty), walking the player round to the correct side each step. */
fun Draft.dragTo(idx: Int, tx: Int, ty: Int, limit: Int = 400): Boolean {
    repeat(limit) {
        val t = items[idx]
        if (t.x == tx && t.y == ty) return true
        val m = when {
            t.x != tx -> if (tx > t.x) Dir.RIGHT else Dir.LEFT
            else -> if (ty > t.y) Dir.DOWN else Dir.UP
        }
        val sx = t.x + m.dx
        val sy = t.y + m.dy
        if (sx < 0 || sy < 0 || sx >= w || sy >= h) return false
        if (occupied(sx, sy)) return false
        if (!walkTo(sx, sy)) return false
        if (!unmove(opposite(m), idx)) return false
    }
    return items[idx].x == tx && items[idx].y == ty
}

fun main() {
    val W = 17; val H = 9
    val d = Draft(W, H)

    // Winning position: sentence along the bottom right, player standing on the flag.
    d.place(Word.THING, true, 0, 0)
    d.place(Word.IS, true, 1, 0)
    d.place(Word.YOU, true, 2, 0)
    d.place(Word.FLAG, true, 13, 8)
    d.place(Word.IS, true, 14, 8)
    d.place(Word.WIN, true, 15, 8)
    d.place(Word.FLAG, false, 16, 8)
    d.place(Word.THING, false, 16, 8)
    d.setPlayer(16, 8)

    // The LAST tile of each kind is the goal word; earlier ones belong to the control rule.
    val flagW = d.items.indexOfLast { it.isText && it.word == Word.FLAG }
    val isW = d.items.indexOfLast { it.isText && it.word == Word.IS }
    val winW = d.items.indexOfLast { it.isText && it.word == Word.WIN }

    var ok = true
    ok = ok && d.dragTo(flagW, 2, 8)
    ok = ok && d.dragTo(isW, 2, 6)
    ok = ok && d.dragTo(winW, 2, 4)
    ok = ok && d.walkTo(0, 8)

    val st = d.state()
    val sol = d.solution()
    val dirs = sol.mapNotNull {
        when (it) { 'U' -> Dir.UP; 'D' -> Dir.DOWN; 'L' -> Dir.LEFT; 'R' -> Dir.RIGHT; else -> null }
    }
    println("planOk=$ok moves=${dirs.size} replayWins=${Solver.replay(st, dirs)} winAtStart=${Solver.isWin(st)}")
    d.render().forEach { println("    \"$it\",") }
    println("solution=$sol")
}
