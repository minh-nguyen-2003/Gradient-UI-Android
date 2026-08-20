package vn.minh_nguyen.vkey.gradient.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout

/**
 * Container viền gradient bo góc + nền gradient tuỳ chọn — pattern "viền gradient, nền trong
 * suốt" bằng custom view thay vì drawable, nên đổi màu / đổi hướng được lúc chạy và không phải
 * đẻ ra một file drawable cho mỗi tổ hợp màu.
 *
 * Dùng như FrameLayout thường, content đặt bên trong:
 *   app:aurora_strokeColors      — mảng màu viền (`<array>`); mặc định cam → hồng → tím
 *   app:aurora_fillColors        — mảng màu nền; KHÔNG set thì nền trong suốt
 *   app:aurora_strokeWidth       — độ dày viền (mặc định 1dp)
 *   app:aurora_cornerRadius      — bán kính bo (bị bỏ qua khi bật `aurora_pill`)
 *   app:aurora_pill              — true: bán kính = nửa cạnh ngắn ⇒ view vuông ra HÌNH TRÒN,
 *                                  view ngang ra PILL, không cần biết trước kích thước
 *   app:aurora_orientation       — hướng gradient dùng chung, đóng vai trò default cho cả 2 lớp
 *   app:aurora_strokeOrientation — override hướng gradient riêng cho viền
 *   app:aurora_fillOrientation   — override hướng gradient riêng cho nền
 *   app:aurora_clipContent       — bo góc luôn content bên trong (mặc định false)
 *
 * Lưu ý: gradient **không tự đảo chiều theo RTL**.
 */
class AuroraView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val drawRect = RectF()

    private val auroraOutlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val radius = AuroraGeometry.resolveRadius(
                isPill, cornerRadius, view.width.toFloat(), view.height.toFloat()
            )
            outline.setRoundRect(0, 0, view.width, view.height, radius)
        }
    }

    /** Mảng màu viền. Mảng 1 màu ⇒ viền đơn sắc; mảng rỗng ⇒ không vẽ viền. */
    var strokeColors: IntArray = intArrayOf()
        set(value) {
            field = value
            strokePaint.shader = null
            invalidate()
        }

    /** Mảng màu nền; `null` (mặc định) ⇒ nền trong suốt. */
    var fillColors: IntArray? = null
        set(value) {
            field = value
            fillPaint.shader = null
            invalidate()
        }

    /** Độ dày viền, đơn vị px. */
    var strokeWidth: Float = 0f
        set(value) {
            if (field == value) return
            field = value
            invalidate()
        }

    var cornerRadius: Float = 0f
        set(value) {
            if (field == value) return
            field = value
            refreshOutline()
            invalidate()
        }

    /** true ⇒ bán kính = nửa cạnh ngắn (tròn khi vuông, pill khi ngang), bỏ qua [cornerRadius]. */
    var isPill: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            refreshOutline()
            invalidate()
        }

    /** Hướng gradient của viền — độc lập với nền. */
    var strokeOrientation: Int = AuroraOrientation.LEFT_RIGHT
        set(value) {
            if (field == value) return
            field = value
            strokePaint.shader = null
            invalidate()
        }

    /** Hướng gradient của nền — độc lập với viền. */
    var fillOrientation: Int = AuroraOrientation.LEFT_RIGHT
        set(value) {
            if (field == value) return
            field = value
            fillPaint.shader = null
            invalidate()
        }

    /** Bo góc luôn content bên trong. Mặc định false — bật khi bên trong có ảnh tràn viền. */
    var isClipContent: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            refreshOutline()
            invalidate()
        }

    init {
        // ViewGroup mặc định bỏ qua onDraw — phải tắt cờ này thì viền mới được vẽ
        setWillNotDraw(false)

        val ta = context.obtainStyledAttributes(
            attrs, R.styleable.AuroraView, defStyleAttr, 0
        )
        strokeWidth = ta.getDimension(
            R.styleable.AuroraView_aurora_strokeWidth,
            resources.getDimension(R.dimen.aurora_stroke_width)
        )
        cornerRadius = ta.getDimension(
            R.styleable.AuroraView_aurora_cornerRadius,
            resources.getDimension(R.dimen.aurora_corner_radius)
        )
        isPill = ta.getBoolean(R.styleable.AuroraView_aurora_pill, false)

        // aurora_orientation = default chung; 2 attr chuyên biệt override từng lớp
        val baseOrientation = ta.getInt(
            R.styleable.AuroraView_aurora_orientation,
            AuroraOrientation.LEFT_RIGHT
        )
        strokeOrientation =
            ta.getInt(R.styleable.AuroraView_aurora_strokeOrientation, baseOrientation)
        fillOrientation =
            ta.getInt(R.styleable.AuroraView_aurora_fillOrientation, baseOrientation)

        val strokeRes = ta.getResourceId(R.styleable.AuroraView_aurora_strokeColors, 0)
        strokeColors = resources.getIntArray(
            if (strokeRes != 0) strokeRes else R.array.aurora_default_stroke
        )
        val fillRes = ta.getResourceId(R.styleable.AuroraView_aurora_fillColors, 0)
        if (fillRes != 0) fillColors = resources.getIntArray(fillRes)

        isClipContent = ta.getBoolean(R.styleable.AuroraView_aurora_clipContent, false)
        ta.recycle()

        refreshOutline()
    }

    /**
     * Đổi nhiều thuộc tính trong 1 lần gọi — mỗi tham số bỏ trống thì giữ nguyên giá trị hiện
     * tại. Tiện khi bind item list: một view, nhiều trạng thái màu.
     */
    @JvmOverloads
    fun applyAurora(
        strokeColors: IntArray = this.strokeColors,
        fillColors: IntArray? = this.fillColors,
        strokeWidth: Float = this.strokeWidth,
        cornerRadius: Float = this.cornerRadius,
        pill: Boolean = this.isPill,
        strokeOrientation: Int = this.strokeOrientation,
        fillOrientation: Int = this.fillOrientation,
        clipContent: Boolean = this.isClipContent
    ) {
        this.strokeColors = strokeColors
        this.fillColors = fillColors
        this.strokeWidth = strokeWidth
        this.cornerRadius = cornerRadius
        this.isPill = pill
        this.strokeOrientation = strokeOrientation
        this.fillOrientation = fillOrientation
        this.isClipContent = clipContent
    }

    /** Set 1 lần cho cả 2 lớp — tiện khi không cần tách hướng viền / nền. */
    fun setOrientation(orientation: Int) {
        strokeOrientation = orientation
        fillOrientation = orientation
    }

    private fun refreshOutline() {
        outlineProvider =
            if (isClipContent) auroraOutlineProvider else ViewOutlineProvider.BACKGROUND
        clipToOutline = isClipContent
        invalidateOutline()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // shader phụ thuộc kích thước — dựng lại ở lần vẽ kế tiếp
        strokePaint.shader = null
        fillPaint.shader = null
    }

    private fun buildShader(colors: IntArray, orientation: Int): Shader {
        val p = AuroraGeometry.endpoints(orientation, width.toFloat(), height.toFloat())
        val safeColors = AuroraGeometry.normalizeColors(colors)
        return LinearGradient(p[0], p[1], p[2], p[3], safeColors, null, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val stroke = AuroraGeometry.clampStrokeWidth(strokeWidth, w, h)
        val inset = AuroraGeometry.strokeInset(stroke)
        val radius = AuroraGeometry.insetRadius(
            AuroraGeometry.resolveRadius(isPill, cornerRadius, w, h), inset
        )
        drawRect.set(inset, inset, w - inset, h - inset)

        fillColors?.takeIf { it.isNotEmpty() }?.let { colors ->
            if (fillPaint.shader == null) fillPaint.shader = buildShader(colors, fillOrientation)
            canvas.drawRoundRect(drawRect, radius, radius, fillPaint)
        }

        if (stroke <= 0f || strokeColors.isEmpty()) return
        if (strokePaint.shader == null) {
            strokePaint.shader = buildShader(strokeColors, strokeOrientation)
        }
        strokePaint.strokeWidth = stroke
        canvas.drawRoundRect(drawRect, radius, radius, strokePaint)
    }
}
