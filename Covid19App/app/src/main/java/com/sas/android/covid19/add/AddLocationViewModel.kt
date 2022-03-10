package com.sas.android.covid19.add

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.sas.android.covid19.MainApplication
import com.sas.android.covid19.util.isWorldwide
import com.sas.android.covid19.util.toLocalizedLocation

class AddLocationViewModel(private val app: MainApplication) : ViewModel() {

    private val repo = app.reportRepo

    val allLocations = repo.allLocations
    val selectedLocations = repo.selectedLocations
    val curIndex = repo.curIndex

    // Raw -> localized pairs
    val suggestions = MediatorLiveData<List<Pair<String, String>>>().apply {
        fun recalculate() {
            val selected = selectedLocations.value.orEmpty()
            value = allLocations.value.orEmpty().filter {
                it !in selected
            }.map {
                it to it.toLocalizedLocation(app)
            }.sortedWith(
                Comparator { a, b ->
                    when {
                        a.first.isWorldwide -> -1
                        b.first.isWorldwide -> 1
                        else -> a.second.compareTo(b.second, true)
                    }
                }
            )
        }

        addSource(allLocations) { _ ->
            recalculate()
        }

        addSource(selectedLocations) { _ ->
            recalculate()
        }

        recalculate()
    }
}
