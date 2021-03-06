package com.sas.android.covid19

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(activity: FragmentActivity, private val locations: List<String>?) :
        FragmentStateAdapter(activity) {
    /*
     * RecyclerView.Adapter methods
     */

    override fun getItemCount() = locations?.size ?: 0

    /*
     * FragmentStateAdapter methods
     */

    override fun createFragment(position: Int): Fragment =
        PageFragment.create(locations!![position])
}
