package com.germainkevin.library.prototypes

import android.graphics.Canvas
import com.germainkevin.library.prototype_impl.PresentationBuilder
import kotlinx.coroutines.CoroutineScope

/**
 * The lifecycle of a [PresenterShape].
 * [buildSelfWith] is always called before [onDrawInPresenterWith] when presenting
 * a [PresentationBuilder.mPresenter] inside the DecorView.
 *
 * [buildSelfWith] will be populated from a [PresentationBuilder]'s [PresentationBuilder.present]
 * method
 *
 * [onDrawInPresenterWith] will be called from the
 * [Presenter][com.germainkevin.library.presenter_view.Presenter]'s
 * [onDraw][com.germainkevin.library.presenter_view.Presenter.onDraw] method
 *
 * @see [com.germainkevin.library.prototype_impl.presentation_shapes.SquircleShape] for example
 * @author Kevin Germain
 * */
interface ShapeLifecycle {
    /**
     * This method is executed asynchronously on a [kotlinx.coroutines.Dispatchers.Main] inside the
     * [com.germainkevin.library.prototype_impl.PresentationBuilder.present] method
     * Right after you call the [PresentationBuilder.set]
     *
     * Builds the [PresenterShape] that will be drawn on the call of [onDrawInPresenterWith]
     * Some information necessary for positioning are available in the [builder][PresentationBuilder]
     * Which is why it is put there as a parameter, and will be assigned from the
     * [builder][PresentationBuilder] calling this method
     *
     * Should always be called before [onDrawInPresenterWith]
     *
     * @param builder The builder that will create this [PresenterShape]
     * by calling its [PresentationBuilder.present] method
     * @see [com.germainkevin.library.prototype_impl.presentation_shapes.SquircleShape.buildSelfWith]
     * for example
     */
    fun buildSelfWith(builder: PresentationBuilder<*>)

    /**
     * Will be called by the [Presenter][PresentationBuilder.mPresenter] added
     * to the decorView, right after the call of the [PresentationBuilder.present] method
     *
     * The [presenter][PresentationBuilder.mPresenter] will call this through its
     * [onDraw(canvas: Canvas)][com.germainkevin.library.presenter_view.Presenter.onDraw] method
     * @param canvas The canvas coming from the [presenter's][PresentationBuilder.mPresenter]
     * [com.germainkevin.library.presenter_view.Presenter.onDraw] method
     * @see [com.germainkevin.library.prototype_impl.presentation_shapes.SquircleShape.onDrawInPresenterWith]
     * for example
     */
    fun onDrawInPresenterWith(canvas: Canvas?)
}