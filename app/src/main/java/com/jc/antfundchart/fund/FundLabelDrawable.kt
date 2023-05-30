package com.jc.antfundchart.fund

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import com.jc.antfundchart.utils.px
import com.jc.antfundchart.utils.sp2px

/**
 * @author JereChen
 */
class FundLabelDrawable : Drawable() {

    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 16f.sp2px
        color = Color.parseColor("#D3D3D3")
    }
    private val paddingBottom = 10f.px

    private fun drawLabelTag(canvas: Canvas) {
        canvas.drawText(
            "JC基金复刻",
            bounds.left.toFloat(),
            bounds.bottom - paddingBottom,
            labelTextPaint
        )
    }

    override fun draw(canvas: Canvas) {
        drawLabelTag(canvas)
    }

    override fun setAlpha(alpha: Int) {
        labelTextPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        labelTextPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return when (labelTextPaint.alpha) {
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