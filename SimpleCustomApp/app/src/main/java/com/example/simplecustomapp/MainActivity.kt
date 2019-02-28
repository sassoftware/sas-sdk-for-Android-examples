package com.example.simplecustomapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sas.android.visualanalytics.report.controller.FullScreenRequest
import com.sas.android.visualanalytics.report.controller.ReportViewController
import com.sas.android.visualanalytics.sdk.ReportDescriptor
import com.sas.android.visualanalytics.sdk.SASManager
import com.sas.android.visualanalytics.sdk.model.Report
import com.sas.android.visualanalytics.sdk.model.Server
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var connection: Server
    private lateinit var reportViewController: ReportViewController
    private lateinit var reportProgress: View
    private lateinit var progressText: TextView

    private val reports = mutableListOf<Report>()

    /**
     * Sample onConnectionComplete() to illustrate the callback from the connection creation process
     */
    private fun onConnectionComplete(result: SASManager.Result) {
        if (result is SASManager.Result.Success) {
            connection = result.server
            connectionReady()
        }
    }

    /**
     * Sample onSubscribeComplete to illustrate the callback from the report subscription process
     */
    private fun onSubscribeComplete(result: Server.Result) {
        if (result is Server.Result.Success) {
            val report = result.report
            reports.add(report)
            reportSubscribed(report)
        }
    }

    override fun onBackPressed() {
        if (reportViewController.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    private fun connectionReady() {
        progressText.text = "Connection created. Subscribing to report."
    }

    private fun reportSubscribed(report: Report) {
        loadReportViewer(report)
    }

    /**
     * Launch custom report view activity. Pass in the report id from SubscribeListener Result
     */
    private fun loadReportViewer(report: Report) {
        reportViewController = ReportViewController(this, reportView, report.id).also  {
            it.addReportEventListener { reportEvent ->
                when (reportEvent){
                    is FullScreenRequest -> supportActionBar?.run {
                        if (reportEvent.fullScreen) {
                            hide()
                        } else {
                            show()
                        }
                    }
                }
            }
        }
        reportLayoutRoot.visibility = View.VISIBLE
        reportProgress.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        reportProgress = findViewById(R.id.report_progress)
        progressText = findViewById(R.id.progress_text)
        progressText.text = "Creating Connection"

        // create SASManager and have it create connection and subscribe to reports listed in
        // StartupConnectionDescriptor
        val connectionDescriptor = StartupConnectionDescriptor()
        (application as MainApplication).sasManager.create(connectionDescriptor,
                connectionDescriptor.reports, ::onConnectionComplete, ::onSubscribeComplete)
    }
}
