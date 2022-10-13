package com.example.simplecustomapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.simplecustomapp.databinding.ActivityMainBinding
import com.example.util.viewBinding
import com.sas.android.visualanalytics.report.controller.FullScreenRequest
import com.sas.android.visualanalytics.report.controller.ReportViewController
import com.sas.android.visualanalytics.sdk.SASManager
import com.sas.android.visualanalytics.sdk.model.Report
import com.sas.android.visualanalytics.sdk.model.Server

class MainActivity : AppCompatActivity() {
    /*
     * Properties/init
     */

    val binding by viewBinding(ActivityMainBinding::inflate)

    private var rvc: ReportViewController? = null

    /*
     * Activity methods
     */

    override fun onBackPressed() {
        if (rvc?.onBackPressed() == true) {
            return
        }
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.message.text = "Creating Connection"

        // Create SASManager and have it create connection and subscribe to reports listed in
        // StartupConnectionDescriptor
        val descriptor = StartupConnectionDescriptor()
        (application as MainApplication).sasManager.create(
            descriptor,
            descriptor.reports,
            ::onConnectionComplete,
            ::onSubscribeComplete
        )
    }

    /*
     * Private methods
     */

    /**
     * Launch a custom report view activity, passing in the report id from SubscribeListener Result.
     */
    private fun loadReportViewer(report: Report) {
        rvc = ReportViewController(this, this, binding.reportView, report.id).also {
            it.addReportEventListener { reportEvent ->
                if (reportEvent is FullScreenRequest) {
                    supportActionBar?.run {
                        if (reportEvent.fullScreen) {
                            hide()
                        } else {
                            show()
                        }
                    }
                }
            }
        }
        binding.reportLayoutRoot.visibility = View.VISIBLE
        binding.loading.visibility = View.GONE
    }

    /**
     * Sample onConnectionComplete to illustrate the callback from the connection creation process.
     */
    private fun onConnectionComplete(result: SASManager.Result) {
        when (result) {
            is SASManager.Result.Success -> {
                binding.message.text = "Connection created. Subscribing to report…"
            }

            is SASManager.Result.Failure -> {
                binding.message.text = result.message ?: result.state.toString()
                binding.progress.visibility = View.GONE
            }
        }
    }

    /**
     * Sample onSubscribeComplete to illustrate the callback from the report subscription process.
     */
    private fun onSubscribeComplete(result: Server.Result) {
        if (result is Server.Result.Success) {
            loadReportViewer(result.report)
        }
    }
}
