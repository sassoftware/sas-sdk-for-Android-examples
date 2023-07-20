package com.example.customapp

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.customapp.databinding.ActivityReportViewBinding
import com.example.util.viewBinding
import com.google.android.material.color.MaterialColors
import com.sas.android.visualanalytics.report.controller.FullScreenRequest
import com.sas.android.visualanalytics.report.controller.ReportViewController

internal class ReportViewActivity : AppCompatActivity() {
    /*
     * Properties/init
     */

    val binding by viewBinding(ActivityReportViewBinding::inflate)

    private lateinit var reportViewController: ReportViewController

    /*
     * Activity methods
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get report id from intent extra and pass it to ReportViewController
        val reportId = intent.getStringExtra(ReportViewActivity.EXTRA_REPORT_ID)!!
        reportViewController = ReportViewController(this, this, binding.reportView, reportId).also {
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

        setUpOnBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val returnValue = if (!reportViewController.onOptionsItemSelected(item)) {
            if (android.R.id.home == item.itemId) {
                finish()
                true
            } else {
                super.onOptionsItemSelected(item)
            }
        } else {
            true
        }

        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary, null)
            )
        )
        return returnValue
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        reportViewController.onPrepareOptionsMenu(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    /*
     * Private methods
     */

    private fun setUpOnBackPressed() {
        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val handled = reportViewController.onBackPressed()
                    if (!handled) {
                        finish()
                    }
                }
            }
        )
    }

    /*
     * Companion
     */

    companion object {
        private val EXTRA_PREFIX = ReportViewActivity::class.java.name + "."
        val EXTRA_REPORT_ID = EXTRA_PREFIX + "EXTRA_REPORT_ID"
    }
}
