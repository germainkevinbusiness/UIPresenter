package com.germainkevin.library.prototypes

import android.graphics.*
import android.text.TextPaint
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.ColorInt
import com.germainkevin.library.prototype_impl.presentation_shapes.SquircleShape

/**
 * A [PresenterShape] represents the shape in which a [com.germainkevin.library.Presenter] is drawn
 * @author Kevin Germain
 */
abstract class PresenterShape : ShapeLifecycle {

    /**
     * The shadowed window, is just a [Rect] that's the same size as the decorView and is given
     * a black shadowed color.
     *
     * It is drawn on top of a clipped out view to present on the canvas
     * in the [PresenterShape.onDrawInPresenterWith] method in [SquircleShape]
     *
     * To make it the same size as the decorView, you should do that in the implemented
     * [PresenterShape.buildSelfWith] like so:
     *
     * val rect = Rect()
     * builder.resourceFinder.getDecorView()!!.getGlobalVisibleRect(rect)
     * shadowedWindow.set(rect) // takes the coordinates of the decorView
     * */
    protected var shadowedWindow = Rect()

    /**
     * The color of the [shadowedWindow] which is a [Rect]
     * */
    protected var shadowedWindowPaint = Paint()

    /**
     * Checks [com.germainkevin.library.UIPresenter.mPresenterHasShadowedWindow] to
     * know whether the developer wants a [shadowedWindow] drawn on the canvas through the
     * [PresenterShape.onDrawInPresenterWith] method
     *
     * It is true by default in the [com.germainkevin.library.UIPresenter]
     * */
    protected var hasShadowedWindow = true

    /**
     * A [TextPaint] for the description text that will be drawn by a [PresenterShape]
     */
    protected var mDescriptionTextPaint: TextPaint = TextPaint()

    init {
        mDescriptionTextPaint.isAntiAlias = true
        shadowedWindowPaint.isAntiAlias = true
        shadowedWindowPaint.style = Paint.Style.FILL
    }

    /**
     * Sets the color of the [shadowedWindow], it is made Shadowed in black by default
     * with the "Color.parseColor("#80000000")" color
     * */
    open fun setShadowedWindowColor(@ColorInt color: Int) {
        shadowedWindowPaint.color = color
    }

    /**
     * @param color the background color for the [PresenterShape]
     */
    open fun setShapeBackgroundColor(@ColorInt color: Int) {}

    /**
     * @param textColor the background color for the [presenter's][PresenterShape] description text
     */
    open fun setDescriptionTextColor(@ColorInt textColor: Int) {
        mDescriptionTextPaint.color = textColor
    }

    /**
     * Sets the text size of the description text
     *
     * @param textSize       The desired text size wanted for the description text
     * @param typedValueUnit the unit in which the description text should be displayed,
     * e.g. [android.util.TypedValue.COMPLEX_UNIT_SP]
     * @param displayMetrics Necessary information to calculate the accurate TextSize
     */
    open fun setDescriptionTextSize(
        typedValueUnit: Int,
        textSize: Float,
        displayMetrics: DisplayMetrics
    ) {
        mDescriptionTextPaint.textSize =
            TypedValue.applyDimension(typedValueUnit, textSize, displayMetrics)
    }

    /**
     * Sets the [Typeface] for the description text
     */
    open fun setDescriptionTypeface(typeface: Typeface?) {
        mDescriptionTextPaint.typeface = typeface
    }

    /**
     * Helps to know if a click occurred on this [shape][PresenterShape]
     * by capturing the coordinates where the click event fired, and comparing it to
     * coordinates present in this [shape][PresenterShape]'s drawn bounds.
     *
     * Does the [shape][PresenterShape] contain the point.
     *
     * @param x x coordinate.
     * @param y y coordinate.
     * @return True if the [shape][PresenterShape] contains the point, false otherwise.
     */
    open fun shapeContains(x: Float, y: Float): Boolean = false

    /**
     * Helps to know if a click occurred on the
     * [view to present][android.view.View] by capturing the
     * coordinates where the click event fired, and comparing it to
     * coordinates present in this [view to present][android.view.View]'s drawn bounds.
     *
     * Does the [view to present][android.view.View] contain the point.
     *
     * @param x x coordinate.
     * @param y y coordinate.
     * @return True if the [view to present][android.view.View] contains the point, false otherwise.
     */
    open fun viewToPresentContains(x: Float, y: Float): Boolean = false
}