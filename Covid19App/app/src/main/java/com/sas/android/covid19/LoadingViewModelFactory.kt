package com.sas.android.covid19

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LoadingViewModelFactory(private val app: MainApplication) : ViewModelProvider.Factory {
    /*
     * ViewModelProvider.Factory methods
     */

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = LoadingViewModel(app) as T
}
