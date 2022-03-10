package com.sas.android.covid19.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
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
 * @param observer
 *     the observer, passed both the old and new values
 */
fun <T> LiveData<T>.observe(
    owner: LifecycleOwner?,
    onlyOnChanged: Boolean = false,
    observer: (oldValue: T?, newValue: T?) -> Unit
) = observePrivate(owner, onlyOnChanged, observer)

/**
 * @suppress
 */
fun <T> LiveData<T>.observePrivate(
    owner: LifecycleOwner?,
    onlyOnChanged: Boolean = false,
    observer: (oldValue: T?, newValue: T?) -> Any
) = object : Observer<T> {
    private var oldValue: T? = null

    override fun onChanged(newValue: T?) {
        if (!onlyOnChanged || oldValue != newValue) {
            val result = observer.invoke(oldValue, newValue)
            if (result is Boolean && result) {
                removeObserver(this)
            }
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

/**
 * Observe this LiveData with an observer that provides both the old and the new
 * values, and remove that observer if it returns true.
 *
 * @param owner
 *     null to observe forever, non-null to tie observation to this LifecycleOwner
 *
 * @param onlyOnChanged
 *     false to notify whenever this LiveData's value is set, true to notify whenever it changes
 *
 * @param observer
 *     the observer, passed both the old and new values
 */
fun <T> LiveData<T>.observeUntil(
    owner: LifecycleOwner?,
    onlyOnChanged: Boolean = false,
    observer: (oldValue: T?, newValue: T?) -> Boolean
) = observePrivate(owner, onlyOnChanged, observer)
