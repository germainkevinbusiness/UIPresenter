package com.germainkevin.library.prototypes

import android.graphics.Canvas
import com.germainkevin.library.prototype_impl.PresentationBuilder

/**
 * [buildSelfWith] should always be called before [bindCanvasToDraw]
 *
 * [buildSelfWith] will be populated from a [PresentationBuilder]
 *
 * [bindCanvasToDraw] will be called from a [com.germainkevin.library.Presenter]'s
 * onDraw method
 * */
interface ShapeLifecycle {
    /**
     * Builds a [PresenterShape] to be drawn on
     * a [presenter][com.germainkevin.library.Presenter], by using data
     * provided by a [builder][PresentationBuilder]
     *
     * Should always be called before [bindCanvasToDraw]
     *
     * @param builder The builder from which the [presenter][com.germainkevin.library.Presenter]
     * was created.
     */
    fun buildSelfWith(builder: PresentationBuilder<*>)

    /**
     * Made to be called from a [com.germainkevin.library.Presenter]
     * that will pass its [Canvas] to this method, so this method's implementation
     * will be executed
     */
    fun bindCanvasToDraw(canvas: Canvas?)
}