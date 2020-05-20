package com.sas.android.covid19.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/*
 * LiveData extension functions
 */

/**
 * Observe this LiveData with an observer that provides both the old and the new
 * values.
 */
fun <T> LiveData<T>.observe(
    owner: LifecycleOwner?,
    onChanged: (oldValue: T?, newValue: T?) -> Unit
) = object : Observer<T> {
        private var oldValue: T? = null
        override fun onChanged(newValue: T?) {
            onChanged(oldValue, newValue)
            oldValue = newValue
        }
    }.also {
        if (owner == null) {
            observeForever(it)
        } else {
            observe(owner, it)
        }
    }

/*
 * MutableLiveData extension functions
 */

/**
 * Force a notification.  Useful when T is a mutable type (e.g. MutableList).
 */
fun <T> MutableLiveData<T>.notifyObservers() {
    this.value = this.value
}
