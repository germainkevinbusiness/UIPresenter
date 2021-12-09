package com.germainkevin.library.prototype_impl

import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import com.germainkevin.library.presenter_view.Presenter
import com.germainkevin.library.R
import com.germainkevin.library.presenter_view.RevealAnimation
import com.germainkevin.library.prototype_impl.presentation_shapes.SquircleShape
import com.germainkevin.library.prototype_impl.presentation_shapes.TestShape
import com.germainkevin.library.prototypes.PresenterShape
import com.germainkevin.library.prototypes.ResourceFinder
import com.germainkevin.library.utils.*
import kotlinx.coroutines.*


/**
 * Contains all the methods for presenting a UI element.
 * Provides data for [mPresenter] and any [presenter shapes][PresenterShape]
 * Those data are marked as internal variables
 * @param resourceFinder is an interface that gives access to an Activity or
 * a fragment's environment
 * @param T whatever class that implements this class
 */
abstract class PresentationBuilder<T : PresentationBuilder<T>>(val resourceFinder: ResourceFinder) {

    /**
     * The view that the [mPresenter] will present.
     * Made public to be accessed from a [PresenterShape] for example [SquircleShape]
     */
    internal var mViewToPresent: View? = null

    /**
     * Has the [mViewToPresent] been set successfully?
     * true, if the [mViewToPresent] is not null or false otherwise
     * Made public to be accessed from the [mPresenter]
     */
    internal var mIsViewToPresentSet = false

    /**
     * [DecorView][ViewGroup] of [mViewToPresent]
     * It is responsible for adding [mPresenter] to your UI
     */
    private var mDecorView: ViewGroup? = null

    /**
     * The [Presenter] that will be created and added by the [mDecorView]
     */
    private var mPresenter: Presenter? = null

    /**
     * Exposes the current state of this [mPresenter] to the function [isRemoving] and [isRemoved]
     */
    @Presenter.PresenterState
    private var mState = Presenter.STATE_NOT_SHOWN

    /**
     * Exposes state changes from the [mPresenter] to the user of this library
     */
    private var mPresenterStateChangeListener: (Int, (Unit) -> Unit) -> Unit =
        { _: Int, _: (Unit) -> Unit -> }

    /**
     * Should the back button press remove the [mPresenter].
     */
    private var mRemoveOnBackPress = true

    /**
     * Should the [mPresenter] be removed when a click event is detected on the [mDecorView]
     * */
    private var mAutoRemoveOnClickEvent = true

    /**
     * The [PresenterShape] by default or set by the user for this [mPresenter]
     * */
    private var mPresenterShape: PresenterShape = TestShape()

    /** Represents what animation to use to animate [mPresenter]
     */
    private var mPresenterRevealAnimation: RevealAnimation = RevealAnimation.CIRCULAR_REVEAL

    /**
     * The duration of the animation when revealing the [mPresenter]
     * */
    private var mRevealAnimDuration = 600L

    /**
     * The duration of the animation when removing the [mPresenter]
     * */
    private var mRemovingAnimDuration = 600L

    init {
        mDecorView = resourceFinder.getDecorView()
        mPresenter = resourceFinder.getContext()?.let { Presenter(it) }?.also {
            it.mPresentationBuilder = this
            it.presenterShape = mPresenterShape
            it.mPresenterStateChangeNotifier = object : Presenter.StateChangeNotifier {
                override fun onPresenterStateChange(state: Int) {
                    onPresenterStateChanged(state)
                    when (state) {
                        Presenter.STATE_CANVAS_DRAWN -> {
                            when (mPresenterRevealAnimation) {
                                RevealAnimation.CIRCULAR_REVEAL -> {
                                    mPresenter?.circularReveal(mRevealAnimDuration)
                                }
                                RevealAnimation.FADE_IN -> {
                                    mPresenter?.fadeIn(mRemovingAnimDuration)
                                }
                                RevealAnimation.ROTATION_X -> {
                                    mPresenter?.rotationXByImpl(mRevealAnimDuration)
                                }
                                RevealAnimation.ROTATION_Y -> {
                                    mPresenter?.rotationYByImpl(mRevealAnimDuration)
                                }
                                else -> {}
                            }
                            onPresenterStateChange(Presenter.STATE_REVEALED)
                        }

                        Presenter.STATE_BACK_BUTTON_PRESSED -> {
                            if (mAutoRemoveOnClickEvent && mRemoveOnBackPress) {
                                removingPresenter()
                            }
                        }
                        Presenter.STATE_VTP_PRESSED,
                        Presenter.STATE_FOCAL_PRESSED,
                        Presenter.STATE_NON_FOCAL_PRESSED -> {
                            if (mAutoRemoveOnClickEvent) {
                                removingPresenter()
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * This method is made to only be called after you've finished
     * propagating data to a [PresentationBuilder]
     * It displays a [Presenter] inside a [DecorView][ViewGroup]
     *
     * @return the current [PresentationBuilder]
     */
    open fun present(): T {
        mainThread {
            // By this time, every configuration necessary would have already
            // been done, we can now pass this builder to the PresenterShape
            // so it builds a Shape to output to the UI.
            mViewToPresent?.let {
                val job = async {
                    mPresenterShape.buildSelfWith(this@PresentationBuilder)
                    val job1 = async { removePresenterIfPresent() }
                    job1.await()
                    job1.join()
                }
                job.await()
                job.join()
                if (job.isCompleted) {
                    mPresenter?.let { _v ->
                        mDecorView?.addView(_v)
                    }
                }
            }
        }

        return this as T
    }

    /**
     * Is the [Presenter]'s current state: [Presenter.STATE_REMOVING].
     *
     * @return True if removing.
     */
    private fun isRemoving(): Boolean = mState == Presenter.STATE_REMOVING

    /**
     * Is the [Presenter]'s current state: [Presenter.STATE_REMOVED].
     *
     * @return True if removed.
     */
    private fun isRemoved(): Boolean = mState == Presenter.STATE_REMOVED

    /**
     * Called when removing the [mPresenter]
     * */
    private fun removingPresenter() {
        onPresenterStateChanged(Presenter.STATE_REMOVING)
        removePresenterIfPresent()
    }

    /**
     * Removes the [mPresenter] from the [mDecorView],
     * if it's present in the [mDecorView]
     * */
    private fun removePresenterIfPresent() = mainThread {
        var mViewToRemove: View? = null
        val job = async {
            // Never reference mPresenter directly, always reference the mPresenter this way
            mViewToRemove = mDecorView?.findViewById(R.id.android_ui_presenter)
        }
        job.await()
        job.join()
        if (job.isCompleted) {
            mViewToRemove?.let {
                if (isRemoving() && !isRemoved()) {
                    fadeOut(mPresenter, mRemovingAnimDuration) {
                        onPresenterStateChanged(Presenter.STATE_REMOVED)
                        mDecorView?.removeView(it)
                        finish()
                    }
                }
            }
        }
    }

    // nullify all nullables, they are no longer useful
    private fun finish() {
        mDecorView = null
        mPresenter = null
        mViewToPresent = null
    }

    /**
     * This method is only called from the [mPresenter] to notify this builder
     * of its state change
     */
    private fun onPresenterStateChanged(@Presenter.PresenterState state: Int) {
        mState = state
        mPresenterStateChangeListener(state) { removingPresenter() }
    }

    /**
     * Sets a listener that listens to the [presenter][Presenter] state changes.
     * @param listener The listener to use
     */
    open fun setPresenterStateChangeListener(listener: (Int, (Unit) -> Unit) -> Unit): T {
        mPresenterStateChangeListener = listener
        return this as T
    }

    /**
     * Sets which one of the [RevealAnimation]to run to reveal the [mPresenter]
     * */
    open fun setRevealAnimation(presenterRevealAnimation: RevealAnimation): T {
        mPresenterRevealAnimation = presenterRevealAnimation
        return this as T
    }

    /**
     * Defines how long the [mPresenterRevealAnimation] should run.
     * 600L is the default value
     * */
    open fun setRevealAnimationDuration(duration: Long): T {
        mRevealAnimDuration = duration
        return this as T
    }

    /**
     * Defines how long the removing animation of the [mPresenter] should run.
     * 600L is the default value
     * */
    open fun setRemovingAnimationDuration(duration: Long): T {
        mRemovingAnimDuration = duration
        return this as T
    }

    /**
     * Sets the view to place the [presenter][Presenter] around.
     * @param view The view to present
     */
    open fun setViewToPresent(view: View?): T {
        mViewToPresent = view
        mIsViewToPresentSet = mViewToPresent != null
        return this as T
    }

    /**
     * Sets the view to place the [presenter][Presenter] around.
     * @param viewId The id of the view to present
     */
    open fun setViewToPresent(@IdRes viewId: Int): T {
        mViewToPresent = resourceFinder.findViewById(viewId)
        mIsViewToPresentSet = mViewToPresent != null
        return this as T
    }

    /**
     * Sets the shape of the [Presenter] to be added to the UI
     */
    open fun setPresenterShape(mPresenterShape: PresenterShape): T {
        this.mPresenterShape = mPresenterShape
        return this as T
    }

    /**
     * Sets the background color of the [mPresenterShape]
     * */
    open fun setBackgroundColor(bgColor: Int): T {
        mPresenterShape.setBackgroundColor(bgColor)
        return this as T
    }

    /**
     * @param choice defines whether the shape should have
     * a shadow layer drawn in its background or not
     * */
    open fun setHasShadowLayer(choice: Boolean): T {
        mPresenterShape.setHasShadowLayer(choice)
        return this as T
    }

    /**
     * @param shadowColor The color of the shadow layer
     * */
    open fun setShadowLayerColor(@ColorInt shadowColor: Int): T {
        mPresenterShape.setShadowLayerColor(shadowColor)
        return this as T
    }

    /**
     * The text that will be explaining the ui element you
     * want to present to the user
     * @param descriptionText The text you want to explain the ui element
     * */
    open fun setDescriptionText(descriptionText: String): T {
        mPresenterShape.setDescriptionText(descriptionText)
        return this as T
    }

    open fun setDescriptionTextColor(textColor: Int): T {
        mPresenterShape.setDescriptionTextColor(textColor)
        return this as T
    }

    open fun setDescriptionTextSize(
        typedValueUnit: Int = TypedValue.COMPLEX_UNIT_SP,
        textSize: Float
    ): T {
        mPresenterShape.setDescriptionTextSize(typedValueUnit, textSize)
        return this as T
    }

    open fun setDescriptionTypeface(typeface: Typeface?): T {
        mPresenterShape.setDescriptionTypeface(typeface)
        return this as T
    }

    /**
     * Defines whether or not a detected click event on the [mDecorView],
     * should result in the removal of the [mPresenter] from the [mDecorView].
     * It is true by default
     */
    open fun setAutoRemoveOnClickEvent(autoRemoveApproval: Boolean): T {
        mAutoRemoveOnClickEvent = autoRemoveApproval
        return this as T
    }

    /**
     * Sets whether or not a press on the back button should result in the removal of
     * the [mPresenter] from the [mDecorView]. True by default
     */
    open fun setRemoveOnBackPress(enabled: Boolean): T {
        mRemoveOnBackPress = enabled
        return this as T
    }
}