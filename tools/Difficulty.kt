import com.thingisyou.engine.*

/**
 * Reports, for each level, how the win condition is reached:
 *  - "GIVEN"    a win rule is already active on turn 1 (level is a walk, not a puzzle)
 *  - "BUILT"    no win rule at the start; the player has to create one
 * Also reports the shortest solution found, as a crude difficulty signal.
 */
fun main(args: Array<String>) {
    val from = args.getOrNull(0)?.toInt() ?: 0
    val to = args.getOrNull(1)?.toInt() ?: Levels.all.size
    var given = 0
    for (idx in from until minOf(to, Levels.all.size)) {
        val lvl = Levels.all[idx]
        val s = lvl.build()
        val rs = Rules.evaluate(s)
        // A negated WIN (e.g. FLAG IS NOT WIN) is not a free win.
        val winActive = rs.rules.any { r ->
            r.objects.withIndex().any { (i, o) -> o == Word.WIN && !r.objectNeg[i] }
        }
        val tag = if (winActive) "GIVEN" else "built"
        if (winActive) given++
        val t0 = System.currentTimeMillis()
        val res = Solver.solve(s, maxDepth = 45, maxNodes = 2_000_000, budgetMs = 12000)
        val ok = res.solved && Solver.replay(s, res.path)
        println("${lvl.id} ${lvl.name}: $tag, " +
            (if (ok) "${res.path.size} moves" else "UNSOLVED") + " (${System.currentTimeMillis()-t0}ms)")
        System.out.flush()
    }
    println("---- levels with a free win rule: $given")
}
