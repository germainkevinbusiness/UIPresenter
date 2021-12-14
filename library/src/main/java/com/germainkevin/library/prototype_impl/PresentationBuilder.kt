package com.germainkevin.library.prototype_impl

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.germainkevin.library.PresenterShadowLayer
import com.germainkevin.library.mainThread
import com.germainkevin.library.presenter_view.Presenter
import com.germainkevin.library.prototype_impl.presentation_shapes.SquircleShape
import com.germainkevin.library.prototypes.PresenterShape
import com.germainkevin.library.prototypes.RemoveAnimation
import com.germainkevin.library.prototypes.ResourceFinder
import com.germainkevin.library.prototypes.RevealAnimation
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
     * Exposes the current state of this [mPresenter]
     */
    @Presenter.PresenterState
    private var mState = Presenter.STATE_NOT_SHOWN

    /**
     * Exposes state changes from the [mPresenter] to the user of this library
     */
    private var mPresenterStateChangeListener: (Int, () -> Unit) -> Unit =
        { _: Int, _: () -> Unit -> }

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
    internal var mPresenterShape: PresenterShape = SquircleShape()

    // The background color of the mPresenterShape
    internal var mBackgroundColor: Int? = null

    // Should the mPresenterShape contain a shadowLayer
    internal var mHasShadowLayer = true

    // Shadow layer configs
    internal var presenterShadowLayer = PresenterShadowLayer()

    // description text configs
    internal var mDescriptionText: String? = null

    /**
     * Desired text size to be displayed in a [TypedValue] unit
     * */
    internal var mDescriptionTextSize: Float = 18f
    internal var mDescriptionTextColor: Int? = null

    /**
     * [TypedValue] unit in which the [mDescriptionText] should be displayed.
     * Usually a text on android is displayed in the [TypedValue.COMPLEX_UNIT_SP] unit
     */
    internal var mDescriptionTextUnit: Int = TypedValue.COMPLEX_UNIT_SP

    /**
     * The [Typeface] to use for this [mDescriptionText]
     * */
    internal var mTypeface = Typeface.DEFAULT

    // Created so that click events inside the [mPresenter] only get propagated
    // when the reveal animation is done running
    // This variable will be accessed from the [mPresenter]
    internal var isRevealAnimationDone = false

    /**
     * Created to launch animations that need a [CoroutineScope]
     * */
    private lateinit var coroutineScope: CoroutineScope

    /**
     * Should the Presenter's whole View have a shadowed background
     * */
    private var mPresenterHasShadowedWindow: Boolean = false

    /**
     * Gives default colors to [mBackgroundColor] and [mDescriptionTextColor]
     * if they are not set in the [PresentationBuilder.set] method
     * */
    private fun Context.provideDefaultColors() {
        when (this.resources.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                mBackgroundColor = Color.WHITE
                mDescriptionTextColor = Color.BLACK
            }
            else -> {
                mBackgroundColor = Color.BLACK
                mDescriptionTextColor = Color.WHITE
            }
        }
    }

    init {
        mDecorView = resourceFinder.getDecorView()
        resourceFinder.getContext()?.let { context ->
            coroutineScope = (context as LifecycleOwner).lifecycleScope
            if (mBackgroundColor == null && mDescriptionTextColor == null) {
                context.provideDefaultColors()
            }
            mPresenter = Presenter(context)
            with(mPresenter!!) {
                mPresentationBuilder = this@PresentationBuilder
                mPresenterStateChangeNotifier = object : Presenter.StateChangeNotifier {
                    override fun onStateChange(state: Int) {
                        onPresenterStateChanged(state)
                        when (state) {
                            Presenter.STATE_CANVAS_DRAWN -> {
                                mPresenterRevealAnimation
                                    .runAnimation(coroutineScope, this@with, mRevealAnimDuration) {
                                        isRevealAnimationDone = true
                                        if (mPresenterHasShadowedWindow) {
                                            mPresenterRevealAnimation = NoRevealAnimation()
                                            mPresenterRevealAnimation.runAnimation(
                                                coroutineScope,
                                                this@with,
                                                mRevealAnimDuration
                                            ) {
                                                // Transparent-like color
                                                setBackgroundColor(Color.parseColor("#80000000"))
                                                onPresenterStateChanged(Presenter.STATE_REVEALED)
                                            }
                                        } else {
                                            onPresenterStateChanged(Presenter.STATE_REVEALED)
                                        }
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
     * @param presenterHasShadowedWindow Should the [mPresenter]'s whole View on screen have a shadowed background
     * @param shadowLayer Sets a Shadow layer for the [mPresenter]
     * @param descriptionText The text that describes the view you want to present
     * @param descriptionTextColor The text color of the description text
     * @param descriptionTextSize The desired text size of the description text
     * @param descriptionTextUnit The unit you want your description text to be in
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
        backgroundColor: Int = mBackgroundColor!!,
        hasShadowLayer: Boolean = mHasShadowLayer,
        presenterHasShadowedWindow: Boolean = mPresenterHasShadowedWindow,
        shadowLayer: PresenterShadowLayer = presenterShadowLayer,
        descriptionText: String,
        descriptionTextColor: Int = mDescriptionTextColor!!,
        descriptionTextSize: Float = mDescriptionTextSize,
        descriptionTextUnit: Int = mDescriptionTextUnit,
        descriptionTextTypeface: Typeface = mTypeface,
        revealAnimation: RevealAnimation = mPresenterRevealAnimation,
        removeAnimation: RemoveAnimation = mPresenterRemoveAnimation,
        revealAnimDuration: Long = mRevealAnimDuration,
        removalAnimDuration: Long = mRemovingAnimDuration,
        removeOnBackPress: Boolean = mRemoveOnBackPress,
        removePresenterOnAnyClickEvent: Boolean = mAutoRemoveOnClickEvent,
        presenterStateChangeListener: (Int, () -> Unit) -> Unit
    ): T {
        mPresenterStateChangeListener = presenterStateChangeListener
        mPresenterShape = presenterShape
        mViewToPresent = resourceFinder.findViewById(viewToPresentId)
        mIsViewToPresentSet = mViewToPresent != null
        mBackgroundColor = backgroundColor
        mHasShadowLayer = hasShadowLayer
        mPresenterHasShadowedWindow = presenterHasShadowedWindow
        presenterShadowLayer = shadowLayer
        mDescriptionText = descriptionText
        mDescriptionTextColor = descriptionTextColor
        mDescriptionTextSize = descriptionTextSize
        mDescriptionTextUnit = descriptionTextUnit
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
     * @param presenterHasShadowedWindow Should the [mPresenter]'s whole View on screen have a shadowed background
     * @param shadowLayer Sets a Shadow layer for the [mPresenter]. The shadowLayer only works if
     * hardware acceleration is disabled on a device
     * @param descriptionText The text that describes the view you want to present
     * @param descriptionTextColor The text color of the description text
     * @param descriptionTextSize The desired text size of the description text
     * @param descriptionTextUnit The unit you want your description text to be in
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
        backgroundColor: Int = mBackgroundColor!!,
        hasShadowLayer: Boolean = mHasShadowLayer,
        presenterHasShadowedWindow: Boolean = mPresenterHasShadowedWindow,
        shadowLayer: PresenterShadowLayer = presenterShadowLayer,
        descriptionText: String,
        descriptionTextColor: Int = mDescriptionTextColor!!,
        descriptionTextSize: Float = mDescriptionTextSize,
        descriptionTextUnit: Int = mDescriptionTextUnit,
        descriptionTextTypeface: Typeface = mTypeface,
        revealAnimation: RevealAnimation = mPresenterRevealAnimation,
        removeAnimation: RemoveAnimation = mPresenterRemoveAnimation,
        revealAnimDuration: Long = mRevealAnimDuration,
        removalAnimDuration: Long = mRemovingAnimDuration,
        removeOnBackPress: Boolean = mRemoveOnBackPress,
        removePresenterOnAnyClickEvent: Boolean = mAutoRemoveOnClickEvent,
        presenterStateChangeListener: (Int, () -> Unit) -> Unit
    ): T {
        mPresenterStateChangeListener = presenterStateChangeListener
        mViewToPresent = viewToPresent
        mIsViewToPresentSet = mViewToPresent != null
        mPresenterShape = presenterShape
        mBackgroundColor = backgroundColor
        mHasShadowLayer = hasShadowLayer
        mPresenterHasShadowedWindow = presenterHasShadowedWindow
        presenterShadowLayer = shadowLayer
        mDescriptionText = descriptionText
        mDescriptionTextColor = descriptionTextColor
        mDescriptionTextSize = descriptionTextSize
        mDescriptionTextUnit = descriptionTextUnit
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
                mPresenter?.let {
                    mDecorView?.addView(it)
                }
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