import com.thingisyou.engine.*
import kotlin.random.Random

/**
 * Builds boards where WIN is pinned in a corner (so it cannot move at all) and the two other
 * words start on the far side behind STOP obstacles. The sentence therefore has to be carried
 * the length of the board, which is what makes the solution long.
 */

private fun cellToken(it: Item): String = (if (it.isText) "^" else "") + it.word.label

fun render(s: State): List<String> {
    val grid = Array(s.h) { arrayOfNulls<String>(s.w) }
    for (y in 0 until s.h) for (x in 0 until s.w) {
        val cell = s.items.filter { it.x == x && it.y == y }
        grid[y][x] = if (cell.isEmpty()) "." else cell.joinToString("+") { cellToken(it) }
    }
    return (0 until s.h).map { y -> (0 until s.w).joinToString(" ") { grid[y][it]!! } }
}

fun main(args: Array<String>) {
    val minMoves = args.getOrNull(0)?.toInt() ?: 40
    val budgetMs = args.getOrNull(1)?.toLong() ?: 200_000
    val deadline = System.currentTimeMillis() + budgetMs
    val rnd = Random(7)
    val found = ArrayList<Triple<Int, List<String>, String>>()
    var tries = 0

    while (System.currentTimeMillis() < deadline) {
        tries++
        val w = 9; val h = 7
        val items = ArrayList<Item>()
        val taken = HashSet<Int>()
        fun free(x: Int, y: Int) = taken.add(y * w + x)

        for ((i, word) in listOf(Word.THING, Word.IS, Word.YOU).withIndex()) { items.add(Item(word, true, i, 0)); free(i, 0) }
        for ((i, word) in listOf(Word.ROCK, Word.IS, Word.STOP).withIndex()) { items.add(Item(word, true, 4 + i, 0)); free(4 + i, 0) }

        // WIN pinned in the bottom-right corner: immovable in every direction
        items.add(Item(Word.WIN, true, 8, 6)); free(8, 6)

        fun place(item: Item): Boolean {
            repeat(60) {
                val x = rnd.nextInt(w); val y = rnd.nextInt(1, h)
                if (free(x, y)) { items.add(item.copy(x = x, y = y)); return true }
            }
            return false
        }

        if (!place(Item(Word.FLAG, true, 0, 0))) continue
        if (!place(Item(Word.IS, true, 0, 0))) continue
        if (!place(Item(Word.FLAG, false, 0, 0))) continue
        if (!place(Item(Word.THING, false, 0, 0))) continue
        repeat(4) { place(Item(Word.ROCK, false, 0, 0)) }

        val state = State(w, h, items)
        if (!Rules.evaluate(state).hasYou) continue
        if (Solver.isWin(state)) continue

        val res = Solver.solve(state, maxDepth = 90, maxNodes = 900_000, budgetMs = 4000)
        if (!res.solved || !Solver.replay(state, res.path)) continue
        if (res.path.size < minMoves) continue

        val moves = res.path.joinToString("") {
            when (it) { Dir.UP -> "U"; Dir.DOWN -> "D"; Dir.LEFT -> "L"; Dir.RIGHT -> "R"; else -> "" }
        }
        found.add(Triple(res.path.size, render(state), moves))
        println("candidate ${res.path.size} moves (try $tries)")
        System.out.flush()
        if (found.size >= 5) break
    }

    found.sortByDescending { it.first }
    println("=== ${found.size} candidates ===")
    for ((n, rows, moves) in found.take(3)) {
        println("--- $n moves")
        rows.forEach { println("    \"$it\",") }
        println("    solution=$moves")
    }
}
