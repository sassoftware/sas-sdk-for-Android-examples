package com.sas.android.covid19.util

import android.widget.TextView

fun TextView.setTextOrGone(text: CharSequence?) {
    this.text = text
    setVisibleOrGone(!text.isNullOrEmpty())
}
