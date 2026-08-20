package vn.minh_nguyen.vkey.gradient.ui

/**
 * Hướng chạy của gradient trong [AuroraView] — thuần Kotlin (không import `android.*`) để unit
 * test được trên JVM.
 *
 * Giá trị 0..3 PHẢI khớp enum `aurora_orientation` trong `attrs.xml`; đổi số ở đây là hỏng mọi
 * layout đang khai bằng XML.
 */
object AuroraOrientation {

    /** Trái → phải (mặc định). */
    const val LEFT_RIGHT = 0

    /** Trên → dưới. */
    const val TOP_BOTTOM = 1

    /** Chéo góc trên-trái → dưới-phải. */
    const val TOP_LEFT_BOTTOM_RIGHT = 2

    /** Chéo góc trên-phải → dưới-trái. */
    const val TOP_RIGHT_BOTTOM_LEFT = 3
}
