package com.sas.android.covid19

import android.content.Intent
import android.graphics.Color
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
import androidx.lifecycle.MediatorLiveData
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
import com.sas.android.covid19.util.LOCATION_WORLDWIDE
import com.sas.android.covid19.util.VisualLoader
import com.sas.android.covid19.util.logV
import com.sas.android.covid19.util.observe
import com.sas.android.covid19.util.setVisibleOrGone
import com.sas.android.covid19.util.toLocalizedLocation
import com.sas.android.visualanalytics.sdk.model.Report
import com.sas.android.visualanalytics.sdk.report.ReportObject
import com.sas.android.visualanalytics.sdk.report.ReportObjectProvider
import java.util.Locale
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    /*
     * Properties/init
     */

    val viewModel by lazy {
        ViewModelProvider(this, MainViewModelFactory(application as MainApplication))
            .get(MainViewModel::class.java)
    }

    private val reportObjectProvider by lazy {
        MediatorLiveData<ReportObjectProvider?>().apply {
            // Set viewModel.defaultLocations whenever we get a new ReportObjectProvider
            observe(null, true) { _, newValue ->
                (
                    newValue?.loadReportObjects("ve88574")?.firstOrNull() as?
                        ReportObject.CategoricalFilter
                    )?.also { filter ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        viewModel.defaultLocations.value = listOf(LOCATION_WORLDWIDE) +
                            filter.getUniqueValues()
                    }
                }
            }

            fun recalculate() = viewModel.report.value?.let { report ->
                viewModel.reportStatus.value?.let { status ->
                    when (status) {
                        Report.ReportStatus.REPORT_UNSUBSCRIBED,
                        Report.ReportStatus.REPORT_UNAVAILABLE -> null
                        else -> (application as MainApplication).sasManager.getReportObjectProvider(
                            report, this@MainActivity, this@MainActivity
                        )
                    }
                }
            }

            addSource(viewModel.report) { _ ->
                value = recalculate()
            }

            addSource(viewModel.reportStatus) { newValue ->
                when (newValue) {
                    Report.ReportStatus.REPORT_UNSUBSCRIBED,
                    Report.ReportStatus.REPORT_UNAVAILABLE,
                    Report.ReportStatus.REPORT_UPDATED -> value = recalculate()
                    else -> {}
                }
            }
        }
    }

    val visualLoader by lazy {
        MediatorLiveData<VisualLoader?>().apply {
            // Set viewModel.allLocations whenever we create a new VisualLoader
            observe(null, true) { _, newValue ->
                lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.allLocations.value = newValue?.getAllLocations()
                }
            }

            fun recalculate() = reportObjectProvider.value?.let {
                VisualLoader(it)
            }

            addSource(reportObjectProvider) { _ ->
                value = recalculate()
            }
        }
    }

    private var snackbar: Snackbar? = null

    lateinit var tabLayoutMediator: TabLayoutMediator

    private val selectedLocations
        get() = viewModel.selectedLocations.value

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
                        val locations = selectedLocations!!

                        // Don't notify observers unnecessarily
                        if (fromIndex == null) {
                            fromIndex = viewModel.curIndex.value ?: 0
                            viewModel.fromLocation.value = locations.getOrNull(fromIndex!!)
                        }

                        var fromIndex = this.fromIndex!!

                        val toLocation = when (position) {
                            fromIndex -> locations.getOrNull(fromIndex + 1)
                            fromIndex - 1 -> locations.getOrNull(fromIndex - 1)
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

        viewModel.curIndex.observe(
            this,
            Observer<Int?> { curIndex ->
                curIndex?.also {
                    collapsing_toolbar.title =
                        selectedLocations?.getOrNull(curIndex)?.toLocalizedLocation(this)
                    supportActionBar?.subtitle = null
                    addLocationButton.show()
                }
            }
        )

        visualLoader.observe(this, true) { _, _ ->
            rebuildPagerAdapter()
        }

        viewModel.selectedLocations.observe(
            this,
            Observer<List<String>?> {
                rebuildPagerAdapter()
            }
        )

        viewModel.reportStatus.observe(this, true) { _, status ->
            if (status == Report.ReportStatus.REPORT_UPDATING) {
                Snackbar.make(
                    addLocationButton,
                    getString(R.string.activity_main_updating_message),
                    Snackbar.LENGTH_LONG
                ).apply {
                    setTextColor(Color.WHITE)
                    setBackgroundTint(
                        ContextCompat.getColor(context, R.color.snackbar_background)
                    )
                    show()
                }
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
        reportObjectProvider.value = null
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_main_manage_locations -> {
            ManageLocationsActivity.launch(this)
            true
        }
        R.id.menu_main_send_feedback -> {
            val url = "https://forms.office.com/Pages/ResponsePage.aspx?" +
                "id=XE3BsSU2s0WkMJVSNzoML3RNFolnWOBCiL6xGKcJQJtUREVMSE1HQ1hLRjZZSUw4SU1SVVBKTEsx" +
                "WCQlQCN0PWcu"
            val webpage = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
            true
        }

        R.id.menu_main_about -> {
            lifecycleScope.launch(Dispatchers.Main) {
                visualLoader.value?.getTitleAndPayloadForLegal(this@MainActivity)?.also { payload ->
                    showExpanded(
                        payload.view, getString(R.string.menu_main_about), null,
                        true
                    )
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

        tabLayout.setVisibleOrGone(selectedLocations?.size ?: 0 > 1)
    }

    private fun setUpLocation() {
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
            location ->
            location?.also {
                val countryName = Geocoder(this, Locale.getDefault())
                    .getFromLocation(location.latitude, location.longitude, 1)
                    .getOrNull(0)?.countryName
                logV("Got \"$countryName\" from location services")
                viewModel.localLocation.value = countryName
            }
        }
    }
}
