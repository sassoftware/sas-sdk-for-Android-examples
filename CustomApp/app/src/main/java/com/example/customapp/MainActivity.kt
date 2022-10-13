package com.example.customapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customapp.databinding.ActivityMainBinding
import com.example.customapp.databinding.ReportCardViewBinding
import com.example.util.viewBinding
import com.sas.android.visualanalytics.sdk.SASManager
import com.sas.android.visualanalytics.sdk.model.Report
import com.sas.android.visualanalytics.sdk.model.Server

private val metadata = listOf(
    ReportMetadata(
        "Capital Campaign",
        "This report shows the donations received towards a campaign goal per state over time",
        R.drawable.capital_campaign
    ),
    ReportMetadata(
        "Retail Insights",
        "This report shows the performance of three different stores by region and state",
        R.drawable.retal_insights
    ),
    ReportMetadata(
        "Warranty Analysis",
        "This report shows the annual trends in warranty cost as well as cost forecasts.",
        R.drawable.warranty_analysis
    ),
    ReportMetadata(
        "Water Consumption and Monitoring",
        "This report shows you the location and trends over time of high water consumers.",
        R.drawable.water_consumption
    )
)

data class ReportMetadata(
    val title: String,
    val description: String,
    @DrawableRes val thumbnail: Int
)

class MainActivity : AppCompatActivity() {
    /*
     * Properties/init
     */

    val binding by viewBinding(ActivityMainBinding::inflate)

    private val reports = mutableListOf<Report>()
    private val reportsAdapter = ReportsAdapter()
    private val descriptor = StartupConnectionDescriptor()

    /*
     * Activity methods
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.progressText.text = "Creating Connection"
        binding.reportsView.let {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = reportsAdapter
        }

        // Create connection and subscribe to reports listed in StartupConnectionDescriptor
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
     * Launch custom report view activity. Pass in the report id from SubscribeListener Result
     */
    private fun launchReportViewer(id: String) {
        startActivity(
            Intent(this, ReportViewActivity::class.java).apply {
                putExtra(ReportViewActivity.EXTRA_REPORT_ID, id)
            }
        )
    }

    /**
     * Sample onConnectionComplete() to illustrate the callback from the connection creation process
     */
    private fun onConnectionComplete(result: SASManager.Result) {
        when (result) {
            is SASManager.Result.Success ->
                binding.progressText.text = "Connection added. Subscribing to reports…"

            is SASManager.Result.Failure -> {
                binding.progressBar.visibility = View.GONE
                binding.progressText.text = result.message ?: "Unable to create connection."
            }
        }
    }

    /**
     * Sample onSubscribeComplete to illustrate the callback from the report subscription process
     */
    private fun onSubscribeComplete(result: Server.Result) {
        if (result is Server.Result.Success) {
            reports += result.report

            binding.reportsView.visibility = View.VISIBLE
            binding.progressText.text = "Downloaded ${reports.size} of ${descriptor.reports.size}"

            val done = reports.size == descriptor.reports.size
            if (done) {
                binding.progressView.visibility = View.GONE
            }

            reportsAdapter.notifyDataSetChanged()
        }
    }

    /*
     * Classes
     */

    /**
     * RecyclerView adapter that provides a list of buttons to open the subscribed reports
     */
    inner class ReportsAdapter : RecyclerView.Adapter<ReportsAdapter.ReportCardViewHolder>() {
        /*
         * RecyclerView.Adapter methods
         */

        override fun getItemCount() = reports.size

        override fun onBindViewHolder(holder: ReportsAdapter.ReportCardViewHolder, position: Int) {
            val report = reports[position]
            val meta = metadata.find {
                it.title == report.name
            }

            with(holder.binding) {
                title.text = report.name
                description.text = meta?.description
                image.setImageResource(meta?.thumbnail ?: 0)
                root.setOnClickListener {
                    launchReportViewer(reports[position].id)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ReportCardViewHolder(
            ReportCardViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        /*
         * Classes
         */

        inner class ReportCardViewHolder(val binding: ReportCardViewBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}
