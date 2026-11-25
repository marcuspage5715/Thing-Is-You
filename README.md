# Thing Is You

An Android puzzle game about pushing words around to change the rules of the world.

Each level is a single screen of objects and word tiles. The word tiles spell rules:
`THING IS YOU`, `WALL IS STOP`, `FLAG IS WIN`. Move the words and the rules change with
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

- **Swipe** anywhere on the board to move one tile. One swipe moves exactly one tile, so a
  long drag will not run away from you.
- **Arrow pad** at the bottom of the screen moves one tile per tap. Use this when you want
  to be certain of a single step.
- Movement snaps from tile to tile. There is no sliding animation, so a piece is never
  drawn straddling a grid line or part-way between cells.
- **UNDO** steps back one move, all the way to the start.
- **RESET** returns the puzzle to its opening position.
- **BACK** leaves the puzzle for the level list.
- **EXIT** on the main screen closes the app.

There is no timer and no move limit. Nothing is scored.

## Progress

Levels unlock in order: the next puzzle opens when you finish the one before it. Solved
levels stay open so you can replay them. Progress is stored on the device in
SharedPreferences and nothing leaves the phone.

---

## The words

Objects: `THING` `PIP` `FLAG` `WALL` `ROCK` `WATER` `LAVA` `GRASS` `METAL` `ICE`
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

Twenty-two puzzles, in order. Each unlocks when the one before it is solved.

**No level ever starts with a WIN rule in place.** That is the design rule this set is built
on: if `FLAG IS WIN` is already sitting on the board, the level is a walk, not a puzzle. In
every level here you have to build the winning sentence yourself — bring scattered words
into one line, work out which object can actually be reached, or rewrite what the player is.
`tools/Difficulty.kt` fails the build if any level ever hands the answer over.

| # | Name | The problem |
| --- | --- | --- |
| 01 | Assembly Required | Nothing says FLAG is WIN. Line the words up. |
| 02 | The Wrong Flag | There is no flag on the board. Pick a different goal. |
| 03 | Become The Goal | You do not have to go anywhere — make yourself the goal. |
| 04 | Not Quite | A NOT sits inside the sentence. Remove it and close the gap. |
| 05 | Two Halves | Both halves are in one row; push them together. |
| 06 | Cornered | The words are jammed against the edge. Push up the column. |
| 07 | Break The Wall | The flag is STOP, so you cannot enter it. Change what FLAG is. |
| 08 | Deep Water | You sink. Make yourself able to cross. |
| 09 | Sink Or Swim | One rock clears one tile of water. Then finish the sentence. |
| 10 | Fire And Ice | Melt the ice, then build above it. |
| 11 | Left Behind | HAS leaves something behind. Sink the rock and look. |
| 12 | Who Are You | You are not the thing you think you are. |
| 13 | Wordplay | You drive the words themselves. |
| 14 | Doorways | The flag is sealed in; the door is the only way. |
| 15 | Mind The Gap | Build the sentence, then cross without touching lava. |
| 16 | Frozen | ROCK IS ROCK freezes rocks. Break it, then finish the other sentence. |
| 17 | Moving Parts | You are sealed in. Let the moving rock do the work. |
| 18 | Fireproof | You are MELT and the lava is HOT. Cancel that, then build. |
| 19 | Making Things | MAKE turns a destroyed thing into something else. |
| 20 | Wrong Way Round | FLAG IS LAVA, and there is no flag. Give the goal to something else. |
| 21 | Copy | ROCK IS FLAG already works. It still needs something worth reaching. |
| 22 | Finale | Break a rule, rewrite an object, finish the sentence. |

## How the puzzles were checked

The rules engine is plain Kotlin with no Android dependencies, so it runs on the JVM
during development.

- `tools/Tests.kt` — 49 assertions covering push chains, blocking, every property, the
  parser, undo, and the "no double move in one turn" guarantees.
- `tools/Solver.kt` — a depth-first search with a transposition table.
- `tools/Verify.kt` — runs the solver over every level and then **replays the solution**
  through the real engine, so a reported solution is a witness rather than a guess.
- `tools/Lint.kt` — checks that no level draws walls as scenery without a rule making them
  STOP, which would let the player walk straight through them.
- `tools/Difficulty.kt` — checks that no level starts with a WIN rule already in place, and
  reports the solution length of each puzzle.

Run them with a JDK and the Kotlin command line compiler:

```
E=app/src/main/java/com/thingisyou/engine/*.kt

kotlinc $E tools/Solver.kt tools/Tests.kt -include-runtime -d tests.jar && java -jar tests.jar
kotlinc $E tools/Solver.kt tools/Verify.kt -include-runtime -d verify.jar && java -jar verify.jar
kotlinc $E tools/Lint.kt -include-runtime -d lint.jar && java -jar lint.jar
kotlinc $E tools/Solver.kt tools/Difficulty.kt -include-runtime -d diff.jar && java -jar diff.jar
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
    Levels.kt      the twenty-two puzzles
  MainActivity.kt         title screen, PLAY / LEVELS / EXIT
  LevelSelectActivity.kt  grid of puzzles, locked until reached
  GameActivity.kt         one puzzle, arrow pad, and the end-of-level overlay
  GameView.kt             canvas rendering, swipe input, move animation
  Sfx.kt                  one-shot sound effects
  Progress.kt             solved-level tracking
  Levels_order.kt         unlocking order and next-level lookup
tools/                    tests, solver, verifier, wall lint, difficulty audit (not shipped)
```

---

## Credits

Inspired by the rule-manipulation puzzle genre popularised by *Baba Is You* (Hempuli Oy).
This is an independent implementation: no assets, code, or levels from that game are used,
and all twenty-two puzzles were designed and machine-verified for this project.
