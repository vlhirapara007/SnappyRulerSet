package com.snappyrulerset.util

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt

class DpiCalibration(private val context: Context) {

    private val displayMetrics: DisplayMetrics = context.resources.displayMetrics

    // Standard assumption: 160 DPI = 1dp ≈ 1px
    private val standardDpi = 160f

    fun dpToPx(dp: Float): Float {
        return dp * displayMetrics.density
    }

    fun pxToDp(px: Float): Float {
        return px / displayMetrics.density
    }

    fun mmToPx(mm: Float): Float {
        // 1 inch = 25.4mm
        val dpi = displayMetrics.densityDpi
        return (mm / 25.4f * dpi)
    }

    fun pxToMm(px: Float): Float {
        val dpi = displayMetrics.densityDpi
        return (px * 25.4f / dpi)
    }

    fun cmToPx(cm: Float): Float {
        return mmToPx(cm * 10f)
    }

    fun pxToCm(px: Float): Float {
        return pxToMm(px) / 10f
    }

    fun formatLength(px: Float): String {
        val cm = pxToCm(px)
        return String.format("%.1f cm", cm)
    }

    fun formatAngle(degrees: Float): String {
        return String.format("%.1f°", degrees)
    }

    fun getGridSizeInPx(gridSizeMm: Float = 5f): Float {
        return mmToPx(gridSizeMm)
    }
}
