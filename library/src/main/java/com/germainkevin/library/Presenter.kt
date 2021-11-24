package com.germainkevin.library

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextUtils
import android.view.GestureDetector
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
import androidx.core.view.GestureDetectorCompat
import com.germainkevin.library.prototypes.PresenterShape


/**
 * A [View] that will be added to a UI by a DecorView to present a UI element
 * The public variables here are to be set by a the [UIPresenter] that will create
 * this [Presenter]
 * */
open class Presenter constructor(context: Context) : View(context) {

    /**
     * Mainly to access information to draw on the [Canvas]
     * Will be set by the [PresentationBuilder] that will create this [Presenter]
     * */
    internal lateinit var mPresentationBuilder: PresentationBuilder<*>

    /**
     * The presenter shape coming from the [mPresentationBuilder]
     * */
    private val presenterShape: PresenterShape by lazy { mPresentationBuilder.getPresenterShape() }

    internal lateinit var mPresenterStateChangeNotifier: StateChangeNotifier

    /**
     * A set of states that this class's [Presenter] can be in
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
         * The view to present has been pressed
         * */
        const val STATE_VTP_PRESSED = 5

        /**
         * The [Presenter]'s [PresentationBuilder.getPresenterShape] has been pressed
         */
        const val STATE_FOCAL_PRESSED = 6

        /**
         * The [Presenter] has been pressed outside the [PresentationBuilder.getPresenterShape]
         * and not on the view to present
         */
        const val STATE_NON_FOCAL_PRESSED = 7

        /**
         * The [Presenter] has been dismissed by the system back button being pressed.
         */
        const val STATE_BACK_BUTTON_PRESSED = 8
    }

    /**
     * Interface definition for a callback to be invoked when a
     * [presenter][Presenter] state has changed.
     */
    interface StateChangeNotifier {

        fun onPresenterStateChange(@PresenterState eventType: Int)
    }

    init {
        id = R.id.android_ui_presenter
        accessibilityDelegate = AccessibilityDelegate()
        isFocusableInTouchMode = true
        requestFocus()
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
        circularReveal(view = this, duration = 600L)
        mPresenterStateChangeNotifier.onPresenterStateChange(STATE_REVEALED)
    }

    private class AccessibilityDelegate : View.AccessibilityDelegate() {
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
            info.contentDescription = info.hashCode().toString()
            info.text = info.hashCode().toString()
        }

        override fun onPopulateAccessibilityEvent(host: View, event: AccessibilityEvent) {
            super.onPopulateAccessibilityEvent(host, event)
            val contentDescription: CharSequence =
                "This is a Presenter View, that explains certain UI views"
            if (!TextUtils.isEmpty(contentDescription)) {
                event.text.add(contentDescription)
            }
        }
    }
}