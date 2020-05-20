package com.sas.android.covid19.manage

import androidx.lifecycle.ViewModel

import com.sas.android.covid19.MainApplication

class ManageRegionsViewModel(private val app: MainApplication) : ViewModel() {

    private val repo = app.reportRepo

    val selectedRegions = repo.selectedRegions
    val selectedIndex = repo.selectedIndex
}
