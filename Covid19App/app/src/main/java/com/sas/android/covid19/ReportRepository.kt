package com.sas.android.covid19

import java.io.IOException
import java.io.InputStreamReader

import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sas.android.covid19.util.REGION_WORLDWIDE
import com.sas.android.covid19.util.isWorldwide
import com.sas.android.covid19.util.logE
import com.sas.android.covid19.util.logV
import com.sas.android.covid19.util.notifyObservers
import com.sas.android.visualanalytics.sdk.SASManager.Result
import com.sas.android.visualanalytics.sdk.model.Report
import com.sas.android.visualanalytics.sdk.model.Server.Result as ServerResult
import com.sas.covid19.kotlin.with

class ReportRepository(private val app: MainApplication) {
    /*
     * Properties/init
     */

    // Set by MainActivity
    val allRegions = MutableLiveData<List<String>>()
    val localRegion = MutableLiveData<String>()

    // The resolved and added local region, if any
    val selectedRegions = MutableLiveData<List<String>>().apply {
        val key = "SETTING_REGIONS"
        val sharedPrefs = app.sasContext.sharedPreferences
            ?: PreferenceManager.getDefaultSharedPreferences(app)

        observeForever { list ->
            sharedPrefs.edit {
                if (list == null) {
                    remove(key)
                } else {
                    putString(key, Gson().toJson(list))
                }
            }
        }

        value = sharedPrefs.getString(key, null)?.let { jsonText ->
            val strListType = object : TypeToken<List<String>>() {}.type
            Gson().fromJson<List<String>>(jsonText, strListType)
        } ?: listOf(REGION_WORLDWIDE, "United States", "China", "Italy", "Spain", "Germany")
    }

    val selectedIndex = MutableLiveData<Int>().apply {
        value = 0
    }

    val report = MutableLiveData<Report>()
    val error = MutableLiveData<String>()

    init {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                report.value = fetchReport()?.apply {
                    reportStatusListeners += ::onReportStatusUpdate
                }
                error.value = null
            } catch (e: Throwable) {
                logE("unable to fetch report", e)
                error.value = app.getString(R.string.activity_main_message_failed)
                report.value = null
            }
        }

        // To add the local region to the selected regions, all of these properties must be set
        allRegions.observeForever { addLocalRegionToSelected() }
        localRegion.observeForever { addLocalRegionToSelected() }
        selectedRegions.observeForever { addLocalRegionToSelected() }
    }

    /*
     * Private methods
     */

    private fun addLocalRegionToSelected() {
        // Wait until all data are non-null
        val allRegions = this@ReportRepository.allRegions.value
        var localRegion = this@ReportRepository.localRegion.value
        val selectedRegions = this@ReportRepository.selectedRegions.value
        if (allRegions != null && localRegion != null && selectedRegions != null) {
            val key = "SETTING_LOCAL_REGION_SET"
            val sharedPrefs = app.sasContext.sharedPreferences
                ?: PreferenceManager.getDefaultSharedPreferences(app)

            // Set only once so as to not risk re-adding a removed region
            if (!sharedPrefs.getBoolean(key, false)) {
                sharedPrefs.edit {
                    putBoolean(key, true)
                }

                logV("localRegion: $localRegion")

                // If localRegion is valid…
                allRegions.find {
                    it.equals(localRegion, true)
                }?.also { convertedRegion ->
                    if (localRegion != convertedRegion) {
                        logV("\"$localRegion\" translates to \"$convertedRegion\"")
                    }

                    val selected = convertedRegion in selectedRegions

                    // …and not already selected…
                    if (selected) {
                        logV("\"$convertedRegion\" already selected")
                    } else {
                        // Insert at beginning, or just after if worldwide is first
                        val index = if (!selectedRegions.isEmpty() &&
                            selectedRegions[0].isWorldwide) 1 else 0
                        logV("Inserting \"$convertedRegion\" at position $index")
                        this.selectedRegions.value = selectedRegions.with(index, convertedRegion)
                    }
                }
            }
        }
    }

    private suspend fun fetchReport(): Report? {
        val file = if (BuildConfig.DEBUG) "connections-debug.json" else "connections-release.json"
        var descriptors = try {
            app.sasManager.fromJson(InputStreamReader(app.assets.open(file)), app)
        } catch (e: IOException) {
            logE("unable to read connections json", e)
            return null
        }

        // We define only a single report on a single server in our json
        app.sasManager.getReport(descriptors[0].reports[0])?.also {
            logV("report already subscribed")
            return@fetchReport it
        }

        logV("subscribing to report")
        return suspendCancellableCoroutine<Report?> { cont ->
            app.sasManager.create(descriptors[0], descriptors[0].reports, { result ->
                if (result is Result.Failure) {
                    cont.cancel(Exception(result.state.toString()))
                }
            }, { result ->
                when (result) {
                    is ServerResult.Success -> cont.resume(result.report)
                    is ServerResult.Failure -> cont.cancel(Exception(result.message))
                }
            })
        }
    }

    private fun onReportStatusUpdate(status: Report.ReportStatus) {
        if (status == Report.ReportStatus.REPORT_UPDATED) {
            logV("new report data is available, updating…")
            report.value?.update { result ->
                when (result) {
                    is ServerResult.Success -> {
                        logV("report update succeeded")
                        report.notifyObservers()
                    }
                    is ServerResult.Failure -> {
                        logE("report update failed: ${result.message}")
                    }
                }
            }
        }
    }
}
