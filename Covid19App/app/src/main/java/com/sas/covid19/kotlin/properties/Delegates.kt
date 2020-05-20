package com.sas.covid19.kotlin.properties

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object Delegates {
    /**
     * Returns a property delegate for an interceptable, observable,
     * read/write property.
     *
     * @param initialValue
     *     the initial value of the property.
     *
     * @param onIntercept
     *     called to intercept (and possibly change) the value before it is set.
     *
     * @param onChanged
     *     called after the change of the property is made.
     */
    inline fun <T> all(
        initialValue: T,

        crossinline onIntercept: (
            newValue: T,
            isInitial: Boolean
        ) -> T = { newValue, _ -> newValue },

        crossinline onChanged: (
            property: KProperty<*>,
            oldValue: T,
            newValue: T
        ) -> Unit = { _, _, _ -> }
    ): ReadWriteProperty<Any?, T> =
        object : OmniProperty<T>(initialValue) {
            override fun intercept(newValue: T, isInitial: Boolean) =
                onIntercept(newValue, isInitial)

            override fun onValueChanged(
                property: KProperty<*>,
                oldValue: T,
                newValue: T
            ) {
                onChanged(property, oldValue, newValue)
            }
        }

    /**
     * Returns a property delegate for a bounded, observable, read/write
     * property.
     *
     * @param initialValue
     *     the initial value of the property.
     *
     * @param min
     *     function to return the minimum bound, or null if there isn't one.
     *
     * @param max
     *     function to return the maximum bound, or null if there isn't one.
     *
     * @param onChanged
     *     called after the change of the property is made.
     */
    inline fun <T : Comparable<T>> bounded(
        initialValue: T,
        crossinline min: () -> T?,
        crossinline max: () -> T?,
        crossinline onChanged: (
            property: KProperty<*>,
            oldValue: T,
            newValue: T
        ) -> Unit = { _, _, _ -> }
    ): ReadWriteProperty<Any?, T> =
        all(initialValue,
            { newValue, _ ->
                val minValue = min()
                val maxValue = max()

                require(minValue == null || maxValue == null ||
                        minValue <= maxValue) {
                    "minimum ($minValue) must be less than maximum ($maxValue)!"
                }

                var intercepted = newValue

                if (minValue != null && intercepted < minValue) {
                    intercepted = minValue
                }

                if (maxValue != null && intercepted > maxValue) {
                    intercepted = maxValue
                }

//              if (intercepted != newValue) {
//                  Memo.v("intercepted %s: %s -> %s", property.name,
//                      newValue, intercepted);
//              }

                intercepted
            }, onChanged)

    /**
     * Returns a property delegate for an observable read/write property.
     * This differs from Delegates.observable() in that onChanged is called
     * only if the value actually changes, rather than every time it is
     * assigned.
     *
     * @param initialValue
     *     the initial value of the property.
     *
     * @param onChanged
     *     called after the change of the property is made.
     */
    inline fun <T> observable(
        initialValue: T,
        crossinline onChanged: (
            property: KProperty<*>,
            oldValue: T,
            newValue: T
        ) -> Unit = { _, _, _ -> }
    ): ReadWriteProperty<Any?, T> = all(initialValue, onChanged = onChanged)
}
