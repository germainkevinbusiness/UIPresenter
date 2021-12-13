package com.germainkevin.library.prototype_impl.presentation_shapes

import android.graphics.Canvas
import android.graphics.Typeface
import android.util.DisplayMetrics
import com.germainkevin.library.mainThread
import com.germainkevin.library.prototype_impl.PresentationBuilder
import com.germainkevin.library.prototypes.PresenterShape
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

class PointerShape : PresenterShape {

    override lateinit var buildSelfJob: Deferred<Unit>

    override fun setBackgroundColor(color: Int) {
    }

    override fun setDescriptionTextColor(textColor: Int) {
    }

    override fun setDescriptionTextSize(
        typedValueUnit: Int,
        textSize: Float,
        displayMetrics: DisplayMetrics
    ) {
    }

    override fun setDescriptionTypeface(typeface: Typeface?) {
    }

    override fun shapeContains(x: Float, y: Float): Boolean {
        return false
    }

    override fun viewToPresentContains(x: Float, y: Float): Boolean {
        return false
    }

    override fun buildSelfWith(builder: PresentationBuilder<*>) {
        mainThread {
            buildSelfJob = async { }
        }
    }

    override fun onDrawInPresenterWith(canvas: Canvas?) {
    }
}