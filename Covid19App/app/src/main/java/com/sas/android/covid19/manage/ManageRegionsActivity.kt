package com.sas.android.covid19.manage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

import com.google.android.material.snackbar.Snackbar
import com.sas.android.covid19.MainApplication
import com.sas.android.covid19.R
import com.sas.android.covid19.add.AddRegionActivity
import com.sas.android.covid19.ui.recycler.LinearSpaceItemDecoration
import com.sas.android.covid19.ui.recycler.ListAdapter
import com.sas.android.covid19.util.toLocalizedRegion
import com.sas.covid19.kotlin.move
import com.sas.covid19.kotlin.with

import kotlinx.android.synthetic.main.activity_manage.*

class ManageRegionsActivity : AppCompatActivity() {
    /*
     * Properties/init
     */

    val viewModel: ManageRegionsViewModel by lazy {
        ViewModelProviders.of(this, ManageRegionsViewModelFactory(application as MainApplication))
            .get(ManageRegionsViewModel::class.java)
    }

    private var selectedRegions
        get() = viewModel.selectedRegions.value!!
        set(value) {
            viewModel.selectedRegions.value = value
        }

    /*
     * Activity methods
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        viewModel.selectedRegions.observe(this, Observer<List<String>?> {
            @Suppress("UNCHECKED_CAST")
            (recycler.adapter as ListAdapter<String>).model = it

            // Without this, spacing before last element is too large
            recycler.adapter?.notifyDataSetChanged()

            // Select first element in list on return to MainActivity
            viewModel.selectedIndex.value = 0
        })

        recycler.apply recycler@{
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@ManageRegionsActivity,
                LinearLayoutManager.VERTICAL, false)

            val spacing = resources.getDimension(R.dimen.recycler_spacing).toInt()
            addItemDecoration(LinearSpaceItemDecoration(spacing, true, true))

            adapter = RegionAdapter(this@ManageRegionsActivity, selectedRegions)

            // Swipe to remove
            ItemTouchHelper(object : SimpleCallback(0, 0) {
                override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder) =
                    ItemTouchHelper.Callback.makeMovementFlags(
                        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

                override fun isLongPressDragEnabled() = true

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: ViewHolder,
                    target: ViewHolder
                ): Boolean {
                    val fromPos = viewHolder.adapterPosition
                    val toPos = target.adapterPosition
                    selectedRegions = selectedRegions.move(fromPos, toPos)
                    return true
                }

                override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                    val position = viewHolder.getAdapterPosition()
                    val deleted = selectedRegions[position]
                    selectedRegions = selectedRegions.filterIndexed { i, _ ->
                        i != position
                    }

                    Snackbar.make(this@recycler, getString(
                            R.string.activity_manage_regions_deleted_message,
                            deleted.toLocalizedRegion(this@ManageRegionsActivity)),
                            Snackbar.LENGTH_LONG).apply {
                        setActionTextColor(ContextCompat.getColor(context, R.color.snackbar_action))
                        setAction(R.string.activity_manage_regions_deleted_undo) {
                            selectedRegions = selectedRegions.with(position, deleted)
                        }
                    }.show()
                }

                override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
                    super.onSelectedChanged(viewHolder, actionState)

                    if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                        viewHolder?.itemView?.alpha = 0.5f
                    }
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    viewHolder.itemView.alpha = 1.0f
                }
            }).attachToRecyclerView(recycler)
        }

        addRegionButton.setOnClickListener {
            AddRegionActivity.launch(this, false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /*
     * Companion
     */

    companion object {

        fun launch(activity: Activity) = Intent(activity, ManageRegionsActivity::class.java).also {
            activity.startActivity(it)
        }
    }
}
