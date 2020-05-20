package com.sas.android.covid19.ui.recycler

import android.content.Context

/**
 * A RefinableAdapter backed by a List.
 *
 * @param context
 *     A Context for convenience.
 *
 * @param hasStableIds
 *     Whether this adapter publishes a unique long value that can act as an
 *     identifier for the item at a given position in the data set.
 *
 * @param model
 *     The model.
 *
 * @param delegates
 *     One [AdapterDelegate] for each supported view type.
 */
open class ListAdapter<T>(
    context: Context,
    hasStableIds: Boolean,
    model: List<T>?,
    delegates: List<AdapterDelegate<T>>?
) : RefinableAdapter<T, List<T>?>(context, hasStableIds, model, delegates) {
    /*
     * RefinableAdapter properties
     */

    override val modelAsSequence
        get() = model?.asSequence()

    /*
     * RefinableAdapter methods
     */

    override fun getModelItem(position: Int, model: List<T>?) =
        model!![position]

    override fun getModelItemCount(model: List<T>?) = model?.size ?: 0
}
