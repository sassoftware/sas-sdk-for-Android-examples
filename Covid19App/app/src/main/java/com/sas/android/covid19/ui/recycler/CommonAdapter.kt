package com.sas.android.covid19.ui.recycler

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * A simple RecyclerView.Adapter that supports common Adapter functionality.
 *
 * @param context
 *     A Context for convenience.
 *
 * @param hasStableIds
 *     Whether this adapter publishes a unique long value that can act as an
 *     identifier for the item at a given position in the data set.  Including
 *     this as a constructor argument a) forces subclasses to consciously choose
 *     whether they support stable IDs, rather than relying on the default out
 *     of ignorance, and b) gives subclasses like SelectableAdapter the
 *     opportunity to register an AdapterDataObserver in their constructors
 *     (which precludes subsequent calls to setHasStableIds(true))
 */
abstract class CommonAdapter<T>(
    val context: Context,
    hasStableIds: Boolean
) : Adapter<ViewHolder>() {
    /*
     * Properties/init
     */

    val inflater by lazy {
        LayoutInflater.from(context)
    }

    init {
        setHasStableIds(hasStableIds)
    }

    /*
     * RecyclerView.Adapter methods
     */

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is OnViewRecycledListener) {
            holder.onViewRecycled()
        }
    }

    /*
     * CommonAdapter methods
     */

    /**
     * This should not have been omitted from the RecyclerView.Adapter class.
     */
    abstract fun getItem(position: Int): T

    /*
     * Classes
     */

    interface OnViewRecycledListener {
        fun onViewRecycled()
    }
}
