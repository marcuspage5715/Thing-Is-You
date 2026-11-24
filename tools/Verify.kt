import com.thingisyou.engine.*

fun main(args: Array<String>) {
    val from = args.getOrNull(0)?.toInt() ?: 0
    val to = args.getOrNull(1)?.toInt() ?: Levels.all.size
    val budget = args.getOrNull(2)?.toLong() ?: 40_000L
    var bad = 0
    for (idx in from until minOf(to, Levels.all.size)) {
        val lvl = Levels.all[idx]
        val start = try { lvl.build() } catch (e: Exception) {
            println("${lvl.id} ${lvl.name}: PARSE ERROR ${e.message}"); bad++; continue
        }
        val t0 = System.currentTimeMillis()
        val res = Solver.solve(start, maxDepth = 45, maxNodes = 4_000_000, budgetMs = budget)
        val ms = System.currentTimeMillis() - t0
        if (res.solved && Solver.replay(start, res.path)) {
            println("${lvl.id} ${lvl.name}: OK ${res.path.size} moves, ${res.nodes} nodes, ${ms}ms  " +
                res.path.joinToString(" ") { it.name.first().toString() })
        } else if (res.solved) {
            bad++; println("${lvl.id} ${lvl.name}: WITNESS FAILED")
        } else {
            bad++
            println("${lvl.id} ${lvl.name}: NOT SOLVED exhausted=${res.exhausted} nodes=${res.nodes} ${ms}ms")
        }
        System.out.flush()
    }
    println("---- batch done, failures=$bad")
}
