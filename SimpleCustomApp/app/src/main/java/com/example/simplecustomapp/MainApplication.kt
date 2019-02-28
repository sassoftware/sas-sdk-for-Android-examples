package com.example.simplecustomapp

import androidx.multidex.MultiDexApplication
import com.sas.android.visualanalytics.sdk.SASManagerContext
import com.sas.android.visualanalytics.sdk.SASManager

class MainApplication : MultiDexApplication() {
    lateinit var sasManager: SASManager
        private set

    override fun onCreate() {
        super.onCreate()
        sasManager = SASManager.init(this, SASManagerContext())
    }
}
