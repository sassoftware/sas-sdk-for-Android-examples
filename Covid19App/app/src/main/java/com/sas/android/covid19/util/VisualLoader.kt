package com.sas.android.covid19.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes

import com.sas.android.covid19.util.VisualLoader.RenderMethod.AsBitmap
import com.sas.android.covid19.util.VisualLoader.RenderMethod.AsText
import com.sas.android.covid19.util.VisualLoader.RenderMethod.AsView
import com.sas.android.visualanalytics.sdk.report.ReportObject
import com.sas.android.visualanalytics.sdk.report.ReportObjectProvider

class VisualLoader(val repObjProvider: ReportObjectProvider) {
    /*
     * Properties/init
     */

    private val worldwide = listOf(
        toVisual(repObjProvider, "ve85261", AsBitmap(true, 8f), null, false),
        toVisual(repObjProvider, "ve85159", AsBitmap(true, 5f), null, false),
        toVisual(repObjProvider, "ve85170", AsBitmap(true, 5f), null, false),
        toVisual(repObjProvider, "ve85253", AsText(false), null, false),
        toVisual(repObjProvider, "ve85240", AsBitmap(false, 1f), null, true),
        toVisual(repObjProvider, "ve85217", AsBitmap(true, 5f), null, false),
        toVisual(repObjProvider, "ve85184", AsBitmap(true, 5f), null, false),
        toVisual(repObjProvider, "ve85197", AsBitmap(false, 1f), null, true)
    ).filterNotNull()

    private val country = listOf(
        toVisual(repObjProvider, "ve85456", AsBitmap(true, 8f), null, false),
        toVisual(repObjProvider, "ve85465", AsBitmap(true, 5f), null, false),
        toVisual(repObjProvider, "ve85483", AsBitmap(true, 5f), null, false),
        toVisual(repObjProvider, "ve85524", AsBitmap(false, 1f), null, true),
        toVisual(repObjProvider, "ve85474", AsBitmap(true, 5f), null, false),
        toVisual(repObjProvider, "ve85495", AsBitmap(true, 5f), null, false),
        toVisual(repObjProvider, "ve85528", AsBitmap(false, 1f), null, true)
    ).filterNotNull()

    private val legal = toVisual(repObjProvider, "ve85839", AsView(false), null, false)

    private val filter = repObjProvider.loadReportObjects("ve87003").firstOrNull() as?
        ReportObject.CategoricalFilter

    private val bitmapCache = mutableMapOf<String, Bitmap?>()
    private val bitmapMutex = Mutex()
    private var latestRegion = REGION_WORLDWIDE

    /*
     * VisualLoader methods
     */

    suspend fun getAllRegions() = listOf(REGION_WORLDWIDE) + filter?.getUniqueValues().orEmpty()

    suspend fun getTitleAndPayload(
        context: Context,
        region: String,
        index: Int
    ) = getTitleAndPayload(context, region, getVisualList(region)[index])

    suspend fun getTitleAndPayloadForLegal(context: Context) = legal?.let {
        getTitleAndPayload(context, REGION_WORLDWIDE, it)
    }

    fun getVisualCount(region: String) = getVisualList(region).size

    fun unload() {
        (worldwide + country + legal).mapNotNull {
            it?.id
        }.also {
            repObjProvider.unloadReportObjects(*it.toTypedArray())
        }
    }

    /*
     * Private methods
     */

    private fun appendText(view: View, builder: StringBuilder, level: Int) {
        when (view) {
            is TextView -> {
                val text = view.text
                if (!text.isNullOrEmpty()) {
                    if (!builder.isEmpty()) {
                        builder.append(" ")
                    }
                    builder.append(text)
                }
            }
            is ViewGroup ->
                for (child in view.children) {
                    appendText(child, builder, level + 1)
                }
        }
    }

    private fun clearBitmapCache() {
        bitmapCache.clear()
    }

    private suspend fun getTitleAndPayload(
        context: Context,
        region: String,
        visual: Visual
    ): Payload {
        val title = visual.titleRes?.let {
            context.getString(it)
        } ?: visual.obj.title

        val setFilter = suspend {
            if (region != REGION_WORLDWIDE) {
                filter?.setSelectedValue(region)
                latestRegion = region
            }
        }

        val onExpand = if (visual.expandOnClick) setFilter else null

        return when (val method = visual.method) {
            is AsBitmap -> {
                val key = "${visual.id}:$region"

                // Avoid the lock if possible
                val bitmap = if (key in bitmapCache) {
                    bitmapCache[key]
                } else {
                    // Mutex ensures images are loaded serially
                    bitmapMutex.withLock {
                        // Make another attempt now that we have the lock
                        if (key in bitmapCache) {
                            bitmapCache[key]
                        } else {
                            if (region != latestRegion) {
                                setFilter()
                            }

                            val ratio = method.ratio
                            val width = Dimensions.dpToPx(300)
                            val height = (width / ratio).toInt()
                            (visual.obj as ReportObject.Visual).renderAsBitmap(width, height)
                                    .also { bitmap ->
                                bitmapCache[key] = bitmap
                            }
                        }
                    }
                }

                Payload.WithBitmap(title, method.titleIsInner, visual.obj.view, onExpand, bitmap)
            }
            is AsText -> {
                val text = (visual.obj as ReportObject.Text).getTextContent()
                Payload.WithText(title, method.titleIsInner, visual.obj.view, onExpand, text)
            }
            is AsView -> Payload.WithView(title, method.titleIsInner, visual.obj.view, null)
        }
    }

    private fun getVisualList(region: String) = if (region.isWorldwide) worldwide else country

    private fun toText(view: View?) = view?.let {
        val builder = StringBuilder()
        appendText(it, builder, 0)
        builder.toString()
    }

    private fun toVisual(
        repObjProvider: ReportObjectProvider,
        id: String,
        method: RenderMethod,
        @StringRes titleRes: Int? = null,
        expandOnClick: Boolean
    ) = repObjProvider.loadReportObjects(id).getOrNull(0).let { obj ->
        if (obj == null) {
            logE("unable to load object: $id")
            null
        } else {
            Visual(obj, id, method, titleRes, expandOnClick)
        }
    }

    /*
     * Classes
     */

    sealed class Payload(
        val title: String?,
        val titleIsInner: Boolean,
        val view: View?,
        val onExpand: (suspend () -> Unit)?
    ) {
        class WithBitmap(
            title: String?,
            titleIsInner: Boolean,
            view: View?,
            onExpand: (suspend () -> Unit)?,
            val bitmap: Bitmap?
        ) : Payload(title, titleIsInner, view, onExpand)

        class WithText(
            title: String?,
            titleIsInner: Boolean,
            view: View?,
            onExpand: (suspend () -> Unit)?,
            val text: CharSequence?
        ) : Payload(title, titleIsInner, view, onExpand)

        class WithView(
            title: String?,
            titleIsInner: Boolean,
            view: View?,
            onExpand: (suspend () -> Unit)?
        ) : Payload(title, titleIsInner, view, onExpand)
    }

    sealed class RenderMethod(val titleIsInner: Boolean) {
        class AsBitmap(titleIsInner: Boolean, val ratio: Float) : RenderMethod(titleIsInner)
        class AsText(titleIsInner: Boolean) : RenderMethod(titleIsInner)
        class AsView(titleIsInner: Boolean) : RenderMethod(titleIsInner)
    }

    class Visual(
        val obj: ReportObject,
        val id: String,
        val method: RenderMethod,
        @StringRes val titleRes: Int? = null,
        val expandOnClick: Boolean
    )
}
