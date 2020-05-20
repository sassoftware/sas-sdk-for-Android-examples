package com.sas.android.covid19.util

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet

/*
 * Context extension functions
 */

fun <T> Context.getStyledAttributes(
    attrSet: AttributeSet?,
    attr: Int,
    block: TypedArray.() -> T
) = getStyledAttributes(attrSet, intArrayOf(attr), block)

/**
 * Gets the given styled attributes, runs the given block in the context of the
 * resulting TypedArray, then recycles that TypedArray.  Note that attrs must be
 * sorted, due to a bug/efficiency in the obtainStyledAttributes() API.
 */
@Suppress("Recycle")
fun <T> Context.getStyledAttributes(
    attrSet: AttributeSet?,
    attrs: IntArray,
    block: TypedArray.() -> T
) = obtainStyledAttributes(attrSet, attrs, 0, 0).use(block)

/*
 * TypedArray extension functions
 */

fun <T> TypedArray.use(block: TypedArray.() -> T) =
    try {
        block()
    } finally {
        recycle()
    }
