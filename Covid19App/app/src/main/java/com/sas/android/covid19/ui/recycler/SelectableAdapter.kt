package com.sas.android.covid19.ui.recycler

import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import androidx.core.util.set
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.sas.android.covid19.util.isEmpty

/**
 * A simple RecyclerView.Adapter that supports selection.
 *
 * @param context
 *     A Context for convenience.
 *
 * @param hasStableIds
 *     Whether this adapter publishes a unique long value that can act as an
 *     identifier for the item at a given position in the data set.
 */
abstract class SelectableAdapter<T>(
    context: Context,
    hasStableIds: Boolean
) : CommonAdapter<T>(context, hasStableIds) {
    /*
     * Properties/init
     */

    private val selPosToIdMap = SparseArray<Long>()

    val selectionSize
        get() = selPosToIdMap.size()

    // Redraw all visible items
    var isInSelectionMode = false
        private set(value) {
            if (field != value) {
                if (value) {
                    assertSelectionEnabled()
                }
                field = value

                selPosToIdMap.clear()
                notifyItemRangeChanged(0, itemCount)

                if (field) {
                    notifySelectionModeStarted()
                } else {
                    notifySelectionModeStopped()
                }
            }
        }

    var isSelectionEnabled = false

    private var selectionListener: SelectionListener? = null

    /**
     * Returns a mapping of selected positions to their IDs.
     */
    val selection
        get() = selPosToIdMap.clone()

    /**
     * Returns a mapping of selected positions to their IDs and values.
     */
    val selectionExt
        get() = List(selPosToIdMap.size()) { i ->
            val position = selPosToIdMap.keyAt(i)
            val id = selPosToIdMap.valueAt(i)
            val value = getItem(position)
            Triple(position, id, value)
        }

    init {
        // Update selection ID when the adapter changes.To avoid flicker, this
        // Observer needs to be registered before the RecyclerView.
        registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                // The positions of selected items may have changed, but the IDs
                // are (theoretically) stable.  Rebuild the selection map with
                // new positions.  This should be the first AdapterDataObserver
                // notified (since it was registered in the adapter's
                // constructor), which gives us the unique opportunity to adjust
                // the selection map before any other AdapterDataObservers have
                // been notified.
                rebuildSelPosToIdMap()
            }

            override fun onItemRangeInserted(first: Int, itemCount: Int) {
                // TODO a narrower handling of this event is needed
                onChanged()
            }

            override fun onItemRangeRemoved(first: Int, itemCount: Int) {
                // TODO a narrower handling of this event is needed
                onChanged()
            }

            override fun onItemRangeMoved(
                first: Int,
                newFirst: Int,
                itemCount: Int
            ) {
                // TODO a narrower handling of this event is needed
                onChanged()
            }
        })
    }

    /*
     * SelectableAdapter methods
     */

    fun clearSelection() {
        if (!selPosToIdMap.isEmpty) {
            // Redraw only deselected items
            val firstSelPosition = selPosToIdMap.keyAt(0)
            val lastSelPosition = selPosToIdMap.keyAt(selPosToIdMap.size() - 1)

            selPosToIdMap.clear()

            notifyItemRangeChanged(
                firstSelPosition,
                lastSelPosition - firstSelPosition + 1
            )
            notifySelectionChanged()
        }
    }

    abstract fun isSelectable(position: Int): Boolean

    fun isSelected(position: Int) = selPosToIdMap[position] != null

    open fun onRestoreInstanceState(savedInstanceState: Bundle) {
        isInSelectionMode = savedInstanceState.getBoolean(KEY_SELECTION_STATE)
        if (isInSelectionMode) {
            clearSelection()

            val positions = savedInstanceState.getIntArray(
                KEY_SELECTION_POSITIONS
            )
            if (positions != null) {
                val ids = savedInstanceState.getLongArray(KEY_SELECTION_IDS)
                check(ids != null && ids.size == positions.size)
                for (i in positions.indices) {
                    setSelected(positions[i], ids[i], true)
                }
            }
        }
    }

    open fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_SELECTION_STATE, isInSelectionMode)
        if (isInSelectionMode) {
            val positions = IntArray(selPosToIdMap.size())
            val ids = LongArray(positions.size)
            for (i in ids.indices) {
                positions[i] = selPosToIdMap.keyAt(i)
                ids[i] = selPosToIdMap.valueAt(i)
            }
            outState.putIntArray(KEY_SELECTION_POSITIONS, positions)
            outState.putLongArray(KEY_SELECTION_IDS, ids)
        }
    }

    fun setAllSelected(isSelected: Boolean) {
        var position = 0
        val n = itemCount
        while (position < n) {
            if (isSelectable(position)) {
                setSelected(position, isSelected)
            }
            position++
        }
    }

    /**
     * Selects or deselects the item at the given position.
     *
     * @return boolean indicating whether the selected state was changed
     */
    fun setSelected(position: Int, isSelected: Boolean) =
        setSelected(position, getItemId(position), isSelected)

    fun setSelectionListener(selectionListener: SelectionListener) {
        this.selectionListener = selectionListener
    }

    fun startSelectionMode() {
        isInSelectionMode = true
    }

    fun stopSelectionMode() {
        isInSelectionMode = false
    }

    fun toggleSelected(position: Int) {
        setSelected(position, !isSelected(position))
    }

    /*
     * Private methods
     */

    private fun assertSelectionEnabled() {
        require(isSelectionEnabled) {
            "selection mode not enabled"
        }
    }

    private fun notifyItemSelectionChanged(
        position: Int,
        id: Long,
        isSelected: Boolean
    ) {
        selectionListener?.onItemSelectionChanged(position, id, isSelected)
    }

    private fun notifySelectionChanged() {
        selectionListener?.onSelectionChanged()
    }

    private fun notifySelectionModeStarted() {
        selectionListener?.onSelectionModeStarted()
    }

    private fun notifySelectionModeStopped() {
        selectionListener?.onSelectionModeStopped()
    }

    private fun rebuildSelPosToIdMap() {
        if (!selPosToIdMap.isEmpty) {
            if (hasStableIds()) {
                val nIds = selPosToIdMap.size()
                val ids = LongArray(nIds)
                for (i in 0 until nIds) {
                    ids[i] = selPosToIdMap.valueAt(i)
                }
                ids.sort()

                selPosToIdMap.clear()

                // This is O(n) slow, and to be avoided if possible
                var position = 0
                val n = itemCount
                while (position < n && selPosToIdMap.size() < nIds) {
                    val id = getItemId(position)
                    // Was this id previously selected?
                    if (ids.binarySearch(id) >= 0) {
                        selPosToIdMap[position] = id
                    }
                    position++
                }
            } else {
                // We are unable to maintain the selection without stable IDs
                selPosToIdMap.clear()
            }

            notifySelectionChanged()
        }
    }

    private fun setSelected(position: Int, id: Long, isSelected: Boolean):
        Boolean {
        assertSelectionEnabled()

        if (isSelected(position) != isSelected) {
            if (isSelected) {
                if (!isSelectable(position)) {
                    // Position not selectable
                    return false
                }

                if (!isInSelectionMode) {
                    startSelectionMode()
                }

                selPosToIdMap[position] = id
            } else {
                selPosToIdMap.remove(position)
            }

            // Redraw item if visible
            notifyItemChanged(position)

            // Notify SelectionListeners
            notifyItemSelectionChanged(position, id, isSelected)

            return true
        }

        return false
    }

    /*
     * Classes
     */

    interface SelectionListener {
        /**
         * Selection changed only for this specific position.
         */
        fun onItemSelectionChanged(position: Int, id: Long, isSelected: Boolean)

        /**
         * Selection changed for one or more positions, or underlying dataset
         * changed rendering the old selection invalid.
         */
        fun onSelectionChanged()

        fun onSelectionModeStarted()

        fun onSelectionModeStopped()
    }

    /*
     * Companion
     */

    companion object {
        private val _KEY_PREFIX = SelectableAdapter::class.java.name + "."
        private val KEY_SELECTION_IDS = _KEY_PREFIX + "ids"
        private val KEY_SELECTION_POSITIONS = _KEY_PREFIX + "positions"
        private val KEY_SELECTION_STATE = _KEY_PREFIX + "state"
    }
}
