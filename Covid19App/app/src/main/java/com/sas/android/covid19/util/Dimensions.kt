package com.sas.android.covid19.util

import kotlin.math.roundToInt

import android.content.res.Resources

object Dimensions {
    fun dpToPx(dp: Float) = dp * Resources.getSystem().displayMetrics.density
    fun dpToPx(dp: Int) = dpToPx(dp.toFloat()).roundToInt()
}
