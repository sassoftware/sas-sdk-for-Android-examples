package com.sas.android.covid19

import androidx.lifecycle.ViewModel

class LoadingViewModel(private val app: MainApplication) : ViewModel() {

    private val repo = app.reportRepo

    val report = repo.report
    val error = repo.error
}
