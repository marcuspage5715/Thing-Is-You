package com.thingisyou.engine

/**
 * Build-time solver. Depth-first with a transposition table and an increasing depth
 * limit: the space of "push any word anywhere" boards is far too large for plain BFS.
 * Whatever it returns is replayed by the caller, so a reported solution is a witness,
 * not a guess.
 */
object Solver {

    data class Result(
        val solved: Boolean,
        val path: List<Dir>,
        val nodes: Int,
        val exhausted: Boolean
    )

    private val DIRS = arrayOf(Dir.UP, Dir.DOWN, Dir.LEFT, Dir.RIGHT)
    @Volatile private var deadline: Long = Long.MAX_VALUE

    fun isWin(state: State): Boolean {
        val rs = Rules.evaluate(state)
        if (!rs.hasYou) return false
        for (i in state.items.indices) {
            if (Prop.YOU !in rs.props[i]) continue
            for (j in state.items.indices) {
                if (Prop.WIN !in rs.props[j]) continue
                if (state.items[i].x == state.items[j].x && state.items[i].y == state.items[j].y) return true
            }
        }
        return false
    }

    /** Replays [path] from [start] and reports whether it really wins. */
    fun replay(start: State, path: List<Dir>): Boolean {
        var cur = start
        for (d in path) {
            val out = Engine.step(cur, d)
            if (out.defeat) return false
            cur = out.state
        }
        return isWin(cur)
    }

    fun solve(initial: State, maxDepth: Int = 40, maxNodes: Int = 3_000_000, budgetMs: Long = 60_000): Result {
        if (isWin(initial)) return Result(true, emptyList(), 1, false)
        val counter = IntArray(1)
        deadline = System.currentTimeMillis() + budgetMs
        var lastExhausted = true
        for (limit in 4..maxDepth) {
            if (System.currentTimeMillis() > deadline) break
            val tt = HashMap<Long, Int>()
            val path = ArrayList<Dir>()
            val hit = dfs(initial, 0, limit, path, tt, counter, maxNodes)
            if (counter[0] > maxNodes) {
                lastExhausted = false
                break
            }
            if (hit) return Result(true, path.toList(), counter[0], false)
            lastExhausted = true
        }
        return Result(false, emptyList(), counter[0], lastExhausted)
    }

    private fun dfs(
        s: State,
        depth: Int,
        limit: Int,
        path: MutableList<Dir>,
        tt: HashMap<Long, Int>,
        counter: IntArray,
        cap: Int
    ): Boolean {
        if (++counter[0] > cap) return false
        if ((counter[0] and 0x3FF) == 0 && System.currentTimeMillis() > deadline) return false
        if (isWin(s)) return true
        if (depth >= limit) return false

        val remaining = limit - depth
        val key = s.fingerprint()
        val seen = tt[key]
        if (seen != null && seen >= remaining) return false
        tt[key] = remaining

        for (d in DIRS) {
            val out = Engine.step(s, d)
            if (out.state == s) continue
            if (out.defeat) continue
            path.add(d)
            if (dfs(out.state, depth + 1, limit, path, tt, counter, cap)) return true
            path.removeAt(path.size - 1)
        }
        return false
    }
}
