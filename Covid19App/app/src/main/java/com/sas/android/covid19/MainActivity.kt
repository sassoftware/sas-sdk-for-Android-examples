package com.sas.android.covid19

import java.util.Locale

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2

import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.sas.android.covid19.add.AddLocationActivity
import com.sas.android.covid19.databinding.ActivityMainBinding
import com.sas.android.covid19.manage.ManageLocationsActivity
import com.sas.android.covid19.util.VisualLoader
import com.sas.android.covid19.util.logV
import com.sas.android.covid19.util.observe
import com.sas.android.covid19.util.setVisibleOrGone
import com.sas.android.covid19.util.toLocalizedLocation
import com.sas.android.visualanalytics.sdk.model.Report

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    /*
     * Properties/init
     */

    val viewModel by lazy {
        ViewModelProvider(this, MainViewModelFactory(application as MainApplication))
            .get(MainViewModel::class.java)
    }

    val visualLoader = MutableLiveData<VisualLoader>().apply {
        observe(null) { oldValue, newValue ->
            if (oldValue != newValue) {
                lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.allLocations.value = newValue?.getAllLocations()
                }
            }
        }
    }

    private var snackbar: Snackbar? = null

    lateinit var tabLayoutMediator: TabLayoutMediator

    private val selectedLocations
        get() = viewModel.selectedLocations.value!!

    /*
     * Activity methods
     */

    override fun onBackPressed() {
        if (expandedVisualContainer.visibility == View.VISIBLE) {
            showExpanded(null, null, null)
        } else if (viewPager.currentItem != 0) {
            viewPager.currentItem = viewPager.currentItem - 1
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.setLifecycleOwner(this)

        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayShowTitleEnabled(true)

            // Uncomment to display logo in action bar
//          setLogo(UiUtil.getDrawable(this@MainActivity,
//              R.drawable.action_bar_logo, android.R.color.white))
//          setDisplayUseLogoEnabled(true)
        }

        collapsing_toolbar.apply {
            val avenir = ResourcesCompat.getFont(context, R.font.anfsas_regular)
            setCollapsedTitleTypeface(avenir)
            setExpandedTitleTypeface(avenir)
            title = getString(R.string.app_name_full)
        }

        setUpLocation()

        expandedToolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Pre-load the next off-screen page
        viewPager.offscreenPageLimit = 1

        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                private var state = ViewPager2.SCROLL_STATE_IDLE
                private var fromIndex: Int? = null
                private var lastToLocation: String? = null

                override fun onPageSelected(position: Int) {
                    viewModel.curIndex.value = position
                    fromIndex = null
                    lastToLocation = null
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                        // Don't notify observers unnecessarily
                        if (fromIndex == null) {
                            fromIndex = viewModel.curIndex.value ?: 0
                            viewModel.fromLocation.value = selectedLocations.getOrNull(fromIndex!!)
                        }

                        var fromIndex = this.fromIndex!!

                        val toLocation = when (position) {
                            fromIndex -> selectedLocations.getOrNull(fromIndex + 1)
                            fromIndex - 1 -> selectedLocations.getOrNull(fromIndex - 1)
                            else -> null
                        }

                        // Don't notify observers unnecessarily
                        if (toLocation != lastToLocation) {
                            viewModel.toLocation.value = toLocation
                            lastToLocation = toLocation
                        }
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {
                    this.state = state
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        fromIndex = null
                        lastToLocation = null
                    }
                }
            }
        )

        viewModel.curIndex.observe(this, Observer<Int?> { curIndex ->
            curIndex?.also {
                collapsing_toolbar.title =
                    selectedLocations.getOrNull(curIndex)?.toLocalizedLocation(this)
                supportActionBar?.subtitle = null
                addLocationButton.show()
            }
        })

        viewModel.report.observe(this, true) { _, _ ->
            reportUpdated()
        }

        viewModel.selectedLocations.observe(this, Observer<List<String>?> {
            rebuildPagerAdapter()
        })

        viewModel.reportStatus.observe(this, true) { _, status ->
            when (status) {
                Report.ReportStatus.REPORT_UPDATED -> reportUpdated()

                Report.ReportStatus.REPORT_UPDATING -> {
                    snackbar = Snackbar.make(
                        addLocationButton,
                        getString(R.string.activity_main_updating_message),
                        Snackbar.LENGTH_INDEFINITE
                    ).apply {
                        view.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.snackbar_background
                            )
                        )
                        show()
                    }
                }
                else -> snackbar?.dismiss()
            }
        }

        addLocationButton.setOnClickListener {
            AddLocationActivity.launch(this, true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onDestroy() {
        visualLoader.value = null
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_main_manage_locations -> {
            ManageLocationsActivity.launch(this)
            true
        }
        R.id.menu_main_send_feedback -> {
            val webpage = Uri.parse("https://forms.office.com/Pages/ResponsePage.aspx?" +
                "id=XE3BsSU2s0WkMJVSNzoML3RNFolnWOBCiL6xGKcJQJtUREVMSE1HQ1hLRjZZSUw4SU1SVVBKTEsx" +
                "WCQlQCN0PWcu")
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
            true
        }

        R.id.menu_main_about -> {
            lifecycleScope.launch(Dispatchers.Main) {
                visualLoader.value?.getTitleAndPayloadForLegal(this@MainActivity)?.also { payload ->
                    showExpanded(payload.view, getString(R.string.menu_main_about), null,
                        true)
                }
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /*
     * MainActivity methods
     */

    fun showExpanded(view: View?, title: String?, subtitle: String?, showSasLogo: Boolean = false) {
        expandedVisualContent.removeAllViews()
        expandedVisualContainer.setVisibleOrGone(view != null)
        if (view != null) {
            expandedVisualContent.addView(view)
            expandedToolbar.title = title
            expandedToolbar.subtitle = subtitle
            expandedSasLogo.setVisibleOrGone(showSasLogo)
        }
    }

    /*
     * Private methods
     */

    private fun rebuildPagerAdapter() {
        // Save as it will get overwritten by new PagerAdapter
        val curIndex = viewModel.curIndex.value
        viewPager.adapter = PagerAdapter(this, selectedLocations)
        viewPager.setCurrentItem(curIndex ?: 0, false)

        // Page indicator overlay
        if (!::tabLayoutMediator.isInitialized) {
            tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { _, _ -> }
        } else {
            tabLayoutMediator.detach()
        }
        tabLayoutMediator.attach()

        tabLayout.setVisibleOrGone(selectedLocations.size > 1)
    }

    private fun reportUpdated() {
        visualLoader.value = viewModel.report.value?.let {
            val repObjProvider = (application as MainApplication).sasManager
                .getReportObjectProvider(it, this, this)
            VisualLoader(repObjProvider)
        }
        rebuildPagerAdapter()
    }

    private fun setUpLocation() {
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
                location ->
            location?.also {
                val countryName = Geocoder(this,
                    Locale.getDefault()).getFromLocation(location.latitude, location.longitude,
                    1).getOrNull(0)?.countryName
                logV("Got \"$countryName\" from location services")
                viewModel.localLocation.value = countryName
            }
        }
    }
}
