package vn.minh_nguyen.vkey.gradient.ui

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AuroraGeometryTest {

    // --- Hướng gradient -------------------------------------------------------------------

    @Test
    fun `left_right runs along top edge`() {
        val p = AuroraGeometry.endpoints(AuroraOrientation.LEFT_RIGHT, 320f, 44f)
        assertArrayEquals(floatArrayOf(0f, 0f, 320f, 0f), p, 0f)
    }

    @Test
    fun `top_bottom runs along left edge`() {
        val p = AuroraGeometry.endpoints(AuroraOrientation.TOP_BOTTOM, 320f, 44f)
        assertArrayEquals(floatArrayOf(0f, 0f, 0f, 44f), p, 0f)
    }

    @Test
    fun `diagonal top-left to bottom-right`() {
        val p = AuroraGeometry.endpoints(AuroraOrientation.TOP_LEFT_BOTTOM_RIGHT, 320f, 44f)
        assertArrayEquals(floatArrayOf(0f, 0f, 320f, 44f), p, 0f)
    }

    @Test
    fun `diagonal top-right to bottom-left`() {
        val p = AuroraGeometry.endpoints(AuroraOrientation.TOP_RIGHT_BOTTOM_LEFT, 320f, 44f)
        assertArrayEquals(floatArrayOf(320f, 0f, 0f, 44f), p, 0f)
    }

    @Test
    fun `unknown orientation falls back to left_right`() {
        val p = AuroraGeometry.endpoints(99, 100f, 50f)
        assertArrayEquals(floatArrayOf(0f, 0f, 100f, 0f), p, 0f)
    }

    @Test
    fun `zero size still returns valid endpoints`() {
        val p = AuroraGeometry.endpoints(AuroraOrientation.LEFT_RIGHT, 0f, 0f)
        assertArrayEquals(floatArrayOf(0f, 0f, 0f, 0f), p, 0f)
    }

    // --- Viền đồng tâm --------------------------------------------------------------------

    @Test
    fun `stroke inset is half stroke width`() {
        assertEquals(1.5f, AuroraGeometry.strokeInset(3f), 0f)
    }

    @Test
    fun `inset radius keeps circles concentric`() {
        // view 40x40 bo tròn (r=20), viền dày 4 → rect thụt 2 mỗi bên (36x36) nên r phải là 18
        val inset = AuroraGeometry.strokeInset(4f)
        assertEquals(18f, AuroraGeometry.insetRadius(20f, inset), 0f)
    }

    @Test
    fun `inset radius never goes negative`() {
        assertEquals(0f, AuroraGeometry.insetRadius(2f, 10f), 0f)
    }

    @Test
    fun `stroke width is clamped to half of shorter side`() {
        assertEquals(20f, AuroraGeometry.clampStrokeWidth(80f, 200f, 40f), 0f)
        assertEquals(0f, AuroraGeometry.clampStrokeWidth(-3f, 200f, 40f), 0f)
        assertEquals(3f, AuroraGeometry.clampStrokeWidth(3f, 200f, 40f), 0f)
    }

    // --- Bán kính -------------------------------------------------------------------------

    @Test
    fun `pill on square view gives circle radius`() {
        assertEquals(20f, AuroraGeometry.resolveRadius(true, 4f, 40f, 40f), 0f)
    }

    @Test
    fun `pill on wide view uses shorter side`() {
        assertEquals(30f, AuroraGeometry.resolveRadius(true, 4f, 220f, 60f), 0f)
    }

    @Test
    fun `pill wins over cornerRadius`() {
        assertEquals(50f, AuroraGeometry.resolveRadius(true, 2f, 100f, 100f), 0f)
    }

    @Test
    fun `cornerRadius is clamped to half of shorter side`() {
        assertEquals(30f, AuroraGeometry.resolveRadius(false, 999f, 200f, 60f), 0f)
    }

    @Test
    fun `negative cornerRadius falls back to zero`() {
        assertEquals(0f, AuroraGeometry.resolveRadius(false, -8f, 100f, 100f), 0f)
    }

    @Test
    fun `zero size gives zero radius`() {
        assertEquals(0f, AuroraGeometry.resolveRadius(true, 12f, 0f, 0f), 0f)
        assertEquals(0f, AuroraGeometry.resolveRadius(false, 12f, 40f, 0f), 0f)
    }

    // --- Màu ------------------------------------------------------------------------------

    @Test
    fun `single color is doubled into a flat gradient`() {
        // LinearGradient ném IllegalArgumentException nếu chỉ có 1 màu
        assertArrayEquals(
            intArrayOf(0xFFD66341.toInt(), 0xFFD66341.toInt()),
            AuroraGeometry.normalizeColors(intArrayOf(0xFFD66341.toInt()))
        )
    }

    @Test
    fun `multi color array is passed through untouched`() {
        val colors = intArrayOf(1, 2, 3)
        assertSame(colors, AuroraGeometry.normalizeColors(colors))
    }

    @Test
    fun `empty array stays empty so caller can skip the layer`() {
        assertEquals(0, AuroraGeometry.normalizeColors(intArrayOf()).size)
    }
}
