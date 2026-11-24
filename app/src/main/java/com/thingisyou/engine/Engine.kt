package com.thingisyou.engine

data class StepOutcome(
    val state: State,
    val events: Set<Event>,
    val win: Boolean,
    val defeat: Boolean
)

/**
 * One turn of the simulation.
 *
 * Resolution order, matching the original game:
 *   1. every YOU object moves one tile (all of them together, pushing PUSH objects and word tiles)
 *   2. every MOVE / SHIFT object advances one tile in its facing direction
 *   3. IS-transformations are applied
 *   4. SINK pairs annihilate
 *   5. MELT objects touching HOT objects are destroyed
 *   6. things that arrived on a TELE object are carried to its partner
 *   7. DEFEAT, then WIN, are tested
 *
 * Rules are read once at the start of the turn; they never change halfway through a move,
 * so a rule you cannot see yet cannot help you mid-turn.
 */
object Engine {

    fun step(state: State, dir: Dir): StepOutcome {
        if (dir == Dir.NONE) return StepOutcome(state, emptySet(), false, false)

        val events = HashSet<Event>()
        val rs0 = Rules.evaluate(state)
        if (!rs0.hasYou) return StepOutcome(state, events, false, false)

        val items = state.items.toMutableList()
        val movedIdx = HashSet<Int>()
        val props = rs0.props

        // ---------- 1a. every YOU object moves at once ----------
        // Chains are collected for all of them first and then applied together, so a word
        // tile that is itself YOU cannot be shoved twice in a single turn.
        val youIdx = items.indices.filter { Prop.YOU in props[it] }
        val movers = LinkedHashSet<Int>()
        for (i in youIdx) {
            val local = LinkedHashSet<Int>()
            if (collectChain(items, i, dir, props, state.w, state.h, local, HashSet(), movedIdx)) {
                movers.addAll(local)
            }
        }
        for (i in movers) {
            val it = items[i]
            items[i] = it.copy(x = it.x + dir.dx, y = it.y + dir.dy, dir = dir)
        }
        movedIdx.addAll(movers)

        // ---------- 1b. MOVE / SHIFT objects travel on their own ----------
        for (i in items.indices) {
            if (i in movedIdx) continue
            val p = props[i]
            if (Prop.YOU in p) continue
            if (Prop.MOVE !in p && Prop.SHIFT !in p) continue
            val d = items[i].dir
            if (d == Dir.NONE) continue
            val local = LinkedHashSet<Int>()
            if (collectChain(items, i, d, props, state.w, state.h, local, HashSet(), movedIdx)) {
                for (j in local) {
                    val it = items[j]
                    items[j] = it.copy(x = it.x + d.dx, y = it.y + d.dy, dir = d)
                }
                movedIdx.addAll(local)
            }
        }

        var cur = State(state.w, state.h, items)

        // ---------- 2. transformations ----------
        val rs1 = Rules.evaluate(cur)
        val (afterTransform, didTransform) = transformAll(cur, rs1)
        if (didTransform) {
            events.add(Event.TRANSFORM)
            cur = cur.copy(items = afterTransform)
        }

        // ---------- 3. sink ----------
        val rs2 = Rules.evaluate(cur)
        val sunk = collectSink(cur, rs2)
        if (sunk.isNotEmpty()) {
            events.add(Event.SINK)
            cur = cur.copy(items = removeDead(cur.items, sunk, rs2))
        }

        // ---------- 4. melt ----------
        val rs3 = Rules.evaluate(cur)
        val melted = collectMelt(cur, rs3)
        if (melted.isNotEmpty()) {
            events.add(Event.MELT)
            cur = cur.copy(items = removeDead(cur.items, melted, rs3))
        }

        // ---------- 5. teleport ----------
        val rs4 = Rules.evaluate(cur)
        val (afterTele, didTele) = applyTele(cur, rs4, state)
        if (didTele) {
            events.add(Event.TELE)
            cur = cur.copy(items = afterTele)
        }

        // ---------- 6. win / defeat ----------
        val rs5 = Rules.evaluate(cur)
        val defeat = overlaps(cur, rs5, Prop.YOU, Prop.DEFEAT)
        val win = !defeat && overlaps(cur, rs5, Prop.YOU, Prop.WIN)
        return StepOutcome(cur, events, win, defeat)
    }

    // ------------------------------------------------------------------ movement

    /**
     * Walks the push chain in front of [idx]. Everything that would have to move is added to
     * [movers]; the chain only counts as movable if every tile in it has somewhere to go.
     * A single blocker therefore stops the whole chain - nothing moves partially, and nothing
     * is ever skipped over.
     */
    private fun collectChain(
        items: MutableList<Item>,
        idx: Int,
        dir: Dir,
        props: List<Set<Prop>>,
        w: Int,
        h: Int,
        movers: MutableSet<Int>,
        seen: MutableSet<Int>,
        movedIdx: Set<Int>
    ): Boolean {
        if (!seen.add(idx)) return true
        movers.add(idx)
        val it = items[idx]
        val nx = it.x + dir.dx
        val ny = it.y + dir.dy
        if (nx < 0 || ny < 0 || nx >= w || ny >= h) return false

        for (j in items.indices) {
            if (j == idx) continue
            val o = items[j]
            if (o.x != nx || o.y != ny) continue
            if (j in seen) continue
            // Something that already moved this turn cannot be shifted a second time.
            if (j in movedIdx) return false
            val p = props[j]
            val pushable = o.isText || Prop.PUSH in p
            val blocking = Prop.STOP in p
            if (pushable) {
                if (blocking) return false          // STOP always wins over PUSH
                if (!collectChain(items, j, dir, props, w, h, movers, seen, movedIdx)) return false
            } else if (blocking) {
                return false
            }
        }
        return true
    }

    // ------------------------------------------------------------ transformation

    private fun transformAll(state: State, rs: Ruleset): Pair<List<Item>, Boolean> {
        var changed = false
        val out = ArrayList<Item>(state.items.size)
        for (item in state.items) {
            val targets = transformTargets(state, rs, item)
            if (targets == null) {
                out.add(item)
            } else {
                for (t in targets) out.add(item.copy(word = t, isText = false))
                changed = true
            }
        }
        return Pair(out, changed)
    }

    private fun transformTargets(state: State, rs: Ruleset, item: Item): List<Word>? {
        if (item.isText) return null
        if (rs.transformRules.isEmpty()) return null

        // `A IS A` freezes A: no other transformation may touch it.
        for (r in rs.transformRules) {
            if (!r.subjects.contains(item.word)) continue
            for ((oi, o) in r.objects.withIndex()) {
                if (o == item.word && !r.objectNeg[oi]) return null
            }
        }

        val out = LinkedHashSet<Word>()
        for (r in rs.transformRules) {
            if (!r.subjects.contains(item.word)) continue
            if (!conditionHolds(state, item, r.modifier)) continue
            for ((oi, o) in r.objects.withIndex()) {
                if (o.kind != WordKind.NOUN) continue
                if (r.objectNeg[oi]) continue
                if (o == item.word) continue
                out.add(o)
            }
        }
        return if (out.isEmpty()) null else out.toList()
    }

    private fun conditionHolds(state: State, item: Item, mod: Modifier?): Boolean {
        if (mod == null) return true
        val hit = when (mod.type) {
            Word.ON -> state.hasNoun(item.x, item.y, mod.target)
            Word.FACING -> item.dir != Dir.NONE && state.hasNoun(item.x + item.dir.dx, item.y + item.dir.dy, mod.target)
            else -> true
        }
        return if (mod.neg) !hit else hit
    }

    // ----------------------------------------------------------------- sink/melt

    /**
     * SINK: a tile holding a SINK object swallows anything that cannot FLOAT.
     * Two SINK objects on one tile annihilate each other; a lone SINK object survives,
     * so a sink object standing on its own never disappears by itself.
     */
    private fun collectSink(state: State, rs: Ruleset): Set<Int> {
        val dead = HashSet<Int>()
        for (y in 0 until state.h) {
            for (x in 0 until state.w) {
                val cell = state.itemsAt(x, y)
                if (cell.size < 2) continue
                val sinkers = cell.filter { Prop.SINK in rs.props[it] }
                if (sinkers.isEmpty()) continue
                for (i in cell) {
                    if (Prop.FLOAT in rs.props[i]) continue
                    if (Prop.SINK !in rs.props[i]) {
                        dead.add(i)
                    } else if (sinkers.size >= 2) {
                        dead.add(i)
                    }
                }
            }
        }
        return dead
    }

    private fun collectMelt(state: State, rs: Ruleset): Set<Int> {
        val dead = HashSet<Int>()
        for (y in 0 until state.h) {
            for (x in 0 until state.w) {
                val cell = state.itemsAt(x, y)
                if (cell.none { Prop.HOT in rs.props[it] }) continue
                for (i in cell) if (Prop.MELT in rs.props[i]) dead.add(i)
            }
        }
        return dead
    }

    private fun removeDead(items: List<Item>, dead: Set<Int>, rs: Ruleset): List<Item> {
        val out = ArrayList<Item>(items.size)
        for (i in items.indices) {
            val item = items[i]
            if (i !in dead) {
                out.add(item)
                continue
            }
            for (r in rs.hasRules) {
                if (!r.subjects.contains(item.word)) continue
                for (o in r.objects) if (o.kind == WordKind.NOUN) out.add(Item(o, false, item.x, item.y))
            }
            for (r in rs.makeRules) {
                if (!r.subjects.contains(item.word)) continue
                for (o in r.objects) if (o.kind == WordKind.NOUN) out.add(Item(o, false, item.x, item.y))
            }
        }
        return out
    }

    // ------------------------------------------------------------------ teleport

    /**
     * Only things that *arrived* on a door this turn are carried across, otherwise a
     * flag that happens to start on a door tile would teleport on the very first move.
     */
    private fun applyTele(state: State, rs: Ruleset, turnStart: State): Pair<List<Item>, Boolean> {
        val tele = state.items.indices.filter { Prop.TELE in rs.props[it] }
        if (tele.isEmpty()) return Pair(state.items, false)

        val list = state.items.toMutableList()
        var changed = false
        for (i in list.indices) {
            if (i in tele) continue
            val a = list[i]
            if (!arrived(turnStart, a)) continue
            for (t in tele) {
                val b = list[t]
                if (a.x != b.x || a.y != b.y) continue
                val partners = tele.filter { it != t && list[it].word == b.word }
                if (partners.size == 1) {
                    val p = list[partners[0]]
                    list[i] = a.at(p.x, p.y)
                    changed = true
                    break
                }
            }
        }
        return Pair(list, changed)
    }

    /** True when nothing of this kind was standing on this tile when the turn began. */
    private fun arrived(turnStart: State, item: Item): Boolean {
        for (p in turnStart.items) {
            if (p.x == item.x && p.y == item.y && p.word == item.word && p.isText == item.isText) return false
        }
        return true
    }

    // ---------------------------------------------------------------------- win

    private fun overlaps(state: State, rs: Ruleset, a: Prop, b: Prop): Boolean {
        for (i in state.items.indices) {
            if (a !in rs.props[i]) continue
            for (j in state.items.indices) {
                if (b !in rs.props[j]) continue
                if (state.items[i].x == state.items[j].x && state.items[i].y == state.items[j].y) return true
            }
        }
        return false
    }
}
