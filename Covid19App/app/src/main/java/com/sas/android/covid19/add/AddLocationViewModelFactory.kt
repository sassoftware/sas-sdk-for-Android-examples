package com.sas.android.covid19.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sas.android.covid19.MainApplication

class AddLocationViewModelFactory(private val app: MainApplication) : ViewModelProvider.Factory {
    /*
     * ViewModelProvider.Factory methods
     */

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = AddLocationViewModel(app) as T
}
