package com.jc.antfundchart.fund

import android.graphics.*
import android.graphics.drawable.Drawable
import com.jc.antfundchart.data.DayRateDetail
import com.jc.antfundchart.utils.px
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author JereChen
 */
class RateLineDrawable : Drawable() {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 0.6f.px
        color = Color.parseColor("#CBCBCB")
    }

    //本基金线
    private val yieldLineColor = Color.parseColor("#387DF7")

    //同类平均线
    private val fundTypeYieldLineColor = Color.parseColor("#5ABFF1")

    //沪深300线
    private val indexYieldLineColor = Color.parseColor("#F2A240")

    private val yieldLineWidth = 2f.px
    private val fundTypeYieldLineWidth = 1f.px
    private val indexYieldLineWidth = 1f.px

    private val yieldLinePath = Path()
    private val fundTypeYieldLinePath = Path()
    private val indexYieldLinePath = Path()

    private val yieldShaderLinePath = Path()
    private val shaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dayRateDetailList: CopyOnWriteArrayList<DayRateDetail> = CopyOnWriteArrayList()
    private var maxRate: Double = 0.0
    private var minRate: Double = 0.0
    private var xPxSpec: Double = 0.0
    private var yPxSpec: Double = 0.0


    fun setMaxMinRate(maxRate: Double, minRate: Double) {
        this.maxRate = maxRate
        this.minRate = minRate
    }

    fun setPxSpec(xPxSpec: Double, yPxSpec: Double) {
        this.xPxSpec = xPxSpec
        this.yPxSpec = yPxSpec
    }

    fun setDayDataList(newDayRateDetailList: List<DayRateDetail>) {
        dayRateDetailList.clear()
        dayRateDetailList.addAll(newDayRateDetailList)
    }

    private var x = 0f
    private var yYield = 0f
    private var yFundTypeYield = 0f
    private var yIndexYield = 0f
    private fun calcData() {
        yieldLinePath.reset()
        fundTypeYieldLinePath.reset()
        indexYieldLinePath.reset()
        yieldShaderLinePath.reset()

        yieldShaderLinePath.moveTo(bounds.left.toFloat(), bounds.bottom.toFloat())
        dayRateDetailList.forEachIndexed { index, dayRateDetail ->
            x = bounds.left + index.times(xPxSpec).toFloat()
            yYield = bounds.top + (maxRate - dayRateDetail.yield.toDouble()).div(yPxSpec).toFloat()
            yFundTypeYield =
                bounds.top + (maxRate - dayRateDetail.fundTypeYield.toDouble()).div(yPxSpec)
                    .toFloat()
            yIndexYield =
                bounds.top + (maxRate - dayRateDetail.indexYield.toDouble()).div(yPxSpec).toFloat()
            when (index) {
                0 -> {
                    yieldLinePath.moveTo(x, yYield)
                    fundTypeYieldLinePath.moveTo(x, yFundTypeYield)
                    indexYieldLinePath.moveTo(x, yIndexYield)

                    yieldShaderLinePath.lineTo(x, yYield)
                }

                dayRateDetailList.lastIndex -> {
                    yieldLinePath.lineTo(x, yYield)
                    fundTypeYieldLinePath.lineTo(x, yFundTypeYield)
                    indexYieldLinePath.lineTo(x, yIndexYield)

                    yieldShaderLinePath.lineTo(x, yYield)
                    yieldShaderLinePath.lineTo(x, bounds.bottom.toFloat())
                    yieldShaderLinePath.close()
                }

                else -> {
                    yieldLinePath.lineTo(x, yYield)
                    fundTypeYieldLinePath.lineTo(x, yFundTypeYield)
                    indexYieldLinePath.lineTo(x, yIndexYield)

                    yieldShaderLinePath.lineTo(x, yYield)
                }
            }
        }

    }

    override fun draw(canvas: Canvas) {
        calcData()

        linePaint.color = indexYieldLineColor
        linePaint.strokeWidth = indexYieldLineWidth
        canvas.drawPath(indexYieldLinePath, linePaint)

        linePaint.color = fundTypeYieldLineColor
        linePaint.strokeWidth = fundTypeYieldLineWidth
        linePaint.shader = null
        canvas.drawPath(fundTypeYieldLinePath, linePaint)

        linePaint.color = yieldLineColor
        linePaint.strokeWidth = yieldLineWidth
        canvas.drawPath(yieldLinePath, linePaint)

        shaderPaint.shader = LinearGradient(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.left.toFloat(),
            bounds.bottom.toFloat(),
            intArrayOf(
                Color.parseColor("#33387DF7"),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(yieldShaderLinePath, shaderPaint)
    }

    override fun setAlpha(alpha: Int) {
        linePaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        linePaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return when (linePaint.alpha) {
            0xff -> {
                PixelFormat.OPAQUE
            }

            0x00 -> {
                PixelFormat.TRANSPARENT
            }

            else -> {
                PixelFormat.TRANSLUCENT
            }
        }
    }

}