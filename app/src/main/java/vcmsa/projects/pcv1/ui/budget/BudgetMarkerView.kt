// Takudzwa Murwira – ST10392257, Jason Daniel Isaacs – ST10039248, Daniel Gorin – ST10438307 and Moegammad-Yaseen Salie – ST10257795
//PROG7313

//References:
//            https://medium.com/@SeanAT19/how-to-use-mpandroidchart-in-android-studio-c01a8150720f
//            https://chatgpt.com/
//            https://www.youtube.com/playlist?list=PLWz5rJ2EKKc8SmtMNw34wvYkqj45rV1d3
//            https://www.youtube.com/playlist?list=PLSrm9z4zp4mEPOfZNV9O-crOhoMa0G2-o
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

    // TextView to display content inside marker
    private val tvContent: TextView = findViewById(R.id.tvContent)

    // Update marker content based on highlighted Entry
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val label = if (e.x.toInt() - 1 in xLabels.indices) xLabels[e.x.toInt() - 1] else ""
            tvContent.text = "Date: $label\nRemaining: R${e.y.toInt()}"
        }
        super.refreshContent(e, highlight)
    }

    // Position the marker centered above the selected value
    override fun getOffset(): MPPointF = MPPointF(-(width / 2f), -height.toFloat())
}
