package com.sas.android.covid19.util

import android.app.Activity
import android.view.View

val Activity.contentView
    get() = findViewById<View>(android.R.id.content)
