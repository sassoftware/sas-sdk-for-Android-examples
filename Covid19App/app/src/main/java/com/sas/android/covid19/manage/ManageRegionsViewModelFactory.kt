package com.sas.android.covid19.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.sas.android.covid19.MainApplication

class ManageRegionsViewModelFactory(private val app: MainApplication) : ViewModelProvider.Factory {
    /*
     * ViewModelProvider.Factory methods
     */

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = ManageRegionsViewModel(app) as T
}
