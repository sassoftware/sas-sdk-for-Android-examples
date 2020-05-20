package com.sas.android.covid19.ui.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder

interface AdapterDelegate<T> {
    /**
     * Return a stable ID for the item at the given position, or
     * RecyclerView.NO_ID if such items don't have stable IDs.
     *
     * @param position
     *     The position in the adapter, or null if unknown.
     *
     * @param item
     *     The item.
     *
     * @param adapter
     *     The DelegatingAdapter delegating to this AdapterDelegate.
     */
    fun getItemId(position: Int?, item: T, adapter: DelegatingAdapter<T>): Long

    /**
     * Indicates whether this AdapterDelegate can display the given item at the
     * given position.  This default implementation returns true.
     * AdapterDelegates who are not the sole delegate for their DelgatingAdapter
     * should override this method.
     *
     * @param position
     *     The position in the adapter, or null if unknown.
     *
     * @param item
     *     The item.
     */
    fun isDelegateFor(position: Int?, item: T) = true

    /**
     * Determines whether the given item is selectable in selection mode.
     * AdapterDelegates that return true should implement [.getItemId] to return
     * a stable ID.
     *
     * @param adapter
     * the DelegatingAdapter delegating to this AdapterDelegate
     */
    fun isSelectable(
        position: Int,
        item: T,
        adapter: DelegatingAdapter<T>
    ): Boolean

    /**
     * Populates the Views saved in the ViewHolder
     * [created][.onCreateViewHolder] by this AdapterDelegate.
     *
     * @param adapter
     *     The DelegatingAdapter delegating to this AdapterDelegate.
     */
    fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int,
        item: T,
        adapter: DelegatingAdapter<T>
    )

    /**
     * Create a new View and ViewHolder.
     *
     * @param adapter
     *     The DelegatingAdapter delegating to this AdapterDelegate.
     */
    fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        adapter: DelegatingAdapter<T>
    ): ViewHolder
}
