package vcmsa.projects.pcv1.ui.budget

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import vcmsa.projects.pcv1.R
import com.github.mikephil.charting.data.Entry

class BudgetMarkerView(
    context: Context,
    layoutResource: Int,
    private val xLabels: List<String>
) : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val label = if (e.x.toInt() - 1 in xLabels.indices) xLabels[e.x.toInt() - 1] else ""
            tvContent.text = "Date: $label\nRemaining: R${e.y.toInt()}"
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF = MPPointF(-(width / 2f), -height.toFloat())
}
