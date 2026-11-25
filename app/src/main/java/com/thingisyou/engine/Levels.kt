package com.thingisyou.engine

/**
 * The shipped levels.
 *
 * Design rule for this set: **no level ever starts with a WIN rule in place.** A puzzle is
 * only a puzzle if the player has to build the winning sentence themselves — bring the
 * scattered word tiles into one line, choose the right noun, or rewrite what the player is.
 * `tools/Difficulty.kt` fails the build if any level regresses to handing the answer over.
 *
 * Every level is machine-checked: [Solver] finds a solution and [Solver.replay] plays it
 * back through the real engine.
 *
 * Conventions:
 *  - `X+Y` puts two things on one tile (e.g. `THING+GRASS`).
 *  - A "rule pocket" is a sealed two-row block of `WALL IS STOP` at the top of a level.
 *    Levels that use WALL as a barrier need it; otherwise walls are pure decoration.
 */
object Levels {

    val all: List<LevelDef> = listOf(

        // ------------------------------------------------------- 1. learn to build

        LevelDef(
            "01", "Assembly Required",
            listOf(
                "^THING ^IS ^YOU . . . .",
                ". . . . . . .",
                "^FLAG . . ^IS . ^WIN .",
                ". . . . . . .",
                "THING . . . . FLAG ."
            ),
            hint = "Nothing says FLAG is WIN yet. Push the words into one line."
        ),

        LevelDef(
            "02", "The Wrong Flag",
            listOf(
                "^THING ^IS ^YOU . . . . .",
                ". . . . . . . .",
                "^FLAG ^IS . . . . ^WIN .",
                "^ROCK . . . . . . .",
                ". . . . . . . .",
                "THING . ROCK . . . . ."
            ),
            hint = "There is no flag on this board. Look for something that is."
        ),

        LevelDef(
            "03", "Become The Goal",
            listOf(
                ". ^THING ^IS ^YOU .",
                ". . . . .",
                ". ^THING . ^IS .",
                ". . . . .",
                "THING . . . ^WIN"
            ),
            hint = "You are not required to go anywhere. Make yourself the thing worth reaching."
        ),

        LevelDef(
            "04", "Not Quite",
            listOf(
                "^THING ^IS ^YOU . . . .",
                ". . . . . . .",
                "^FLAG ^IS ^NOT ^WIN . .",
                ". . . . . . .",
                ". . . . . . .",
                "THING . . . . FLAG ."
            ),
            hint = "A NOT is sitting in the sentence. Get it out, then close the gap."
        ),

        LevelDef(
            "05", "Two Halves",
            listOf(
                "^THING ^IS ^YOU . . .",
                ". . . . . .",
                ". ^FLAG . ^IS . .",
                ". . . . . .",
                "THING . . . FLAG ^WIN"
            ),
            hint = "Both halves are in the same row. Push them together."
        ),

        // ------------------------------------------------------- 2. choosing well

        LevelDef(
            "06", "Cornered",
            listOf(
                ". . ^THING ^IS ^YOU",
                "^FLAG . . . .",
                ". . . . .",
                "^IS . . . .",
                ". . . . .",
                "^WIN . . . .",
                ". . . . .",
                "THING . . . FLAG"
            ),
            hint = "The words are jammed against the edge. Push them up the column."
        ),

        LevelDef(
            "07", "Break The Wall",
            listOf(
                ". . . . . .",
                "^FLAG ^IS ^STOP . ^WIN .",
                ". . . . . .",
                "^THING ^IS ^YOU . . .",
                ". . . . . .",
                "THING . . . . FLAG"
            ),
            hint = "The flag is STOP, so you cannot reach it. Change what FLAG is."
        ),

        LevelDef(
            "08", "Deep Water",
            listOf(
                "^THING ^IS ^YOU . . . .",
                "^WATER ^IS ^SINK . . . .",
                ". . . . . . .",
                "^FLAG . . ^IS . ^WIN .",
                ". . . . . . .",
                "THING . WATER WATER FLAG ."
            ),
            hint = "You cannot cross water. Make yourself able to."
        ),

        LevelDef(
            "09", "Sink Or Swim",
            listOf(
                "^THING ^IS ^YOU . . .",
                "^WATER ^IS ^SINK . . .",
                "^ROCK ^IS ^PUSH ^AND ^SINK .",
                ". . . . .",
                "^FLAG . . ^IS ^WIN",
                ". . . . .",
                "THING ROCK WATER . FLAG"
            ),
            hint = "The rock can clear one tile of water. Then build the sentence."
        ),

        // ------------------------------------------------------- 3. properties

        LevelDef(
            "10", "Fire And Ice",
            listOf(
                "WALL WALL WALL WALL WALL WALL",
                "WALL ^WALL ^IS ^STOP WALL WALL",
                "^THING ^IS ^YOU . . .",
                "^ICE ^IS ^DEFEAT ^AND ^MELT .",
                "^LAVA ^IS ^PUSH ^AND ^HOT .",
                "^FLAG . . ^IS ^WIN .",
                ". . . . . .",
                "WALL THING LAVA ICE . FLAG",
                "WALL WALL WALL WALL WALL WALL"
            ),
            hint = "Melt the ice, then finish the sentence in the row above."
        ),

        LevelDef(
            "11", "Left Behind",
            listOf(
                "^THING ^IS ^YOU ^AND ^FLOAT .",
                "^WATER ^IS ^SINK . . . .",
                "^ROCK ^IS ^PUSH ^AND ^SINK .",
                "^ROCK ^HAS ^FLAG . . . .",
                "^FLAG . . ^IS ^WIN .",
                ". . . . . .",
                "THING ROCK . WATER . ."
            ),
            hint = "HAS leaves something behind. Sink the rock and look."
        ),

        LevelDef(
            "12", "Who Are You",
            listOf(
                "^WALL ^IS ^YOU . . .",
                "^THING ^IS ^STOP . . .",
                ". . . . . .",
                "^FLAG . . ^IS ^WIN",
                ". . . . . .",
                "THING . WALL . . .",
                ". . . . . .",
                ". . . . FLAG ."
            ),
            hint = "You are not the thing you think you are."
        ),

        LevelDef(
            "13", "Wordplay",
            listOf(
                "^TEXT ^IS ^YOU . . .",
                ". . . . . .",
                "^FLAG . ^IS . .",
                ". . . . . .",
                ". . . FLAG . .",
                ". . . . . ^WIN",
                "^ROCK ^IS ^PUSH . . ."
            ),
            hint = "You drive the words themselves. Push a word onto the flag."
        ),

        LevelDef(
            "14", "Doorways",
            listOf(
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL ^WALL ^IS ^STOP WALL WALL WALL",
                "^THING ^IS ^YOU . . . .",
                "^DOOR ^IS ^TELE . . . .",
                "^FLAG . . ^IS ^WIN .",
                ". . . . . . .",
                "THING DOOR . . . . .",
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL WALL WALL FLAG+DOOR WALL WALL WALL",
                "WALL WALL WALL WALL WALL WALL WALL"
            ),
            hint = "The flag is sealed in. The door is the only way in."
        ),

        LevelDef(
            "15", "Mind The Gap",
            listOf(
                "^THING ^IS ^YOU . . . .",
                "^LAVA ^IS ^DEFEAT . . . .",
                "^FLAG . . ^IS ^WIN .",
                ". . . . . . .",
                "THING . . . . . .",
                ". . . . . . .",
                "LAVA LAVA . LAVA LAVA .",
                ". . . . . . .",
                ". . . FLAG . ."
            ),
            hint = "Build the sentence, then cross without touching the lava."
        ),

        LevelDef(
            "16", "Moving Parts",
            listOf(
                "^FLAG . ^IS . ^WIN . ROCK<",
                ". . . . . . .",
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL THING FLAG . . . WALL",
                "WALL WALL WALL WALL WALL WALL WALL",
                ". . . . . . .",
                "^THING ^IS ^YOU . . . .",
                "^ROCK ^IS ^MOVE . . . .",
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL ^WALL ^IS ^STOP WALL WALL WALL",
                "WALL WALL WALL WALL WALL WALL WALL"
            ),
            hint = "You are sealed in. Let the moving rock do the work."
        ),

        LevelDef(
            "17", "Fireproof",
            listOf(
                "WALL WALL WALL WALL WALL WALL",
                "WALL ^WALL ^IS ^STOP WALL WALL",
                "^THING ^IS ^YOU . . .",
                ". . . ^AND ^NOT ^MELT",
                "^THING ^IS ^MELT . . .",
                "^LAVA ^IS ^HOT . . .",
                "^FLAG . ^IS ^WIN .",
                ". . . . . .",
                "THING . LAVA LAVA FLAG"
            ),
            hint = "You are made of MELT and the lava is HOT. Cancel that, then build."
        ),

        LevelDef(
            "18", "Making Things",
            listOf(
                "^THING ^IS ^YOU . . .",
                "^WATER ^IS ^SINK . . .",
                "^ROCK ^IS ^PUSH ^AND ^SINK .",
                "^ROCK ^MAKE ^STAR . .",
                "^FLAG . . ^IS ^WIN",
                ". . . . .",
                "THING ROCK WATER . FLAG"
            ),
            hint = "MAKE turns a destroyed thing into something else."
        ),

        LevelDef(
            "19", "Wrong Way Round",
            listOf(
                "^THING ^IS ^YOU . . .",
                "^FLAG ^IS ^LAVA . .",
                ". . . . . .",
                ". ^ROCK . ^IS . .",
                ". . . . . .",
                "THING . . ROCK . ^WIN"
            ),
            hint = "FLAG IS LAVA, and there is no flag anyway. Give the goal to something else."
        ),

        LevelDef(
            "20", "Copy",
            listOf(
                "^THING ^IS ^YOU . . .",
                "^ROCK ^IS ^FLAG . .",
                ". . . . . .",
                "^FLAG . ^IS ^WIN .",
                ". . . . . .",
                "THING . . ROCK . ^WIN"
            ),
            hint = "ROCK IS FLAG already works. It still needs something to be worth reaching."
        ),

        LevelDef(
            "21", "Corner Office",
            listOf(
                "^THING . . . . . .",
                "^IS . ^FLAG . . ^IS .",
                "^YOU . . . . . .",
                ". . . . . . .",
                "THING . FLAG . . . .",
                ". . . . . . ^WIN"
            ),
            hint = "WIN is in the corner and cannot move at all. The sentence has to be built around it."
        ),

        LevelDef(
            "22", "The Long Carry",
            listOf(
                "^THING ^IS ^YOU . . . . . . . .",
                ". ^FLAG . . . . . . . . .",
                ". ^IS . . . . . . . . .",
                ". . . . . . . . . . .",
                ". . . . . . . . . . .",
                ". FLAG . . . . . . . . .",
                ". THING . . . . . . . . ^WIN"
            ),
            hint = "WIN cannot move. Both words have to travel the width of the board, one at a time."
        ),

        LevelDef(
            "23", "The Long Haul",
            listOf(
                "^THING ^IS ^YOU . . . . . . . . . . . .",
                ". ^FLAG . . . . . . . . . . . . .",
                ". ^IS . . . . . . . . . . . . .",
                ". . . . . . . . . . . . . . .",
                ". . . . . . . . . . . . . . .",
                ". . . . . . . . . . . . . . .",
                ". FLAG THING . . . . . . . . . . . ^WIN"
            ),
            hint = "Wider board, same problem. Two long carries and a walk back at the end."
        ),

        LevelDef(
            "24", "Finale",
            listOf(
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL ^WALL ^IS ^STOP WALL WALL WALL",
                "^THING ^IS ^YOU . . . .",
                "^WALL ^IS ^STOP . . . .",
                "^ROCK ^IS ^FLAG . . . .",
                "^FLAG . . ^IS ^WIN .",
                ". . . . . . .",
                ". . . . . . .",
                "THING . . WALL ROCK . ."
            ),
            hint = "Everything at once: break a rule, rewrite an object, finish the sentence."
        )
    )

    fun byId(id: String): LevelDef? = all.firstOrNull { it.id == id }
}
