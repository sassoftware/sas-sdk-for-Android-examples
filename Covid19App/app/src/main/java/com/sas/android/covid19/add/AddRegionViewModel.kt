package com.sas.android.covid19.add

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel

import com.sas.android.covid19.MainApplication
import com.sas.android.covid19.util.isWorldwide
import com.sas.android.covid19.util.toLocalizedRegion

class AddRegionViewModel(private val app: MainApplication) : ViewModel() {

    private val repo = app.reportRepo

    val allRegions = repo.allRegions
    val selectedRegions = repo.selectedRegions
    val selectedIndex = repo.selectedIndex

    // Raw -> localized pairs
    val suggestions = MediatorLiveData<List<Pair<String, String>>>().apply {
        fun recalculate() {
            val selected = selectedRegions.value.orEmpty()
            value = allRegions.value.orEmpty().filter {
                it !in selected
            }.map {
                it to it.toLocalizedRegion(app)
            }.sortedWith(Comparator { a, b ->
                when {
                    a.first.isWorldwide -> -1
                    b.first.isWorldwide -> 1
                    else -> a.second.compareTo(b.second, true)
                }
            })
        }

        addSource(allRegions) { _ ->
            recalculate()
        }

        addSource(selectedRegions) { _ ->
            recalculate()
        }

        recalculate()
    }
}
