package com.thingisyou.engine

/** Four kinds of tile that exist in the word vocabulary. */
enum class WordKind { NOUN, PROP, VERB, OP }

/**
 * Every word that can appear on a text tile.
 * NOUN  - names an object class (and can be a rule subject or object)
 * PROP  - a property that can be granted by an `IS` rule
 * VERB  - IS / HAS / MAKE
 * OP    - AND / NOT / ON / FACING
 */
enum class Word(val label: String, val kind: WordKind) {
    // ---- nouns (objects) ----
    THING("THING", WordKind.NOUN),
    PIP("PIP", WordKind.NOUN),
    FLAG("FLAG", WordKind.NOUN),
    WALL("WALL", WordKind.NOUN),
    ROCK("ROCK", WordKind.NOUN),
    WATER("WATER", WordKind.NOUN),
    LAVA("LAVA", WordKind.NOUN),
    GRASS("GRASS", WordKind.NOUN),
    METAL("METAL", WordKind.NOUN),
    ICE("ICE", WordKind.NOUN),
    FIRE("FIRE", WordKind.NOUN),
    BOX("BOX", WordKind.NOUN),
    STAR("STAR", WordKind.NOUN),
    TREE("TREE", WordKind.NOUN),
    DOOR("DOOR", WordKind.NOUN),
    KEY("KEY", WordKind.NOUN),
    MOON("MOON", WordKind.NOUN),
    TEXT("TEXT", WordKind.NOUN),

    // ---- properties ----
    YOU("YOU", WordKind.PROP),
    WIN("WIN", WordKind.PROP),
    STOP("STOP", WordKind.PROP),
    PUSH("PUSH", WordKind.PROP),
    SINK("SINK", WordKind.PROP),
    DEFEAT("DEFEAT", WordKind.PROP),
    HOT("HOT", WordKind.PROP),
    MELT("MELT", WordKind.PROP),
    MOVE("MOVE", WordKind.PROP),
    SHIFT("SHIFT", WordKind.PROP),
    TELE("TELE", WordKind.PROP),
    FLOAT("FLOAT", WordKind.PROP),

    // ---- verbs ----
    IS("IS", WordKind.VERB),
    HAS("HAS", WordKind.VERB),
    MAKE("MAKE", WordKind.VERB),

    // ---- operators ----
    AND("AND", WordKind.OP),
    NOT("NOT", WordKind.OP),
    ON("ON", WordKind.OP),
    FACING("FACING", WordKind.OP);

    companion object {
        private val byLabel: Map<String, Word> = entries.associateBy { it.label }

        /** Case-insensitive lookup used by the level parser. */
        fun of(label: String): Word? = byLabel[label.uppercase()]
    }
}

/** The properties a rule can grant. Kept separate so the hot path uses cheap sets. */
enum class Prop {
    YOU, WIN, STOP, PUSH, SINK, DEFEAT, HOT, MELT, MOVE, SHIFT, TELE, FLOAT;

    companion object {
        fun of(w: Word): Prop? = when (w) {
            Word.YOU -> YOU
            Word.WIN -> WIN
            Word.STOP -> STOP
            Word.PUSH -> PUSH
            Word.SINK -> SINK
            Word.DEFEAT -> DEFEAT
            Word.HOT -> HOT
            Word.MELT -> MELT
            Word.MOVE -> MOVE
            Word.SHIFT -> SHIFT
            Word.TELE -> TELE
            Word.FLOAT -> FLOAT
            else -> null
        }
    }
}

/** Grid direction. NONE means "not facing anywhere". */
enum class Dir(val dx: Int, val dy: Int) {
    NONE(0, 0), UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

    val opposite: Dir
        get() = when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
            NONE -> NONE
        }

    companion object {
        /** Parses the optional one-character facing suffix used in level text. */
        fun ofSuffix(c: Char): Dir? = when (c) {
            '>' -> RIGHT
            '<' -> LEFT
            '^' -> UP
            'v', 'V' -> DOWN
            else -> null
        }
    }
}
