package com.thingisyou.engine

/**
 * The shipped levels.
 *
 * Every one of these has been machine-checked with [Solver] at build time, so each has a
 * real solution. They are original puzzles written for this game, not copies of any
 * existing level.
 */
object Levels {

    val all: List<LevelDef> = listOf(

        LevelDef(
            "01", "First Steps",
            listOf(
                "BABA . . . . FLAG",
                ". . . . . .",
                "^BABA ^IS ^YOU . . .",
                "^FLAG ^IS ^WIN . . ."
            ),
            hint = "You are BABA. Touch the FLAG."
        ),

        LevelDef(
            "02", "Break The Wall",
            listOf(
                "^BABA ^IS ^YOU . . . . .",
                "^FLAG ^IS ^WIN . . . . .",
                ". . . . . . . .",
                ". . . . WALL . . .",
                ". . . . WALL . . .",
                ". . . . WALL . . .",
                ". . . . WALL . . .",
                ". . ^WALL . WALL . . .",
                ". . ^IS . WALL . . .",
                "BABA . ^STOP . WALL . FLAG ."
            ),
            hint = "Rules are only true while their words stay in a line."
        ),

        LevelDef(
            "03", "Push Comes To Shove",
            listOf(
                "^BABA ^IS ^YOU . .",
                "^ROCK ^IS ^PUSH . .",
                "^FLAG ^IS ^WIN . .",
                "WALL WALL WALL WALL WALL",
                "WALL BABA ROCK . WALL",
                "WALL WALL . WALL WALL",
                "WALL WALL FLAG WALL WALL"
            ),
            hint = "Move the rock out of your way."
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
                "^BABA ^IS ^YOU . . .",
                ". . . . . BABA FLAG"
            ),
            hint = "FLAG is not WIN yet. Make it so - and mind the NOT."
        ),

        LevelDef(
            "05", "Float Your Boat",
            listOf(
                "^BABA ^IS ^YOU . . . .",
                "^FLAG ^IS ^WIN . . . .",
                ". . . . . . .",
                ". . . ^AND ^FLOAT . .",
                ". . . . . . .",
                "BABA . . WATER WATER FLAG .",
                "^WATER ^IS ^SINK . . . ."
            ),
            hint = "AND joins two properties onto one rule."
        ),

        LevelDef(
            "06", "Metamorphosis",
            listOf(
                "^FLAG ^IS ^WIN . . .",
                "^ROCK ^IS . ^FLAG . .",
                "^BABA ^IS ^YOU . . .",
                ". . . . . .",
                "BABA ROCK . . . ."
            ),
            hint = "ROCK IS FLAG turns every rock into a flag."
        ),

        LevelDef(
            "07", "Stay On The Path",
            listOf(
                "^BABA ^ON ^GRASS ^IS ^YOU . .",
                "^FLAG ^IS ^WIN . . . . .",
                ". . . . . . .",
                "GRASS GRASS GRASS GRASS GRASS GRASS .",
                "BABA+GRASS . . . . FLAG+GRASS ."
            ),
            hint = "You are only YOU while standing on grass."
        ),

        LevelDef(
            "08", "Face The Music",
            listOf(
                "^BABA ^FACING ^STAR ^IS ^WIN .",
                "^BABA ^IS ^YOU . . . .",
                ". . . . . .",
                ". . . . . .",
                ". . . STAR . .",
                ". . . . . .",
                "BABA . . . . ."
            ),
            hint = "FACING checks the tile you last moved towards."
        ),

        LevelDef(
            "09", "Sink Or Swim",
            listOf(
                "^BABA ^IS ^YOU . . .",
                "^WATER ^IS ^SINK . . .",
                "^ROCK ^IS ^PUSH ^AND ^SINK .",
                "^FLAG ^IS ^WIN .",
                "BABA ROCK WATER FLAG ."
            ),
            hint = "Two SINK things on one tile destroy each other."
        ),

        LevelDef(
            "10", "Fire And Ice",
            listOf(
                "^BABA ^IS ^YOU . . .",
                "^FLAG ^IS ^WIN . . .",
                "^ICE ^IS ^DEFEAT ^AND ^MELT .",
                "^LAVA ^IS ^PUSH ^AND ^HOT .",
                "WALL WALL WALL WALL WALL WALL",
                "WALL BABA LAVA ICE . FLAG",
                "WALL WALL WALL WALL WALL WALL"
            ),
            hint = "MELT things vanish when they touch something HOT."
        ),

        LevelDef(
            "11", "Left Behind",
            listOf(
                "^BABA ^IS ^YOU ^AND ^FLOAT .",
                "^WATER ^IS ^SINK . . . .",
                "^ROCK ^IS ^PUSH ^AND ^SINK .",
                "^ROCK ^HAS ^FLAG . . . .",
                "^FLAG ^IS ^WIN . . . .",
                ". . . . . .",
                "BABA ROCK . WATER . ."
            ),
            hint = "HAS leaves something behind when a thing is destroyed."
        ),

        LevelDef(
            "12", "Who Are You",
            listOf(
                "^WALL ^IS ^YOU . . .",
                "^BABA ^IS ^STOP . . .",
                "^FLAG ^IS ^WIN . . .",
                ". . . . . .",
                "BABA . WALL . . .",
                ". . . . . .",
                ". . . . FLAG ."
            ),
            hint = "YOU does not have to mean BABA."
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
                "^BABA ^IS ^YOU . . . .",
                "^DOOR ^IS ^TELE . . . .",
                "^FLAG ^IS ^WIN . . . .",
                ". . . . . . .",
                "BABA DOOR . . . . .",
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL WALL WALL FLAG+DOOR WALL WALL WALL",
                "WALL WALL WALL WALL WALL WALL WALL"
            ),
            hint = "Step on a door and come out of the other one."
        ),

        LevelDef(
            "15", "Mind The Gap",
            listOf(
                "^BABA ^IS ^YOU . . . .",
                "^LAVA ^IS ^DEFEAT . . . .",
                "^FLAG ^IS ^WIN . . . .",
                ". . . . . . .",
                "BABA . . . . . .",
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
                ". ^BABA ^IS ^YOU . . .",
                ". . . . . . .",
                "BABA . . . ROCK . ."
            ),
            hint = "A IS A stops every other change to A."
        ),

        LevelDef(
            "17", "Moving Parts",
            listOf(
                "^FLAG ^IS . ^WIN . ROCK< .",
                ". . . . . . .",
                "WALL WALL WALL WALL WALL WALL WALL",
                "WALL BABA FLAG . . . WALL",
                "WALL WALL WALL WALL WALL WALL WALL",
                ". . . . . . .",
                "^BABA ^IS ^YOU . . . ."
            ),
            hint = "That rock will not wait for you."
        )
    )

    fun byId(id: String): LevelDef? = all.firstOrNull { it.id == id }
}
