package com.germainkevin.library.prototypes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.ColorInt
import com.germainkevin.library.presenter_view.Presenter
import com.germainkevin.library.prototype_impl.presentation_shapes.SquircleShape
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Deferred

/**
 * A [PresenterShape] represents the shape in which a
 * [com.germainkevin.library.presenter_view.Presenter] is
 * @author Kevin Germain
 */
abstract class PresenterShape : ShapeLifecycle {

    /**
     * A [TextPaint] for the description text that will be drawn by a [PresenterShape]
     */
    protected var mDescriptionTextPaint: TextPaint = TextPaint()

    init {
        mDescriptionTextPaint.isAntiAlias = true
    }

    /**
     * @param color the background color for the [PresenterShape]
     */
    open fun setBackgroundColor(@ColorInt color: Int) {}

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