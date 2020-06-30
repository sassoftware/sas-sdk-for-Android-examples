package com.sas.android.covid19

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.sas.android.covid19.databinding.ActivityLoadingBinding
import com.sas.android.covid19.util.onMeasured
import com.sas.android.visualanalytics.sdk.model.Report

import kotlinx.android.synthetic.main.activity_loading.*

class LoadingActivity : AppCompatActivity() {
    /*
     * Properties/init
     */

    val viewModel by lazy {
        ViewModelProvider(this, LoadingViewModelFactory(application as MainApplication))
            .get(LoadingViewModel::class.java)
    }

    /*
     * Activity methods
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw behind status bar
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        viewModel.report.observe(this, Observer<Report?> { report ->
            if (report != null) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        })

        val binding: ActivityLoadingBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_loading)
        binding.viewModel = viewModel
        binding.setLifecycleOwner(this)

        spinner.alpha = 0f
        spinner.onMeasured {
            ObjectAnimator.ofFloat(spinner, View.ALPHA, 0f, 1f).apply {
                // Should align more or less with time until Spots stops animating
                startDelay = 5000
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }
}
