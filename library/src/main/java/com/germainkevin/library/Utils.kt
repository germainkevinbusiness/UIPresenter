package com.germainkevin.library

import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewAnimationUtils
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.math.hypot


// CoroutineScope always launched on a Main Dispatcher
class MainScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}

// Lambda function to launch a coroutine on a Main Dispatcher
fun mainThread(block: suspend MainScope.() -> Unit) {
    val mainScope = MainScope()
    with(mainScope) {
        launch {
            block.invoke(this@with)
        }
    }
}

fun setShadowLayer(paint: Paint, @ColorInt shadowLayerColor: Int) {
    paint.setShadowLayer(10f, 5f, 5f, shadowLayerColor)
}

/**
 * Gets the exact coordinate on the screen, of the view to present
 * */
fun calculateVTPBounds(viewToPresent: View): Pair<PointF, PointF> {
    val rect = Rect()
    viewToPresent.getGlobalVisibleRect(rect)
    val viewToPresentLeftTopPosition = PointF(rect.left.toFloat(), rect.top.toFloat())
    val viewToPresentRightBottomPosition = PointF(rect.right.toFloat(), rect.bottom.toFloat())
    return Pair(viewToPresentLeftTopPosition, viewToPresentRightBottomPosition)
}

fun calculatedTextSize(
    mDisplayMetrics: DisplayMetrics,
    mDefaultTextUnit: Int,
    mDefaultTextSize: Float
): Float = TypedValue.applyDimension(mDefaultTextUnit, mDefaultTextSize, mDisplayMetrics)

fun getTextHeight(text: String, paint: Paint): Float {
    val rect = Rect()
    paint.getTextBounds(text, 0, text.length, rect)
    return rect.height().toFloat()
}

/**
 * Creates a [ViewAnimationUtils.createCircularReveal] animation on a [View]
 * */
fun Presenter.circularReveal(duration: Long?) {
    val cx: Int = this.width / 2
    val cy: Int = this.height / 2
    val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
    val anim = ViewAnimationUtils.createCircularReveal(this, cx, cy, 0f, finalRadius)
    duration?.let { anim.duration = it }
    this.isVisible = true
    anim.start()
}