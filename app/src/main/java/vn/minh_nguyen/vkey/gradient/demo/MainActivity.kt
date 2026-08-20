package vn.minh_nguyen.vkey.gradient.demo

import android.os.Bundle
import android.util.TypedValue
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import vn.minh_nguyen.vkey.gradient.demo.databinding.ActivityMainBinding
import vn.minh_nguyen.vkey.gradient.ui.AuroraOrientation

/**
 * Demo xoay quanh khối `hero` đặt trên một "ảnh nền" nhiều màu:
 * - `Bật / tắt ruột` — đổi `fillColors` giữa null (XUYÊN THẤU) / mờ / đặc. Đây là thứ
 *   `<shape><stroke>` không làm được: stroke của drawable chỉ nhận 1 màu, muốn viền gradient
 *   phải chồng 2 shape và shape trong buộc phải đặc ⇒ che mất nền.
 * - `Đổi hướng viền` / `Đổi hướng nền` — chứng minh 2 lớp gradient là độc lập.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var fillMode = 0
    // Khớp với aurora_strokeOrientation / aurora_fillOrientation khai trong layout
    private var strokeIndex = ORIENTATIONS.indexOfFirst {
        it.orientation == AuroraOrientation.TOP_BOTTOM
    }
    private var fillIndex = ORIENTATIONS.indexOfFirst {
        it.orientation == AuroraOrientation.LEFT_RIGHT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyFillMode()

        binding.btnFillMode.setOnClickListener {
            fillMode = (fillMode + 1) % FILL_MODES.size
            applyFillMode()
        }

        binding.btnStroke.setOnClickListener {
            strokeIndex = (strokeIndex + 1) % ORIENTATIONS.size
            binding.hero.strokeOrientation = ORIENTATIONS[strokeIndex].orientation
            showLabels()
        }

        binding.btnFill.setOnClickListener {
            fillIndex = (fillIndex + 1) % ORIENTATIONS.size
            binding.hero.fillOrientation = ORIENTATIONS[fillIndex].orientation
            showLabels()
        }

        showStrokeWidth(binding.seekStroke.progress)
        binding.seekStroke.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.hero.strokeWidth = dp(progress.toFloat())
                showStrokeWidth(progress)
            }

            override fun onStartTrackingTouch(bar: SeekBar) = Unit
            override fun onStopTrackingTouch(bar: SeekBar) = Unit
        })

        // Đổi cả bộ màu của một item bằng 1 lời gọi — kiểu dùng thật khi bind RecyclerView
        binding.itemIdle.setOnClickListener {
            binding.itemIdle.applyAurora(
                strokeColors = resources.getIntArray(R.array.item_selected_stroke),
                fillColors = resources.getIntArray(R.array.item_selected_fill)
            )
        }
    }

    /** null ⇒ ruột trong suốt, nhìn xuyên qua thấy ảnh nền phía sau. */
    private fun applyFillMode() {
        val mode = FILL_MODES[fillMode]
        binding.hero.fillColors = mode.colorsRes?.let { resources.getIntArray(it) }
        showLabels()
    }

    private fun showLabels() {
        val mode = FILL_MODES[fillMode]
        val stroke = ORIENTATIONS[strokeIndex]
        val fill = ORIENTATIONS[fillIndex]

        binding.tvFillMode.setText(mode.labelRes)
        binding.tvStroke.text = getString(R.string.hero_stroke, stroke.label, stroke.arrow)
        binding.tvFillDir.text = if (mode.colorsRes == null) {
            getString(R.string.hero_fill_dir_none)
        } else {
            getString(R.string.hero_fill_dir, fill.label, fill.arrow)
        }
    }

    private fun showStrokeWidth(dp: Int) {
        binding.tvStrokeWidth.text = getString(R.string.label_stroke, dp)
    }

    private fun dp(value: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics
    )

    private class Choice(val label: String, val arrow: String, val orientation: Int)

    /** [colorsRes] = null nghĩa là không set nền ⇒ ruột trong suốt. */
    private class FillMode(val labelRes: Int, val colorsRes: Int?)

    companion object {
        private val ORIENTATIONS = listOf(
            Choice("left_right", "→", AuroraOrientation.LEFT_RIGHT),
            Choice("top_bottom", "↓", AuroraOrientation.TOP_BOTTOM),
            Choice("top_left_bottom_right", "↘", AuroraOrientation.TOP_LEFT_BOTTOM_RIGHT),
            Choice("top_right_bottom_left", "↙", AuroraOrientation.TOP_RIGHT_BOTTOM_LEFT)
        )

        private val FILL_MODES = listOf(
            FillMode(R.string.hero_fill_none, null),
            FillMode(R.string.hero_fill_translucent, R.array.hero_fill_soft),
            FillMode(R.string.hero_fill_solid, R.array.hero_fill_solid)
        )
    }
}
