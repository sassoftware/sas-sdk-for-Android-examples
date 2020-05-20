package com.sas.android.covid19.ui.recycler

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * DelegatingAdapter is a RecyclerView.Adapter that provides support for View
 * creation and population through delegates. It also makes it easy to support
 * multiple [view types][.getItemViewType] in an adapter view.
 *
 * Clients or subclasses of this class provide an [AdapterDelegate] for each
 * supported view type (usually just one). The AdapterDelegate handles creation
 * of the View, creation of a ViewHolder object (to save references to sub-views
 * for fast retrieval later), and the population of that View using the
 * ViewHolder object and the relevant data modeled by the adapter.
 *
 * A typical use case is to create a DelegatingAdapter with a single
 * [TextViewDelegate]. That AdapterDelegate finds a TextView within the
 * hierarchy of the created view and populates it by converting the given data
 * item to a CharSequence. Subclasses can control exactly how the TextView is
 * found and how the data is converted.
 *
 * To support multiple view types, multiple AdapterDelegates can be specified.
 * The [.getDelegate] method should then be overridden to indicate which
 * delegate to use for a particular position in the model.
 *
 * @param context
 *     A Context for convenience.
 *
 * @param hasStableIds
 *     Whether this adapter publishes a unique long value that can act as an
 *     identifier for the item at a given position in the data set.
 *
 * @param delegates
 *     One [AdapterDelegate] for each supported view type.  If no delegates are
 *     specified here, [delegates] must be assigned before this adapter can be
 *     used.
 */
abstract class DelegatingAdapter<T>(
    context: Context,
    hasStableIds: Boolean,
    delegates: List<AdapterDelegate<T>>?
) : SelectableAdapter<T>(context, hasStableIds) {
    /*
     * Properties/init
     */

    private val viewTypes = mutableMapOf<AdapterDelegate<T>, Int>()

    /**
     * The AdapterDelegates used by this DelegatingAdapter, or null if no
     * AdapterDelegates have been set *yet*.
     *
     * Each AdapterDelegate corresponds to a view type supported by this
     * DelegatingAdapter.
     *
     * If more than one delegate is specified, subclasses should provide a
     * custom implementation of the [.getDelegate] method.
     *
     * This property should be set only once, when this object is initialized.
     *
     * @throws IllegalStateException
     *     If delegates have already been set.
     *
     * @throws IllegalArgumentException
     *     If the given delegates are null or empty.
     */
    open var delegates: List<AdapterDelegate<T>>? = null
        set(value) {
            check(field == null) {
                "delegates have already been set"
            }

            require(!value.isNullOrEmpty()) {
                "delegates must not be null/empty"
            }

            val newValue = value.toList()

            // Initialize view types
            var i = 0
            for (delegate in newValue) {
                viewTypes[delegate] = i++
            }

            field = newValue
        }

    init {
        if (!delegates.isNullOrEmpty()) {
            this.delegates = delegates
        }
    }

    /*
     * RecyclerView.Adapter methods
     */

    /**
     * Gets the ID of the item at the given position.  Default implementation
     * that calls the other [.getItemId], which delegates to the appropriate
     * AdapterDelgate.
     */
    override fun getItemId(position: Int) =
        getItem(position).let {
            getItemId(position, it, getDelegate(position, it))
        }

    /**
     * Calls [.getDelegate] to get the AdapterDelegate for the given position,
     * then returns the item view type reported by [.getItemViewType].
     */
    override fun getItemViewType(position: Int) =
        getItemViewType(getDelegate(position, getItem(position)))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).let {
            getDelegate(position, it).onBindViewHolder(holder, position, it,
                this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        delegates!![viewType].onCreateViewHolder(inflater, parent, this)

    /*
     * SelectableAdapter methods
     */

    override fun isSelectable(position: Int) =
        getItem(position).let {
            getDelegate(position, it).isSelectable(position, it, this)
        }

    /*
     * DelegatingAdapter methods
     */

    /**
     * Gets an AdapterDelegate to use for the given position. This default
     * implementation returns the first delegate that returns true from its
     * isDelegateFor() method.
     *
     * @param position
     *     The position in the adapter, or null if unknown.
     *
     * @param item
     *     The item.
     */
    open fun getDelegate(position: Int?, item: T) =
        delegates?.find {
            it.isDelegateFor(position, item)
        } ?: error("no delegate found for position $position " +
            "(${getItemTypeString(item)})")

    /**
     * Gets the ID of the given item, delegating to the given AdapterDelgate.
     *
     * @param position
     *     The position in the adapter, or null if unknown.
     *
     * @param item
     *     The item.
     *
     * @param delegate
     *     The AdapterDelgate to delegate to.
     *
     * @throws IllegalArgumentException
     *     If [.hasStableIds] returns true but the ID returned from the given
     *     delegate is RecyclerView.NO_ID.
     */
    protected fun getItemId(
        position: Int?,
        item: T,
        delegate: AdapterDelegate<T>
    ) = delegate.getItemId(position, item, this).also {
            require(position != null &&
                    (!hasStableIds() || it != RecyclerView.NO_ID)) {
                "${this::class.qualifiedName}.hasStableIds() returns true " +
                "but ID for position $position (item " +
                "${getItemTypeString(item)}, delegate " +
                "${delegate::class.qualifiedName}) is RecyclerView.NO_ID"
            }
        }

    /**
     * Returns the index of the given AdapterDelegate in the [delegates
     * list][.getDelegate].
     *
     * @exception NullPointerException
     *     if the given AdapterDelegate is not one of this DelegatingAdapter's
     *     AdapterDelegates
     */
    fun getItemViewType(delegate: AdapterDelegate<T>) = viewTypes[delegate]!!

    /*
     * Private methods
     */

    private fun getItemTypeString(item: T) =
        if (item == null) "null" else (item as Any)::class.qualifiedName
}
