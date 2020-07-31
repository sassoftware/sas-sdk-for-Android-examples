package com.sas.android.covid19

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.sas.android.covid19.util.logV

class MainViewModel(private val app: MainApplication) : ViewModel() {
    private val repo = app.reportRepo

    val allLocations = repo.allLocations
    val localLocation = repo.localLocation
    val defaultLocations = repo.defaultLocations
    val selectedLocations = repo.selectedLocations
    val curIndex = repo.curIndex
    val report = repo.report
    val reportStatus = repo.reportStatus

    /**
     * The location, if any, the ViewPager2 is swiping from.
     */
    val fromLocation = MutableLiveData<String>().apply {
        observeForever { v ->
            logV("fromLocation: $v", tag = MainViewModel::class)
        }
    }

    /**
     * The current y offset of the location, if any, the ViewPager2 is swiping from.
     */
    val fromLocationOffset = MutableLiveData<Int>().apply {
        observeForever { v ->
            logV("fromLocationOffset: $v", tag = MainViewModel::class)
        }
    }

    /**
     * The location, if any, the ViewPager2 is swiping to.
     */
    val toLocation = MutableLiveData<String>().apply {
        observeForever { v ->
            logV("toLocation: $v", tag = MainViewModel::class)
        }
    }

    /**
     * Minimum height of country pages, kept in sync so that scroll position can
     * be synchronized even if visuals have not yet loaded.
     */
    val pageMinHeight = MutableLiveData<Int>().apply {
        value = 0
    }
}
