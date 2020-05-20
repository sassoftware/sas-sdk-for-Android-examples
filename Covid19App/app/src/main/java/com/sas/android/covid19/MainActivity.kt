package com.sas.android.covid19

import java.util.Locale

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.widget.ViewPager2

import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayoutMediator
import com.sas.android.covid19.add.AddRegionActivity
import com.sas.android.covid19.databinding.ActivityMainBinding
import com.sas.android.covid19.manage.ManageRegionsActivity
import com.sas.android.covid19.util.UiUtil
import com.sas.android.covid19.util.VisualLoader
import com.sas.android.covid19.util.logV
import com.sas.android.covid19.util.observe
import com.sas.android.covid19.util.setVisibleOrGone
import com.sas.android.covid19.util.toLocalizedRegion
import com.sas.android.visualanalytics.sdk.model.Report

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    /*
     * Properties/init
     */

    val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this, MainViewModelFactory(application as MainApplication))
            .get(MainViewModel::class.java)
    }

    val visualLoader = MutableLiveData<VisualLoader>().apply {
        observe(null) { oldValue, newValue ->
            if (oldValue != newValue) {
                // XXX Remove from try/catch
                try {
                    oldValue?.unload()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

                GlobalScope.launch(Dispatchers.Main) {
                    viewModel.allRegions.value = newValue?.getAllRegions()
                }
            }
        }
    }

    lateinit var tabLayoutMediator: TabLayoutMediator

    private val selectedRegions
        get() = viewModel.selectedRegions.value!!

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
            title = getString(R.string.app_name_full)
        }

        setUpLocation()

        expandedToolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    viewModel.selectedIndex.value = position
                }
            }
        )

        viewModel.selectedIndex.observe(this, Observer<Int?> { selected ->
            selected?.also {
                collapsing_toolbar.title =
                    selectedRegions.getOrNull(selected)?.toLocalizedRegion(this)
                supportActionBar?.subtitle = null
                addRegionButton.show()
            }
        })

        viewModel.report.observe(this, Observer<Report?> { report ->
            visualLoader.value = report?.let {
                val repObjProvider = (application as MainApplication).sasManager
                    .getReportObjectProvider(report, this, this)
                VisualLoader(repObjProvider)
            }

            rebuildPagerAdapter()
        })

        viewModel.selectedRegions.observe(this, Observer<List<String>?> {
            rebuildPagerAdapter()
        })

        addRegionButton.setOnClickListener {
            AddRegionActivity.launch(this, true)
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
            ManageRegionsActivity.launch(this)
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
            GlobalScope.launch(Dispatchers.Main) {
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
        val selectedIndex = viewModel.selectedIndex.value
        viewPager.adapter = PagerAdapter(this, selectedRegions)
        viewPager.setCurrentItem(selectedIndex ?: 0, false)

        // Page indicator overlay
        if (!::tabLayoutMediator.isInitialized) {
            tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { _, _ -> }
        } else {
            tabLayoutMediator.detach()
        }
        tabLayoutMediator.attach()

        tabLayout.setVisibleOrGone(selectedRegions.size > 1)
    }

    private fun setUpLocation() {
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
                location ->
            location?.also {
                val countryName = Geocoder(this,
                    Locale.getDefault()).getFromLocation(location.latitude, location.longitude,
                    1).getOrNull(0)?.countryName
                logV("Got \"$countryName\" from location services")
                viewModel.localRegion.value = countryName
            }
        }
    }
}
