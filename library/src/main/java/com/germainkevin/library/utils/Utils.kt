package com.germainkevin.library.utils

import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import kotlinx.coroutines.*

// Lambda function to launch a coroutine on a Main Dispatcher
fun mainThread(block: suspend CoroutineScope.() -> Unit) {
    val mainScope = CoroutineScope(Dispatchers.Main)
    mainScope.launch { block.invoke(this) }
}

fun setShadowLayer(paint: Paint, @ColorInt shadowLayerColor: Int) {
    paint.setShadowLayer(10f, 5f, 5f, shadowLayerColor)
}

/**
 * Gets the exact coordinate on the screen of a View
 *
 * With this data we can do some positioning for the presenter shapes
 * */
fun View.getBounds(): RectF {
    val rect = Rect()
    val viewBounds = RectF() // Will get the position of the view to present
    this.getGlobalVisibleRect(rect)
    // We now have the exact left,top,right,bottom position of the view on the screen
    viewBounds.set(rect)
    return viewBounds
}

fun calculatedTextSize(
    mDisplayMetrics: DisplayMetrics,
    mDefaultTextUnit: Int,
    mDefaultTextSize: Float
): Float = TypedValue.applyDimension(mDefaultTextUnit, mDefaultTextSize, mDisplayMetrics)

//fun getTextHeight(text: String, paint: Paint): Float {
//    val rect = Rect()
//    paint.getTextBounds(text, 0, text.length, rect)
//    return rect.height().toFloat()
//}

fun buildStaticLayout(text: String, textPaint: TextPaint, textWidth: Int): StaticLayout {
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
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            StaticLayout.Builder.obtain(
                text,
                0,
                text.length,
                textPaint,
                textWidth
            )
                .setBreakStrategy(LineBreaker.BREAK_STRATEGY_BALANCED)
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