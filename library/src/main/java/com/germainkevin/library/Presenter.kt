package com.germainkevin.library

import android.content.Context
import android.graphics.Canvas
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import com.germainkevin.library.prototype_impl.PresentationBuilder
import com.germainkevin.library.prototypes.PresenterShape


/**
 * A [View] that will be added to a UI by a DecorView to present a UI element
 * The public variables here are to be set by a the [UIPresenter] that will create
 * this [Presenter]
 *
 * [Presenters][Presenter] are created at the creation of the constructor of a [PresentationBuilder]
 * */
open class Presenter(context: Context) : View(context) {

    /**
     * Here to know if the [PresentationBuilder.mIsViewToPresentSet]
     * Will be set by the [PresentationBuilder] that will create this [Presenter]
     * */
    internal lateinit var mPresentationBuilder: PresentationBuilder<*>

    /**
     * The default presenter shape in the [mPresentationBuilder]
     * Will be set by the [PresentationBuilder] that will create this [Presenter]
     * */
    internal lateinit var presenterShape: PresenterShape

    /**
     * Interface definition for a callback to be invoked when a
     * [presenter][Presenter] state has changed.
     */
    interface StateChangeNotifier {
        fun onPresenterStateChange(@PresenterState state: Int)
    }

    /**
     * Exposed to the [PresentationBuilder] that will create this [Presenter]
     * so that it can notify this builder of state changes in this [Presenter]
     * */
    internal lateinit var mPresenterStateChangeNotifier: StateChangeNotifier


    /**
     * A set of states that this [Presenter] can be in
     */
    @IntDef(
        STATE_NOT_SHOWN,
        STATE_REVEALING,
        STATE_REVEALED,
        STATE_REMOVING,
        STATE_REMOVED,
        STATE_VTP_PRESSED,
        STATE_FOCAL_PRESSED,
        STATE_NON_FOCAL_PRESSED,
        STATE_BACK_BUTTON_PRESSED
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class PresenterState

    companion object {
        /**
         * [Presenter] is yet to be shown.
         */
        const val STATE_NOT_SHOWN = 0

        /**
         * [Presenter] reveal animation is running.
         */
        const val STATE_REVEALING = 1

        /**
         * [Presenter] reveal animation has finished and its view is displayed.
         */
        const val STATE_REVEALED = 2

        /**
         * The [PresentationBuilder.isRemoving] method has been called and
         * the [Presenter] is being removed from the UI.
         */
        const val STATE_REMOVING = 3

        /**
         * The [Presenter] has been removed from view
         * after it has been pressed in the focal area.
         */
        const val STATE_REMOVED = 4

        /**
         * The view to present has been pressed
         * */
        const val STATE_VTP_PRESSED = 5

        /**
         * The [Presenter]'s [PresentationBuilder.mPresenterShape] has been pressed
         */
        const val STATE_FOCAL_PRESSED = 6

        /**
         * The [Presenter] has been pressed outside the [PresentationBuilder.mPresenterShape]
         * and not on the view to present
         */
        const val STATE_NON_FOCAL_PRESSED = 7

        /**
         * The [Presenter] has been dismissed by the system back button being pressed.
         */
        const val STATE_BACK_BUTTON_PRESSED = 8
    }

    init {
        id = R.id.android_ui_presenter
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Make this view take its parent's size.
        val parent = this.parent as View
        setMeasuredDimension(parent.measuredWidth, parent.measuredHeight)
    }

    override fun dispatchKeyEventPreIme(event: KeyEvent?): Boolean {
        if (event!!.keyCode == KeyEvent.KEYCODE_BACK) {
            mPresenterStateChangeNotifier.onPresenterStateChange(STATE_BACK_BUTTON_PRESSED)
        }
        return super.dispatchKeyEventPreIme(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        val x = event!!.x
        val y = event.y
        val captureEventViewToPresentPressed = presenterShape.viewToPresentContains(x, y)
        val captureEventFocal = presenterShape.shapeContains(x, y)
        // !captureEventFocal means that a click event is detected outside the presenterShape
        // and the view to present
        val eventCaptured =
            captureEventViewToPresentPressed || captureEventFocal || !captureEventFocal
        if (captureEventViewToPresentPressed) {
            mPresenterStateChangeNotifier.onPresenterStateChange(STATE_VTP_PRESSED)
        }
        if (captureEventFocal) {
            mPresenterStateChangeNotifier.onPresenterStateChange(STATE_FOCAL_PRESSED)
        }
        if (!captureEventFocal && !captureEventViewToPresentPressed) {
            mPresenterStateChangeNotifier.onPresenterStateChange(STATE_NON_FOCAL_PRESSED)
        }
        return eventCaptured
    }

    override fun onDraw(canvas: Canvas?) {
        if (mPresentationBuilder.mIsViewToPresentSet) {
            mPresenterStateChangeNotifier.onPresenterStateChange(STATE_REVEALING)
            presenterShape.bindCanvasToDraw(canvas)
            handleViewAnimation()
        }
    }

    private fun handleViewAnimation() {
        circularReveal(duration = 600L)
        mPresenterStateChangeNotifier.onPresenterStateChange(STATE_REVEALED)
    }
}