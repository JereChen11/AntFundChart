package com.jc.antfundchart.utils

import android.content.res.Resources
import android.util.TypedValue
import java.text.DecimalFormat

/**
 * @author JereChen
 */
val Float.px: Float
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        )
    }

val Float.sp2px: Float
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            Resources.getSystem().displayMetrics
        )
    }

fun Number.toStringAsFixed(digits: Int, tailZero: Boolean = true): String {
    if (digits < 0) return this.toString()
    if (digits == 0) return this.toInt().toString()
    val stringBuffer = StringBuffer("0.")
    if (tailZero) {
        for (i in 0 until digits) {
            stringBuffer.append("0")
        }
    } else {
        for (i in 0 until digits) {
            stringBuffer.append("#")
        }
    }
    return DecimalFormat(stringBuffer.toString()).format(this)
}