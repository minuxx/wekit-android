package com.coconutplace.wekit.ui.body_graph

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.BodyGraph
import com.coconutplace.wekit.ui.BaseFragment
import com.coconutplace.wekit.ui.home.XValueFormatter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_body_graph.*

class RecentBodyGraphFragment(private val bodyGraph: BodyGraph): BaseFragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_body_graph, container, false)
    }

    override fun onStart() {
        super.onStart()

        bodyGraph.let {
            setWeightChart(bodyGraph.xData!!, bodyGraph.weightData!!)
            setBasalMetabolismChart(bodyGraph.xData!!, bodyGraph.basalMetabolismData!!)
            setBmiChart(bodyGraph.xData!!, bodyGraph.bmiData!!)
        }
    }


    private fun setWeightChart(xData: ArrayList<String>, yData: ArrayList<Entry>){
        body_graph_weight_chart.apply {
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
        }

        val xAxis = body_graph_weight_chart.xAxis

        xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            position = XAxis.XAxisPosition.BOTTOM
            labelCount = 7
            isGranularityEnabled = true
            textSize = 12f
            textColor = context?.let{ ContextCompat.getColor(it, R.color.home_chart_x) }!!
            valueFormatter = XValueFormatter(xData)
        }

        val yAxis = body_graph_weight_chart.axisLeft

        yAxis.apply {
            setDrawAxisLine(false)
            setDrawLabels(false)
            isGranularityEnabled = true
            granularity = 1.8f
            gridLineWidth = 0.5f
            gridColor = context?.let{ ContextCompat.getColor(it, R.color.body_graph_grid) }!!
            spaceMin = 10f
        }


        val setWeight = LineDataSet(yData, "")

        setWeight.apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircleHole(false)

            color = context?.let{ ContextCompat.getColor(it, R.color.body_graph_line) }!!
            lineWidth = 1.2f
            valueTextColor = context?.let{ ContextCompat.getColor(it, R.color.body_graph_value) }!!

            setCircleColor(context?.let{ ContextCompat.getColor(it, R.color.body_graph_line) }!!)
            circleRadius = 5f
            valueTextSize = 12f
            isHighlightEnabled = false
        }

        val lineData = LineData(setWeight)
        body_graph_weight_chart.data = lineData
        body_graph_weight_chart.invalidate()
    }

    private fun setBasalMetabolismChart(xData: ArrayList<String>, yData: ArrayList<Entry>){
        body_graph_basal_metabolism_chart.apply {
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
        }

        val xAxis = body_graph_basal_metabolism_chart.xAxis

        xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            position = XAxis.XAxisPosition.BOTTOM
            labelCount = 7
            isGranularityEnabled = true
            textSize = 12f
            textColor = context?.let{ ContextCompat.getColor(it, R.color.home_chart_x) }!!
            valueFormatter = XValueFormatter(xData)
        }

        val yAxis = body_graph_basal_metabolism_chart.axisLeft

        yAxis.apply {
            setDrawAxisLine(false)
            setDrawLabels(false)
            isGranularityEnabled = true
            granularity = 1.8f
            gridLineWidth = 0.5f
            gridColor = context?.let{ ContextCompat.getColor(it, R.color.body_graph_grid) }!!
            spaceMin = 10f
        }


        val setBasalMetabolism = LineDataSet(yData, "")

        setBasalMetabolism.apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircleHole(false)

            color = context?.let{ ContextCompat.getColor(it, R.color.body_graph_line) }!!
            lineWidth = 1.2f
            valueTextColor = context?.let{ ContextCompat.getColor(it, R.color.body_graph_value) }!!

            setCircleColor(context?.let{ ContextCompat.getColor(it, R.color.body_graph_line) }!!)
            circleRadius = 5f
            valueTextSize = 12f
            isHighlightEnabled = false
        }

        val lineData = LineData(setBasalMetabolism)
        body_graph_basal_metabolism_chart.data = lineData
        body_graph_basal_metabolism_chart.invalidate()
    }

    private fun setBmiChart(xData: ArrayList<String>, yData: ArrayList<Entry>) {
        body_graph_bmi_chart.apply {
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
        }

        val xAxis = body_graph_bmi_chart.xAxis

        xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            position = XAxis.XAxisPosition.BOTTOM
            labelCount = 7
            isGranularityEnabled = true
            textSize = 12f
            textColor = context?.let { ContextCompat.getColor(it, R.color.home_chart_x) }!!
            valueFormatter = XValueFormatter(xData)
        }

        val yAxis = body_graph_bmi_chart.axisLeft

        yAxis.apply {
            setDrawAxisLine(false)
            setDrawLabels(false)
            isGranularityEnabled = true
            granularity = 1.8f
            gridLineWidth = 0.5f
            gridColor = context?.let { ContextCompat.getColor(it, R.color.body_graph_grid) }!!
            spaceMin = 10f
        }


        val setBmi = LineDataSet(yData, "")

        setBmi.apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircleHole(false)

            color = context?.let { ContextCompat.getColor(it, R.color.body_graph_line) }!!
            lineWidth = 1.2f
            valueTextColor = context?.let { ContextCompat.getColor(it, R.color.body_graph_value) }!!

            setCircleColor(context?.let { ContextCompat.getColor(it, R.color.body_graph_line) }!!)
            circleRadius = 5f
            valueTextSize = 12f
            isHighlightEnabled = false
        }

        val lineData = LineData(setBmi)
        body_graph_bmi_chart.data = lineData
        body_graph_bmi_chart.invalidate()
    }
}