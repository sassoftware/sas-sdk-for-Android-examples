package com.sas.android.covid19

import com.sas.android.covid19.ui.recycler.ListAdapter

class VisualAdapter(activity: MainActivity, private val region: String) :
    ListAdapter<Int>(activity, true,
        (0 until (activity.visualLoader.value?.getVisualCount(region) ?: 0)).toList(),
        listOf(VisualDelegate(activity, region)))
