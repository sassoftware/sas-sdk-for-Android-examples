package com.sas.android.covid19.util

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import kotlin.sequences.Sequence

/*
 * ViewGroup extension properties
 */

val ViewGroup.children
    get(): Sequence<View> = object : Sequence<View> {
        override fun iterator(): Iterator<View> = object : Iterator<View> {
            private var position = 0

            override fun hasNext() = position < childCount

            override fun next(): View {
                val next = getChildAt(position)
                    ?: throw NoSuchElementException()
                position++
                return next
            }
        }
    }

/*
 * View extension properties
 */

fun View.setVisibleOrGone(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

/**
 * Run the given function when this View has been laid out and measured.
 */
inline fun <T : View> T.onMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(
        object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
}
