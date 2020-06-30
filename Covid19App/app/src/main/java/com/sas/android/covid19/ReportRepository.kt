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
import com.sas.android.covid19.util.LOCATION_WORLDWIDE
import com.sas.android.covid19.util.isWorldwide
import com.sas.android.covid19.util.logE
import com.sas.android.covid19.util.logV
import com.sas.android.covid19.util.observe
import com.sas.android.visualanalytics.sdk.SASManager.Result
import com.sas.android.visualanalytics.sdk.model.Report
import com.sas.android.visualanalytics.sdk.model.Server.Result as ServerResult
import com.sas.covid19.kotlin.with

class ReportRepository(private val app: MainApplication) {
    /*
     * Properties/init
     */

    // Set by MainActivity
    val allLocations = MutableLiveData<List<String>>()
    val localLocation = MutableLiveData<String>()

    // The resolved and added local location, if any
    val selectedLocations = MutableLiveData<List<String>>().apply {
        val key = "SETTING_LOCATIONS"
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
        } ?: listOf(LOCATION_WORLDWIDE, "United States", "Russian Federation", "Brazil",
            "United Kingdom")
    }

    val curIndex = MutableLiveData<Int>().apply {
        value = 0
    }

    val reportStatus = MutableLiveData<Report.ReportStatus>()
    val report = MutableLiveData<Report>().apply {
        // Observe forever, notify only when changed
        observe(null, true) { _, newValue ->
            reportStatus.value = newValue?.status
        }
    }

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

        // To add the local location to the selected locations, all of these properties must be set
        allLocations.observeForever { addLocalLocationToSelected() }
        localLocation.observeForever { addLocalLocationToSelected() }
        selectedLocations.observeForever { addLocalLocationToSelected() }
    }

    /*
     * Private methods
     */

    private fun addLocalLocationToSelected() {
        // Wait until all data are non-null
        val allLocations = this@ReportRepository.allLocations.value
        var localLocation = this@ReportRepository.localLocation.value
        val selectedLocations = this@ReportRepository.selectedLocations.value
        if (allLocations != null && localLocation != null && selectedLocations != null) {
            val key = "SETTING_LOCAL_LOCATION_SET"
            val sharedPrefs = app.sasContext.sharedPreferences
                ?: PreferenceManager.getDefaultSharedPreferences(app)

            // Set only once so as to not risk re-adding a removed location
            if (!sharedPrefs.getBoolean(key, false)) {
                sharedPrefs.edit {
                    putBoolean(key, true)
                }

                logV("localLocation: $localLocation")

                // If localLocation is valid…
                allLocations.find {
                    it.equals(localLocation, true)
                }?.also { convertedLocation ->
                    if (localLocation != convertedLocation) {
                        logV("\"$localLocation\" translates to \"$convertedLocation\"")
                    }

                    val selected = convertedLocation in selectedLocations

                    // …and not already selected…
                    if (selected) {
                        logV("\"$convertedLocation\" already selected")
                    } else {
                        // Insert at beginning, or just after if worldwide is first
                        val index = if (!selectedLocations.isEmpty() &&
                            selectedLocations[0].isWorldwide) 1 else 0
                        logV("Inserting \"$convertedLocation\" at position $index")
                        this.selectedLocations.value =
                            selectedLocations.with(index, convertedLocation)
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
                    is ServerResult.Success -> {
                        cont.resume(result.report)
                    }
                    is ServerResult.Failure -> cont.cancel(Exception(result.message))
                }
            })
        }
    }

    private fun onReportStatusUpdate(status: Report.ReportStatus) {
        logV("report status changed: $status")
        reportStatus.value = status
    }
}
