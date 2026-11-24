package com.thingisyou.engine

/**
 * The shipped levels.
 *
 * Every one of these has been machine-checked with [Solver] at build time: the solver finds
 * a solution and [Solver.replay] then plays it back through the real engine, so "solvable"
 * is a witnessed fact rather than a guess.
 *
 * All puzzles are original. They use the same vocabulary and spirit as the genre but none of
 * them are transcriptions of existing levels.
 *
 * Two conventions used below:
 *  - A "rule pocket" is the two-row block at the top of a level:
 *        WALL WALL WALL ...
 *        WALL ^WALL ^IS ^STOP WALL ...
 *    It is sealed on all sides, so the player can never reach those tiles. Levels that use
 *    WALL as a barrier need it, otherwise walls would be pure decoration you can walk through.
 *  - `X+Y` puts two things on one tile (e.g. `THING+GRASS`).
 */
object Levels {

    val all: List<LevelDef> = listOf(

        // ---------------------------------------------------------------- chapter 1

        LevelDef(
            "01", "First Steps",
            listOf(
                "THING . . . . FLAG",
                ". . . . . .",
                "^THING ^IS ^YOU . . .",
                "^FLAG ^IS ^WIN . . ."
            ),
            hint = "You are THING. Touch the FLAG."
        ),

        LevelDef(
            "02", "Break The Wall",
            listOf(
                "^THING ^IS ^YOU . . . . .",
                "^FLAG ^IS ^WIN . . . . .",
                ". . . . . . . .",
                ". . . . WALL . . .",
                ". . . . WALL . . .",
                ". . . . WALL . . .",
                ". . ^WALL . WALL . . .",
                ". . ^IS . WALL . . .",
                "THING . ^STOP . WALL . FLAG ."
            ),
            hint = "A rule is only true while its words stay in one line."
        ),

        LevelDef(
            "03", "Push Comes To Shove",
            listOf(
                "WALL WALL WALL WALL WALL",
                "WALL ^WALL ^IS ^STOP WALL",
                "^THING ^IS ^YOU . . .",
                "^ROCK ^IS ^PUSH . . .",
                "^FLAG ^IS ^WIN . . .",
                "WALL THING ROCK . WALL",
                "WALL WALL . WALL WALL",
                "WALL WALL FLAG WALL WALL"
            ),
            hint = "The rock is in your way. Move it."
        ),

        LevelDef(
            "04", "Build A Rule",
            listOf(
                ". . . . . . .",
                ". . . . . . .",
                "^FLAG ^IS . ^NOT . .",
                ". . ^WIN . . . .",
                ". . . . . . .",
                ". . . . . . .",
                "^THING ^IS ^YOU . . .",
                ". . . . . THING FLAG"
            ),
            hint = "FLAG is not WIN yet. Finish the sentence - and mind the NOT."
        ),

        LevelDef(
            "05", "Float Your Boat",
            listOf(
                "^THING ^IS ^YOU . . . .",
                "^FLAG ^IS ^WIN . . . .",
                ". . . . . . .",
                ". . . ^AND ^FLOAT . .",
                ". . . . . . .",
                "THING . . WATER WATER FLAG .",
                "^WATER ^IS ^SINK . . . ."
            ),
            hint = "AND bolts a second property onto the same rule."
        ),

        // ---------------------------------------------------------------- chapter 2

        LevelDef(
            "06", "Metamorphosis",
            listOf(
                "^FLAG ^IS ^WIN . . .",
                "^ROCK ^IS . ^FLAG . .",
                "^THING ^IS ^YOU . . .",
                ". . . . . .",
                "THING ROCK . . . ."
            ),
            hint = "ROCK IS FLAG turns every rock on the board into a flag."
        ),

        LevelDef(
            "07", "Stay On The Path",
            listOf(
                "^THING ^ON ^GRASS ^IS ^YOU . .",
                "^FLAG ^IS ^WIN . . . . .",
                ". . . . . . .",
                "GRASS GRASS GRASS GRASS GRASS GRASS .",
                "THING+GRASS . . . . FLAG+GRASS ."
            ),
            hint = "Step off the grass and you stop being YOU."
        ),

        LevelDef(
            "08", "Face The Music",
            listOf(
                "^THING ^FACING ^STAR ^IS ^WIN .",
                "^THING ^IS ^YOU . . . .",
                ". . . . . .",
                ". . . . . .",
                ". . . STAR . .",
                ". . . . . .",
                "THING . . . . ."
            ),
            hint = "FACING looks at the tile you last moved towards."
        ),

        LevelDef(
            "09", "Sink Or Swim",
            listOf(
                "^THING ^IS ^YOU . . .",
                "^WATER ^IS ^SINK . . .",
                "^ROCK ^IS ^PUSH ^AND ^SINK .",
                "^FLAG ^IS ^WIN .",
                "THING ROCK WATER FLAG ."
            ),
            hint = "Two SINK things on one tile destroy each other."
        ),

        LevelDef(
            "10", "Fire And Ice",
            listOf(
                "WALL WALL WALL WALL WALL WALL",
                "WALL ^WALL ^IS ^STOP WALL WALL",
                "^THING ^IS ^YOU . . .",
                "^FLAG ^IS ^WIN . . .",
                "^ICE ^IS ^DEFEAT ^AND ^MELT .",
                "^LAVA ^IS ^PUSH ^AND ^HOT .",
                "WALL THING LAVA ICE . FLAG",
                "WALL WALL WALL WALL WALL WALL"
            ),
            hint = "MELT things vanish when they touch something HOT."
        ),

        // ---------------------------------------------------------------- chapter 3

        LevelDef(
            "11", "Left Behind",
            listOf(
                "^THING ^IS ^YOU ^AND ^FLOAT .",
                "^WATER ^IS ^SINK . . . .",
                "^ROCK ^IS ^PUSH ^AND ^SINK .",
                "^ROCK ^HAS ^FLAG . . . .",
                "^FLAG ^IS ^WIN . . . .",
                ". . . . . .",
                "THING ROCK . WATER . ."
            ),
            hint = "HAS leaves something behind when a thing is destroyed."
        ),

        LevelDef(
            "12", "Who Are You",
            listOf(
                "^WALL ^IS ^YOU . . .",
                "^THING ^IS ^STOP . . .",
                "^FLAG ^IS ^WIN . . .",
                ". . . . . .",
                "THING . WALL . . .",
                ". . . . . .",
                ". . . . FLAG ."
            ),
            hint = "YOU does not have to mean THING."
        ),

        LevelDef(
            "13", "Wordplay",
            listOf(
                "^TEXT ^IS ^YOU . . .",
                "^FLAG ^IS ^WIN . . .",
                ". . . FLAG . .",
                ". . . . . .",
                "^ROCK ^IS ^PUSH . . ."
            ),
            hint = "TEXT means every word tile on the board."
        ),

        LevelDef(
            "14", "Doorways",
            listOf(
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL ^WALL ^IS ^STOP WALL WALL WALL",
                "^THING ^IS ^YOU . . . .",
                "^DOOR ^IS ^TELE . . . .",
                "^FLAG ^IS ^WIN . . . .",
                ". . . . . . .",
                "THING DOOR . . . . .",
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL WALL WALL FLAG+DOOR WALL WALL WALL",
                "WALL WALL WALL WALL WALL WALL WALL"
            ),
            hint = "Step on a door and you come out of the other one."
        ),

        LevelDef(
            "15", "Mind The Gap",
            listOf(
                "^THING ^IS ^YOU . . . .",
                "^LAVA ^IS ^DEFEAT . . . .",
                "^FLAG ^IS ^WIN . . . .",
                ". . . . . . .",
                "THING . . . . . .",
                ". . . . . . .",
                "LAVA LAVA . LAVA LAVA .",
                ". . . . . . .",
                ". . . FLAG . ."
            ),
            hint = "DEFEAT ends the level. Find the safe crossing."
        ),

        LevelDef(
            "16", "Frozen",
            listOf(
                ". . . . . . .",
                ". ^ROCK ^IS ^ROCK . . .",
                ". . . . . . .",
                ". ^ROCK ^IS ^FLAG . . .",
                ". . . . . . .",
                ". ^FLAG ^IS ^WIN . . .",
                ". ^THING ^IS ^YOU . . .",
                ". . . . . . .",
                "THING . . . ROCK . ."
            ),
            hint = "A IS A stops every other change to A."
        ),

        LevelDef(
            "17", "Moving Parts",
            listOf(
                "^FLAG ^IS . ^WIN . ROCK< .",
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
            hint = "That rock will not wait for you."
        ),

        // ---------------------------------------------------------------- chapter 4

        LevelDef(
            "18", "Fireproof",
            listOf(
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL ^WALL ^IS ^STOP WALL WALL WALL",
                "^THING ^IS ^YOU . . . .",
                ". . . . . . .",
                ". . . ^AND ^NOT ^MELT .",
                "^THING ^IS ^MELT . . . .",
                "^LAVA ^IS ^HOT . . . .",
                "^FLAG ^IS ^WIN . . . .",
                ". . . . . . .",
                ". WALL WALL WALL WALL WALL WALL",
                "THING . LAVA LAVA LAVA FLAG ."
            ),
            hint = "You are made of MELT and the lava is HOT. NOT fixes that."
        ),

        LevelDef(
            "19", "Making Things",
            listOf(
                "^THING ^IS ^YOU . . .",
                "^WATER ^IS ^SINK . . .",
                "^ROCK ^IS ^PUSH ^AND ^SINK .",
                "^ROCK ^MAKE ^STAR . .",
                "^STAR ^IS ^WIN . . .",
                ". . . . .",
                "THING ROCK WATER . ."
            ),
            hint = "MAKE turns a destroyed thing into something else."
        ),

        LevelDef(
            "20", "Follow The Leader",
            listOf(
                "^FLAG ^IS . ^WIN . BOX< .",
                ". . . . . . .",
                ". . . . . . .",
                "^THING ^IS ^YOU . . . .",
                ". . . . . . .",
                ". . . . . . .",
                "THING . . . . FLAG ."
            ),
            hint = "SHIFT things move by themselves. Let the box help."
        ),

        LevelDef(
            "21", "Two Ways",
            listOf(
                "^THING ^AND ^PIP ^IS ^YOU . .",
                "^PIP ^IS ^STOP . . . .",
                "^FLAG ^IS ^WIN . . . .",
                ". . . . . . .",
                ". . . . . . .",
                "THING . . . . . .",
                "PIP . . . . . .",
                ". . . . FLAG ."
            ),
            hint = "You drive both of them at once. PIP will not let you through."
        ),

        LevelDef(
            "22", "The Long Push",
            listOf(
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL ^WALL ^IS ^STOP WALL WALL WALL",
                "^THING ^IS ^YOU . . . .",
                "^ROCK ^IS ^PUSH . . . .",
                "^FLAG ^IS ^WIN . . . .",
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL THING ROCK . FLAG . WALL",
                "WALL WALL WALL WALL WALL WALL WALL"
            ),
            hint = "Push the rock out of the corridor."
        ),

        LevelDef(
            "23", "Mirror Image",
            listOf(
                "^THING ^IS ^YOU . . . .",
                "^FLAG ^IS ^WIN . . . .",
                "^ROCK ^IS ^PUSH ^AND ^STOP . .",
                ". . . . . . .",
                ". . . . . . .",
                "THING . ROCK . . . .",
                ". . . . . . .",
                ". . . . . . FLAG"
            ),
            hint = "That rock blocks you and will not budge. Change the rule."
        ),

        LevelDef(
            "24", "Half A Rule",
            listOf(
                ". . . . . . .",
                "^THING ^IS ^YOU . . . .",
                ". . . . . . .",
                "^FLAG ^IS . . . . .",
                ". . . ^WIN . . .",
                ". . . . . . .",
                "THING . . . . . .",
                ". . . . . . FLAG"
            ),
            hint = "Two halves of a sentence do nothing until they meet."
        ),

        LevelDef(
            "25", "Sinking Feeling",
            listOf(
                "^THING ^IS ^YOU . . . .",
                "^WATER ^IS ^SINK . . . .",
                "^ROCK ^IS ^PUSH ^AND ^SINK . .",
                "^FLAG ^IS ^WIN . . . .",
                ". . . . . . .",
                "THING ROCK WATER WATER FLAG ."
            ),
            hint = "You only have one rock. Choose where it goes."
        ),

        LevelDef(
            "26", "Hot Pursuit",
            listOf(
                "^THING ^IS ^YOU . . .",
                "^FLAG ^IS ^WIN . . .",
                "^LAVA ^IS ^DEFEAT ^AND ^PUSH .",
                ". . . . . .",
                ". . . . . .",
                "THING LAVA . . FLAG ."
            ),
            hint = "Push the lava out of your way. Do not walk into it."
        ),

        LevelDef(
            "27", "Copycat",
            listOf(
                "^THING ^IS ^YOU . . .",
                "^FLAG ^IS ^WIN . . .",
                "^ROCK ^IS ^PUSH . . .",
                "^PIP ^IS ^ROCK . . .",
                ". . . . . .",
                "THING PIP . ROCK . .",
                ". . . . . .",
                ". . . . FLAG ."
            ),
            hint = "One IS rule can rewrite every other object."
        ),

        LevelDef(
            "28", "Elevator",
            listOf(
                "^THING ^IS ^YOU . . .",
                "^THING ^ON ^METAL ^IS ^WIN .",
                "^ROCK ^IS ^PUSH . . .",
                ". . . . . .",
                ". . . . . .",
                "THING . ROCK METAL . ."
            ),
            hint = "You only win while you are standing on metal."
        ),

        LevelDef(
            "29", "Crossing",
            listOf(
                "^THING ^IS ^YOU . . .",
                "^WATER ^IS ^SINK . . .",
                "^ROCK ^IS ^PUSH ^AND ^SINK .",
                "^DOOR ^IS ^TELE . . .",
                "^FLAG ^IS ^WIN . . .",
                ". . . . . .",
                "THING ROCK WATER . DOOR FLAG"
            ),
            hint = "Sink the rock, then find the door."
        ),

        LevelDef(
            "30", "Wall Pusher",
            listOf(
                "^THING ^IS ^YOU . . .",
                "^WALL ^IS ^PUSH . . .",
                "^FLAG ^IS ^WIN . . .",
                ". . . . . .",
                ". . . . . .",
                "THING . WALL . FLAG"
            ),
            hint = "Nothing says a wall has to be a wall."
        ),

        LevelDef(
            "31", "The Shelf",
            listOf(
                "WALL WALL WALL WALL WALL WALL",
                "WALL ^WALL ^IS ^STOP WALL WALL",
                "^THING ^IS ^YOU . . .",
                "^FLAG ^IS ^WIN . . .",
                "WALL THING ROCK . . WALL",
                "WALL WALL . WALL WALL WALL",
                "WALL WALL FLAG WALL WALL WALL",
                "WALL WALL WALL WALL WALL WALL"
            ),
            hint = "The rock is sitting on the only way down."
        ),

        LevelDef(
            "32", "Double Trouble",
            listOf(
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL ^WALL ^IS ^STOP WALL WALL WALL",
                "^THING ^IS ^YOU . . . .",
                "^FLAG ^IS ^WIN . . . .",
                "WALL THING ROCK ROCK . . WALL",
                "WALL WALL WALL . WALL WALL WALL",
                "WALL WALL WALL FLAG WALL WALL WALL",
                "WALL WALL WALL WALL WALL WALL WALL"
            ),
            hint = "Two rocks, one gap. Push them both past it."
        ),

        LevelDef(
            "33", "Finale",
            listOf(
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL ^WALL ^IS ^STOP WALL WALL WALL",
                "^THING ^IS ^YOU . . . .",
                "^WALL ^IS ^STOP . . . .",
                "^ROCK ^IS ^FLAG . . . .",
                "^FLAG ^IS ^WIN . . . .",
                ". . . . . . .",
                ". . . . . . .",
                "THING . . WALL ROCK . ."
            ),
            hint = "Everything you have learned, in one room."
        )
    )

    fun byId(id: String): LevelDef? = all.firstOrNull { it.id == id }
}
