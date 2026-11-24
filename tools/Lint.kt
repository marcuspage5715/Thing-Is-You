import com.thingisyou.engine.*

/**
 * Catches the class of bug where a level draws walls as scenery but no rule makes them
 * STOP, so the player simply walks through them.
 */
fun main() {
    var problems = 0
    for (lvl in Levels.all) {
        val s = lvl.build()
        val rs = Rules.evaluate(s)
        var walls = 0
        var wallStop = false
        var wallOther = false
        for (i in s.items.indices) {
            val it = s.items[i]
            if (it.isText || it.word != Word.WALL) continue
            walls++
            if (Prop.STOP in rs.props[i]) wallStop = true
            if (Prop.PUSH in rs.props[i] || Prop.YOU in rs.props[i]) wallOther = true
        }
        if (walls == 0) continue
        val flag = when {
            wallOther -> "intended (wall is PUSH/YOU)"
            wallStop -> "ok (walls are STOP)"
            else -> "PROBLEM: walls are scenery"
        }
        if (flag.startsWith("PROBLEM")) problems++
        println("${lvl.id} ${lvl.name}: $walls wall tiles -> $flag")
    }
    println("----")
    println(if (problems == 0) "no decorative-wall problems" else "$problems level(s) need WALL IS STOP")
}
