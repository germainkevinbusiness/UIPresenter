package com.germainkevin.library

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextUtils
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.IntDef
import com.germainkevin.library.R
import com.germainkevin.library.UIPresenter
import com.germainkevin.library.prototype_impl.PresentationBuilder
import timber.log.Timber

/**
 * A [View] that will be added to a UI by a DecorView to present a UI element
 * The public variables here are to be set by a the [UIPresenter] that will create
 * this [Presenter]
 * */
open class Presenter constructor(context: Context) : View(context) {
    init {
        id = R.id.android_ui_presenter
        isFocusableInTouchMode = true
        accessibilityDelegate = AccessibilityDelegate()
        requestFocus()
    }

    /**
     * Mainly to access information to draw on the [Canvas]
     * Will be set by the [UIPresenter] that will create this [Presenter]
     * */
    internal var mPresentationBuilder: PresentationBuilder<*>? = null

    /**
     * A set of states that this class's [Presenter] can be in
     */
    @IntDef(
        STATE_NOT_SHOWN,
        STATE_REVEALING,
        STATE_REVEALED,
        STATE_REMOVING,
        STATE_REMOVED,
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
         * The [isRemoving] method has been called and
         * the [Presenter] is being removed from the UI.
         */
        const val STATE_REMOVING = 3

        /**
         * The [Presenter] has been removed from view
         * after it has been pressed in the focal area.
         */
        const val STATE_REMOVED = 4

        /**
         * The [Presenter] has been pressed in the focal area.
         */
        const val STATE_FOCAL_PRESSED = 5

        /**
         * The [Presenter] has been pressed outside the focal area.
         */
        const val STATE_NON_FOCAL_PRESSED = 6

        /**
         * The [Presenter] has been dismissed by the system back button being pressed.
         */
        const val STATE_BACK_BUTTON_PRESSED = 7
    }

    /**
     * Exposes the state of this [Presenter] for the functions [isRemoving] and [isRemoved]
     */
    @Presenter.PresenterState
    private var mState = STATE_NOT_SHOWN

    /**
     * Is the [Presenter]'s current state: [STATE_REMOVING].
     *
     * @return True if removing.
     */
    internal fun isRemoving(): Boolean = mState == STATE_REMOVING

    /**
     * Is the [Presenter]'s current state: [STATE_REMOVED].
     *
     * @return True if removed.
     */
    internal fun isRemoved(): Boolean = mState == STATE_REMOVED

    /**
     * Interface to listen to this [View.onTouchEvent]
     * */
    internal var mPresenterTouchEventListener: TouchEventListener? = null

    /**
     * Interface definition for a callback to be invoked when a
     * [presenter][Presenter] is touched, or the back button is clicked.
     */
    interface TouchEventListener {
        /**
         * Called when the view to present is pressed.
         */
        fun onFocalPressed()

        /**
         * Called when anywhere but the view to present is pressed
         */
        fun onNonFocalPressed()

        /**
         * Called when the system back button is pressed.
         */
        fun onBackButtonPressed()
    }

    /**
     * This method notifies the [builder][PresentationBuilder] about a state
     * change, so an implementation of the builder's
     * [PresentationBuilder.setPresenterStateChangeListener] gets notified of the state changes
     * via the builder's [PresentationBuilder.onPresenterStateChanged] method
     * @param state The new state that the builder's [PresentationBuilder.onPresenterStateChanged]
     * method gets notified of
     */
    open fun notifyBuilderOfStateChange(@Presenter.PresenterState state: Int) {
        mState = state
        mPresentationBuilder?.onPresenterStateChanged(mState)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Make this view take its parent's size.
        val parent = this.parent as View
        setMeasuredDimension(parent.measuredWidth, parent.measuredHeight)
    }

    override fun dispatchKeyEventPreIme(event: KeyEvent?): Boolean {
        if (event!!.keyCode == KeyEvent.KEYCODE_BACK) {
            mPresenterTouchEventListener?.onBackButtonPressed()
            notifyBuilderOfStateChange(STATE_BACK_BUTTON_PRESSED)
        }
        return super.dispatchKeyEventPreIme(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event!!.x
        val y = event.y
        val presenterShape = mPresentationBuilder!!.getPresenterShape()
        val captureEventFocal = presenterShape.viewToPresentContains(x, y)
        val captureEventNonFocal = presenterShape.shapeContains(x, y)
        val eventCaptured = captureEventFocal || captureEventNonFocal
        mPresenterTouchEventListener?.let {
            if (captureEventFocal) {
                notifyBuilderOfStateChange(STATE_FOCAL_PRESSED)
                it.onFocalPressed()
            }
            if (captureEventNonFocal) {
                notifyBuilderOfStateChange(STATE_NON_FOCAL_PRESSED)
                it.onNonFocalPressed()
            }
        }
        return eventCaptured
    }

    override fun onDraw(canvas: Canvas?) {
        mPresentationBuilder?.let { builder ->
            if (builder.mIsViewToPresentSet) {
                notifyBuilderOfStateChange(STATE_REVEALING)
                builder.getPresenterShape().bindCanvasToDraw(canvas)
                circularReveal(this, 600L)
                notifyBuilderOfStateChange(STATE_REVEALED)
            }
        }
    }

    internal inner class AccessibilityDelegate : View.AccessibilityDelegate() {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            val viewPackage: Package? = Presenter::class.java.getPackage()
            if (viewPackage != null) {
                info.packageName = viewPackage.name
            }
            info.setSource(host)
            info.isClickable = true
            info.isEnabled = true
            info.isChecked = false
            info.isFocusable = true
            info.isFocused = true
            info.contentDescription = mPresentationBuilder?.getPresenterShape()?.descriptionText
            info.text = mPresentationBuilder?.getPresenterShape()?.descriptionText
        }

        override fun onPopulateAccessibilityEvent(host: View, event: AccessibilityEvent) {
            super.onPopulateAccessibilityEvent(host, event)
            val contentDescription: CharSequence =
                mPresentationBuilder?.getPresenterShape()?.descriptionText.toString()
            if (!TextUtils.isEmpty(contentDescription)) {
                event.text.add(contentDescription)
            }
        }
    }
}