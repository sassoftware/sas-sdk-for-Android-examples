package com.sas.android.covid19.util

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.reflect.KClass

private fun Any.log(
    priority: Int,
    message: String?,
    t: Throwable?,
    tag: KClass<*>
) {
    val tagStr = tag.simpleName.nullIfBlank
        ?: tag.toString().nullIfBlank?.replace("^([^$]*\\.)?(\\w+).*".toRegex(), "$2") ?: "LOG"

    if (message != null) {
        Log.println(priority, tagStr, message)
    }

    if (t != null) {
        // Includes t.getMessage(), t.getCause(), etc.
        val sout = StringWriter()
        val pout = PrintWriter(sout)
        t.printStackTrace(pout)
        pout.flush()
        sout.toString().trim().nullIfBlank?.also {
            Log.println(priority, tagStr, it)
        }
    }
}

fun Any.logE(message: String? = null, t: Throwable? = null, tag: KClass<*> = this::class) {
    log(Log.ERROR, message, t, tag)
}

fun Any.logV(message: String? = null, t: Throwable? = null, tag: KClass<*> = this::class) {
    log(Log.VERBOSE, message, t, tag)
}
