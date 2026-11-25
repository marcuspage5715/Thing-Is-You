import com.thingisyou.engine.*

/**
 * Drafts a level by walking backwards from the winning position, then replays the derived
 * forward solution as proof.
 */
fun main() {
    val W = 17; val H = 9
    val d = Draft(W, H)

    d.place(Word.THING, true, 0, 0)
    d.place(Word.IS, true, 1, 0)
    d.place(Word.YOU, true, 2, 0)
    d.place(Word.FLAG, true, 14, 8)
    d.place(Word.IS, true, 15, 8)
    d.place(Word.WIN, true, 16, 8)
    d.place(Word.FLAG, false, 13, 6)
    d.place(Word.THING, false, 13, 6)
    d.setPlayer(13, 6)

    var ok = true
    fun walk(dir: Dir, n: Int) { repeat(n) { if (!d.unmove(dir)) ok = false } }
    fun drag(dir: Dir, n: Int) { repeat(n) { if (!d.unmoveDragging(dir)) ok = false } }

    // unmove(DOWN) lifts the player up; unmove(LEFT) walks it right; unmove(RIGHT) walks left.
    walk(Dir.UP, 2)          // drop to the bottom row
    drag(Dir.RIGHT, 12)      // pull FLAG word left to x=2
    walk(Dir.DOWN, 1)        // up to row 7
    walk(Dir.LEFT, 12)       // right to x=13
    walk(Dir.UP, 1)          // back down to row 8
    drag(Dir.RIGHT, 9)       // pull IS word left to x=6
    walk(Dir.LEFT, 9)        // right to x=15
    drag(Dir.RIGHT, 6)       // pull WIN word left to x=10
    walk(Dir.DOWN, 1)        // up to row 7
    walk(Dir.RIGHT, 4)       // left to x=6
    walk(Dir.UP, 1)          // down to row 8

    val st = d.state()
    val sol = d.solution()
    val dirs = sol.mapNotNull {
        when (it) { 'U' -> Dir.UP; 'D' -> Dir.DOWN; 'L' -> Dir.LEFT; 'R' -> Dir.RIGHT; else -> null }
    }
    val wins = Solver.replay(st, dirs)
    println("scramble ok=$ok  moves=${dirs.size}  replayWins=$wins")
    println("winAtStart=${Solver.isWin(st)}")
    d.render().forEach { println("    \"$it\",") }
    println("solution=$sol")
}
