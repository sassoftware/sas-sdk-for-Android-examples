package com.sas.covid19.kotlin.properties

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A read/write property with a callback to intercept and amend its value
 * when is being set, and another to notify when it has been successfully
 * changed.
 */
abstract class OmniProperty<T> : ReadWriteProperty<Any?, T> {
    /*
     * Properties/init
     */

    private var value: Any?
    private var mayOnlyBeSetOnce: Boolean

    /*
     * Constructors
     */

    /**
     * Construct an OmniProperty with the given initial value.
     *
     * @param initialValue
     *     the initial value of the property.
     */
    constructor(initialValue: T) {
        value = intercept(initialValue, true)
        mayOnlyBeSetOnce = false
    }

    /**
     * Construct an uninitialized OmniProperty.
     */
    constructor() {
        value = UNINITIALIZED
        mayOnlyBeSetOnce = true
    }

    /*
     * ReadWriteProperty methods
     */

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        check(value != UNINITIALIZED) {
            "${property.name} has not been initialized!"
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        check(!mayOnlyBeSetOnce || this.value == UNINITIALIZED) {
            "${property.name} has already been initialized!"
        }

        @Suppress("UNCHECKED_CAST")
        val oldValue = this.value as T
        val newValue = intercept(value, this.value == UNINITIALIZED)

        // Referencial check
        if (oldValue !== newValue) {
            this.value = newValue

            // Value check
            if (oldValue != newValue) {
                onValueChanged(property, oldValue, newValue)
            }
        }
    }

    /*
     * OmniProperty methods
     */

    /**
     *  Called before a change to the property value is made.
     */
    protected open fun intercept(newValue: T, isInitial: Boolean): T = newValue

    /**
     * Called after a change to the value of the property is made.
     */
    protected open fun onValueChanged(
        property: KProperty<*>,
        oldValue: T,
        newValue: T
    ) {}

    /*
     * Objects
     */

    private object UNINITIALIZED
}
