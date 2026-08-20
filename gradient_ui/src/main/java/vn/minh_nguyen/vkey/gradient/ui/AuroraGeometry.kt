package vn.minh_nguyen.vkey.gradient.ui

/**
 * Phần tính toán của [AuroraView] — thuần Kotlin (không import `android.*`) để unit test được
 * trên JVM.
 *
 * Quy ước hình học: viền và nền vẽ trên CÙNG một rect đã thụt vào nửa độ dày viền, bán kính
 * giảm đúng nửa độ dày đó ([insetRadius]). Nhờ vậy mép NGOÀI của viền trùng mép view và hình
 * tròn / pill vẫn tròn đều.
 */
object AuroraGeometry {

    /**
     * Toạ độ đầu-cuối cho LinearGradient theo orientation, trả về [x0, y0, x1, y1];
     * orientation lạ rơi về [AuroraOrientation.LEFT_RIGHT].
     */
    fun endpoints(orientation: Int, width: Float, height: Float): FloatArray = when (orientation) {
        AuroraOrientation.TOP_BOTTOM -> floatArrayOf(0f, 0f, 0f, height)
        AuroraOrientation.TOP_LEFT_BOTTOM_RIGHT -> floatArrayOf(0f, 0f, width, height)
        AuroraOrientation.TOP_RIGHT_BOTTOM_LEFT -> floatArrayOf(width, 0f, 0f, height)
        else -> floatArrayOf(0f, 0f, width, 0f)
    }

    /**
     * Viền stroke vẽ giữa đường path nên rect phải inset nửa độ dày viền,
     * không thì nửa ngoài của viền bị cắt mất ở mép view.
     */
    fun strokeInset(strokeWidth: Float): Float = strokeWidth / 2f

    /**
     * Bán kính thực dùng: `pill` = true thì tự lấy nửa cạnh ngắn (w == h ra hình tròn,
     * w > h ra pill) và bỏ qua [cornerRadius]; ngược lại clamp [cornerRadius] để không vỡ hình.
     */
    fun resolveRadius(pill: Boolean, cornerRadius: Float, width: Float, height: Float): Float {
        val maxRadius = minOf(width, height) / 2f
        if (maxRadius <= 0f) return 0f
        return if (pill) maxRadius else cornerRadius.coerceIn(0f, maxRadius)
    }

    /** Bán kính của rect đã thụt vào [inset] để vẫn đồng tâm với đường bao ngoài. */
    fun insetRadius(radius: Float, inset: Float): Float = (radius - inset).coerceAtLeast(0f)

    /** Viền dày quá nửa cạnh ngắn thì rect thụt vào sẽ âm → clamp lại. */
    fun clampStrokeWidth(strokeWidth: Float, width: Float, height: Float): Float =
        strokeWidth.coerceIn(0f, minOf(width, height) / 2f)

    /**
     * `LinearGradient` đòi tối thiểu 2 màu — mảng 1 màu (rất hay gặp khi muốn viền/nền đơn sắc)
     * được nhân đôi thành gradient phẳng thay vì ném `IllegalArgumentException`.
     * Mảng rỗng giữ nguyên rỗng: chỗ gọi hiểu là "không vẽ lớp này".
     */
    fun normalizeColors(colors: IntArray): IntArray =
        if (colors.size == 1) intArrayOf(colors[0], colors[0]) else colors
}
