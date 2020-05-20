package com.sas.android.covid19.manage

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder

import com.sas.android.covid19.R
import com.sas.android.covid19.ui.recycler.AbstractAdapterDelegate
import com.sas.android.covid19.ui.recycler.DelegatingAdapter
import com.sas.android.covid19.util.toLocalizedRegion

import kotlinx.android.synthetic.main.delegate_region.view.*

class RegionDelegate(context: Context) :
        AbstractAdapterDelegate<String>(context, R.layout.delegate_region) {
    /*
     * AdapterDelegate methods
     */

    // Required to support selection
    override fun getItemId(
        position: Int?,
        item: String,
        adapter: DelegatingAdapter<String>
    ) = item.hashCode().toLong()

    override fun isDelegateFor(position: Int?, item: String) = true

    override fun isSelectable(
        position: Int,
        item: String,
        adapter: DelegatingAdapter<String>
    ) = true

    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int,
        item: String,
        adapter: DelegatingAdapter<String>
    ) {
        viewHolder as LocalViewHolder
        viewHolder.textView.text = item.toLocalizedRegion(context)
    }

    /*
     * AbstractAdapterDelegate methods
     */

    override fun onCreateViewHolder(
        view: View,
        adapter: DelegatingAdapter<String>
    ) = LocalViewHolder(view)

    /*
     * Classes
     */

    class LocalViewHolder(view: View) : ViewHolder(view) {
        val textView = view.textView
    }
}
