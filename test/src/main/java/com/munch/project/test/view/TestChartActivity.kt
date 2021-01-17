package com.munch.project.test.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.renderer.YAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.munch.lib.helper.getColorCompat
import com.munch.lib.helper.px2Dp
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.test.R
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/1/4 9:34.
 */
class TestChartActivity : TestBaseTopActivity() {

    private val lineChart: LineChart by lazy { findViewById(R.id.test_chart_line) }
    private val barChart: BarChart by lazy { findViewById(R.id.test_chart_bar) }
    private val pieChart: PieChart by lazy { findViewById(R.id.test_chart_pie) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity_test_chart)

        val lineData = LineDataSet(generateLineData(), "label")
        setLineChartStyle(lineData, lineChart)
        lineChart.run {
            data = LineData(lineData)
        }

        val barData = BarDataSet(generateBarData(), "label")
        barChart.data = BarData(barData)

        val data = generatePieData()
        val pieData = PieDataSet(data, "label")
        pieData.setColors(*IntArray(data.size) {
            Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
        })
        pieChart.isDrawHoleEnabled = false
        pieChart.data = PieData(pieData)
    }

    private fun generatePieData(): MutableList<PieEntry> {
        val list = arrayListOf<PieEntry>()

        for (i in 0..11) {
            // x , y 轴
            list.add(PieEntry(i.toFloat(), Random.nextInt(24).toFloat()))
        }

        return list
    }

    private fun generateBarData(): MutableList<BarEntry> {
        val list = arrayListOf<BarEntry>()

        for (i in 0..11) {
            // x , y 轴
            list.add(BarEntry(i.toFloat(), Random.nextInt(300).toFloat()))
        }

        return list
    }

    private fun setLineChartStyle(data: LineDataSet, lineChart: LineChart) {
        data.run {
            color = getColorCompat(R.color.colorPrimary) //线条颜色
            setCircleColor(getColorCompat(R.color.colorPrimaryDark)) //圆点颜色
            lineWidth = 1f //线条宽度
            highLightColor = Color.TRANSPARENT // 不显示辅助线
            mode = LineDataSet.Mode.CUBIC_BEZIER //圆滑线
            setDrawFilled(true) //填充区域
        }

        //lineChart.isHighlightPerTapEnabled = false //点击时高亮显示
        lineChart.run {
            //axisLeft.isEnabled = false//禁用左侧y轴
            axisRight.isEnabled = false//禁用右侧y轴
            //设置y轴单位，因为要自定义换行，所以这里不使用以避免空出宽度
            /*axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}m"
                }
            }*/
            //同时设置最大值最小值则页面y轴间隔固定
            axisLeft.axisMinimum = 0f
            axisLeft.mAxisMaximum = 300f
            axisLeft.setDrawZeroLine(true)
            //将y轴数值与单位分成两行
            rendererLeftYAxis = (object : YAxisRenderer(
                viewPortHandler,
                getAxis(YAxis.AxisDependency.LEFT),
                getTransformer(YAxis.AxisDependency.LEFT)
            ) {
                override fun drawYLabels(
                    c: Canvas?,
                    fixedPosition: Float,
                    positions: FloatArray?,
                    offset: Float
                ) {
                    /*super.drawYLabels(c, fixedPosition, positions, offset)*/
                    val from = if (mYAxis.isDrawBottomYLabelEntryEnabled) 0 else 1
                    val to =
                        if (mYAxis.isDrawTopYLabelEntryEnabled) mYAxis.mEntryCount else mYAxis.mEntryCount - 1
                    // draw
                    for (i in from until to) {
                        val text = mYAxis.getFormattedLabel(i)
                        if (i == 0) {
                            c!!.drawText(
                                "${text}m",
                                fixedPosition,
                                positions!![i * 2 + 1] + offset,
                                mAxisLabelPaint
                            )
                        } else {
                            val textSize = paintAxisLabels.textSize
                            val width = mAxisLabelPaint.measureText(text)
                            c!!.drawText(
                                text,
                                fixedPosition,
                                positions!![i * 2 + 1] + offset - textSize / 2,
                                mAxisLabelPaint
                            )
                            //让单位显示到第二行并居中
                            c.drawText(
                                "m",
                                fixedPosition - (width - textSize) / 2,
                                positions[i * 2 + 1] + offset + textSize / 2,
                                mAxisLabelPaint
                            )
                        }
                    }
                }
            })
            //设置x轴
            xAxis.run {
                textSize = px2Dp(22f)
                setDrawAxisLine(true) //绘制轴线
                axisMinimum = 0f
                setDrawAxisLine(true)//是否绘制轴线
                setDrawGridLines(false)//设置x轴上每个点对应的线
                setDrawLabels(true)//绘制标签  指x轴上的对应数值
                position = XAxis.XAxisPosition.BOTTOM//设置x轴的显示位置
                granularity = 1f//禁止放大后x轴标签重绘

                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt() + 1}月"
                    }
                }
            }
            // 透明化图例
            legend.run {
                form = Legend.LegendForm.NONE
                textColor = Color.TRANSPARENT
            }
            //隐藏x轴描述
            description = Description().apply { isEnabled = false }

            marker = DetailsMarkerView(this@TestChartActivity).apply {
                chartView = this@run //边缘自动调整
            }
        }
    }

    private fun generateLineData(): ArrayList<Entry> {
        val list = arrayListOf<Entry>()

        for (i in 0..11) {
            // x , y 轴
            list.add(Entry(i.toFloat(), Random.nextInt(300).toFloat()))
        }

        return list
    }

    class DetailsMarkerView(context: Context) : MarkerView(context, R.layout.test_layout_chart_marker) {

        private val tv1: TextView by lazy { findViewById(R.id.chart_marker_tv1) }
        private val tv2: TextView by lazy { findViewById(R.id.chart_marker_tv2) }

        @SuppressLint("SetTextI18n")
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            super.refreshContent(e, highlight)
            e ?: return
            if (e.y == 0f) {
                tv2.text = "暂无数据"
            } else {
                tv2.text = "${e.y.toInt()}米"
            }
            tv1.text = "${e.x.toInt() + 1}月"
        }

        //布局偏移，相较于折线点
        override fun getOffset(): MPPointF {
            return MPPointF(-width / 2f, -height.toFloat() - 10f)
        }
    }
}