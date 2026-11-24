package com.thingisyou

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.thingisyou.engine.Dir
import com.thingisyou.engine.Event
import com.thingisyou.engine.GameSession
import com.thingisyou.engine.Item
import com.thingisyou.engine.State
import com.thingisyou.engine.Word
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Draws one puzzle and nothing else. All art is flat vector work done in code, so the
 * app ships no image assets.
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var session: GameSession? = null
        private set

    /** Fired after every accepted move, with the side effects worth a sound. */
    var onEvents: ((Set<Event>) -> Unit)? = null

    /** Fired once when the level is won or lost. */
    var onEnd: ((Boolean) -> Unit)? = null

    private var state: State = State(1, 1, emptyList())
    private val sources = ArrayList<IntArray>()
    private var animStart = 0L
    private val animDurMs = 110f

    private var cell = 0f
    private var ox = 0f
    private var oy = 0f

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val wordBg = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans-serif", Typeface.BOLD) }
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    private val density = resources.displayMetrics.density
    private var downX = 0f
    private var downY = 0f
    private var gestureConsumed = false

    private val path = Path()
    private val rect = RectF()

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    // ------------------------------------------------------------------ binding

    fun bind(s: GameSession) {
        session = s
        state = s.state
        sources.clear()
        for (item in state.items) sources.add(intArrayOf(item.x, item.y))
        layoutBoard()
        invalidate()
    }

    fun refresh(animate: Boolean) {
        val s = session ?: return
        apply(s.state, animate)
        layoutBoard()
    }

    private fun apply(newState: State, animate: Boolean) {
        val old = state
        sources.clear()
        if (animate && old.w == newState.w && old.h == newState.h) {
            val used = BooleanArray(old.items.size)
            // First lock in everything that stayed put, then match one-tile moves.
            val src = arrayOfNulls<IntArray>(newState.items.size)
            for (i in newState.items.indices) {
                val item = newState.items[i]
                for (j in old.items.indices) {
                    if (used[j]) continue
                    val o = old.items[j]
                    if (o.word == item.word && o.isText == item.isText && o.x == item.x && o.y == item.y) {
                        used[j] = true
                        src[i] = intArrayOf(o.x, o.y)
                        break
                    }
                }
            }
            for (i in newState.items.indices) {
                if (src[i] != null) continue
                val item = newState.items[i]
                for (j in old.items.indices) {
                    if (used[j]) continue
                    val o = old.items[j]
                    if (o.word != item.word || o.isText != item.isText) continue
                    if (abs(o.x - item.x) + abs(o.y - item.y) <= 1) {
                        used[j] = true
                        src[i] = intArrayOf(o.x, o.y)
                        break
                    }
                }
            }
            for (i in newState.items.indices) {
                sources.add(src[i] ?: intArrayOf(newState.items[i].x, newState.items[i].y))
            }
        } else {
            for (item in newState.items) sources.add(intArrayOf(item.x, item.y))
        }
        state = newState
        animStart = System.currentTimeMillis()
        invalidate()
    }

    // -------------------------------------------------------------------- layout

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutBoard()
    }

    private fun layoutBoard() {
        if (width == 0 || height == 0) return
        val pad = 10f * density
        val availW = width - pad * 2
        val availH = height - pad * 2
        cell = min(availW / state.w, availH / state.h)
        ox = (width - cell * state.w) / 2f
        oy = (height - cell * state.h) / 2f
    }

    // --------------------------------------------------------------------- input

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = e.x
                downY = e.y
                gestureConsumed = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // One move per gesture. Firing again mid-drag made the board lurch and made
                // short swipes feel like they were being swallowed.
                if (gestureConsumed) return true
                val dx = e.x - downX
                val dy = e.y - downY
                val thresh = maxOf(18f * density, cell * 0.28f)
                if (abs(dx) >= thresh || abs(dy) >= thresh) {
                    gestureConsumed = true
                    move(directionOf(dx, dy))
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!gestureConsumed) {
                    val dx = e.x - downX
                    val dy = e.y - downY
                    val thresh = maxOf(12f * density, cell * 0.18f)
                    if (abs(dx) > thresh || abs(dy) > thresh) move(directionOf(dx, dy))
                }
                performClick()
                return true
            }
        }
        return super.onTouchEvent(e)
    }

    private fun directionOf(dx: Float, dy: Float): Dir =
        if (abs(dx) > abs(dy)) {
            if (dx > 0) Dir.RIGHT else Dir.LEFT
        } else {
            if (dy > 0) Dir.DOWN else Dir.UP
        }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun move(dir: Dir) {
        val s = session ?: return
        if (s.won || s.lost) return
        val events = s.step(dir)
        apply(s.state, true)
        if (events.isNotEmpty()) onEvents?.invoke(events)
        if (s.won) onEnd?.invoke(true)
        else if (s.lost) onEnd?.invoke(false)
    }

    // ---------------------------------------------------------------------- draw

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (cell <= 0f) layoutBoard()

        val t = ((System.currentTimeMillis() - animStart) / animDurMs).coerceIn(0f, 1f)
        val ease = 1f - (1f - t) * (1f - t)

        // board plate
        tilePaint.color = 0xFF171A22.toInt()
        rect.set(ox, oy, ox + cell * state.w, oy + cell * state.h)
        canvas.drawRoundRect(rect, cell * 0.18f, cell * 0.18f, tilePaint)

        // faint grid
        gridPaint.color = 0x14FFFFFF
        gridPaint.strokeWidth = 1f * density
        for (x in 0..state.w) {
            val px = ox + x * cell
            canvas.drawLine(px, oy, px, oy + cell * state.h, gridPaint)
        }
        for (y in 0..state.h) {
            val py = oy + y * cell
            canvas.drawLine(ox, py, ox + cell * state.w, py, gridPaint)
        }

        // items, terrain first
        val order = state.items.indices.sortedBy { zOrder(state.items[it]) }
        for (i in order) {
            val item = state.items[i]
            val src = sources.getOrNull(i)
            val fx = if (src != null) src[0] + (item.x - src[0]) * ease else item.x.toFloat()
            val fy = if (src != null) src[1] + (item.y - src[1]) * ease else item.y.toFloat()
            val cx = ox + (fx + 0.5f) * cell
            val cy = oy + (fy + 0.5f) * cell
            if (item.isText) drawWordTile(canvas, item.word, cx, cy, cell)
            else drawObject(canvas, item.word, cx, cy, cell)
        }
    }

    private fun zOrder(item: Item): Int = when (item.word) {
        Word.GRASS, Word.WATER, Word.LAVA -> 0
        Word.ICE, Word.METAL -> 1
        Word.WALL, Word.TREE -> 2
        Word.ROCK, Word.BOX, Word.DOOR, Word.KEY -> 3
        Word.FLAG, Word.STAR, Word.MOON, Word.FIRE -> 4
        else -> 5
    }

    // ------------------------------------------------------------------ word art

    private fun wordColor(w: Word): Int = when (w.kind) {
        com.thingisyou.engine.WordKind.NOUN -> 0xFF3B6FD4.toInt()
        com.thingisyou.engine.WordKind.PROP -> 0xFF7C4FD6.toInt()
        com.thingisyou.engine.WordKind.VERB -> 0xFFE6E9F0.toInt()
        com.thingisyou.engine.WordKind.OP -> 0xFFD9A038.toInt()
    }

    private fun wordTextColor(w: Word): Int =
        if (w.kind == com.thingisyou.engine.WordKind.VERB) 0xFF1A1D26.toInt() else Color.WHITE

    private fun drawWordTile(canvas: Canvas, word: Word, cx: Float, cy: Float, size: Float) {
        val r = size * 0.42f
        wordBg.color = wordColor(word)
        canvas.drawRoundRect(cx - r, cy - r, cx + r, cy + r, size * 0.14f, size * 0.14f, wordBg)

        stroke.color = 0x33FFFFFF
        stroke.strokeWidth = size * 0.035f
        canvas.drawRoundRect(cx - r, cy - r, cx + r, cy + r, size * 0.14f, size * 0.14f, stroke)

        textPaint.color = wordTextColor(word)
        val maxW = size * 0.80f
        var ts = size * 0.34f
        textPaint.textSize = ts
        var m = textPaint.measureText(word.label)
        if (m > maxW) {
            ts *= maxW / m
            textPaint.textSize = ts
            m = textPaint.measureText(word.label)
        }
        val fm = textPaint.fontMetrics
        canvas.drawText(word.label, cx - m / 2f, cy - (fm.ascent + fm.descent) / 2f, textPaint)
    }

    // ---------------------------------------------------------------- object art

    private fun drawObject(canvas: Canvas, word: Word, cx: Float, cy: Float, size: Float) {
        val s = size
        when (word) {
            Word.THING -> drawCreature(canvas, cx, cy, s, 0xFFF4F6FA.toInt(), 0xFF2A2E3A.toInt())
            Word.PIP -> drawCreature(canvas, cx, cy, s, 0xFFE8686B.toInt(), 0xFF2A2E3A.toInt())

            Word.WALL -> {
                fill.color = 0xFF3E4350.toInt()
                rect.set(cx - s * 0.46f, cy - s * 0.46f, cx + s * 0.46f, cy + s * 0.46f)
                canvas.drawRoundRect(rect, s * 0.08f, s * 0.08f, fill)
                stroke.color = 0xFF596072.toInt()
                stroke.strokeWidth = s * 0.06f
                canvas.drawRoundRect(rect, s * 0.08f, s * 0.08f, stroke)
            }

            Word.ROCK -> {
                fill.color = 0xFF8B919D.toInt()
                path.reset()
                path.moveTo(cx - s * 0.42f, cy + s * 0.30f)
                path.lineTo(cx - s * 0.36f, cy - s * 0.22f)
                path.lineTo(cx - s * 0.10f, cy - s * 0.44f)
                path.lineTo(cx + s * 0.28f, cy - s * 0.36f)
                path.lineTo(cx + s * 0.44f, cy + s * 0.06f)
                path.lineTo(cx + s * 0.22f, cy + s * 0.42f)
                path.lineTo(cx - s * 0.22f, cy + s * 0.42f)
                path.close()
                canvas.drawPath(path, fill)
            }

            Word.WATER -> {
                fill.color = 0xFF2F6FCB.toInt()
                rect.set(cx - s * 0.46f, cy - s * 0.46f, cx + s * 0.46f, cy + s * 0.46f)
                canvas.drawRoundRect(rect, s * 0.12f, s * 0.12f, fill)
                stroke.color = 0x66FFFFFF
                stroke.strokeWidth = s * 0.05f
                for (k in 0..1) {
                    val yy = cy - s * 0.14f + k * s * 0.30f
                    path.reset()
                    path.moveTo(cx - s * 0.28f, yy)
                    path.quadTo(cx - s * 0.10f, yy - s * 0.10f, cx + s * 0.02f, yy)
                    path.quadTo(cx + s * 0.14f, yy + s * 0.10f, cx + s * 0.28f, yy)
                    canvas.drawPath(path, stroke)
                }
            }

            Word.LAVA -> {
                fill.color = 0xFFD8451C.toInt()
                rect.set(cx - s * 0.46f, cy - s * 0.46f, cx + s * 0.46f, cy + s * 0.46f)
                canvas.drawRoundRect(rect, s * 0.12f, s * 0.12f, fill)
                fill.color = 0xFFFFA33C.toInt()
                canvas.drawCircle(cx - s * 0.14f, cy + s * 0.10f, s * 0.13f, fill)
                canvas.drawCircle(cx + s * 0.16f, cy - s * 0.12f, s * 0.09f, fill)
            }

            Word.GRASS -> {
                fill.color = 0xFF2F7A4B.toInt()
                rect.set(cx - s * 0.46f, cy - s * 0.46f, cx + s * 0.46f, cy + s * 0.46f)
                canvas.drawRoundRect(rect, s * 0.12f, s * 0.12f, fill)
                stroke.color = 0xFF4FA46B.toInt()
                stroke.strokeWidth = s * 0.045f
                for (k in -1..1) {
                    val bx = cx + k * s * 0.20f
                    canvas.drawLine(bx, cy + s * 0.22f, bx, cy - s * 0.10f, stroke)
                }
            }

            Word.METAL -> {
                fill.color = 0xFF7E8797.toInt()
                rect.set(cx - s * 0.46f, cy - s * 0.46f, cx + s * 0.46f, cy + s * 0.46f)
                canvas.drawRoundRect(rect, s * 0.10f, s * 0.10f, fill)
                fill.color = 0xFFB4BCC9.toInt()
                for (dx in listOf(-0.26f, 0.26f)) for (dy in listOf(-0.26f, 0.26f)) {
                    canvas.drawCircle(cx + s * dx, cy + s * dy, s * 0.045f, fill)
                }
            }

            Word.ICE -> {
                fill.color = 0xCC9FD3F0.toInt()
                rect.set(cx - s * 0.44f, cy - s * 0.44f, cx + s * 0.44f, cy + s * 0.44f)
                canvas.drawRoundRect(rect, s * 0.10f, s * 0.10f, fill)
                stroke.color = 0xFFEAF7FF.toInt()
                stroke.strokeWidth = s * 0.05f
                canvas.drawRoundRect(rect, s * 0.10f, s * 0.10f, stroke)
            }

            Word.FIRE -> {
                fill.color = 0xFFFF7A29.toInt()
                path.reset()
                path.moveTo(cx, cy - s * 0.46f)
                path.quadTo(cx + s * 0.34f, cy - s * 0.02f, cx + s * 0.20f, cy + s * 0.34f)
                path.quadTo(cx, cy + s * 0.46f, cx - s * 0.20f, cy + s * 0.34f)
                path.quadTo(cx - s * 0.34f, cy - s * 0.02f, cx, cy - s * 0.46f)
                path.close()
                canvas.drawPath(path, fill)
                fill.color = 0xFFFFD166.toInt()
                canvas.drawCircle(cx, cy + s * 0.12f, s * 0.11f, fill)
            }

            Word.BOX -> {
                fill.color = 0xFFA9743F.toInt()
                rect.set(cx - s * 0.42f, cy - s * 0.42f, cx + s * 0.42f, cy + s * 0.42f)
                canvas.drawRoundRect(rect, s * 0.07f, s * 0.07f, fill)
                stroke.color = 0xFF6E4826.toInt()
                stroke.strokeWidth = s * 0.05f
                canvas.drawLine(cx - s * 0.42f, cy - s * 0.42f, cx + s * 0.42f, cy + s * 0.42f, stroke)
                canvas.drawLine(cx + s * 0.42f, cy - s * 0.42f, cx - s * 0.42f, cy + s * 0.42f, stroke)
            }

            Word.STAR -> {
                fill.color = 0xFFFFD447.toInt()
                path.reset()
                for (k in 0 until 10) {
                    val ang = (-90 + k * 36) * Math.PI / 180.0
                    val rad = if (k % 2 == 0) s * 0.46f else s * 0.20f
                    val px = cx + (cos(ang) * rad).toFloat()
                    val py = cy + (sin(ang) * rad).toFloat()
                    if (k == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                canvas.drawPath(path, fill)
            }

            Word.TREE -> {
                fill.color = 0xFF6B4A2A.toInt()
                rect.set(cx - s * 0.08f, cy + s * 0.06f, cx + s * 0.08f, cy + s * 0.44f)
                canvas.drawRect(rect, fill)
                fill.color = 0xFF2E8B57.toInt()
                path.reset()
                path.moveTo(cx, cy - s * 0.46f)
                path.lineTo(cx + s * 0.34f, cy + s * 0.10f)
                path.lineTo(cx - s * 0.34f, cy + s * 0.10f)
                path.close()
                canvas.drawPath(path, fill)
            }

            Word.DOOR -> {
                fill.color = 0xFF8B5E34.toInt()
                rect.set(cx - s * 0.30f, cy - s * 0.44f, cx + s * 0.30f, cy + s * 0.44f)
                canvas.drawRoundRect(rect, s * 0.14f, s * 0.14f, fill)
                fill.color = 0xFFFFD447.toInt()
                canvas.drawCircle(cx + s * 0.16f, cy + s * 0.02f, s * 0.05f, fill)
            }

            Word.KEY -> {
                fill.color = 0xFFFFC94A.toInt()
                canvas.drawCircle(cx - s * 0.12f, cy, s * 0.17f, fill)
                fill.color = 0xFF171A22.toInt()
                canvas.drawCircle(cx - s * 0.12f, cy, s * 0.07f, fill)
                fill.color = 0xFFFFC94A.toInt()
                rect.set(cx + s * 0.02f, cy - s * 0.05f, cx + s * 0.40f, cy + s * 0.05f)
                canvas.drawRect(rect, fill)
                rect.set(cx + s * 0.26f, cy + s * 0.02f, cx + s * 0.34f, cy + s * 0.16f)
                canvas.drawRect(rect, fill)
            }

            Word.MOON -> {
                fill.color = 0xFFE3E8F2.toInt()
                canvas.drawCircle(cx, cy, s * 0.40f, fill)
                fill.color = 0xFF171A22.toInt()
                canvas.drawCircle(cx + s * 0.16f, cy - s * 0.10f, s * 0.33f, fill)
            }

            else -> {
                fill.color = 0xFF8B919D.toInt()
                canvas.drawCircle(cx, cy, s * 0.36f, fill)
            }
        }
    }

    private fun drawCreature(canvas: Canvas, cx: Float, cy: Float, s: Float, body: Int, ink: Int) {
        fill.color = body
        val r = s * 0.36f
        // ears
        canvas.drawCircle(cx - r * 0.72f, cy - r * 0.78f, r * 0.30f, fill)
        canvas.drawCircle(cx + r * 0.72f, cy - r * 0.78f, r * 0.30f, fill)
        rect.set(cx - r, cy - r, cx + r, cy + r)
        canvas.drawRoundRect(rect, r * 0.55f, r * 0.55f, fill)

        fill.color = ink
        canvas.drawCircle(cx - r * 0.34f, cy - r * 0.10f, r * 0.13f, fill)
        canvas.drawCircle(cx + r * 0.34f, cy - r * 0.10f, r * 0.13f, fill)
        stroke.color = ink
        stroke.strokeWidth = r * 0.12f
        path.reset()
        path.moveTo(cx - r * 0.20f, cy + r * 0.32f)
        path.quadTo(cx, cy + r * 0.50f, cx + r * 0.20f, cy + r * 0.32f)
        canvas.drawPath(path, stroke)
    }
}
