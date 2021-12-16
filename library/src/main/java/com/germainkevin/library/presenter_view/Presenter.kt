package com.germainkevin.library.presenter_view

import android.content.Context
import android.graphics.Canvas
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import com.germainkevin.library.R
import com.germainkevin.library.UIPresenter
import com.germainkevin.library.prototypes.PresenterShape


/**
 * A [Presenter] is like a tour guide for your app's Views. You use it to explain what is the
 * role of a [View] in your UI.
 *
 * The [Presenter] is a [View] that presents a [View] in your
 * [Activity][android.app.Activity] or [Fragment][android.app.Fragment]
 *
 * This [View] is created at the creation of the constructor of a [UIPresenter].
 * It is added and made visible inside your [Activity][android.app.Activity]
 * or [Fragment][android.app.Fragment] by your app's "decorView",
 * at the call of [UIPresenter.set]
 *
 * For Example:
 *
 *  UIPresenter(activity = this).set(
 *  viewToPresentId = R.id.the_view_to_present,
 *  descriptionText = "The role of this view is to help you with....",
 *  presenterStateChangeListener = { _, _ -> }
 *  )
 *
 * The internal variables here are to be set by the [UIPresenter][UIPresenter]
 * that will create this [Presenter]
 *
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
         * The [UIPresenter.mPresenterShape]'s
         * [PresenterShape.onDrawInPresenterWith] method has been executed
         * and the [presenter's][Presenter] reveal animation is running.
         * */
        const val STATE_CANVAS_DRAWN = 2

        /**
         * [Presenter] reveal animation has finished and its view is displayed.
         */
        const val STATE_REVEALED = 3

        /**
         * The [Presenter] is being removed from the decorView.
         */
        const val STATE_REMOVING = 4

        /**
         * The [Presenter] has been removed from the decorView.
         */
        const val STATE_REMOVED = 5

        /**
         * The view to present has been pressed
         * */
        const val STATE_VTP_PRESSED = 6

        /**
         * The [Presenter]'s [UIPresenter.mPresenterShape] has been pressed
         */
        const val STATE_FOCAL_PRESSED = 7

        /**
         * The [Presenter] has been pressed outside the [UIPresenter.mPresenterShape]
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
     * Here to know if the [UIPresenter.mIsViewToPresentSet]
     * Will be set by the [UIPresenter] that will create this [Presenter]
     * */
    internal lateinit var mUIPresenter: UIPresenter

    /**
     * Interface definition for a callback to be invoked when a
     * [presenter's][Presenter] [state][PresenterState] has changed.
     */
    interface StateChangeNotifier {
        fun onStateChange(@PresenterState state: Int)
    }

    /**
     * Exposed to the [UIPresenter] that will create this [Presenter]
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
        val captureEventVTP = mUIPresenter.mPresenterShape.viewToPresentContains(x, y)
        val captureEventFocal = mUIPresenter.mPresenterShape.shapeContains(x, y)

        // Only propagate click events, when the reveal animation is done running
        if (mUIPresenter.isRevealAnimationDone) {
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
        if (mUIPresenter.mIsViewToPresentSet) {
            mPresenterStateChangeNotifier.onStateChange(STATE_REVEALING)
            mUIPresenter.mPresenterShape.onDrawInPresenterWith(canvas)
            mPresenterStateChangeNotifier.onStateChange(STATE_CANVAS_DRAWN)
        }
    }
}