package com.sas.android.covid19

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sas.android.covid19.util.VisualLoader
import com.sas.android.covid19.util.VisualLoader.Payload
import com.sas.android.covid19.util.isWorldwide
import com.sas.android.covid19.util.logV
import com.sas.android.covid19.util.setImageBitmapOrGone
import com.sas.android.covid19.util.setTextOrGone
import com.sas.android.covid19.util.setVisibleOrGone
import com.sas.android.covid19.util.toLocalizedLocation
import kotlinx.android.synthetic.main.delegate_visual.view.*
import kotlinx.android.synthetic.main.fragment_page.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PageFragment : Fragment() {
    /*
     * Properties/init
     */

    private val mainActivity
        get() = activity as MainActivity

    private val location
        get() = arguments!!.getString(ARG_LOCATION)!!

    /*
     * Fragment methods
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide/show FAB on scroll
        scroll.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            val dy = scrollY - oldScrollY
            val fab = mainActivity.findViewById<FloatingActionButton>(R.id.addLocationButton)
            if (dy > 0) {
                fab.hide()
            } else if (dy < -10) {
                fab.show()
            }
        }

        mainActivity.visualLoader.observe(
            this,
            Observer<VisualLoader?> { _ ->
                relayout()
            }
        )

        // Country pages only
        if (!location.isWorldwide) {
            mainActivity.viewModel.apply {
                // Synchronize scroll positions between pages
                fromLocation.observe(
                    this@PageFragment,
                    Observer<String?> { fromLocation ->
                        if (fromLocation == location) {
                            val pos = scroll.scrollY
                            mainActivity.viewModel.fromLocationOffset.value = pos
                            logV("$location is scrolled to: $pos", tag = PageFragment::class)
                        }
                    }
                )
                fromLocationOffset.observe(
                    this@PageFragment,
                    Observer<Int?> { _ ->
                        syncScrollIfAppropriate()
                    }
                )
                toLocation.observe(
                    this@PageFragment,
                    Observer<String?> { _ ->
                        syncScrollIfAppropriate()
                    }
                )

                // Synchronize heights between pages so we can scroll to the same offset even if
                // the visuals haven't yet loaded
                content.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
                    val height = bottom - top
                    if (height > pageMinHeight.value ?: 0) {
                        pageMinHeight.value = height
                    }
                }
                pageMinHeight.observe(
                    this@PageFragment,
                    Observer<Int?> { v ->
                        if (v != null && v > content.minimumHeight) {
                            logV("$location: setting minimumHeight to $v")
                            content.minimumHeight = v
                        }
                    }
                )
            }
        }
    }

    /*
     * Private methods
     */

    private fun relayout() {
        viewLifecycleOwner.lifecycleScope.async(Dispatchers.Main) {
            content.removeAllViews()

            // Gradually fade in spinner
            spinner.alpha = 0f
            ObjectAnimator.ofFloat(spinner, "alpha", 0f, 1f).apply {
                startDelay = 500
                duration = 3000
                start()
            }

            spinner.setVisibleOrGone(true)

            val visualLoader = mainActivity.visualLoader
            val visualCount = visualLoader.value?.getVisualCount(location) ?: 0
            for (i in 0 until visualCount) {
                val visual = layoutInflater.inflate(R.layout.delegate_visual, content, false)

                val payload = visualLoader.value?.getTitleAndPayload(mainActivity, location, i)
                var outerTitle: String? = null
                var innerTitle: String? = null
                var bitmap: Bitmap? = null
                var text: CharSequence? = null

                visual.objView.setVisibleOrGone(false)

                if (payload != null) {
                    if (payload.titleIsInner) {
                        innerTitle = payload.title
                    } else {
                        outerTitle = payload.title
                    }

                    when (payload) {
                        is Payload.WithBitmap -> {
                            bitmap = payload.bitmap
                        }
                        is Payload.WithText -> {
                            text = payload.text
                        }
                        is Payload.WithView -> {
                            visual.objView.addView(payload.view)
                            visual.objView.setVisibleOrGone(true)
                        }
                    }
                }

                visual.outerTitleView.setTextOrGone(outerTitle)
                visual.innerTitleView.setTextOrGone(innerTitle)
                visual.imageView.setImageBitmapOrGone(bitmap)
                visual.textView.setTextOrGone(text)

                visual.setOnClickListener {
                    payload?.onExpand?.also {
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                            it.invoke()
                            mainActivity.showExpanded(
                                payload.view,
                                location.toLocalizedLocation(mainActivity), null
                            )
                        }
                    }
                }

                content.addView(visual)
            }

            spinner.setVisibleOrGone(false)
        }
    }

    /**
     * If this page is about to be displayed, set its scroll position to match
     * that of the previously loaded page.
     */
    private fun syncScrollIfAppropriate() {
        if (mainActivity.viewModel.toLocation.value == location) {
            mainActivity.viewModel.fromLocationOffset.value?.also { pos ->
                logV("scrolling $location to: $pos", tag = PageFragment::class)
                scroll.scrollTo(0, pos)
            }
        }
    }

    /*
     * Companion
     */

    companion object {
        /*
         * Properties/init
         */

        private const val ARG_LOCATION: String = "ARG_LOCATION"

        /*
         * Companion methods
         */

        fun create(location: String) = PageFragment().apply {
            setArguments(
                Bundle().apply {
                    putString(ARG_LOCATION, location)
                }
            )
        }
    }
}
