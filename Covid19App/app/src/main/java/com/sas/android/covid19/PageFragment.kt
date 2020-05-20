package com.sas.android.covid19

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sas.android.covid19.ui.recycler.LinearSpaceItemDecoration
import com.sas.android.covid19.util.VisualLoader

import kotlinx.android.synthetic.main.fragment_page.*

class PageFragment : Fragment() {
    /*
     * Fragment methods
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as MainActivity

        recycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

            val spacing = activity.resources.getDimension(R.dimen.recycler_spacing).toInt()
            addItemDecoration(LinearSpaceItemDecoration(spacing, true, true))

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val fab = activity.findViewById<FloatingActionButton>(R.id.addRegionButton)
                    if (dy > 0) {
                        fab.hide()
                    } else if (dy < -10) {
                        fab.show()
                    }
                }
            })

            rebuildAdapter()
        }

        activity.visualLoader.observe(this, Observer<VisualLoader?> { _ ->
            rebuildAdapter()
        })
    }

    /*
     * Private methods
     */

    private fun rebuildAdapter() {
        recycler.adapter = VisualAdapter(activity as MainActivity,
            arguments!!.getString(ARG_REGION)!!)
    }

    /*
     * Companion
     */

    companion object {
        /*
         * Properties/init
         */

        private const val ARG_REGION: String = "ARG_REGION"

        /*
         * Companion methods
         */

        fun create(region: String) = PageFragment().apply {
            setArguments(Bundle().apply {
                putString(ARG_REGION, region)
            })
        }
    }
}
