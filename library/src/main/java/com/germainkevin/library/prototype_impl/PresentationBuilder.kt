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

    /** The animation that runs when adding the [mPresenter] to the [mDecorView]
     */
    private var mPresenterRevealAnimation: RevealAnimation = CircularRevealAnimation()

    /** The animation that runs when removing the [mPresenter] from the [mDecorView]
     */
    private var mPresenterRemoveAnimation: RemoveAnimation = FadeOutAnimation()

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

    // The background color of the mPresenterShape
    internal var mBackgroundColor = Color.BLACK

    // Should the mPresenterShape contain a shadowLayer
    internal var mHasShadowLayer = true

    // Shadow layer configs
    internal var shadowLayerRadius = 8f
    internal var shadowLayerDx = 0f
    internal var shadowLayerDy = 1f
    internal var shadowLayerColor = Color.DKGRAY

    // description text configs
    internal var mDescriptionText: String? = null

    /**
     * Desired text size to be displayed in a [TypedValue] unit
     * */
    internal var mDescriptionTextSize: Float = 18f
    internal var mDescriptionTextColor = Color.WHITE

    /**
     * [TypedValue] unit in which the [mDescriptionText] should be displayed.
     * Usually a text on android is displayed in the [TypedValue.COMPLEX_UNIT_SP] unit
     */
    internal var mTypedValueUnit: Int = TypedValue.COMPLEX_UNIT_SP

    /**
     * The [Typeface] to use for this [mDescriptionText]
     * */
    internal var mTypeface = Typeface.DEFAULT

    /**
     * Created to launch animations that need a [CoroutineScope]
     * */
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

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
                            mPresenterRevealAnimation
                                .runAnimation(coroutineScope, it, mRevealAnimDuration) {
                                    onPresenterStateChanged(Presenter.STATE_REVEALED)
                                }
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
     * This method propagates state changes from the [mPresenter]
     */
    private fun onPresenterStateChanged(@Presenter.PresenterState state: Int) {
        mState = state
        mPresenterStateChangeListener(state) { removingPresenter() }
    }

    /**
     * @param viewToPresentId The id of the view you want to present to a user
     * @param presenterShape The shape you want the [mPresenter] to be in, when added to your [mDecorView]
     * @param backgroundColor The background color of the [mPresenter]
     * @param hasShadowLayer Sets whether the presenter should have a shadow layer of not
     * @param shadowLayer Sets a Shadow layer for the [mPresenter]
     * @param descriptionText The text that describes the view you want to present
     * @param descriptionTextColor The text color of the description text
     * @param descriptionTextSize The desired text size of the description text
     * @param typedValueUnit The unit you want your description text to be in
     * texts are usually in the [TypedValue.COMPLEX_UNIT_SP] unit on android
     * @param descriptionTextTypeface The typeface you want your description text to be in
     * @param revealAnimation The animation that runs when adding the [mPresenter] to the [mDecorView]
     * @param revealAnimDuration The duration of the reveal animation
     * @param removeAnimation The animation that runs when removing the [mPresenter] from the [mDecorView]
     * @param removalAnimDuration The duration of the remove animation
     * @param removeOnBackPress Sets whether the presenter should be removed from the [mDecorView]
     * when [android.app.Activity.onBackPressed] is detected or not
     * @param removePresenterOnAnyClickEvent Sets whether any click event detected anywhere on the screen
     * or even [android.app.Activity.onBackPressed] should result in the removal of the [mPresenter] or not
     * @param presenterStateChangeListener This listener, listens for state changes inside the [mPresenter]
     * */
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
        descriptionTextTypeface: Typeface = mTypeface,
        revealAnimation: RevealAnimation = mPresenterRevealAnimation,
        removeAnimation: RemoveAnimation = mPresenterRemoveAnimation,
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
        mTypeface = descriptionTextTypeface
        mPresenterRevealAnimation = revealAnimation
        mPresenterRemoveAnimation = removeAnimation
        mRevealAnimDuration = revealAnimDuration
        mRemovingAnimDuration = removalAnimDuration
        mRemoveOnBackPress = removeOnBackPress
        mAutoRemoveOnClickEvent = removePresenterOnAnyClickEvent
        present()
        return this as T
    }

    /**
     * @param viewToPresent The view you want to present to a user
     * @param presenterShape The shape you want the [mPresenter] to be in, when added to your [mDecorView]
     * @param backgroundColor The background color of the [mPresenter]
     * @param hasShadowLayer Sets whether the presenter should have a shadow layer of not
     * @param shadowLayer Sets a Shadow layer for the [mPresenter]
     * @param descriptionText The text that describes the view you want to present
     * @param descriptionTextColor The text color of the description text
     * @param descriptionTextSize The desired text size of the description text
     * @param typedValueUnit The unit you want your description text to be in
     * texts are usually in the [TypedValue.COMPLEX_UNIT_SP] unit on android
     * @param descriptionTextTypeface The typeface you want your description text to be in
     * @param revealAnimation The animation that runs when adding the [mPresenter] to the [mDecorView]
     * @param revealAnimDuration The duration of the reveal animation
     * @param removeAnimation The animation that runs when removing the [mPresenter] from the [mDecorView]
     * @param removalAnimDuration The duration of the remove animation
     * @param removeOnBackPress Sets whether the presenter should be removed from the [mDecorView]
     * when [android.app.Activity.onBackPressed] is detected or not
     * @param removePresenterOnAnyClickEvent Sets whether any click event detected anywhere on the screen
     * or even [android.app.Activity.onBackPressed] should result in the removal of the [mPresenter] or not
     * @param presenterStateChangeListener This listener, listens for state changes inside the [mPresenter]
     * */
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
        descriptionTextTypeface: Typeface = mTypeface,
        revealAnimation: RevealAnimation = mPresenterRevealAnimation,
        removeAnimation: RemoveAnimation = mPresenterRemoveAnimation,
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
        mTypeface = descriptionTextTypeface
        mPresenterRevealAnimation = revealAnimation
        mPresenterRemoveAnimation = removeAnimation
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
                mPresenterRemoveAnimation
                    .runAnimation(coroutineScope, it, mRemovingAnimDuration) {
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