package com.germainkevin.library

import android.content.Context
import android.graphics.Canvas
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import com.germainkevin.library.prototypes.PresenterShape

/**
 * A [Presenter] is like a tour guide for your app's Views. You use it to explain what is the
 * role of a [View] in your UI.
 *
 * The [Presenter] is a [View] that presents a [View] in your
 * [Activity][android.app.Activity] or [Fragment][android.app.Fragment]
 *
 * This [View] (the [Presenter]) is created at the initialization of the constructor of a [UIPresenter].
 * It is added and made visible inside your [Activity][android.app.Activity]
 * or [Fragment][android.app.Fragment] by your app's "decorView",
 * at the call of [UIPresenter.set]
 *
 * The internal variables here are to be set by the [UIPresenter][UIPresenter]
 * that will create this [Presenter]
 * @author Kevin Germain
 * */
open class Presenter(context: Context) : View(context) {

    /** A set of states that this [Presenter] can be in */
    @IntDef(
        STATE_NOT_SHOWN, STATE_REVEALING, STATE_CANVAS_DRAWN, STATE_REVEALED, STATE_REMOVING,
        STATE_REMOVED, STATE_VTP_PRESSED, STATE_FOCAL_PRESSED, STATE_NON_FOCAL_PRESSED,
        STATE_BACK_BUTTON_PRESSED
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    internal annotation class PresenterState

    companion object {
        /** [Presenter] is yet to be shown. */
        const val STATE_NOT_SHOWN = 0

        /**
         * The [PresenterShape] inside [Presenter.onDraw], has a method called
         * [PresenterShape.onDrawInPresenterWith] that is about to be executed
         */
        const val STATE_REVEALING = 1

        /**
         * The [UIPresenter.presenterShape]'s [PresenterShape.onDrawInPresenterWith] method
         * has been executed and the [presenter's][Presenter] reveal animation is running.
         * */
        const val STATE_CANVAS_DRAWN = 2

        /** [Presenter] reveal animation has finished and its view is displayed. */
        const val STATE_REVEALED = 3

        /** The [Presenter] is being removed from the decorView. */
        const val STATE_REMOVING = 4

        /** The [Presenter] has been removed from the decorView. */
        const val STATE_REMOVED = 5

        /** The view to present has been pressed */
        const val STATE_VTP_PRESSED = 6

        /** The [Presenter]'s [UIPresenter.presenterShape] has been pressed */
        const val STATE_FOCAL_PRESSED = 7

        /**
         * The [Presenter] has been pressed outside both the [UIPresenter.presenterShape]
         * and the view to present
         */
        const val STATE_NON_FOCAL_PRESSED = 8

        /** When a press on the back button is detected */
        const val STATE_BACK_BUTTON_PRESSED = 9
    }

    /**
     * Will be assigned to the MotionEvent given to us by the [onTouchEvent] function so we can
     * detect press events on this [Presenter]
     **/
    private var motionEvent: MotionEvent? = null

    /**
     * Here so we can check [UIPresenter.isRevealAnimationDone] & access [UIPresenter.presenterShape]
     * */
    internal lateinit var uiPresenter: UIPresenter

    /**
     * Interface definition for a callback to be invoked when a [presenter's][Presenter]
     * [state][PresenterState] has changed.
     */
    interface StateChangeNotifier {
        fun onStateChange(@PresenterState state: Int)
    }

    /**
     * Will be accessed by the [UIPresenter] that will create this [Presenter] so that it can be
     * notified of state changes in this [Presenter]
     * */
    internal lateinit var stateChangeNotifier: StateChangeNotifier

    init {
        id = R.id.android_ui_presenter
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parent = this.parent as View
        setMeasuredDimension(parent.measuredWidth, parent.measuredHeight)
    }

    override fun dispatchKeyEventPreIme(event: KeyEvent?): Boolean {
        if (event!!.keyCode == KeyEvent.KEYCODE_BACK) {
            stateChangeNotifier.onStateChange(STATE_BACK_BUTTON_PRESSED)
        }
        return super.dispatchKeyEventPreIme(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        motionEvent = event
        if (event?.action == MotionEvent.ACTION_UP) {
            // Enables accessibility services to perform the custom click action
            // for users who are not able to use a touch screen
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        val x = motionEvent!!.x
        val y = motionEvent!!.y
        val isContainedOnVTPSurface = uiPresenter.presenterShape.viewToPresentContains(x, y)
        val isContainedOnShapeSurface = uiPresenter.presenterShape.shapeContains(x, y)
        // Only propagate click events, when the reveal animation is done running
        if (uiPresenter.isRevealAnimationDone) {
            if (isContainedOnVTPSurface) stateChangeNotifier.onStateChange(STATE_VTP_PRESSED)
            if (isContainedOnShapeSurface) stateChangeNotifier.onStateChange(STATE_FOCAL_PRESSED)
            if (!isContainedOnShapeSurface && !isContainedOnVTPSurface) stateChangeNotifier
                .onStateChange(STATE_NON_FOCAL_PRESSED)
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        stateChangeNotifier.onStateChange(STATE_REVEALING)
        uiPresenter.presenterShape.onDrawInPresenterWith(canvas)
        stateChangeNotifier.onStateChange(STATE_CANVAS_DRAWN)
    }
}