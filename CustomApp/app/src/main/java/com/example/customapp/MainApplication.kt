package com.example.customapp

import androidx.multidex.MultiDexApplication
import com.sas.android.visualanalytics.sdk.SASManager
import com.sas.android.visualanalytics.sdk.SASManagerContext

class MainApplication : MultiDexApplication() {
    lateinit var sasManager: SASManager
        private set

    override fun onCreate() {
        super.onCreate()
        sasManager = SASManager.init(this, SASManagerContext())
    }
}
