package com.germainkevin.library.prototype_impl

import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import com.germainkevin.library.presenter_view.Presenter
import com.germainkevin.library.R
import com.germainkevin.library.prototype_impl.presentation_shapes.SquircleShape
import com.germainkevin.library.prototypes.PresenterShape
import com.germainkevin.library.prototypes.ResourceFinder
import com.germainkevin.library.utils.*
import kotlinx.coroutines.*
import timber.log.Timber


/**
 * Contains all the methods for presenting a UI element with a [Presenter].
 *
 * Provides data for [mPresenter] and [mPresenterShape] that are marked as internal variables
 * @param resourceFinder is an interface that gives access to an Activity or
 * a fragment's environment
 * @param T whatever [PresentationBuilder] class that implements this class
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

    /** Represents what animation to use to animate [mPresenter]
     */
    private var mPresenterRevealAnimation: RevealAnimation = RevealAnimation.CIRCULAR_REVEAL

    /**
     * The duration of the animation when revealing the [mPresenter]
     * */
    private var mRevealAnimDuration = 1000L

    /**
     * The duration of the animation when removing the [mPresenter]
     * */
    private var mRemovingAnimDuration = 600L

    /**
     * The [PresenterShape] by default or set by the user for this [mPresenter]
     * */
    private var mPresenterShape: PresenterShape = SquircleShape()

    // Presenter configs
    internal var shadowLayerRadius = 8f
    internal var shadowLayerDx = 0f
    internal var shadowLayerDy = 1f
    internal var shadowLayerColor = Color.DKGRAY

    // background configs
    internal var mBackgroundColor = Color.BLACK
    internal var mHasShadowLayer = true

    // description text configs
    internal var mDescriptionText: String? = null

    /**
     * Desired text size to be displayed in a [TypedValue] unit
     * */
    internal var mDescriptionTextSize: Float = 18f
    internal var mDescriptionTextColor = Color.WHITE

    /**
     * [TypedValue] unit in which the [PresentationBuilder.mDescriptionText]
     * should be displayed.
     * Usually a text on android is displayed in the [TypedValue.COMPLEX_UNIT_SP] unit
     *
     */
    internal var mTypedValueUnit: Int = TypedValue.COMPLEX_UNIT_SP
    internal var mTypeface = Typeface.DEFAULT

    init {
        mDecorView = resourceFinder.getDecorView()
        mPresenter = resourceFinder.getContext()?.let { Presenter(it) }?.also {
            it.presenterShape = mPresenterShape
            it.mPresentationBuilder = this
            it.mPresenterStateChangeNotifier = object : Presenter.StateChangeNotifier {
                override fun onStateChange(state: Int) {
                    onPresenterStateChanged(state)
                    when (state) {
                        Presenter.STATE_CANVAS_DRAWN -> {
                            when (mPresenterRevealAnimation) {
                                RevealAnimation.CIRCULAR_REVEAL -> {
                                    it.circularReveal(mRevealAnimDuration)
                                }
                                RevealAnimation.ROTATION_X -> {
                                    it.rotationXByImpl(mRevealAnimDuration)
                                }
                                RevealAnimation.ROTATION_Y -> {
                                    it.rotationYByImpl(mRevealAnimDuration)
                                }
                                else -> Unit
                            }
                            onPresenterStateChanged(Presenter.STATE_REVEALED)
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
     * This method is only called from the [mPresenter] to notify this builder
     * of its state change
     */
    private fun onPresenterStateChanged(@Presenter.PresenterState state: Int) {
        mState = state
        mPresenterStateChangeListener(state) { removingPresenter() }
    }

    open fun set(
        @IdRes viewToPresentId: Int,
        presenterShape: PresenterShape = mPresenterShape,
        backgroundColor: Int = mBackgroundColor,
        hasShadowLayer: Boolean = mHasShadowLayer,
        shadowLayer: PresenterShadowLayer = PresenterShadowLayer(),
        descriptionText: String,
        descriptionTextColor: Int = mDescriptionTextColor,
        descriptionTextSize: Float = mDescriptionTextSize,
        typedValueUnit: Int = mTypedValueUnit,
        typeface: Typeface = mTypeface,
        revealAnimation: RevealAnimation = mPresenterRevealAnimation,
        revealAnimDuration: Long = mRevealAnimDuration,
        removalAnimDuration: Long = mRemovingAnimDuration,
        removeOnBackPress: Boolean = mRemoveOnBackPress,
        removePresenterOnAnyClickEvent: Boolean = mAutoRemoveOnClickEvent,
        presenterStateChangeListener: (Int, (Unit) -> Unit) -> Unit
    ): T {
        mPresenterStateChangeListener = presenterStateChangeListener
        mPresenterShape = presenterShape
        mViewToPresent = resourceFinder.findViewById(viewToPresentId)
        mIsViewToPresentSet = mViewToPresent != null
        mBackgroundColor = backgroundColor
        mHasShadowLayer = hasShadowLayer
        shadowLayerRadius = shadowLayer.radius
        shadowLayerDx = shadowLayer.dx
        shadowLayerDy = shadowLayer.dy
        shadowLayerColor = shadowLayer.shadowColor
        mDescriptionText = descriptionText
        mDescriptionTextColor = descriptionTextColor
        mDescriptionTextSize = descriptionTextSize
        mTypedValueUnit = typedValueUnit
        mTypeface = typeface
        mPresenterRevealAnimation = revealAnimation
        mRevealAnimDuration = revealAnimDuration
        mRemovingAnimDuration = removalAnimDuration
        mRemoveOnBackPress = removeOnBackPress
        mAutoRemoveOnClickEvent = removePresenterOnAnyClickEvent
        present()
        return this as T
    }

    open fun set(
        viewToPresent: View? = mViewToPresent,
        presenterShape: PresenterShape = mPresenterShape,
        backgroundColor: Int = mBackgroundColor,
        hasShadowLayer: Boolean = mHasShadowLayer,
        shadowLayer: PresenterShadowLayer = PresenterShadowLayer(),
        descriptionText: String,
        descriptionTextColor: Int = mDescriptionTextColor,
        descriptionTextSize: Float = mDescriptionTextSize,
        typedValueUnit: Int = mTypedValueUnit,
        typeface: Typeface = mTypeface,
        revealAnimation: RevealAnimation = mPresenterRevealAnimation,
        revealAnimDuration: Long = mRevealAnimDuration,
        removalAnimDuration: Long = mRemovingAnimDuration,
        removeOnBackPress: Boolean = mRemoveOnBackPress,
        removePresenterOnAnyClickEvent: Boolean = mAutoRemoveOnClickEvent,
        presenterStateChangeListener: (Int, (Unit) -> Unit) -> Unit
    ): T {
        mPresenterStateChangeListener = presenterStateChangeListener
        mViewToPresent = viewToPresent
        mIsViewToPresentSet = mViewToPresent != null
        mPresenterShape = presenterShape
        mBackgroundColor = backgroundColor
        mHasShadowLayer = hasShadowLayer
        shadowLayerRadius = shadowLayer.radius
        shadowLayerDx = shadowLayer.dx
        shadowLayerDy = shadowLayer.dy
        shadowLayerColor = shadowLayer.shadowColor
        mDescriptionText = descriptionText
        mDescriptionTextColor = descriptionTextColor
        mDescriptionTextSize = descriptionTextSize
        mTypedValueUnit = typedValueUnit
        mTypeface = typeface
        mPresenterRevealAnimation = revealAnimation
        mRevealAnimDuration = revealAnimDuration
        mRemovingAnimDuration = removalAnimDuration
        mRemoveOnBackPress = removeOnBackPress
        mAutoRemoveOnClickEvent = removePresenterOnAnyClickEvent
        present()
        return this as T
    }


    /**
     * This method is called only after you've finished
     * propagating data for this [PresentationBuilder] through the [PresentationBuilder.set] method
     * It displays the [mPresenter] inside a [DecorView][ViewGroup]
     */
    private fun present() = mainThread {
        mViewToPresent?.let { _ ->
            val removeAndBuildJob = async {
                removePresenterIfPresent()
                val buildJob = async {
                    // This is when the presenter shape builds itself
                    mPresenterShape.buildSelfWith(this@PresentationBuilder)
                }
                buildJob.await()
                buildJob.join()
            }
            removeAndBuildJob.await()
            removeAndBuildJob.join()
            if (removeAndBuildJob.isCompleted) {
                mPresenter?.let { mDecorView?.addView(it) }
            }
        }
    }

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
        mPresenter?.let {
            if (mState == Presenter.STATE_REMOVING && mState != Presenter.STATE_REMOVED) {
                fadeOut(it, mRemovingAnimDuration) {
                    mDecorView?.removeView(it)
                    onPresenterStateChanged(Presenter.STATE_REMOVED)
                    finish()
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
}