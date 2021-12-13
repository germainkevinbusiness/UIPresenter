package com.germainkevin.library.presenter_view

import android.content.Context
import android.graphics.Canvas
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import com.germainkevin.library.R
import com.germainkevin.library.prototype_impl.PresentationBuilder
import com.germainkevin.library.prototypes.PresenterShape
import timber.log.Timber


/**
 * The [Presenter] is a [View] that presents a [View]
 * in your [Activity][android.app.Activity], [Dialog][android.app.Dialog]
 * or [Fragment][android.app.Fragment]
 *
 * The public variables here are to be set by the
 * [PresentationBuilder][com.germainkevin.library.prototype_impl.PresentationBuilder]
 * that will create this [Presenter]
 * at the call of [PresentationBuilder.present]
 *
 * [Presenters][Presenter] are created at the creation of the constructor of a [PresentationBuilder]
 * It is made visible by your activity, dialog, or fragment's "decorView"
 * at the call of [PresentationBuilder.present]
 *
 * Made visible means that the "decorView" adds it with [android.view.ViewGroup.addView]
 * @author Kevin Germain
 * */
open class Presenter(context: Context) : View(context) {

    /**
     * A set of states that this [Presenter] can be in
     */
    @IntDef(
        STATE_NOT_SHOWN,
        STATE_REVEALING,
        STATE_CANVAS_DRAWN,
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
         * The [PresenterShape] inside [Presenter.onDraw], has a method
         * called [PresenterShape.onDrawInPresenterWith] that is about to be executed
         */
        const val STATE_REVEALING = 1

        /**
         * The [presenterShape]'s [PresenterShape.onDrawInPresenterWith] method has been executed
         * and the [presenter's][Presenter] reveal animation is running.
         * */
        const val STATE_CANVAS_DRAWN = 2

        /**
         * [Presenter] reveal animation has finished and its view is displayed.
         */
        const val STATE_REVEALED = 3

        /**
         * The [PresentationBuilder.isRemoving] method has been called and
         * the [Presenter] is being removed from the UI.
         */
        const val STATE_REMOVING = 4

        /**
         * The [Presenter] has been removed from view
         * after it has been pressed in the focal area.
         */
        const val STATE_REMOVED = 5

        /**
         * The view to present has been pressed
         * */
        const val STATE_VTP_PRESSED = 6

        /**
         * The [Presenter]'s [PresentationBuilder.mPresenterShape] has been pressed
         */
        const val STATE_FOCAL_PRESSED = 7

        /**
         * The [Presenter] has been pressed outside the [PresentationBuilder.mPresenterShape]
         * and not on the view to present
         */
        const val STATE_NON_FOCAL_PRESSED = 8

        /**
         * The [Presenter] has been dismissed by the system back button being pressed.
         */
        const val STATE_BACK_BUTTON_PRESSED = 9
    }

    /**
     * Will be assigned to the MotionEvent given to us by the [onTouchEvent] function
     * so we can detect press events on this Presenter
     **/
    private var motionEvent: MotionEvent? = null

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
     * [presenter's][Presenter] [state][PresenterState] has changed.
     */
    interface StateChangeNotifier {
        fun onStateChange(@PresenterState state: Int)
    }

    /**
     * Exposed to the [PresentationBuilder] that will create this [Presenter]
     * so that it can notify this builder of state changes in this [Presenter]
     * */
    internal lateinit var mPresenterStateChangeNotifier: StateChangeNotifier

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
            mPresenterStateChangeNotifier.onStateChange(STATE_BACK_BUTTON_PRESSED)
        }
        return super.dispatchKeyEventPreIme(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        motionEvent = event
        // after a press has occurred on this Presenter
        if (event?.action == MotionEvent.ACTION_UP) {
            performClick()
        }
        return true
    }

    /**
     * For people with disabilities, an android talkback app will execute this logic for them
     * */
    override fun performClick(): Boolean {
        super.performClick()
        val x = motionEvent!!.x
        val y = motionEvent!!.y
        val captureEventVTP = presenterShape.viewToPresentContains(x, y)
        val captureEventFocal = presenterShape.shapeContains(x, y)

        // Only propagate click events, when the reveal animation is done running
        if (mPresentationBuilder.isRevealAnimationDone) {
            if (captureEventVTP) {
                mPresenterStateChangeNotifier.onStateChange(STATE_VTP_PRESSED)
            }
            if (captureEventFocal) {
                mPresenterStateChangeNotifier.onStateChange(STATE_FOCAL_PRESSED)
            }
            if (!captureEventFocal && !captureEventVTP) {
                mPresenterStateChangeNotifier.onStateChange(STATE_NON_FOCAL_PRESSED)
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas?) {
        if (mPresentationBuilder.mIsViewToPresentSet) {
            if (presenterShape.buildSelfJob.isCompleted) {
                mPresenterStateChangeNotifier.onStateChange(STATE_REVEALING)
                presenterShape.onDrawInPresenterWith(canvas)
                mPresenterStateChangeNotifier.onStateChange(STATE_CANVAS_DRAWN)
            }
        }
    }
}