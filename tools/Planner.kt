import com.thingisyou.engine.*

/**
 * Route planning for the reverse drafter.
 *
 * A word tile can only be dragged one cell at a time, and each step needs the player standing
 * on the far side of it. So moving a tile is itself a search problem: BFS over cells the tile
 * can reach, where a cell is reachable only if it is empty (the player has to stand there).
 * The player's own walk is a second BFS on top.
 */

fun opposite(d: Dir) = when (d) {
    Dir.UP -> Dir.DOWN; Dir.DOWN -> Dir.UP
    Dir.LEFT -> Dir.RIGHT; Dir.RIGHT -> Dir.LEFT; Dir.NONE -> Dir.NONE
}

/** The player's own cell is never an obstacle. */
fun Draft.occupied(x: Int, y: Int): Boolean =
    items.any { it.x == x && it.y == y && !(!it.isText && it.word == Word.THING) }

fun Draft.walkTo(tx: Int, ty: Int): Boolean {
    if (px == tx && py == ty) return true
    val start = py * w + px
    val target = ty * w + tx
    val prev = HashMap<Int, Int>()
    val q = ArrayDeque<Int>()
    prev[start] = -1
    q.add(start)
    while (q.isNotEmpty()) {
        val c = q.removeFirst()
        if (c == target) break
        val cx = c % w; val cy = c / w
        for ((dx, dy) in listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)) {
            val nx = cx + dx; val ny = cy + dy
            if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue
            val k = ny * w + nx
            if (prev.containsKey(k) || occupied(nx, ny)) continue
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

/** BFS over where the tile itself can be dragged. A cell is reachable if it is empty. */
fun Draft.tilePath(idx: Int, tx: Int, ty: Int): List<Dir>? {
    val s = items[idx]
    val start = s.y * w + s.x
    val goal = ty * w + tx
    if (start == goal) return emptyList()
    val prev = HashMap<Int, Pair<Int, Dir>>()
    val q = ArrayDeque<Int>()
    prev[start] = Pair(-1, Dir.NONE)
    q.add(start)
    while (q.isNotEmpty()) {
        val c = q.removeFirst()
        if (c == goal) break
        val cx = c % w; val cy = c / w
        for (m in listOf(Dir.UP, Dir.DOWN, Dir.LEFT, Dir.RIGHT)) {
            val nx = cx + m.dx; val ny = cy + m.dy
            if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue
            if (occupied(nx, ny)) continue
            val k = ny * w + nx
            if (prev.containsKey(k)) continue
            prev[k] = Pair(c, m)
            q.add(k)
        }
    }
    if (!prev.containsKey(goal)) return null
    val out = ArrayList<Dir>()
    var c = goal
    while (c != start) {
        val (p, m) = prev[c]!!
        out.add(m)
        c = p
    }
    out.reverse()
    return out
}

/** Drag tile [idx] to (tx,ty), routing both the tile and the player. */
fun Draft.dragTileTo(idx: Int, tx: Int, ty: Int): Boolean {
    val path = tilePath(idx, tx, ty) ?: return false
    for (m in path) {
        val t = items[idx]
        val sx = t.x + m.dx; val sy = t.y + m.dy
        if (sx < 0 || sy < 0 || sx >= w || sy >= h) return false
        if (!walkTo(sx, sy)) return false
        if (!unmove(opposite(m), idx)) return false
    }
    return items[idx].x == tx && items[idx].y == ty
}
