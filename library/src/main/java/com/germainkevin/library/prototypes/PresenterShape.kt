package com.germainkevin.library.prototypes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.DisplayMetrics
import androidx.annotation.ColorInt
import com.germainkevin.library.presenter_view.Presenter
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Deferred

/**
 * A [PresenterShape] represents the shape drawn
 * by the [Canvas] of the [com.germainkevin.library.presenter_view.Presenter]
 */
interface PresenterShape : ShapeLifecycle {

    /**
     * @param color the background color for the [PresenterShape]
     */
    fun setBackgroundColor(@ColorInt color: Int)

    /**
     * @param textColor the background color for the [presenter's][PresenterShape] description text
     */
    fun setDescriptionTextColor(@ColorInt textColor: Int)

    /**
     * Sets the text size of the description text
     *
     * @param textSize       The desired text size wanted for the description text
     * @param typedValueUnit the unit in which the description text should be displayed,
     * e.g. [android.util.TypedValue.COMPLEX_UNIT_SP]
     * @param displayMetrics Necessary information to calculate the accurate TextSize
     */
    fun setDescriptionTextSize(typedValueUnit: Int, textSize: Float, displayMetrics: DisplayMetrics)

    /**
     * Sets the [Typeface] for the description text
     */
    fun setDescriptionTypeface(typeface: Typeface?)

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
    fun shapeContains(x: Float, y: Float): Boolean

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
    fun viewToPresentContains(x: Float, y: Float): Boolean
}