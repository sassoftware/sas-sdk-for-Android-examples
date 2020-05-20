package com.sas.android.covid19.util

import android.content.Context

import com.sas.android.covid19.R

/*
 * String extension properties
 */

val String.isWorldwide
    get() = this == REGION_WORLDWIDE

/*
 * Properties/init
 */

val REGION_WORLDWIDE = ""

/*
 * String extension functions
 */

fun String.toLocalizedRegion(context: Context) = if (isWorldwide)
    context.getString(R.string.activity_main_title_worldwide, this) else this
