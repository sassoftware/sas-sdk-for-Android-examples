package com.sas.android.covid19.ui.recycler

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.sas.covid19.kotlin.properties.Delegates

/**
 * A DelegatingAdapter backed by a changeable model, with optional
 * filtering/sorting.
 *
 * When a [.sort] or [.filter] has been set, this class maintains a "refined"
 * model containing the sorted/filtered items.
 *
 * Updating the model
 *
 * If no sort/filter is set, the usual discrete methods may be used after
 * updating the model:
 *
 *     notifyDataSetChanged()
 *     notifyItemChanged(position: Int)
 *     notifyItemChanged(position: Int, payload: Any)
 *     notifyItemRangeChanged(positionStart: Int, itemCount: Int)
 *     notifyItemRangeChanged(positionStart: Int, itemCount: Int,
 *         Object payload)
 *     notifyItemInserted(position: Int)
 *     notifyItemMoved(fromPosition: Int, toPosition: Int)
 *     notifyItemRangeInserted(positionStart: Int, itemCount: Int)
 *     notifyItemRemoved(position: Int)
 *     notifyItemRangeRemoved(positionStart: Int, itemCount: Int)
 *
 * If a sort/filter has been set, however, these methods should not be used when
 * updating the model.  Instead, use:
 *
 *     [.modelChanged]
 *
 * to update the refined model and notify listeners.  This method uses DiffUtil
 * when possible to intelligently generate discrete events.  Note that this
 * method is called automatically when setting the [.model] directly.
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
abstract class RefinableAdapter<T, M>(
    context: Context,
    hasStableIds: Boolean,
    model: M,
    delegates: List<AdapterDelegate<T>>?
) : DelegatingAdapter<T>(context, hasStableIds, delegates) {
    /*
     * Properties/init
     */

    /**
     * The model, before filtering/sorting.  If this model is changed outside of
     * this class, [.modelChanged] must be called to reapply any filter or sort,
     * and to notify listeners.  When this property is set, this is done
     * automatically.
     */
    var model by Delegates.observable(model) { _, old, new ->
        refineAndNotify(old, new)
    }

    /**
     * The model, before filtering/sorting, in the form of a [Sequence].
     */
    abstract val modelAsSequence: Sequence<T>?

    /**
     * The model, after filtering/sorting.  If null, the model is used directly.
     */
    private var refined: List<T>? = null

    /**
     * Whether this adapter maintains a refined model for a set sort/filter.
     */
    val isRefined
        get() = refined != null

    /**
     * An optional predicate used to filter the model.
     */
    // Prefer kotlin.properties.Delegates.observable to
    // com.sas.covid19.kotlin.properties.Delegates.observable to invoke
    // onChangedListener every time it is set
    var filter by kotlin.properties.Delegates.observable<((T) -> Boolean)?>(
            null) { _, _, _ ->
        refineAndNotify(model, model)
    }

    /**
     * An optional comparison used to sort the model.
     */
    // Prefer kotlin.properties.Delegates.observable to
    // com.sas.covid19.kotlin.properties.Delegates.observable to invoke
    // onChangedListener every time it is set
    var sort by kotlin.properties.Delegates.observable<((T, T) -> Int)?>(
            null) { _, _, _ ->
        refineAndNotify(model, model)
    }

    /**
     * Whether to notify observers of model changes. Provided only FBO unit
     * tests.
     */
    private val doNotify = true

    /*
     * RecyclerView.Adapter methods
     */

    final override fun getItemCount() =
        refined?.size ?: getModelItemCount(model)

    /*
     * CommonAdapter methods
     */

    final override fun getItem(position: Int) =
        // Note: don't be tempted to use:
        //     refined?.get(position) :? getModelItem(position)
        // here!
        refined.let {
            if (it != null) it[position] else getModelItem(position, model)
        }

    /*
     * RefinableAdapter methods
     */

    /**
     * Used by DiffUtil to determine if two items' visual representations are
     * the same.  This default implementation checks for structural equality.
     */
    open fun areContentsTheSame(old: T, new: T) = old == new

    /**
     * Used by DiffUtil to determine if two items represent the same item.  This
     * default implementation ensures [.hasStableIds] returns true and the
     * items' IDs are not RecyclerView.NO_ID and are structurally equal.
     */
    open fun areItemsTheSame(old: T, new: T): Boolean {
        if (hasStableIds()) {
            val oldId = getDelegate(null, old).getItemId(null, old, this)
            if (oldId != RecyclerView.NO_ID) {
                val newId = getDelegate(null, new).getItemId(null, new, this)
                if (newId == oldId) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Get an item from the underlying model.
     */
    abstract fun getModelItem(position: Int, model: M): T

    /**
     * The item count of the underlying model.
     */
    abstract fun getModelItemCount(model: M): Int

    fun modelChanged() {
        refineAndNotify(model, model)
    }

    /**
     * Called when this RefinableAdapter has been refined with a new [filter] or
     * [sort].  This default implementation does nothing.
     */
    open fun onRefined() {
    }

    /*
     * Private methods
     */

    /**
     * Regenerate the refined model and notify listeners after the [underlying
     * model][.model] or sort/filter have changed.
     */
    private fun refineAndNotify(oldModel: M, newModel: M) {
        val f = filter
        val s = sort
        val oldRefined = refined
        val newRefined = if (f == null && s == null) null else {
            var sequence = modelAsSequence
            if (sequence == null) null else {
                if (f != null) {
                    sequence = sequence.filter(f)
                }

                if (s != null) {
                    sequence = sequence.sortedWith(Comparator(s))
                }
                sequence.toList()
            }
        }
        refined = newRefined

        if (doNotify) {
            if (oldRefined == newRefined && oldModel == newModel) {
                // We have no "before" to compare the current model to, so use
                // the bluntest instrument
                notifyDataSetChanged()
            } else {
                // Examine differences between old/new, generate discrete events
                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun areItemsTheSame(
                        oldPos: Int,
                        newPos: Int
                    ): Boolean {
                        val old = getOldItemAt(oldPos)
                        val new = getNewItemAt(newPos)

                        if (old === new) {
                            return true
                        }

                        return areItemsTheSame(old, new)
                    }

                    override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                        this@RefinableAdapter.areContentsTheSame(
                            getOldItemAt(oldPos), getNewItemAt(newPos))

                    override fun getOldListSize() =
                        oldRefined?.size ?: getModelItemCount(oldModel)

                    override fun getNewListSize() =
                        newRefined?.size ?: getModelItemCount(newModel)

                    private fun getOldItemAt(position: Int) =
                        if (oldRefined != null) oldRefined[position]
                            else getModelItem(position, oldModel)

                    private fun getNewItemAt(position: Int) =
                        if (newRefined != null) newRefined[position]
                            else getModelItem(position, newModel)
                }).dispatchUpdatesTo(this)
            }
        }

        onRefined()
    }
}
