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
        toVisual(repObjProvider, "ve88771", AsText(false), null, false),
        toVisual(repObjProvider, "ve85184", AsBitmap(true, 5f), null, false),
        toVisual(repObjProvider, "ve85197", AsBitmap(false, 1f), null, true)
    ).filterNotNull()

    private val country = listOf(
        toVisual(repObjProvider, "ve85456", AsBitmap(true, 8f), null, false),
        toVisual(repObjProvider, "ve85465", AsBitmap(true, 5f), null, false),
        toVisual(repObjProvider, "ve85483", AsBitmap(true, 5f), null, false),
        toVisual(repObjProvider, "ve85524", AsBitmap(false, 1f), null, true),
        toVisual(repObjProvider, "ve88783", AsText(false), null, false),
        toVisual(repObjProvider, "ve85495", AsBitmap(true, 5f), null, false),
        toVisual(repObjProvider, "ve85528", AsBitmap(false, 1f), null, true)
    ).filterNotNull()

    private val legal = toVisual(repObjProvider, "ve85839", AsView(false), null, false)

    private val filter = repObjProvider.loadReportObjects("ve87003").firstOrNull() as?
        ReportObject.CategoricalFilter

    private val artifactCache = mutableMapOf<String, Any?>()
    private val artifactMutex = Mutex()
    private var latestLocation = LOCATION_WORLDWIDE

    /*
     * VisualLoader methods
     */

    suspend fun getAllLocations() = listOf(LOCATION_WORLDWIDE) + filter?.getUniqueValues().orEmpty()

    suspend fun getTitleAndPayload(
        context: Context,
        location: String,
        index: Int
    ) = getTitleAndPayload(context, location, getVisualList(location)[index])

    suspend fun getTitleAndPayloadForLegal(context: Context) = legal?.let {
        getTitleAndPayload(context, LOCATION_WORLDWIDE, it)
    }

    fun getVisualCount(location: String) = getVisualList(location).size

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
        artifactCache.clear()
    }

    private suspend fun getTitleAndPayload(
        context: Context,
        location: String,
        visual: Visual
    ): Payload {
        val title = visual.titleRes?.let {
            context.getString(it)
        } ?: visual.obj.title

        val setFilter = suspend {
            if (location != LOCATION_WORLDWIDE) {
                filter?.setSelectedValue(location)
                latestLocation = location
            }
        }

        val onExpand = if (visual.expandOnClick) setFilter else null

        return when (val method = visual.method) {
            is AsView -> Payload.WithView(title, method.titleIsInner, visual.obj.view, null)

            else -> {
                val key = "${visual.id}:$location"

                // Avoid the lock if possible
                val artifact = if (key in artifactCache) {
                    artifactCache[key]
                } else {
                    // Mutex ensures images are loaded serially
                    artifactMutex.withLock {
                        // Make another attempt now that we have the lock
                        if (key in artifactCache) {
                            artifactCache[key]
                        } else {
                            if (location != latestLocation) {
                                setFilter()
                            }

                            @Suppress("IMPLICIT_CAST_TO_ANY")
                            when (method) {
                                is AsBitmap -> {
                                    val ratio = method.ratio
                                    val width = Dimensions.dpToPx(300)
                                    val height = (width / ratio).toInt()
                                    (visual.obj as ReportObject.Visual).renderAsBitmap(
                                        width, height)
                                }
                                is AsText -> {
                                    (visual.obj as ReportObject.Text).getTextContent()
                                }
                                else -> {
                                    error("unreachable")
                                }
                            }.also { artifact ->
                                artifactCache[key] = artifact
                            }
                        }
                    }
                }

                when (method) {
                    is AsBitmap -> {
                        Payload.WithBitmap(title, method.titleIsInner, visual.obj.view, onExpand,
                            artifact as Bitmap?)
                    }
                    is AsText -> {
                        val text = artifact as CharSequence?
                        logV("  text: $text")
                        Payload.WithText(title, method.titleIsInner, visual.obj.view, onExpand,
                            text)
                    }
                    else -> {
                        error("unreachable")
                    }
                }
            }
        }
    }

    private fun getVisualList(location: String) = if (location.isWorldwide) worldwide else country

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
