package com.sas.android.covid19

import androidx.multidex.MultiDexApplication
import com.sas.android.visualanalytics.sdk.SASManager
import com.sas.android.visualanalytics.sdk.SASManagerContext

class MainApplication : MultiDexApplication() {
    /*
     * Properties/init
     */

    lateinit var sasContext: SASManagerContext
        private set

    lateinit var sasManager: SASManager
        private set

    lateinit var reportRepo: ReportRepository
        private set

    /*
     * Application methods
     */

    override fun onCreate() {
        super.onCreate()
        sasContext = SASManagerContext()
        sasManager = SASManager.init(this, sasContext)
        reportRepo = ReportRepository(this)
    }
}
