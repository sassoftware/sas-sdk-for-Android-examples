package com.sas.android.covid19.util

import android.content.res.Resources
import kotlin.math.roundToInt

object Dimensions {
    fun dpToPx(dp: Float) = dp * Resources.getSystem().displayMetrics.density
    fun dpToPx(dp: Int) = dpToPx(dp.toFloat()).roundToInt()
}
