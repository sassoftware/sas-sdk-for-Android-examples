package com.example.customapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sas.android.visualanalytics.sdk.ReportDescriptor

import com.sas.android.visualanalytics.sdk.SASManager
import com.sas.android.visualanalytics.sdk.model.Report
import com.sas.android.visualanalytics.sdk.model.Server

class MainActivity : AppCompatActivity() {
    private lateinit var server: Server
    private lateinit var reportsView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var progressView: View
    private lateinit var progressText: TextView

    private val reports = mutableListOf<Report>()
    private val reportsAdapter = ReportsAdapter()

    private val reportThumbnails = intArrayOf(R.drawable.capital_risk, R.drawable.casino_floor,
            R.drawable.digital_advertising, R.drawable.wireless_call_quality)
    private val reportTitles = arrayOf("Capital Exposure and Risk", "Casino Floor Performance",
            "Digital Advertising", "Wireless Call Quality")
    private val reportDescriptions = arrayOf(
            "This report shows the exposure, capital used and return earned across 10 quarters",
            "This report shows overall casino revenue. You can view which machines have the greatest impact on revenue as well",
            "The KPI gauges display average price points for 1,000 impressions on each property",
            "How many dropped calls are there by switch maker? By carrier? By cell technology?")

    /**
     * RecyclerView adapter that provides a list of buttons to open the subscribed reports
     */
    inner class ReportsAdapter: RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {
        inner class ReportViewHolder(v: View): RecyclerView.ViewHolder(v)

        override fun getItemCount(): Int {
            return reports.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.report_card_view, parent, false)
            return ReportViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ReportsAdapter.ReportViewHolder, position: Int) {
            val report = reports[position]
            val metadataIndex = getReportMetadataIndex(report)

            val imageView: ImageView = holder.itemView.findViewById(R.id.card_thumbnail)
            imageView.setImageResource(reportThumbnails[metadataIndex])
            val titleView: TextView = holder.itemView.findViewById(R.id.card_title)
            titleView.text = reportTitles[metadataIndex]
            val descView: TextView = holder.itemView.findViewById(R.id.card_description)
            descView.text = reportDescriptions[metadataIndex]

            holder.itemView.setOnClickListener {
                launchReportViewer(this@MainActivity, reports[position].id)
            }
        }

        private fun getReportMetadataIndex(report: Report): Int {
            when (report.name) {
                "Casino Floor Performance" -> return 1
                "Digital Advertising" -> return 2
                "Wireless call quality analysis report" -> return 3
                else -> return 0
            }
        }
    }

    /**
     * Sample onConnectionComplete() to illustrate the callback from the connection creation process
     */
    private fun onConnectionComplete(result: SASManager.Result) {
        if (result is SASManager.Result.Success) {
            server = result.server
            connectionReady()
        }
    }

    /**
     * Sample onSubscribeComplete to illustrate the callback from the report subscription process
     */
    private fun onSubscribeComplete(result: Server.Result) {
        if (result is Server.Result.Success) {
            reports.add(result.report)
            reportSubscribed()
        }
    }

    private fun connectionReady() {
        progressText.text = "Connection added. Subscribing to reports"
    }

    private fun reportSubscribed() {
        reportsView.visibility = View.VISIBLE
        if (reports.size >= 2)
            progressText.text = ""
        if (reports.size == 4) {
            progressView.visibility = View.GONE
        }
        reportsAdapter.notifyDataSetChanged()
    }

    /**
     * Launch custom report view activity. Pass in the report id from SubscribeListener Result
     */
    private fun launchReportViewer(context: Context, id: String) {
        val intent = Intent(context, ReportViewActivity::class.java)
        intent.putExtra(ReportViewActivity.EXTRA_REPORT_ID, id)
        context.startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressView = findViewById(R.id.progress_view)
        progressText = findViewById(R.id.progress_text)
        progressText.text = "Creating Connection"
        reportsView = findViewById(R.id.reports_view)
        layoutManager = LinearLayoutManager(this)
        reportsView.layoutManager = layoutManager
        reportsView.adapter = reportsAdapter

        // create SASManager and have it create connection and subscribe to reports listed in
        // VATryStartupConnectionDescriptor
        val connectionDescriptor = VATryStartupConnectionDescriptor()
        (application as MainApplication).sasManager.create(connectionDescriptor,
                connectionDescriptor.reports, ::onConnectionComplete, ::onSubscribeComplete)
    }
}
