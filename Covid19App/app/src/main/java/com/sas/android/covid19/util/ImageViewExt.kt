package com.sas.android.covid19.util

import android.graphics.Bitmap
import android.widget.ImageView

fun ImageView.setImageBitmapOrGone(bitmap: Bitmap?) =
    setImageOrGone(bitmap) {
        setImageBitmap(it)
    }

private fun <T> ImageView.setImageOrGone(src: T?, setter: (src: T) -> Unit) {
    val visible = src != null
    if (visible) {
        setter(src!!)
    } else {
        setImageDrawable(null)
    }
    setVisibleOrGone(visible)
}
