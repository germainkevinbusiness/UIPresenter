package com.germainkevin.library.prototypes

import android.graphics.Canvas
import com.germainkevin.library.UIPresenter

/**
 * The lifecycle of a [PresenterShape].
 * [buildSelfWith] is always called before [onDrawInPresenterWith] when presenting
 * a [UIPresenter.mPresenter] inside the DecorView.
 *
 * [buildSelfWith] will be populated from a [UIPresenter]'s [UIPresenter.present]
 * method
 *
 * [onDrawInPresenterWith] will be called from the
 * [Presenter][com.germainkevin.library.Presenter]'s
 * [onDraw][com.germainkevin.library.Presenter.onDraw] method
 *
 * @see [com.germainkevin.library.prototype_impl.presentation_shapes.SquircleShape] for example
 * @author Kevin Germain
 * */
interface ShapeLifecycle {
    /**
     * This method is executed asynchronously on a [kotlinx.coroutines.Dispatchers.Main] inside the
     * [com.germainkevin.library.UIPresenter.present] method
     * Right after you call the [UIPresenter.set]
     *
     * Builds the [PresenterShape] that will be drawn on the call of [onDrawInPresenterWith]
     * Some information necessary for positioning are available in the [builder][UIPresenter]
     * Which is why it is put there as a parameter, and will be assigned from the
     * [builder][UIPresenter] calling this method
     *
     * Should always be called before [onDrawInPresenterWith]
     *
     * @param builder The builder that will create this [PresenterShape]
     * by calling its [UIPresenter.present] method
     * @see [com.germainkevin.library.prototype_impl.presentation_shapes.SquircleShape.buildSelfWith]
     * for example
     */
    fun buildSelfWith(builder: UIPresenter)

    /**
     * Will be called by the [Presenter][UIPresenter.mPresenter] added
     * to the decorView, right after the call of the [UIPresenter.present] method
     *
     * The [presenter][UIPresenter.mPresenter] will call this through its
     * [onDraw(canvas: Canvas)][com.germainkevin.library.Presenter.onDraw] method
     * @param canvas The canvas coming from the [presenter's][UIPresenter.mPresenter]
     * [com.germainkevin.library.Presenter.onDraw] method
     * @see [com.germainkevin.library.prototype_impl.presentation_shapes.SquircleShape.onDrawInPresenterWith]
     * for example
     */
    fun onDrawInPresenterWith(canvas: Canvas?)
}