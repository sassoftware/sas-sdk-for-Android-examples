package com.sas.android.covid19

import androidx.lifecycle.ViewModel

class MainViewModel(private val app: MainApplication) : ViewModel() {

    private val repo = app.reportRepo

    val allRegions = repo.allRegions
    val localRegion = repo.localRegion
    val selectedRegions = repo.selectedRegions
    val selectedIndex = repo.selectedIndex
    val report = repo.report
}
