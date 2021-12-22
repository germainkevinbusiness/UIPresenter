package com.germainkevin.library

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.germainkevin.library.prototype_impl.FadeIn
import com.germainkevin.library.prototype_impl.FadeOut
import com.germainkevin.library.prototype_impl.presentation_shapes.SquircleShape
import com.germainkevin.library.prototype_impl.resource_finders.ActivityResourceFinder
import com.germainkevin.library.prototype_impl.resource_finders.FragmentResourceFinder
import com.germainkevin.library.prototypes.*
import kotlinx.coroutines.*

/**
 * Contains all the methods for presenting a [View] inside your [Activity] or [Fragment]
 * with a [Presenter].
 *
 * Provides data for [mPresenter] and [presenterShape] that are marked as internal variables.
 * @param resourceFinder is an interface implemented in the next two constructors.
 * @author Kevin Germain
 */
open class UIPresenter private constructor(val resourceFinder: ResourceFinder) {
    constructor(activity: Activity) : this(resourceFinder = ActivityResourceFinder(activity))
    constructor(fragment: Fragment) : this(resourceFinder = FragmentResourceFinder(fragment))

    /**
     * The view that the [mPresenter] will present.
     * Made public to be accessed by the [presenterShape] to build itself around it
     */
    internal var viewToPresent: View? = null

    /**
     * [DecorView][ViewGroup] of the [Activity] or [Fragment]
     * It is responsible for adding [mPresenter] to your UI
     */
    private var decorView: ViewGroup? = null

    /** The [Presenter] that will be created and added by the [decorView] */
    private var mPresenter: Presenter? = null

    /**
     * Tracks the state of the [mPresenter] so that the [removePresenterIfPresent] method
     * can work properly
     */
    @Presenter.PresenterState
    private var pState = Presenter.STATE_NOT_SHOWN

    /**
     * Exposes state changes from the [mPresenter] to the user of this library as first parameter
     * & also passes a [removingPresenter] function as second parameter
     */
    private var mPresenterStateChangeListener: (Int, () -> Unit) -> Unit =
        { _: Int, _: () -> Unit -> }

    /** Should the back button press cause the removal of the [mPresenter] from the [decorView].
     * Even if [removeAfterAnyClickEvent] is set to false
     * */
    private var removeOnBackPress = true

    /**
     * Should the [mPresenter] be removed automatically from the [decorView],
     * when any click event is detected on the [decorView]
     * */
    private var removeAfterAnyClickEvent = true

    /** The animation that runs when adding the [mPresenter] to the [decorView] */
    private var revealAnimation: PresenterAnimation = FadeIn()

    /** The animation that runs when removing the [mPresenter] from the [decorView] */
    private var removeAnimation: PresenterAnimation = FadeOut()

    /**The duration of the animation when revealing the [mPresenter] */
    private var revealAnimDuration = 1000L

    /** The duration of the animation when removing the [mPresenter] */
    private var removeAnimDuration = 600L

    /**
     * The [PresenterShape] for this [mPresenter]
     *
     * You can create your own by extending this class: [PresenterShape]
     *
     * Will be accessed by the [mPresenter] to call the [presenterShape]'s
     * [onDrawInPresenterWith][PresenterShape.onDrawInPresenterWith] method on
     * its [onDraw(canvas: Canvas)][Presenter.onDraw] function
     * */
    internal var presenterShape: PresenterShape = SquircleShape()

    /** The background color of the [presenterShape] */
    internal var backgroundColor: Int? = null

    /** The shadow layer to be applied on the [presenterShape] */
    internal var shadowLayer = ShadowLayer()

    /** Should the [presenterShape] contain a shadowLayer */
    internal var hasShadowLayer = true

    /**
     * Is checked by the [presenterShape]
     * The description text shown in your [mPresenter]
     * */
    internal var descriptionText: String = ""

    /**
     * [TypedValue] unit in which the [descriptionText] should be displayed.
     * Usually a text on android is displayed in the [TypedValue.COMPLEX_UNIT_SP] unit
     *
     * Will be accessed by the [presenterShape]
     */
    internal var descriptionTextUnit: Int = TypedValue.COMPLEX_UNIT_SP

    /**
     * Desired text size calculated with the [descriptionTextUnit]
     *
     * Will be accessed by the [presenterShape]
     * */
    internal var descriptionTextSize: Float = 18f

    /**
     * The text color of [descriptionText]
     *
     * Will be accessed by the [presenterShape]
     * */
    internal var descriptionTextColor: Int? = null

    /**
     * The [Typeface] to use for this [descriptionText]
     *
     * Will be accessed by the [presenterShape]
     * */
    internal var typeface = Typeface.DEFAULT

    /** Created so that click events inside the [mPresenter] only get propagated
     * when the reveal animation is done running
     *
     * This variable will be accessed by the [mPresenter]
     */
    internal var isRevealAnimationDone = false

    /**
     * Should the [mPresenter]'s whole View have a shadowed Rect() the same size as the [decorView]
     *
     * Will be accessed by the [presenterShape]
     * */
    internal var hasShadowedWindow: Boolean = true

    /** Will be set to true if the device's orientation is [Configuration.ORIENTATION_LANDSCAPE] */
    internal var isLandscapeMode = false

    private var lifecycleScope: CoroutineScope

    /**
     * Gives default colors to [backgroundColor] and [descriptionTextColor]
     * if they are not set in the [UIPresenter.set] method
     * */
    private fun Context.provideDefaultColors() {
        when (this.resources.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                backgroundColor = Color.WHITE
                descriptionTextColor = Color.BLACK
            }
            else -> {
                backgroundColor = Color.BLACK
                descriptionTextColor = Color.WHITE
            }
        }
    }

    init {
        decorView = resourceFinder.getDecorView()
        val context = resourceFinder.getContext()
        context.apply {
            isLandscapeMode =
                resources.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE
            lifecycleScope = (this as LifecycleOwner).lifecycleScope
            if (backgroundColor == null && descriptionTextColor == null) provideDefaultColors()
            mPresenter = Presenter(this)
        }
        mPresenter!!.apply {
            uiPresenter = this@UIPresenter
            stateChangeNotifier = object : Presenter.StateChangeNotifier {
                override fun onStateChange(state: Int) {
                    onPresenterStateChanged(state)
                    when (state) {
                        Presenter.STATE_CANVAS_DRAWN -> revealAnimation.runAnimation(
                            lifecycleScope, this@apply, revealAnimDuration
                        ) {
                            isRevealAnimationDone = true
                            onPresenterStateChanged(Presenter.STATE_REVEALED)
                        }
                        Presenter.STATE_BACK_BUTTON_PRESSED -> {
                            if (removeAfterAnyClickEvent && removeOnBackPress) removingPresenter()
                            else if (!removeAfterAnyClickEvent && removeOnBackPress) removingPresenter()
                        }
                        Presenter.STATE_VTP_PRESSED,
                        Presenter.STATE_FOCAL_PRESSED, Presenter.STATE_NON_FOCAL_PRESSED -> {
                            if (removeAfterAnyClickEvent) removingPresenter()
                        }
                    }
                }
            }
        }
    }

    /** This method propagates state changes from the [mPresenter] */
    private fun onPresenterStateChanged(@Presenter.PresenterState state: Int) {
        pState = state
        mPresenterStateChangeListener(state) { removingPresenter() }
    }

    /**
     * @param viewToPresentId The id of the [View] you want to present to a user
     * @param presenterShape The shape you want the [mPresenter] to be in, when added to your [decorView]
     * @param backgroundColor The background color of the [mPresenter]
     * @param hasShadowLayer Sets whether the presenter should have a shadow layer of not
     * @param shadowedWindowEnabled Should the [mPresenter]'s whole View on screen have a shadowed background
     * @param shadowLayer Sets a Shadow layer for the [mPresenter].
     * The shadowLayer tend to be visible only when hardware acceleration is disabled on a user's device.
     * @param descriptionText The text that describes the view you want to present
     * @param descriptionTextColor The text color of the description text
     * @param descriptionTextSize The desired text size of the description text
     * @param descriptionTextUnit The unit you want your description text to be in
     * texts are usually in the [TypedValue.COMPLEX_UNIT_SP] unit on android
     * @param descriptionTextTypeface The typeface you want your description text to be in
     * @param revealAnimation The animation that runs when adding the [mPresenter] to the [decorView]
     * @param revealAnimDuration The duration of the reveal animation in milliseconds
     * @param removeAnimation The animation that runs when removing the [mPresenter] from the [decorView]
     * @param removeAnimDuration The duration of the remove animation in milliseconds
     * @param removeOnBackPress Sets whether the presenter should be removed from the [decorView]
     * even if [removeAfterAnyDetectedClickEvent] is set to false
     * when [android.app.Activity.onBackPressed] is detected or not
     * @param removeAfterAnyDetectedClickEvent Sets whether any click event detected anywhere on the screen
     * or even [android.app.Activity.onBackPressed] should result in the removal of the [mPresenter] or not
     * @param presenterStateChangeListener This listener, listens for state changes inside the [mPresenter]
     * */
    open fun set(
        @IdRes viewToPresentId: Int,
        presenterShape: PresenterShape = this.presenterShape,
        @ColorInt backgroundColor: Int = this.backgroundColor!!,
        hasShadowLayer: Boolean = this.hasShadowLayer,
        shadowedWindowEnabled: Boolean = hasShadowedWindow,
        shadowLayer: ShadowLayer = this.shadowLayer,
        descriptionText: String,
        @ColorInt descriptionTextColor: Int = this.descriptionTextColor!!,
        descriptionTextSize: Float = this.descriptionTextSize,
        descriptionTextUnit: Int = this.descriptionTextUnit,
        descriptionTextTypeface: Typeface = typeface,
        revealAnimation: PresenterAnimation = this.revealAnimation,
        removeAnimation: PresenterAnimation = this.removeAnimation,
        revealAnimDuration: Long = this.revealAnimDuration,
        removeAnimDuration: Long = this.removeAnimDuration,
        removeOnBackPress: Boolean = this.removeOnBackPress,
        removeAfterAnyDetectedClickEvent: Boolean = removeAfterAnyClickEvent,
        presenterStateChangeListener: (Int, () -> Unit) -> Unit
    ): UIPresenter {
        mPresenterStateChangeListener = presenterStateChangeListener
        this.presenterShape = presenterShape
        viewToPresent = resourceFinder.findViewById(viewToPresentId)
        this.backgroundColor = backgroundColor
        this.hasShadowLayer = hasShadowLayer
        hasShadowedWindow = shadowedWindowEnabled
        this.shadowLayer = shadowLayer
        this.descriptionText = descriptionText
        this.descriptionTextColor = descriptionTextColor
        this.descriptionTextSize = descriptionTextSize
        this.descriptionTextUnit = descriptionTextUnit
        typeface = descriptionTextTypeface
        this.revealAnimation = revealAnimation
        this.removeAnimation = removeAnimation
        this.revealAnimDuration = revealAnimDuration
        this.removeAnimDuration = removeAnimDuration
        this.removeOnBackPress = removeOnBackPress
        removeAfterAnyClickEvent = removeAfterAnyDetectedClickEvent
        present()
        return this
    }

    /**
     * @param viewToPresent The [View] you want to present to a user
     * @param presenterShape The shape you want the [mPresenter] to be in, when added to your [decorView]
     * @param backgroundColor The background color of the [mPresenter]
     * @param hasShadowLayer Sets whether the presenter should have a shadow layer of not
     * @param shadowedWindowEnabled Should the [mPresenter]'s whole View on screen have a shadowed background
     * @param shadowLayer Sets a Shadow layer for the [mPresenter].
     * The shadowLayer tend to be visible only when hardware acceleration is disabled on a user's device.
     * @param descriptionText The text that describes the view you want to present
     * @param descriptionTextColor The text color of the description text
     * @param descriptionTextSize The desired text size of the description text
     * @param descriptionTextUnit The unit you want your description text to be in
     * texts are usually in the [TypedValue.COMPLEX_UNIT_SP] unit on android
     * @param descriptionTextTypeface The typeface you want your description text to be in
     * @param revealAnimation The animation that runs when adding the [mPresenter] to the [decorView]
     * @param revealAnimDuration The duration of the reveal animation in milliseconds
     * @param removeAnimation The animation that runs when removing the [mPresenter] from the [decorView]
     * @param removeAnimDuration The duration of the remove animation in milliseconds
     * @param removeOnBackPress Sets whether the [mPresenter] should be removed from the [decorView]
     * even if [removeAfterAnyDetectedClickEvent] is set to false
     * when [android.app.Activity.onBackPressed] is detected or not
     * @param removeAfterAnyDetectedClickEvent Sets whether any click event detected anywhere on the screen
     * or even [onBackPressed][android.app.Activity.onBackPressed] should result in the removal of the [mPresenter] or not
     * @param presenterStateChangeListener This listener, listens for state changes inside the [mPresenter]
     * */
    open fun set(
        viewToPresent: View?,
        presenterShape: PresenterShape = this.presenterShape,
        @ColorInt backgroundColor: Int = this.backgroundColor!!,
        hasShadowLayer: Boolean = this.hasShadowLayer,
        shadowedWindowEnabled: Boolean = hasShadowedWindow,
        shadowLayer: ShadowLayer = this.shadowLayer,
        descriptionText: String,
        @ColorInt descriptionTextColor: Int = this.descriptionTextColor!!,
        descriptionTextSize: Float = this.descriptionTextSize,
        descriptionTextUnit: Int = this.descriptionTextUnit,
        descriptionTextTypeface: Typeface = typeface,
        revealAnimation: PresenterAnimation = this.revealAnimation,
        removeAnimation: PresenterAnimation = this.removeAnimation,
        revealAnimDuration: Long = this.revealAnimDuration,
        removeAnimDuration: Long = this.removeAnimDuration,
        removeOnBackPress: Boolean = this.removeOnBackPress,
        removeAfterAnyDetectedClickEvent: Boolean = removeAfterAnyClickEvent,
        presenterStateChangeListener: (Int, () -> Unit) -> Unit
    ): UIPresenter {
        mPresenterStateChangeListener = presenterStateChangeListener
        this.viewToPresent = viewToPresent
        this.presenterShape = presenterShape
        this.backgroundColor = backgroundColor
        this.hasShadowLayer = hasShadowLayer
        hasShadowedWindow = shadowedWindowEnabled
        this.shadowLayer = shadowLayer
        this.descriptionText = descriptionText
        this.descriptionTextColor = descriptionTextColor
        this.descriptionTextSize = descriptionTextSize
        this.descriptionTextUnit = descriptionTextUnit
        typeface = descriptionTextTypeface
        this.revealAnimation = revealAnimation
        this.removeAnimation = removeAnimation
        this.revealAnimDuration = revealAnimDuration
        this.removeAnimDuration = removeAnimDuration
        this.removeOnBackPress = removeOnBackPress
        removeAfterAnyClickEvent = removeAfterAnyDetectedClickEvent
        present()
        return this
    }

    /**
     * This method is called only after you've finished propagating data for this [UIPresenter]
     * through the [UIPresenter.set] method
     *
     * It mainly builds the [presenterShape] then displays the [mPresenter] inside the
     * [resourceFinder]'s [decorView][ResourceFinder.getDecorView]
     */
    private fun present() = lifecycleScope.launch {
        viewToPresent?.let { _ ->
            val removeAndBuildJob = async {
                removePresenterIfPresent()
                val buildJob = async { presenterShape.buildSelfWith(this@UIPresenter) }
                buildJob.await()
                buildJob.join()
            }
            removeAndBuildJob.await()
            removeAndBuildJob.join()
            if (removeAndBuildJob.isCompleted) {
                mPresenter?.apply { decorView?.addView(this) }
            }
        }
    }

    /** Called when removing the [mPresenter] */
    private fun removingPresenter() {
        onPresenterStateChanged(Presenter.STATE_REMOVING)
        removePresenterIfPresent()
    }

    /** Removes the [mPresenter] if present, from the [decorView] */
    private fun removePresenterIfPresent() = lifecycleScope.launch {
        mPresenter?.apply {
            if (pState == Presenter.STATE_REMOVING && pState != Presenter.STATE_REMOVED) {
                removeAnimation
                    .runAnimation(this@launch, this, removeAnimDuration) {
                        decorView?.removeView(this)
                        onPresenterStateChanged(Presenter.STATE_REMOVED)
                        finish()
                    }
            }
        }
    }

    // nullify all nullables, they are no longer being used
    private fun finish() {
        mPresenter = null
        viewToPresent = null
        backgroundColor = null
        descriptionTextColor = null
        decorView = null
    }
}