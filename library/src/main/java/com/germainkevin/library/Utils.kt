package com.germainkevin.library

import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import kotlinx.coroutines.*


/**
 * Class used to store information that helps set a shadow layer
 * inside a [com.germainkevin.library.prototypes.PresenterShape]
 * @param radius The radius of the shadow layer
 * (If radius is 0, then the shadow layer is removed).
 * @param dx The shadow layer dx position
 * @param dy The shadow layer dy position
 * @param shadowColor The color of the shadow layer
 * @author Kevin Germain
 * */
class PresenterShadowLayer(
    val radius: Float = 8f,
    val dx: Float = 0f,
    val dy: Float = 1f,
    val shadowColor: Int = Color.DKGRAY
)

internal fun buildStaticLayout(text: String, textPaint: TextPaint, textWidth: Int): StaticLayout {
    return StaticLayout.Builder.obtain(
        text,
        0,
        text.length,
        textPaint,
        textWidth
    )
        .build()
}