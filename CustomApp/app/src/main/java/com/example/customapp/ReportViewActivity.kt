package com.example.customapp

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.sas.android.visualanalytics.report.controller.ReportViewController
import kotlinx.android.synthetic.main.activity_report_view.*

internal class ReportViewActivity : AppCompatActivity() {
    private lateinit var reportViewController: ReportViewController

    companion object {
        private val EXTRA_PREFIX = ReportViewActivity::class.java.name + "."
        val EXTRA_REPORT_ID = EXTRA_PREFIX + "EXTRA_REPORT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get report id from intent extra and pass it to ReportViewController
        val reportId = intent.getStringExtra(ReportViewActivity.EXTRA_REPORT_ID);
        reportViewController = ReportViewController(this, reportView, reportId).also {
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
    }

    override fun onBackPressed() {
        if (reportViewController.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        reportViewController.onPrepareOptionsMenu(menu)
        return super.onPrepareOptionsMenu(menu)
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

        supportActionBar?.setBackgroundDrawable(ColorDrawable(
                ContextCompat.getColor(this, R.color.colorPrimary)))
        return returnValue
    }
}
