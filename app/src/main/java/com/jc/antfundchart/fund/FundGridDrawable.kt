package com.jc.antfundchart.fund

import android.graphics.*
import android.graphics.drawable.Drawable
import com.jc.antfundchart.data.DayRateDetail
import com.jc.antfundchart.utils.px
import com.jc.antfundchart.utils.sp2px
import com.jc.antfundchart.utils.toStringAsFixed
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author JereChen
 */
class FundGridDrawable : Drawable() {

    internal var lineChartRect = Rect()

    private val dashPathEffect by lazy {
        DashPathEffect(floatArrayOf(3f.px, 3f.px, 3f.px, 3f.px), 1f)
    }

    private val rateLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        pathEffect = dashPathEffect
        strokeWidth = 0.6f.px
        color = Color.parseColor("#CBCBCB")
    }

    internal val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10f.sp2px
        color = Color.parseColor("#CBCBCB")
    }

    private val bottomLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 0.8f.px
        color = Color.parseColor("#CBCBCB")
    }

    private var maxRate: Double = 0.0
    private var minRate: Double = 0.0
    private var xPxSpec: Double = 0.0
    private var yPxSpec: Double = 0.0
    private val rateAbscissaLines = CopyOnWriteArrayList<Int>()

    private var minDateTime: String = ""
    private var middleDateTime: String = ""
    private var maxDateTime: String = ""

    private val paddingBottom = 10f.px

    internal fun setMaxMinRate(maxRate: Double, minRate: Double) {
        this.maxRate = maxRate
        this.minRate = minRate
    }

    internal fun setPxSpec(xPxSpec: Double, yPxSpec: Double) {
        this.xPxSpec = xPxSpec
        this.yPxSpec = yPxSpec
    }

    internal fun setRateAbscissaLines(newRateAbscissaLines: List<Int>) {
        this.rateAbscissaLines.clear()
        this.rateAbscissaLines.addAll(newRateAbscissaLines)
    }

    internal fun setDayDataList(dayRateDetailList: List<DayRateDetail>) {
        minDateTime = dayRateDetailList.first().date
        middleDateTime = dayRateDetailList[dayRateDetailList.size / 2].date
        maxDateTime = dayRateDetailList.last().date
    }

    private var yPx = 0f
    private fun drawRateTextAndLines(canvas: Canvas) {
        rateAbscissaLines.forEach {

            yPx = lineChartRect.top + (maxRate - it).div(yPxSpec).toFloat()

            canvas.drawLine(
                lineChartRect.left.toFloat(),
                yPx,
                lineChartRect.right.toFloat(),
                yPx,
                rateLinePaint
            )

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                "${it.toStringAsFixed(2)}%",
                lineChartRect.left.toFloat() - 10f.px,
                yPx + 5f.px,
                textPaint
            )
        }
    }

    private val textBoundsRect = Rect()
    private var dateTimeTextPxY = 0f
    private fun drawDateTimeText(canvas: Canvas) {
        textPaint.getTextBounds(minDateTime, 0, minDateTime.length, textBoundsRect)

        dateTimeTextPxY = lineChartRect.bottom + textBoundsRect.height() + paddingBottom

        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(
            minDateTime,
            lineChartRect.left.toFloat(),
            dateTimeTextPxY,
            textPaint
        )
        canvas.drawLine(
            lineChartRect.left.toFloat(),
            lineChartRect.bottom.toFloat(),
            lineChartRect.left.toFloat(),
            lineChartRect.bottom.toFloat() + paddingBottom / 2,
            bottomLinePaint
        )

        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            middleDateTime,
            lineChartRect.left.toFloat() + lineChartRect.width() / 2f,
            dateTimeTextPxY,
            textPaint
        )
        canvas.drawLine(
            lineChartRect.left.toFloat() + lineChartRect.width() / 2f,
            lineChartRect.bottom.toFloat(),
            lineChartRect.left.toFloat() + lineChartRect.width() / 2f,
            lineChartRect.bottom.toFloat() + paddingBottom / 2,
            bottomLinePaint
        )

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(
            maxDateTime,
            lineChartRect.right.toFloat(),
            dateTimeTextPxY,
            textPaint
        )
        canvas.drawLine(
            lineChartRect.right.toFloat(),
            lineChartRect.bottom.toFloat(),
            lineChartRect.right.toFloat(),
            lineChartRect.bottom.toFloat() + paddingBottom / 2,
            bottomLinePaint
        )

        canvas.drawLine(
            lineChartRect.left.toFloat(),
            lineChartRect.bottom.toFloat(),
            lineChartRect.right.toFloat(),
            lineChartRect.bottom.toFloat(),
            bottomLinePaint
        )
    }

    override fun draw(canvas: Canvas) {
        drawRateTextAndLines(canvas)

        drawDateTimeText(canvas)
    }

    override fun setAlpha(alpha: Int) {
        rateLinePaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        rateLinePaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return when (rateLinePaint.alpha) {
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