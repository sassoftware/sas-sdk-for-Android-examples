package com.sas.android.covid19.add

import kotlin.properties.Delegates.observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import com.sas.android.covid19.MainApplication
import com.sas.android.covid19.R
import com.sas.android.covid19.util.UiUtil

import kotlinx.android.synthetic.main.activity_add.*

class AddLocationActivity : AppCompatActivity() {
    /*
     * Properties/init
     */

    val viewModel by lazy {
        ViewModelProvider(this, AddLocationViewModelFactory(application as MainApplication))
            .get(AddLocationViewModel::class.java)
    }

    private var selectedLocations
        get() = viewModel.selectedLocations.value!!
        set(value) {
            viewModel.selectedLocations.value = value
        }

    private lateinit var menu: Menu

    private val INIT_RESUMED = 1
    private val INIT_SEARCH_VIEW_CONFIGURED = 2

    /**
     * Await necessary states before starting add mode.
     */
    private var initState by observable(0) { _, _, newValue ->
        if (newValue and INIT_RESUMED != 0 && newValue and INIT_SEARCH_VIEW_CONFIGURED != 0) {
            startAddMode()
        }
    }

    /*
     * Activity methods
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_location, menu)

        this.menu = menu

        lifecycleScope.launch(Dispatchers.Main) {
            configureSearchView()
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        initState = initState or INIT_RESUMED
    }

    /*
     * Private methods
     */

    private suspend fun configureSearchView() {
        val searchItem = menu.findItem(R.id.menu_add_location_search)

        // Allow this to only be triggered programmatically
        searchItem.setVisible(false)

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem) = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                finish()
                return true
            }
        })

        val searchView = searchItem?.actionView as SearchView

        // Hackishly reduce overly-large left margin
        (searchView.findViewById<View>(R.id.search_edit_frame)
            ?.layoutParams as? MarginLayoutParams)?.leftMargin = 0

        // Offer suggestions immediately
        searchView.findViewById<AutoCompleteTextView>(R.id.search_src_text)?.threshold = 0

        searchView.queryHint = getString(R.string.menu_add_location_search_hint)

        val from = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
        val to = intArrayOf(R.id.textView)
        val cursorAdapter = SimpleCursorAdapter(this, R.layout.delegate_location_search, null,
            from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
        val suggestions = viewModel.suggestions.value!!

        fun addLocation(query: String?) {
            query?.trim()?.also {
                // Exact match?
                var match = suggestions.find { (_, localized) ->
                    localized.equals(query, true)
                }

                if (match == null) {
                    val matches = suggestions.filter { (_, localized) ->
                        localized.contains(query, true)
                    }
                    // 1 partial match?
                    if (matches.size == 1) {
                        match = matches[0]
                    }
                }

                if (match != null) {
                    if (match.first !in selectedLocations) {
                        selectedLocations += match.first

                        if (intent.getBooleanExtra(EXTRA_SET_SELECTED_INDEX_ON_ADD, false)) {
                            viewModel.curIndex.value = selectedLocations.size - 1
                        }
                    }
                    searchView.setIconified(true)
                    searchItem.collapseActionView()
                }
            }
        }

        searchView.suggestionsAdapter = cursorAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                UiUtil.hideKeyboard(this@AddLocationActivity)
                addLocation(query)
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                val cursor = MatrixCursor(arrayOf(BaseColumns._ID,
                    SearchManager.SUGGEST_COLUMN_TEXT_1))
                query?.let {
                    suggestions.forEachIndexed { i, suggestion ->
                        val localized = suggestion.second
                        if (localized.contains(query, true)) {
                            cursor.addRow(arrayOf(i, localized))
                        }
                    }
                }

                cursorAdapter.changeCursor(cursor)
                return true
            }
        })

        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                UiUtil.hideKeyboard(this@AddLocationActivity)
                val cursor = searchView.suggestionsAdapter.getItem(position) as Cursor
                val selection = cursor.getString(cursor.getColumnIndex(
                    SearchManager.SUGGEST_COLUMN_TEXT_1))
                addLocation(selection)
                return true
            }
        })

        initState = initState or INIT_SEARCH_VIEW_CONFIGURED
    }

    private fun startAddMode() {
        menu.performIdentifierAction(R.id.menu_add_location_search, 0)
    }

    /*
     * Companion
     */

    companion object {
        /*
         * Properties/init
         */

        const val EXTRA_SET_SELECTED_INDEX_ON_ADD: String = "EXTRA_SET_SELECTED_INDEX_ON_ADD"

        /*
         * Companion methods
         */

        fun launch(activity: Activity, setSelectedIndexOnAdd: Boolean) =
            Intent(activity, AddLocationActivity::class.java).also {
                it.putExtra(EXTRA_SET_SELECTED_INDEX_ON_ADD, setSelectedIndexOnAdd)
                activity.startActivity(it)
            }
    }
}
