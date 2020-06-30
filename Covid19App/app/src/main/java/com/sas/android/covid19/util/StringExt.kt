package com.sas.android.covid19.util

val String?.nullIfBlank
    get() = if (isNullOrBlank()) null else this
