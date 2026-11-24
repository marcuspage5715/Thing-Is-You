# Thing Is You

An Android puzzle game about pushing words around to change the rules of the world.

Each level is a single screen of objects and word tiles. The word tiles spell rules:
`BABA IS YOU`, `WALL IS STOP`, `FLAG IS WIN`. Move the words and the rules change with
them — walls stop blocking, rocks become flags, water starts swallowing things.

Written in Kotlin for Android, no third-party libraries, no image assets: every object is
drawn as flat vector art in code.

---

## Building it

Requirements: Android Studio (Koala or newer), JDK 17, Android SDK 34.

```
git clone https://github.com/marcuspage5715/Thing-Is-You.git
```

Open the folder in Android Studio and press Run, or from the command line:

```
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

- `minSdk` 24 (Android 7.0), `targetSdk` 34
- Application ID `com.thingisyou`
- The app requests no permissions and has no network code.

---

## Controls

- **Swipe** anywhere on the board to move one tile. Hold and drag to keep moving.
- **UNDO** steps back one move, all the way to the start.
- **RESET** returns the puzzle to its opening position.
- **BACK** leaves the puzzle for the level list.
- **EXIT** on the main screen closes the app.

There is no timer and no move limit. Nothing is scored.

---

## The words

Objects: `BABA` `KEKE` `FLAG` `WALL` `ROCK` `WATER` `LAVA` `GRASS` `METAL` `ICE`
`FIRE` `BOX` `STAR` `TREE` `DOOR` `KEY` `MOON` `TEXT`

| Word | Meaning |
| --- | --- |
| `IS` | Links a subject to what it is or what it does. |
| `AND` | Joins two nouns or two properties into one rule. |
| `NOT` | Negates the word after it. |
| `ON` | `A ON B IS C` — the rule only applies while A stands on B. |
| `FACING` | `A FACING B IS C` — only applies while A faces B. |
| `HAS` | `A HAS B` — when A is destroyed, a B is left behind. |
| `MAKE` | `A MAKE B` — when A is destroyed, it becomes a B. |
| `YOU` | You control it. |
| `WIN` | Touching it with something YOU finishes the level. |
| `STOP` | Nothing can enter its tile. Beats PUSH. |
| `PUSH` | Can be shoved by whatever moves into it. |
| `SINK` | Swallows anything that cannot FLOAT. Two SINK things on one tile destroy each other. |
| `FLOAT` | Immune to SINK. |
| `DEFEAT` | Touching it with something YOU ends the level. |
| `HOT` | Destroys anything with MELT that shares its tile. |
| `MELT` | Destroyed by HOT. |
| `MOVE` | Travels one tile per turn in the direction it faces. |
| `SHIFT` | Same as MOVE. |
| `TELE` | A thing that steps on it comes out of its partner. |
| `TEXT` | As a noun, means every word tile on the board. |

Word tiles themselves are always pushable, and are never the noun they spell.

### Rules that hold everywhere

- **One tile per input.** There is no diagonal movement, no jumping, no wrapping.
- **Push chains resolve completely or not at all.** If the far end of a chain is blocked,
  nothing in the chain moves — not even one tile.
- **STOP always beats PUSH.** An object that is both cannot be pushed.
- **Rules are read at the start of a turn.** A rule cannot help you halfway through a move.
- **A sentence must be one unbroken line** of word tiles, read left to right or top to bottom.
  A gap breaks it. So does pushing any word out of the line.
- **`A IS A` freezes A.** No other `A IS ...` rule can transform it.
- **Everything YOU moves at once,** exactly one tile each, so a word tile that is itself YOU
  cannot be shoved twice in a turn.

---

## Levels

Seventeen puzzles, in order:

| # | Name | Teaches |
| --- | --- | --- |
| 01 | First Steps | Moving and touching the goal |
| 02 | Break The Wall | Pushing a word out of a sentence |
| 03 | Push Comes To Shove | PUSH objects |
| 04 | Build A Rule | Completing a half-written sentence |
| 05 | Float Your Boat | `AND`, and crossing water |
| 06 | Metamorphosis | Transformations |
| 07 | Stay On The Path | The `ON` modifier |
| 08 | Face The Music | The `FACING` modifier |
| 09 | Sink Or Swim | SINK |
| 10 | Fire And Ice | HOT and MELT |
| 11 | Left Behind | HAS |
| 12 | Who Are You | `YOU` on something other than BABA |
| 13 | Wordplay | `TEXT IS YOU` |
| 14 | Doorways | TELE |
| 15 | Mind The Gap | DEFEAT |
| 16 | Frozen | `A IS A` |
| 17 | Moving Parts | MOVE objects |

Progress is stored on the device in SharedPreferences. Nothing leaves the phone.

---

## How the puzzles were checked

The rules engine is plain Kotlin with no Android dependencies, so it can be exercised on
the JVM during development.

- `tools/Tests.kt` — 49 assertions covering push chains, blocking, every property, the
  parser, undo, and the "no double move in one turn" guarantees.
- `tools/Solver.kt` — a depth-first search with a transposition table.
- `tools/Verify.kt` — runs the solver over every level and then **replays the solution**
  through the real engine, so a reported solution is a witness rather than a guess.

Run them with a JDK and the Kotlin command line compiler:

```
kotlinc app/src/main/java/com/thingisyou/engine/*.kt tools/Solver.kt tools/Tests.kt \
    -include-runtime -d tests.jar && java -jar tests.jar

kotlinc app/src/main/java/com/thingisyou/engine/*.kt tools/Solver.kt tools/Verify.kt \
    -include-runtime -d verify.jar && java -jar verify.jar
```

The Android sources were additionally type-checked outside Gradle against the platform
`android.jar`, using a throwaway generated `R` class. Nothing from that step ships.

---

## Layout

```
app/src/main/java/com/thingisyou/
  engine/
    Word.kt        vocabulary: nouns, properties, verbs, operators, directions
    Model.kt       board state, level text parser, game session, undo history
    Rules.kt       sentence parser and per-object property resolution
    Engine.kt      one turn: movement, pushes, transforms, sink, melt, teleport, win
    Levels.kt      the seventeen puzzles
  MainActivity.kt         title screen, PLAY / LEVELS / EXIT
  LevelSelectActivity.kt  grid of puzzles
  GameActivity.kt         one puzzle and its overlay
  GameView.kt             canvas rendering, swipe input, move animation
  Sfx.kt                  one-shot sound effects
  Progress.kt             solved-level tracking
tools/                    test suite, solver, verifier (not shipped)
```

---

## Credits

Inspired by the rule-manipulation puzzle genre popularised by *Baba Is You* (Hempuli Oy).
This is an independent, original implementation: no assets, code, or levels from the
original game are used, and all seventeen puzzles were designed and machine-verified for
this project.
