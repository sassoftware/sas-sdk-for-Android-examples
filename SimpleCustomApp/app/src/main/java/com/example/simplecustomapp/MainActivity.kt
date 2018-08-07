package com.example.simplecustomapp

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.sas.android.visualanalytics.report.controller.ReportViewController
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
        val srvr = result.server
        if (srvr != null) {
            connection = srvr
            connectionReady()
        }
    }

    /**
     * Sample onSubscribeComplete to illustrate the callback from the report subscription process
     */
    private fun onSubscribeComplete(result: Server.Result) {
        val report = result.report
        if (report != null) {
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
        loadReportViewer(this, report)
    }

    /**
     * Launch custom report view activity. Pass in the report id from SubscribeListener Result
     */
    private fun loadReportViewer(context: Context, report: Report) {
        reportViewController = ReportViewController(this, reportView, report.id).also  {
            it.addFullScreenListener { fullScreen ->
                supportActionBar?.run {
                    if (fullScreen) {
                        hide()
                    } else {
                        show()
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

        // create SASManager and have it create connection and subscribe to reports
        // specified in StartupConnectionDescriptor
        (application as MainApplication).sasManager.create(StartupConnectionDescriptor(),
                ::onConnectionComplete,
                ::onSubscribeComplete)
    }
}
