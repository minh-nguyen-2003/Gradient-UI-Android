package vn.minh_nguyen.vkey.gradient.demo

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import vn.minh_nguyen.vkey.gradient.demo.databinding.ActivityMainBinding
import vn.minh_nguyen.vkey.gradient.ui.AuroraOrientation
import vn.minh_nguyen.vkey.gradient.ui.AuroraView

/**
 * Demo: layout khai bằng XML, Activity chỉ chỉnh vài thuộc tính lúc chạy để thấy API code-side
 * (`strokeWidth`, `strokeOrientation`, và `applyAurora()` khi đổi trạng thái item).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** Mọi AuroraView trong màn — gom 1 lần để 2 control bên dưới chỉnh đồng loạt. */
    private val auroraViews by lazy { binding.auroraRoot.collectAuroraViews() }

    private var orientationIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showOrientation()
        binding.btnOrientation.setOnClickListener {
            orientationIndex = (orientationIndex + 1) % ORIENTATIONS.size
            auroraViews.forEach { it.setOrientation(ORIENTATIONS[orientationIndex].second) }
            showOrientation()
        }

        showStrokeWidth(binding.seekStroke.progress)
        binding.seekStroke.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
                val px = dp(progress.toFloat())
                auroraViews.forEach { it.strokeWidth = px }
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

    private fun showOrientation() {
        binding.tvOrientation.text =
            getString(R.string.btn_orientation, ORIENTATIONS[orientationIndex].first)
    }

    private fun showStrokeWidth(dp: Int) {
        binding.tvStroke.text = getString(R.string.label_stroke, dp)
    }

    private fun dp(value: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics
    )

    private fun View.collectAuroraViews(): List<AuroraView> {
        val found = mutableListOf<AuroraView>()
        if (this is AuroraView) found += this
        if (this is ViewGroup) {
            for (i in 0 until childCount) found += getChildAt(i).collectAuroraViews()
        }
        return found
    }

    companion object {
        private val ORIENTATIONS = listOf(
            "left_right" to AuroraOrientation.LEFT_RIGHT,
            "top_bottom" to AuroraOrientation.TOP_BOTTOM,
            "top_left" to AuroraOrientation.TOP_LEFT_BOTTOM_RIGHT,
            "top_right" to AuroraOrientation.TOP_RIGHT_BOTTOM_LEFT
        )
    }
}
