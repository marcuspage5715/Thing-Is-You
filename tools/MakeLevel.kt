import com.thingisyou.engine.*

/**
 * Route-planning scrambler.
 *
 * Unwinds from the winning position, but instead of a fixed script it plans: the player is
 * routed around obstacles with a BFS, and each word tile is dragged to a chosen cell. Every
 * step is still the exact inverse of a legal forward move, so the derived solution is legal
 * by construction and replaying it is the proof.
 */

fun main() {
    val W = 19; val H = 11
    val d = Draft(W, H)

    // Winning position: sentence along the bottom right, player standing on the flag.
    d.place(Word.THING, true, 0, 0)
    d.place(Word.IS, true, 1, 0)
    d.place(Word.YOU, true, 2, 0)
    d.place(Word.FLAG, true, 15, 10)
    d.place(Word.IS, true, 16, 10)
    d.place(Word.WIN, true, 17, 10)
    d.place(Word.FLAG, false, 18, 10)
    d.place(Word.THING, false, 18, 10)
    d.setPlayer(18, 10)

    // The LAST tile of each kind is the goal word; earlier ones belong to the control rule.
    val flagW = d.items.indexOfLast { it.isText && it.word == Word.FLAG }
    val isW = d.items.indexOfLast { it.isText && it.word == Word.IS }
    val winW = d.items.indexOfLast { it.isText && it.word == Word.WIN }

    var ok = true
    ok = ok && d.dragTileTo(flagW, 1, 1)
    ok = ok && d.dragTileTo(isW, 17, 1)
    ok = ok && d.dragTileTo(winW, 1, 9)
    ok = ok && d.walkTo(18, 9)

    val st = d.state()
    val sol = d.solution()
    val dirs = sol.mapNotNull {
        when (it) { 'U' -> Dir.UP; 'D' -> Dir.DOWN; 'L' -> Dir.LEFT; 'R' -> Dir.RIGHT; else -> null }
    }
    println("planOk=$ok moves=${dirs.size} replayWins=${Solver.replay(st, dirs)} winAtStart=${Solver.isWin(st)}")
    d.render().forEach { println("    \"$it\",") }
    println("solution=$sol")
}
