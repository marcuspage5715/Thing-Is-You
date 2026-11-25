package com.thingisyou.engine

/** ON / FACING qualifier attached to a sentence. */
data class Modifier(val type: Word, val target: Word, val neg: Boolean)

/** A syntactically valid sentence found on the board. */
data class Rule(
    val subjects: List<Word>,
    val modifier: Modifier?,
    val verb: Word,
    val objects: List<Word>,
    val objectNeg: List<Boolean>
) {
    fun signature(): String {
        val m = modifier?.let { "${it.type.label}:${if (it.neg) "NOT:" else ""}${it.target.label}" } ?: ""
        val os = objects.mapIndexed { i, w -> (if (objectNeg[i]) "NOT:" else "") + w.label }
        return subjects.joinToString(",") { it.label } + "|" + m + "|" + verb.label + "|" + os.joinToString(",")
    }
}

/** Everything the board's current text says about the world. */
class Ruleset(
    val rules: List<Rule>,
    /** Effective properties, indexed like State.items. */
    val props: List<Set<Prop>>,
    val transformRules: List<Rule>,
    val hasRules: List<Rule>,
    val makeRules: List<Rule>,
    val eatRules: List<Rule>
) {
    val hasYou: Boolean = props.any { Prop.YOU in it }

    fun hasYouAt(index: Int): Boolean = Prop.YOU in props[index]

    fun ruleSignature(): String = rules.map { it.signature() }.sorted().joinToString(";")

    companion object {
        val EMPTY = Ruleset(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    }
}

object Rules {

    private val PROPS = Prop.entries.toList()

    // Rules depend only on where the word tiles are, and word tiles rarely move compared
    // with how often a board is evaluated. Caching that step is what makes the solver usable.
    private val ruleCache = HashMap<String, List<Rule>>()

    private fun textKey(state: State): String {
        val sb = StringBuilder(state.items.size * 8)
        for (item in state.items) {
            if (!item.isText) continue
            sb.append(item.x).append(',').append(item.y).append(',').append(item.word.ordinal).append(';')
        }
        return sb.toString()
    }

    fun evaluate(state: State): Ruleset {
        val key = textKey(state)
        var cached = ruleCache[key]
        if (cached == null) {
            if (ruleCache.size > 120_000) ruleCache.clear()
            cached = findRules(state)
            ruleCache[key] = cached
        }
        val rules = cached
        if (rules.isEmpty()) return Ruleset(emptyList(), List(state.items.size) { emptySet() }, emptyList(), emptyList(), emptyList(), emptyList())

        val props = Array(state.items.size) { HashSet<Prop>() }
        val negProps = Array(state.items.size) { HashSet<Prop>() }
        val transformRules = ArrayList<Rule>()
        val hasRules = ArrayList<Rule>()
        val makeRules = ArrayList<Rule>()
        val eatRules = ArrayList<Rule>()

        for (rule in rules) {
            when (rule.verb) {
                Word.IS -> {
                    for (s in rule.subjects) {
                        val hasNounTarget = rule.objects.any { it.kind == WordKind.NOUN }
                        if (hasNounTarget) {
                            transformRules.add(rule)
                        }
                        for ((oi, o) in rule.objects.withIndex()) {
                            if (o.kind != WordKind.PROP) continue
                            val prop = Prop.of(o) ?: continue
                            val negated = rule.objectNeg[oi]
                            for (i in state.items.indices) {
                                val item = state.items[i]
                                if (!State.matches(item, s)) continue
                                if (!conditionHolds(state, item, rule.modifier)) continue
                                if (negated) negProps[i].add(prop) else props[i].add(prop)
                            }
                        }
                    }
                }
                Word.HAS -> if (rule.objects.any { it.kind == WordKind.NOUN }) hasRules.add(rule)
                Word.MAKE -> if (rule.objects.any { it.kind == WordKind.NOUN }) makeRules.add(rule)
                Word.EAT -> if (rule.objects.any { it.kind == WordKind.NOUN }) eatRules.add(rule)
                else -> {}
            }
        }

        val finalProps = ArrayList<Set<Prop>>(state.items.size)
        for (i in state.items.indices) {
            val p = props[i]
            p.removeAll(negProps[i])
            finalProps.add(if (p.isEmpty()) emptySet() else p.toSet())
        }

        return Ruleset(rules, finalProps, transformRules, hasRules, makeRules, eatRules)
    }

    /** True when the sentence's ON / FACING qualifier is satisfied for this item. */
    private fun conditionHolds(state: State, item: Item, mod: Modifier?): Boolean {
        if (mod == null) return true
        val hit = when (mod.type) {
            Word.ON -> state.hasNoun(item.x, item.y, mod.target)
            Word.FACING -> {
                if (item.dir == Dir.NONE) false
                else state.hasNoun(item.x + item.dir.dx, item.y + item.dir.dy, mod.target)
            }
            else -> true
        }
        return if (mod.neg) !hit else hit
    }

    /** Collects every sentence formed by horizontally or vertically adjacent word tiles. */
    fun findRules(state: State): List<Rule> {
        val out = ArrayList<Rule>()
        val seen = HashSet<String>()
        for (y in 0 until state.h) {
            for (x in 0 until state.w) {
                if (state.firstTextAt(x, y) == null) continue
                for (dir in arrayOf(Dir.RIGHT, Dir.DOWN)) {
                    // A sentence must begin at the start of a contiguous run of word tiles.
                    // Without this, "BABA ON GRASS IS YOU" would also silently grant "GRASS IS YOU".
                    val bx = x - dir.dx
                    val by = y - dir.dy
                    if (state.inside(bx, by) && state.firstTextAt(bx, by) != null) continue
                    val words = lineWords(state, x, y, dir)
                    if (words.size < 3) continue
                    val rule = parseLine(words) ?: continue
                    val sig = rule.signature() + "@$x,$y,${dir.name}"
                    if (seen.add(sig)) out.add(rule)
                }
            }
        }
        return out
    }

    private fun lineWords(state: State, x: Int, y: Int, dir: Dir): List<Word> {
        val out = ArrayList<Word>()
        var cx = x
        var cy = y
        while (state.inside(cx, cy)) {
            val w = state.firstTextAt(cx, cy) ?: break
            out.add(w)
            cx += dir.dx
            cy += dir.dy
        }
        return out
    }

    /** Grammar: Noun (AND Noun)* [ON|FACING [NOT] Noun] (IS|HAS|MAKE) [NOT] (Noun|Prop) (AND [NOT] (Noun|Prop))* */
    fun parseLine(words: List<Word>): Rule? {
        val n = words.size
        if (n < 3) return null
        var idx = 0

        if (words[0].kind != WordKind.NOUN) return null
        val subjects = ArrayList<Word>()
        while (true) {
            if (idx >= n || words[idx].kind != WordKind.NOUN) return null
            subjects.add(words[idx])
            idx++
            if (idx < n && words[idx] == Word.AND) {
                idx++
                if (idx >= n || words[idx].kind != WordKind.NOUN) return null
                continue
            }
            break
        }

        var modifier: Modifier? = null
        if (idx < n && (words[idx] == Word.ON || words[idx] == Word.FACING)) {
            val type = words[idx]
            idx++
            var neg = false
            if (idx < n && words[idx] == Word.NOT) {
                neg = true
                idx++
            }
            if (idx >= n || words[idx].kind != WordKind.NOUN) return null
            modifier = Modifier(type, words[idx], neg)
            idx++
        }

        if (idx >= n || words[idx].kind != WordKind.VERB) return null
        val verb = words[idx]
        idx++

        val objects = ArrayList<Word>()
        val negs = ArrayList<Boolean>()
        while (true) {
            if (idx >= n) return null
            var neg = false
            if (words[idx] == Word.NOT) {
                neg = true
                idx++
                if (idx >= n) return null
            }
            val w = words[idx]
            if (w.kind != WordKind.NOUN && w.kind != WordKind.PROP) return null
            objects.add(w)
            negs.add(neg)
            idx++
            if (idx < n && words[idx] == Word.AND) {
                idx++
                continue
            }
            break
        }

        return Rule(subjects, modifier, verb, objects, negs)
    }
}
