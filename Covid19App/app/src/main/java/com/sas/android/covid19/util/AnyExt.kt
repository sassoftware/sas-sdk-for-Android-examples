package com.sas.android.covid19.util

import android.util.Log

fun Any.logE(message: String? = null, throwable: Throwable? = null) {
    Log.e(this::class.simpleName, message, throwable)
}

fun Any.logV(message: String) {
    Log.v(this::class.simpleName, message)
}
