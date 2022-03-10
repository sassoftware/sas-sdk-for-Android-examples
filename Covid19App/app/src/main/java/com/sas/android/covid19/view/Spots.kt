package com.sas.android.covid19.view

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.sas.android.covid19.util.Dimensions
import com.sas.android.covid19.util.getStyledAttributes
import com.sas.android.covid19.util.logV
import com.sas.android.covid19.util.onMeasured
import kotlin.properties.Delegates
import kotlin.random.Random

class Spots : View {
    /*
     * Constructors
     */

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) :
        super(context, attrs, defStyle) {
            init(attrs)
        }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context) : super(context) {
        init(null)
    }

    /*
     * Properties/init
     */

    private val spots = mutableListOf<Spot>()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var stopped = false

    /*
     * View methods
     */

    override fun onDraw(canvas: Canvas) {
        // Draw background
        super.onDraw(canvas)

        for (spot in spots) {
            canvas.drawOval(spot.rect, paint)
        }
    }

    /*
     * Private methods
     */

    private fun addRandomSpot() {
        val x = Random.nextFloat() * width
        val y = Random.nextFloat() * height

        spots += Spot(x, y).also {
            val startDiam = 0f
            val endDiam = Dimensions.dpToPx((20..350).random())
            ValueAnimator.ofObject(it, startDiam, endDiam).apply {
                duration = (2250L..4000L).random()
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }

    private fun addRandomSpots() {
        if (stopped) {
            logV("spots: ${spots.size}")
            return
        }

        for (j in (1..(1..7).random())) {
            addRandomSpot()
        }

        postDelayed({
            addRandomSpots()
        }, 200)
    }

    private fun init(attrs: AttributeSet?) {
        val foreground = attrs?.let {
            context.getStyledAttributes(attrs, android.R.attr.color) {
                getColor(0, Color.BLACK)
            }
        } ?: Color.BLACK

        paint.color = foreground

        onMeasured {
            startAnimations()
        }
    }

    private fun startAnimations() {
        addRandomSpots()

        postDelayed({
            stopped = true
        }, 9000)
    }

    /*
     * Private classes
     */

    private inner class Spot(val x: Float, val y: Float) : TypeEvaluator<Float> {
        /*
         * Properties/init
         */

        val rect = RectF()

        var diameter by Delegates.observable(0f) { _, _, newValue ->
            require(newValue >= 0)
            val radius = newValue / 2
            rect.left = x - radius
            rect.top = y - radius
            rect.right = x + radius
            rect.bottom = y + radius

            // Trigger redraw
            invalidate()
        }

        /*
         * TypeEvaluator methods
         */

        override fun evaluate(fraction: Float, startDiam: Float, endDiam: Float): Float {
            val (min, max) = arrayOf(startDiam, endDiam).also { it.sort() }
            diameter = min + fraction * (max - min)
            return diameter
        }
    }
}
