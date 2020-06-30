package com.sas.android.covid19.manage

import androidx.lifecycle.ViewModel

import com.sas.android.covid19.MainApplication

class ManageLocationsViewModel(private val app: MainApplication) : ViewModel() {

    private val repo = app.reportRepo

    val selectedLocations = repo.selectedLocations
    val curIndex = repo.curIndex
}
