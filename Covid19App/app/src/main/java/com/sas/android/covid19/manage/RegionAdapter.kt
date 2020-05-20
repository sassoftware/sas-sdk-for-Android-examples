package com.sas.android.covid19.manage

import android.content.Context

import com.sas.android.covid19.ui.recycler.ListAdapter

class RegionAdapter(context: Context, model: List<String>?) :
    ListAdapter<String>(context, true, model, listOf(RegionDelegate(context)))
