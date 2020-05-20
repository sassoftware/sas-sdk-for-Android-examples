package com.sas.android.covid19

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import android.graphics.Bitmap
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder

import com.sas.android.covid19.ui.recycler.AbstractAdapterDelegate
import com.sas.android.covid19.ui.recycler.DelegatingAdapter
import com.sas.android.covid19.util.VisualLoader.Payload
import com.sas.android.covid19.util.setImageBitmapOrGone
import com.sas.android.covid19.util.setTextOrGone
import com.sas.android.covid19.util.setVisibleOrGone
import com.sas.android.covid19.util.toLocalizedRegion

import kotlinx.android.synthetic.main.delegate_visual.view.*

class VisualDelegate(
    private val activity: MainActivity,
    private val region: String
) : AbstractAdapterDelegate<Int>(activity, R.layout.delegate_visual) {
    /*
     * AdapterDelegate methods
     */

    // Required to support selection
    override fun getItemId(
        position: Int?,
        item: Int,
        adapter: DelegatingAdapter<Int>
    ) = item.hashCode().toLong()

    override fun isDelegateFor(position: Int?, item: Int) = true

    override fun isSelectable(
        position: Int,
        item: Int,
        adapter: DelegatingAdapter<Int>
    ) = true

    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int,
        item: Int,
        adapter: DelegatingAdapter<Int>
    ) {
        viewHolder as LocalViewHolder

        GlobalScope.launch(Dispatchers.Main) {
            val payload = activity.visualLoader.value?.getTitleAndPayload(context, region, item)
            var outerTitle: String? = null
            var innerTitle: String? = null
            var bitmap: Bitmap? = null
            var text: CharSequence? = null

            viewHolder.objView.setVisibleOrGone(false)

            if (payload != null) {
                if (payload.titleIsInner) {
                    innerTitle = payload.title
                } else {
                    outerTitle = payload.title
                }

                when (payload) {
                    is Payload.WithBitmap -> {
                        bitmap = payload.bitmap
                    }
                    is Payload.WithText -> {
                        text = payload.text
                    }
                    is Payload.WithView -> {
                        viewHolder.objView.addView(payload.view)
                        viewHolder.objView.setVisibleOrGone(true)
                    }
                }
            }

            viewHolder.outerTitleView.setTextOrGone(outerTitle)
            viewHolder.innerTitleView.setTextOrGone(innerTitle)
            viewHolder.imageView.setImageBitmapOrGone(bitmap)
            viewHolder.textView.setTextOrGone(text)

            viewHolder.itemView.setOnClickListener {
                payload?.onExpand?.also {
                    GlobalScope.launch(Dispatchers.Main) {
                        it.invoke()
                        activity.showExpanded(payload.view, region.toLocalizedRegion(context), null)
                    }
                }
            }
        }
    }

    /*
     * AbstractAdapterDelegate methods
     */

    override fun onCreateViewHolder(
        view: View,
        adapter: DelegatingAdapter<Int>
    ) = LocalViewHolder(view)

    /*
     * Classes
     */

    class LocalViewHolder(view: View) : ViewHolder(view) {
        val outerTitleView = view.outerTitleView
        val innerTitleView = view.innerTitleView
        val textView = view.textView
        val imageView = view.imageView
        val objView = view.objView
    }
}
