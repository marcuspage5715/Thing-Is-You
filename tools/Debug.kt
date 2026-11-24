import com.thingisyou.engine.*

fun main(args: Array<String>) {
    val lvl = Levels.all[args[0].toInt()]
    var cur = lvl.build()
    println("== ${lvl.id} ${lvl.name}")
    printBoard(cur)
    val moves = args.drop(1).joinToString("")
    for (c in moves) {
        val d = when (c) { 'U' -> Dir.UP; 'D' -> Dir.DOWN; 'L' -> Dir.LEFT; 'R' -> Dir.RIGHT; else -> continue }
        val out = Engine.step(cur, d)
        cur = out.state
        println("-- $c  win=${out.win} defeat=${out.defeat} events=${out.events}")
        printBoard(cur)
        if (out.win) { println("WIN"); return }
    }
    println("final win=" + Solver.isWin(cur))
}

fun printBoard(s: State) {
    for (y in 0 until s.h) {
        val sb = StringBuilder()
        for (x in 0 until s.w) {
            val cell = s.items.filter { it.x == x && it.y == y }
            val txt = if (cell.isEmpty()) "." else cell.joinToString("+") { (if (it.isText) "^" else "") + it.word.label.take(3) }
            sb.append(txt.padEnd(12))
        }
        println(sb)
    }
    println("  rules: " + Rules.evaluate(s).rules.map { it.signature() })
    println()
}
