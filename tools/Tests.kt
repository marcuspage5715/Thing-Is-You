import com.thingisyou.engine.*

var failures = 0
var passes = 0

fun check(name: String, cond: Boolean) {
    if (cond) passes++ else {
        failures++
        println("FAIL: $name")
    }
}

fun board(vararg rows: String): State = LevelParser.parse(rows.toList())

fun run(s: State, vararg dirs: Dir): State {
    var cur = s
    for (d in dirs) cur = Engine.step(cur, d).state
    return cur
}

fun baba(s: State): Item? = s.items.firstOrNull { !it.isText && it.word == Word.BABA }

fun main() {

    // 1. STOP blocks movement, always
    run {
        val s = board(
            "BABA ^IS",
            "^BABA .",
            "^IS .",
            "^YOU ."
        )
        val after = run(s, Dir.RIGHT)
        check("STOP: baba cannot push text off the board edge", baba(after)!!.x == 0)
    }

    // 2. push chain blocked by STOP leaves everything in place
    run {
        val s = board(
            "BABA ROCK WALL .",
            "^BABA ^IS ^YOU . .",
            "^ROCK ^IS ^PUSH . .",
            "^WALL ^IS ^STOP . ."
        )
        val after = run(s, Dir.RIGHT)
        check("blocked push leaves baba in place", baba(after)!!.x == 0)
        check("blocked push leaves rock in place", after.items.single { !it.isText && it.word == Word.ROCK }.x == 1)
    }

    // 3. push chain that fits moves every link
    run {
        val s = board(
            "BABA ROCK ROCK . .",
            "^BABA ^IS ^YOU . .",
            "^ROCK ^IS ^PUSH . ."
        )
        val after = run(s, Dir.RIGHT)
        check("chain push moves baba", baba(after)!!.x == 1)
        check("chain push moves both rocks", after.items.filter { !it.isText && it.word == Word.ROCK }.map { it.x }.sorted() == listOf(2, 3))
    }

    // 4. word tiles are pushable without any rule
    run {
        val s = board(
            "BABA ^IS . .",
            "^BABA ^IS ^YOU"
        )
        val after = run(s, Dir.RIGHT)
        check("text is pushed", after.firstTextAt(2, 0) == Word.IS)
    }

    // 5. pushing the STOP word out of a sentence frees the wall
    run {
        val s = board(
            "^BABA ^IS ^YOU . .",
            ". . . . .",
            "^WALL ^IS ^STOP . .",
            ". . . . .",
            "BABA . WALL . ."
        )
        check("wall stops baba at first", Rules.evaluate(s).props.any { Prop.STOP in it })
        var cur = run(s, Dir.UP, Dir.RIGHT, Dir.RIGHT)
        check("baba reached the STOP word", baba(cur)!!.x == 2 && baba(cur)!!.y == 3)
        cur = Engine.step(cur, Dir.UP).state
        val rs = Rules.evaluate(cur)
        check("STOP rule broken after push", rs.rules.none { it.signature() == "WALL||IS|STOP" })
        cur = Engine.step(cur, Dir.DOWN).state
        cur = Engine.step(cur, Dir.DOWN).state
        check("baba walks into the wall once STOP is gone", baba(cur)!!.y == 4)
    }

    // 6. AND
    run {
        val rs = Rules.evaluate(board("^BABA ^IS ^YOU ^AND ^WIN"))
        check("AND grants both properties", rs.rules.size == 1 && rs.rules[0].objects.size == 2)
    }

    // 7. NOT
    run {
        val rs = Rules.evaluate(board("WALL .", "^WALL ^IS ^NOT ^STOP"))
        check("NOT STOP leaves no STOP", rs.props[0].isEmpty())
    }

    // 8. transformation runs
    run {
        val s = board(
            "BABA ROCK .",
            "^BABA ^IS ^YOU .",
            "^ROCK ^IS ^FLAG ."
        )
        val out = Engine.step(s, Dir.DOWN)
        check("rock became a flag", out.state.items.none { !it.isText && it.word == Word.ROCK } &&
            out.state.items.any { !it.isText && it.word == Word.FLAG })
    }

    // 9. A IS A freezes A
    run {
        val s = board(
            "ROCK BABA .",
            "^ROCK ^IS ^FLAG .",
            "^ROCK ^IS ^ROCK .",
            "^BABA ^IS ^YOU ."
        )
        val out = Engine.step(s, Dir.DOWN)
        check("A IS A blocks transformation", out.state.items.any { !it.isText && it.word == Word.ROCK })
    }

    // 10. SINK: two sink objects annihilate
    run {
        val s = board(
            "BABA ROCK .",
            "^BABA ^IS ^YOU ^AND ^SINK",
            "^ROCK ^IS ^SINK"
        )
        val out = Engine.step(s, Dir.RIGHT)
        check("sink: baba and rock both gone", out.state.items.none { !it.isText && (it.word == Word.BABA || it.word == Word.ROCK) })
    }

    // 11. SINK: a sink tile swallows a non-floating thing but stays
    run {
        val s = board(
            "BABA WATER .",
            "^BABA ^IS ^YOU .",
            "^WATER ^IS ^SINK ."
        )
        val out = Engine.step(s, Dir.RIGHT)
        check("sink: baba drowns", out.state.items.none { !it.isText && it.word == Word.BABA })
        check("sink: water tile survives", out.state.items.any { !it.isText && it.word == Word.WATER })
    }

    // 12. FLOAT saves you from sinking
    run {
        val s = board(
            "BABA WATER .",
            "^BABA ^IS ^YOU ^AND ^FLOAT",
            "^WATER ^IS ^SINK"
        )
        val out = Engine.step(s, Dir.RIGHT)
        check("FLOAT: baba survives water", out.state.items.any { !it.isText && it.word == Word.BABA })
    }

    // 13. MELT / HOT
    run {
        val s = board(
            "BABA ICE LAVA .",
            "^BABA ^IS ^YOU . .",
            "^ICE ^IS ^PUSH ^AND ^MELT",
            "^LAVA ^IS ^HOT"
        )
        val out = Engine.step(s, Dir.RIGHT)
        check("ice melts on lava", out.state.items.none { !it.isText && it.word == Word.ICE })
    }

    // 14. ON modifier
    run {
        val s = board("BABA+GRASS . .", "^BABA ^ON ^GRASS ^IS ^YOU")
        check("ON grants YOU while standing on it", Rules.evaluate(s).hasYou)
        val s2 = board("BABA . GRASS", "^BABA ^ON ^GRASS ^IS ^YOU")
        check("ON does not grant YOU off it", !Rules.evaluate(s2).hasYou)
    }

    // 15. FACING modifier
    run {
        check("FACING grants YOU", Rules.evaluate(board("BABA> FLAG .", "^BABA ^FACING ^FLAG ^IS ^YOU")).hasYou)
        check("FACING wrong way does not", !Rules.evaluate(board("BABA< FLAG .", "^BABA ^FACING ^FLAG ^IS ^YOU")).hasYou)
    }

    // 16. HAS leaves something behind when destroyed
    run {
        val s = board(
            "BABA ROCK .",
            "^BABA ^IS ^YOU .",
            "^BABA ^HAS ^FLAG .",
            "^ROCK ^IS ^SINK ."
        )
        val out = Engine.step(s, Dir.RIGHT)
        check("HAS spawns a flag where baba died", out.state.items.any { !it.isText && it.word == Word.FLAG })
    }

    // 17. MAKE replaces on destruction
    run {
        val s = board(
            "BABA ROCK .",
            "^BABA ^IS ^YOU .",
            "^BABA ^MAKE ^STAR .",
            "^ROCK ^IS ^SINK ."
        )
        val out = Engine.step(s, Dir.RIGHT)
        check("MAKE spawns a star where baba died", out.state.items.any { !it.isText && it.word == Word.STAR })
    }

    // 18. MOVE object advances on its own
    run {
        val s = board(
            "BABA . . .",
            "ROCK> . . .",
            "^BABA ^IS ^YOU",
            "^ROCK ^IS ^MOVE"
        )
        val out = Engine.step(s, Dir.DOWN)
        check("MOVE object travels", out.state.items.single { !it.isText && it.word == Word.ROCK }.x == 1)
    }

    // 19. MOVE object is blocked by STOP
    run {
        val s = board(
            "BABA . . .",
            "ROCK> WALL . .",
            "^BABA ^IS ^YOU",
            "^ROCK ^IS ^MOVE",
            "^WALL ^IS ^STOP"
        )
        val out = Engine.step(s, Dir.DOWN)
        check("MOVE object blocked by wall", out.state.items.single { !it.isText && it.word == Word.ROCK }.x == 0)
    }

    // 20. TELE
    run {
        val s = board(
            "BABA DOOR . . DOOR .",
            "^BABA ^IS ^YOU . . .",
            "^DOOR ^IS ^TELE . . ."
        )
        val out = Engine.step(s, Dir.RIGHT)
        check("TELE carries baba to the partner door", baba(out.state)!!.x == 4)
    }

    // 21. STOP beats PUSH
    run {
        val s = board(
            "BABA ROCK .",
            "^BABA ^IS ^YOU",
            "^ROCK ^IS ^PUSH ^AND ^STOP"
        )
        val out = Engine.step(s, Dir.RIGHT)
        check("PUSH+STOP does not budge", baba(out.state)!!.x == 0)
    }

    // 22. win / defeat
    run {
        check("win on overlap", Engine.step(board("BABA FLAG .", "^BABA ^IS ^YOU", "^FLAG ^IS ^WIN"), Dir.RIGHT).win)
        check("defeat on overlap", Engine.step(board("BABA LAVA .", "^BABA ^IS ^YOU", "^LAVA ^IS ^DEFEAT"), Dir.RIGHT).defeat)
    }

    // 23. exactly one tile per input, no skipping
    run {
        val s = board("BABA . . . .", "^BABA ^IS ^YOU")
        var cur = s
        var n = 0
        while (n < 10) {
            cur = Engine.step(cur, Dir.RIGHT).state
            n++
        }
        check("one tile per press, stops at the edge", baba(cur)!!.x == 4)
    }

    // 24. subject AND
    run {
        val rs = Rules.evaluate(board("BABA KEKE .", "^BABA ^AND ^KEKE ^IS ^YOU"))
        check("subject AND applies to both", rs.rules.size == 1 && rs.props[0].contains(Prop.YOU) && rs.props[1].contains(Prop.YOU))
    }

    // 25. word tiles are never the noun they spell
    run {
        check("text tile is not YOU", !Rules.evaluate(board("^BABA .", "^BABA ^IS ^YOU")).hasYou)
    }

    // 26. parser rejects malformed sentences
    run {
        check("BABA IS IS YOU invalid", Rules.parseLine(listOf(Word.BABA, Word.IS, Word.IS, Word.YOU)) == null)
        check("IS YOU invalid", Rules.parseLine(listOf(Word.IS, Word.YOU)) == null)
        check("BABA YOU invalid", Rules.parseLine(listOf(Word.BABA, Word.YOU)) == null)
        check("BABA IS YOU valid", Rules.parseLine(listOf(Word.BABA, Word.IS, Word.YOU)) != null)
    }

    // 27. TEXT controls word tiles
    run {
        check("TEXT IS YOU grabs word tiles", Rules.evaluate(board("^ROCK . .", "^TEXT ^IS ^YOU")).hasYou)
    }

    // 28. a sentence cannot start mid-run (no accidental GRASS IS YOU)
    run {
        val rs = Rules.evaluate(board("BABA . GRASS", "^BABA ^ON ^GRASS ^IS ^YOU"))
        check("no sub-sentence parsed from the middle", rs.rules.size == 1)
    }

    // 29. undo history is exact
    run {
        val lvl = LevelDef("t", "t", listOf("BABA . .", "^BABA ^IS ^YOU"))
        val g = GameSession(lvl)
        val start = g.state
        g.step(Dir.RIGHT)
        check("state changed", g.state != start)
        g.undo()
        check("undo restores exact board", g.state == start)
    }

    // 30. level must not be solvable by doing nothing
    run {
        val s = board("BABA . FLAG", "^BABA ^IS ^YOU", "^FLAG ^IS ^WIN")
        check("no instant win", !Solver.isWin(s))
    }

    // 31. several YOU things move exactly one tile per turn (no double-shoving)
    run {
        val s = board(
            "^TEXT ^IS ^YOU . . .",
            "^ROCK ^IS ^PUSH . . ."
        )
        val after = run(s, Dir.RIGHT)
        check("word row shifted exactly one", after.firstTextAt(3, 1) == Word.PUSH)
        check("second word row shifted exactly one", after.firstTextAt(3, 0) == Word.YOU)
    }

    // 32. a push chain never moves a link twice in one turn
    run {
        val s = board(
            "BABA ROCK ROCK . .",
            "^BABA ^IS ^YOU . .",
            "^ROCK ^IS ^PUSH . ."
        )
        val after = run(s, Dir.RIGHT)
        check("chain advanced exactly one", after.items.filter { !it.isText && it.word == Word.ROCK }.map { it.x }.sorted() == listOf(2, 3))
        check("baba advanced exactly one", baba(after)!!.x == 1)
    }

    // 33. two separate YOU objects do not shove each other twice
    run {
        val s = board(
            "BABA . KEKE . .",
            "^BABA ^IS ^YOU . .",
            "^KEKE ^IS ^YOU . ."
        )
        val after = run(s, Dir.RIGHT)
        val bs = after.items.filter { !it.isText && it.word == Word.BABA }.map { it.x }
        val ks = after.items.filter { !it.isText && it.word == Word.KEKE }.map { it.x }
        check("baba moved one tile", bs == listOf(1))
        check("keke moved one tile", ks == listOf(3))
    }

    println("----")
    println("passes=$passes failures=$failures")
    if (failures > 0) throw IllegalStateException("$failures test(s) failed")
}
