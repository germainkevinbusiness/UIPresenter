package com.germainkevin.library.prototype_impl

import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import com.germainkevin.library.Presenter
import com.germainkevin.library.R
import com.germainkevin.library.UIPresenter
import com.germainkevin.library.prototype_impl.presentation_shapes.SquircleShape
import com.germainkevin.library.prototypes.PresenterShape
import com.germainkevin.library.prototypes.ResourceFinder
import kotlinx.coroutines.*


/**
 * Contains all the methods for presenting a UI element.
 * <p>
 * Provides data for [UIPresenter] and any [PresenterShape]s
 *</p>
 * @param T the subclass that extends this class
 */
abstract class PresentationBuilder<T : PresentationBuilder<T>> constructor(val resourceFinder: ResourceFinder) {

    /**
     * [DecorView][View] of [PresentationBuilder.mViewToPresent]
     */
    private var mDecorView: ViewGroup? = null

    /**
     * The [Presenter] that will be created and added by the [mDecorView]
     */
    private var mPresenter: Presenter? = null

    /**
     * Listens for state changes from [mPresenter]
     */
    private var mPresenterStateChangeListener: (Int) -> Unit = {}

    /**
     * The view to place the [presenter][Presenter] around.
     * Will be access from a [PresenterShape] for example [SquircleShape]
     */
    internal var mViewToPresent: View? = null

    /**
     * Has the [View] to present been set successfully.
     * true, if the [PresentationBuilder.mViewToPresent] is not null or false otherwise
     */
    internal var mIsViewToPresentSet = false

    /**
     * The [mViewToPresent] description text
     * */
    private var mVTPDescriptionText: String? = null

    /**
     * Should the back button press dismiss the prompt.
     */
    private var mBackButtonDismissEnabled = true

    private var mAutoRemoveApproval = true // Remove Presenter onClick on its focal area

    private var mPresenterShape: PresenterShape = SquircleShape(mVTPDescriptionText)

    init {
        mDecorView = resourceFinder.getDecorView()
        resourceFinder.getContext()?.let { mPresenter = Presenter(it) }
        mPresenter?.let {
            it.mPresentationBuilder = this
            it.mPresenterTouchEventListener = object : Presenter.TouchEventListener {
                override fun onViewToPresentPressed() {
                    if (mAutoRemoveApproval) {
                        removePresenterIfInView()
                    }
                }

                override fun onFocalPressed() {
                    if (mAutoRemoveApproval) {
                        removePresenterIfInView()
                    }
                }

                override fun onNonFocalPressed() {
                    if (mAutoRemoveApproval) {
                        removePresenterIfInView()
                    }
                }

                override fun onBackButtonPressed() {
                    if (mAutoRemoveApproval && mBackButtonDismissEnabled) {
                        removePresenterIfInView()
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
        CoroutineScope(Dispatchers.Main).launch {
            // By this time, every configuration necessary would have already
            // been done, we can now pass this builder to the PresenterShape
            // so it builds a Shape to output to the UI.
            mViewToPresent?.let {
                val job = async {
                    mPresenterShape.buildSelfWith(this@PresentationBuilder)
                    val job1 = async { removePresenterIfInView() }
                    job1.await()
                    job1.join()
                }
                job.await()
                job.join()
                if (job.isCompleted) {
                    this.cancel()
                    mPresenter?.let { _v ->
                        mDecorView?.addView(_v)
                    }
                }
            }
        }

        return this as T
    }

    /**
     * Removes the [mPresenter] from the [mDecorView]
     * @return the current [PresentationBuilder]
     * */
    open fun removePresenterIfInView(): T {
        mPresenter?.let {
            if (!it.isRemoved()) {
                it.clearFocus() // removes the focus on the Presenter
                it.notifyBuilderOfStateChange(Presenter.STATE_REMOVING)
                mDecorView?.removeView(it)
                it.notifyBuilderOfStateChange(Presenter.STATE_REMOVED)
            }
        }
        return this as T
    }

    /**
     * Sets a listener that listens to the [presenter][Presenter] state changes.
     * @param listener The listener to use
     */
    open fun setPresenterStateChangeListener(listener: (Int) -> Unit): T {
        mPresenterStateChangeListener = listener
        return this as T
    }

    /**
     * This method is only called from the [mPresenter] to notify this builder
     * of its state change
     */
    internal fun onPresenterStateChanged(@Presenter.PresenterState state: Int) {
        mPresenterStateChangeListener(state)
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
     * Returns the Default [PresenterShape]
     * */
    internal fun getPresenterShape(): PresenterShape = mPresenterShape

    /**
     * Sets the shape of the [Presenter] to be added to the UI
     */
    open fun setPresenterShape(mPresenterShape: PresenterShape): T {
        this.mPresenterShape = mPresenterShape
        return this as T
    }

    open fun setPresenterBackgroundColor(bgColor: Int): T {
        mPresenterShape.setBackgroundColor(bgColor)
        return this as T
    }

    open fun setDescriptionText(descriptionText: String?): T {
        mPresenterShape.descriptionText = descriptionText
        return this as T
    }

    open fun setDescriptionTextColor(textColor: Int): T {
        mPresenterShape.setTextColor(textColor)
        return this as T
    }

    open fun setDescriptionTextSize(typedValue: Int, textSize: Float): T {
        mPresenterShape.setTextSize(typedValue, textSize)
        return this as T
    }

    open fun setDescriptionTypeface(typeface: Typeface?): T {
        mPresenterShape.setTypeface(typeface)
        return this as T
    }

    /**
     * Sets whether or not a click on a
     * [Presenter] should result in the removal ot this [Presenter] from the DecorView
     */
    open fun setAutoRemoveApproval(autoRemoveApproval: Boolean): T {
        mAutoRemoveApproval = autoRemoveApproval
        return this as T
    }

    /**
     * Back button can be used to dismiss the prompt.
     * Default: true
     *
     * @param enabled True for back button dismiss enabled
     * @return This Builder object to allow for chaining of calls to set methods
     */
    open fun setBackButtonDismissEnabled(enabled: Boolean): T {
        mBackButtonDismissEnabled = enabled
        return this as T
    }
}