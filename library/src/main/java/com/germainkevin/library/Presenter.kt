package com.germainkevin.library

import android.content.Context
import android.graphics.Canvas
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
        STATE_REMOVED, STATE_VTP_PRESSED, STATE_FOCAL_PRESSED, STATE_NON_FOCAL_PRESSED
    )
    @Retention(AnnotationRetention.SOURCE)
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
     * Will be accessed by the [UIPresenter] that will create this [Presenter] so that it can be
     * notified of [state][PresenterState] changes in this [Presenter]
     * */
    internal var stateChangeNotifier: (state: Int) -> Unit = {}

    init {
        id = R.id.android_ui_presenter
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parent = this.parent as View
        setMeasuredDimension(parent.measuredWidth, parent.measuredHeight)
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
        val pressedOnVTP = uiPresenter.presenterShape.viewToPresentContains(x, y)
        val pressedOnShape = uiPresenter.presenterShape.shapeContains(x, y)
        // Only propagate click events, when the reveal animation is done running
        if (uiPresenter.isRevealAnimationDone) {
            if (pressedOnVTP) stateChangeNotifier(STATE_VTP_PRESSED)
            if (pressedOnShape) stateChangeNotifier(STATE_FOCAL_PRESSED)
            if (!pressedOnShape && !pressedOnVTP) stateChangeNotifier(STATE_NON_FOCAL_PRESSED)
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        stateChangeNotifier(STATE_REVEALING)
        uiPresenter.presenterShape.onDrawInPresenterWith(canvas)
        stateChangeNotifier(STATE_CANVAS_DRAWN)
    }
}