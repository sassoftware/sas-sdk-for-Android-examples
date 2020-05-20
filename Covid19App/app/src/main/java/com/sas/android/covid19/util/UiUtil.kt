package com.sas.android.covid19.util

import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

object UiUtil {
    /**
     * Get the drawable and tint it.
     *
     * @param context
     *      the Context
     *
     * @param drawableId
     *      the resource id for the drawable
     *
     * @param tint
     *      the color id to tint the drawable
     *
     * @return
     *      a tinted drawable
     */
    @JvmStatic
    fun getDrawable(
        context: Context,
        @DrawableRes drawableId: Int,
        @ColorRes tint: Int
    ) = ContextCompat.getDrawable(context, drawableId)?.also {
        it.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(context, tint), PorterDuff.Mode.SRC_ATOP)
    }

    /**
     * Hide the soft keyboard. Should only be called in unusual circumstances
     * where the keyboard is not being hidden automatically.
     */
    @JvmStatic
    fun hideKeyboard(activity: Activity) {
        hideKeyboard(activity.contentView)
    }

    /**
     * Hide the soft keyboard. Should only be called in unusual circumstances
     * where the keyboard is not being hidden automatically.
     */
    @JvmStatic
    fun hideKeyboard(view: View) {
        (view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view.windowToken, 0)
    }
}
