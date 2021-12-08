package com.germainkevin.library

import android.content.Context
import android.graphics.Canvas
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import com.germainkevin.library.Presenter.Companion.ANIM_CIRCULAR_REVEAL
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

    @IntDef(ANIM_CIRCULAR_REVEAL, ANIM_FADE_IN, ANIM_ROTATION_X, ANIM_ROTATION_Y)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class PresenterAnimation

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
         * The [presenterShape]'s [PresenterShape.bindCanvasToDraw] method has been executed
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

        /**
         * The [Presenter] will be shown in the UI using a circular reveal animation
         * */
        const val ANIM_CIRCULAR_REVEAL = 10

        /**
         * The [Presenter] will be shown in the UI using a customRotationXBy animation
         * */
        const val ANIM_ROTATION_X = 11

        /**
         * The [Presenter] will be shown in the UI using a customRotationYBy animation
         * */
        const val ANIM_ROTATION_Y = 12

        /**
         * The [Presenter] will be shown in the UI using a fade in animation
         * */
        const val ANIM_FADE_IN = 13
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
        val captureEventViewToPresentPressed = presenterShape.viewToPresentContains(x, y)
        val captureEventFocal = presenterShape.shapeContains(x, y)

        if (captureEventViewToPresentPressed) {
            mPresenterStateChangeNotifier.onPresenterStateChange(STATE_VTP_PRESSED)
        }
        if (captureEventFocal) {
            mPresenterStateChangeNotifier.onPresenterStateChange(STATE_FOCAL_PRESSED)
        }
        if (!captureEventFocal && !captureEventViewToPresentPressed) {
            mPresenterStateChangeNotifier.onPresenterStateChange(STATE_NON_FOCAL_PRESSED)
        }

        return true
    }

    override fun onDraw(canvas: Canvas?) {
        if (mPresentationBuilder.mIsViewToPresentSet) {
            mPresenterStateChangeNotifier.onPresenterStateChange(STATE_REVEALING)
            presenterShape.bindCanvasToDraw(canvas)
            mPresenterStateChangeNotifier.onPresenterStateChange(STATE_CANVAS_DRAWN)
        }
    }
}