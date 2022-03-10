package com.example.simplecustomapp

import androidx.multidex.MultiDexApplication
import com.sas.android.visualanalytics.sdk.SASManager
import com.sas.android.visualanalytics.sdk.SASManagerContext

class MainApplication : MultiDexApplication() {
    /*
     * Properties/init
     */

    lateinit var sasManager: SASManager
        private set

    /*
     * Application methods
     */

    override fun onCreate() {
        super.onCreate()
        sasManager = SASManager.init(this, SASManagerContext())
    }
}
