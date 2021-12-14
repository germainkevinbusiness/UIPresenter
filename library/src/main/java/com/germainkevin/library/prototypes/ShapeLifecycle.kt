package com.germainkevin.library.prototypes

import android.graphics.Canvas
import com.germainkevin.library.prototype_impl.PresentationBuilder
import kotlinx.coroutines.CoroutineScope

/**
 * The lifecycle of a [PresenterShape]
 * [buildSelfWith] should always be called before [onDrawInPresenterWith]
 *
 * [buildSelfWith] will be populated from a [PresentationBuilder]'s [PresentationBuilder.present]
 * method
 *
 * [onDrawInPresenterWith] will be called from the
 * [Presenter][com.germainkevin.library.presenter_view.Presenter]'s
 * [onDraw][com.germainkevin.library.presenter_view.Presenter.onDraw] method
 * @author Kevin Germain
 * */
interface ShapeLifecycle {
    /**
     * Builds the [PresenterShape] that will be drawn on the call of [onDrawInPresenterWith]
     * Some information necessary for positioning are available in the [builder][PresentationBuilder]
     * Which is why it is put there as a parameter, and will be assigned from the
     * [builder][PresentationBuilder] calling this method
     *
     * Should always be called before [onDrawInPresenterWith]
     *
     * @param builder The builder that will create this [PresenterShape]
     * by calling its [PresentationBuilder.present] method
     */
    fun buildSelfWith(builder: PresentationBuilder<*>)

    /**
     * Made to be called from the [Presenter's][com.germainkevin.library.presenter_view.Presenter]
     * [onDraw][com.germainkevin.library.presenter_view.Presenter.onDraw] method
     * @param canvas The canvas that will come from
     * the [Presenter's][com.germainkevin.library.presenter_view.Presenter]
     * [onDraw][com.germainkevin.library.presenter_view.Presenter.onDraw] method
     */
    fun onDrawInPresenterWith(canvas: Canvas?)
}