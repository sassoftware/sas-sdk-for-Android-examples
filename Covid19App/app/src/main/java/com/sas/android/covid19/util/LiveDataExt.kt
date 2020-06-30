package com.sas.android.covid19.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * Observe this LiveData with an observer that provides both the old and the new
 * values.
 *
 * @param owner
 *     null to observe forever, non-null to tie observation to this LifecycleOwner
 *
 * @param onlyOnChanged
 *     false to notify whenever this LiveData's value is set, true to notify whenever it changes
 *
 * @param onSet
 *     the observer, passed both the old and new values
 */
fun <T> LiveData<T>.observe(
    owner: LifecycleOwner?,
    onlyOnChanged: Boolean = false,
    onSet: (oldValue: T?, newValue: T?) -> Unit
) = object : Observer<T> {
        private var oldValue: T? = null

        override fun onChanged(newValue: T?) {
            if (!onlyOnChanged || oldValue != newValue) {
                onSet.invoke(oldValue, newValue)
            }
            oldValue = newValue
        }
    }.also {
        if (owner == null) {
            observeForever(it)
        } else {
            observe(owner, it)
        }
    }
