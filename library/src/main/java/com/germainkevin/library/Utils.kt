package com.germainkevin.library

import android.graphics.Color
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint


const val TRANSPARENT_BLACK_COLOR = "#80000000"
const val DEFAULT_REVEAL_ANIMATION_DURATION = 1000L
const val DEFAULT_REMOVE_ANIMATION_DURATION = 600L

/**
 * Class used to store information that helps set a shadow layer in a
 * [PresenterShape][com.germainkevin.library.prototypes.PresenterShape]
 * @param radius The radius of the shadow layer (If radius is 0, then the shadow layer is removed).
 * @param dx The shadow layer dx position
 * @param dy The shadow layer dy position
 * @param shadowColor The color of the shadow layer
 * @author Kevin Germain
 * */
class ShadowLayer(
    val radius: Float = 8f,
    val dx: Float = 0f,
    val dy: Float = 1f,
    val shadowColor: Int = Color.DKGRAY
)

/**
 * This method creates a [StaticLayout]
 * @param text The text you want to be laid out in the [StaticLayout]
 * @param textPaint The [TextPaint] of the [text]
 * @param sLWidth The desired with you want for your [StaticLayout]
 * */
fun buildStaticLayout(text: String, textPaint: TextPaint, sLWidth: Int): StaticLayout =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        StaticLayout.Builder.obtain(text, 0, text.length, textPaint, sLWidth).build()
    else StaticLayout(text, textPaint, sLWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false)