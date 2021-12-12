package com.germainkevin.library.utils

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
 * */
class PresenterShadowLayer(
    val radius: Float = 8f,
    val dx: Float = 0f,
    val dy: Float = 1f,
    val shadowColor: Int = Color.DKGRAY
)

// Lambda function to launch a coroutine on a Main Dispatcher
internal fun mainThread(block: suspend CoroutineScope.() -> Unit) {
    val mainScope = CoroutineScope(Dispatchers.Main)
    mainScope.launch { block.invoke(this) }
}

/**
 * Gets the exact coordinates of a View on the decor view
 * With this data we can do some positioning for the presenter shapes
 * */
internal fun View.getBounds(): RectF {
    val rect = Rect()
    val viewBounds = RectF() // Will get the position of the view to present
    this.getGlobalVisibleRect(rect)
    // We now have the exact left,top,right,bottom position of the view on the screen
    viewBounds.set(rect)
    return viewBounds
}

internal fun buildStaticLayout(text: String, textPaint: TextPaint, textWidth: Int): StaticLayout {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
            StaticLayout.Builder.obtain(
                text,
                0,
                text.length,
                textPaint,
                textWidth
            )
                .build()
        }
        else -> {
            StaticLayout(
                text,
                0,
                textWidth,
                textPaint,
                text.length,
                Layout.Alignment.ALIGN_NORMAL,
                1f,
                1f,
                false
            )
        }
    }
}