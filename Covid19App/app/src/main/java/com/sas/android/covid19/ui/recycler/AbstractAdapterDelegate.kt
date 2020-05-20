package com.sas.android.covid19.ui.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

abstract class AbstractAdapterDelegate<T>(
    @JvmField protected val context: Context,
    @JvmField @LayoutRes protected val layoutResource: Int
) : AdapterDelegate<T> {
    /*
     * AdapterDelegate methods
     */

    /**
     * Default implementation that returns RecyclerView.NO_ID.  If the Adapter's
     * hasStableIds() method returns true, this method must be overridden!
     */
    override fun getItemId(
        position: Int?,
        item: T,
        adapter: DelegatingAdapter<T>
    ): Long = RecyclerView.NO_ID

    /**
     * Default implementation that returns false.
     */
    override fun isSelectable(
        position: Int,
        item: T,
        adapter: DelegatingAdapter<T>
    ) = false

    /**
     * Default implementation that [creates a View][.onCreateView], then
     * [creates a ViewHolder][.onCreateViewHolder] from it.
     */
    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: DelegatingAdapter<T>
    ) = onCreateViewHolder(onCreateView(inflater, parent, adapter), adapter)

    /*
     * AbstractAdapterDelegate methods
     */

    /**
     * Create a new View.  This default implementation inflates the resource
     * passed to the constructor (without adding it to the parent).
     *
     * @param adapter
     * the DelegatingAdapter delegating to this AdapterDelegate
     */
    open fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: DelegatingAdapter<T>
    ) = inflater.inflate(layoutResource, parent, false)

    /**
     * Create a ViewHolder for the given View.
     *
     * @param adapter
     * the DelegatingAdapter delegating to this AdapterDelegate
     */
    abstract fun onCreateViewHolder(
        view: View,
        adapter: DelegatingAdapter<T>
    ): ViewHolder
}
