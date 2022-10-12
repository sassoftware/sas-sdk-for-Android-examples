package com.example.util

import android.app.Activity
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

/*
 * Activity extension functions
 */

inline fun <T : ViewBinding> Activity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) = lazy(LazyThreadSafetyMode.NONE) {
    bindingInflater.invoke(layoutInflater)
}
