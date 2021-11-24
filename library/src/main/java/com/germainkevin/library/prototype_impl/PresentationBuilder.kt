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
 * Provides data for [mPresenter] and any [presenter shapes][PresenterShape]
 * Those data are marked as internal variables
 * @param T the subclass that extends this class
 */
abstract class PresentationBuilder<T : PresentationBuilder<T>>(val resourceFinder: ResourceFinder) {

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
    private var mPresenterStateChangeListener: (Int) -> Unit = {}

    /**
     * Should the back button press dismiss the [Presenter].
     */
    private var mBackButtonDismissEnabled = true

    /**
     * Should the [mPresenter] be removed when clicked on the screen while it's displayed
     * */
    private var mAutoRemoveApproval = true

    /**
     * The [PresenterShape] by default or set by the user for this [mPresenter]
     * */
    private var mPresenterShape: PresenterShape = SquircleShape()

    /**
     * The view that the [presenter][Presenter] will present.
     * Will be accessed from a [PresenterShape] for example [SquircleShape]
     */
    internal var mViewToPresent: View? = null

    /**
     * Has the [View] to present been set successfully.
     * true, if the [PresentationBuilder.mViewToPresent] is not null or false otherwise
     */
    internal var mIsViewToPresentSet = false

    init {
        mDecorView = resourceFinder.getDecorView()
        mPresenter = resourceFinder.getContext()?.let { Presenter(it) }?.also {
            it.mPresentationBuilder = this
            it.presenterShape = mPresenterShape
            it.mPresenterStateChangeNotifier = object : Presenter.StateChangeNotifier {
                override fun onPresenterStateChange(state: Int) {
                    onPresenterStateChanged(state)
                    when (state) {
                        Presenter.STATE_BACK_BUTTON_PRESSED -> {
                            if (mAutoRemoveApproval && mBackButtonDismissEnabled) {
                                onPresenterStateChanged(Presenter.STATE_REMOVING)
                                removePresenterIfPresent()
                            }
                        }
                        Presenter.STATE_VTP_PRESSED -> {
                            if (mAutoRemoveApproval) {
                                onPresenterStateChanged(Presenter.STATE_REMOVING)
                                removePresenterIfPresent()
                            }
                        }
                        Presenter.STATE_FOCAL_PRESSED -> {
                            if (mAutoRemoveApproval) {
                                onPresenterStateChanged(Presenter.STATE_REMOVING)
                                removePresenterIfPresent()
                            }
                        }
                        Presenter.STATE_NON_FOCAL_PRESSED -> {
                            if (mAutoRemoveApproval) {
                                onPresenterStateChanged(Presenter.STATE_REMOVING)
                                removePresenterIfPresent()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Is the [Presenter]'s current state: [STATE_REMOVING].
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
                    removePresenterIfPresent()
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
     * Removes the [mPresenter] if present, from the [mDecorView]
     * */
    private fun removePresenterIfPresent() {
        // Never reference mPresenter directly, always reference the mPresenter this way
        val mViewToRemove = mDecorView?.findViewById<View>(R.id.android_ui_presenter)
        mViewToRemove?.let {
            if (isRemoving() && !isRemoved()) {
                onPresenterStateChanged(Presenter.STATE_REMOVED)
                mDecorView?.removeView(it)
            }
        }
    }

    /**
     * This method is only called from the [mPresenter] to notify this builder
     * of its state change
     */
    private fun onPresenterStateChanged(@Presenter.PresenterState state: Int) {
        mState = state
        mPresenterStateChangeListener(state)
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

    open fun setBackgroundColor(bgColor: Int): T {
        mPresenterShape.setBackgroundColor(bgColor)
        return this as T
    }

    open fun setDescriptionText(descriptionText: String): T {
        mPresenterShape.setDescriptionText(descriptionText)
        return this as T
    }

    open fun setDescriptionTextColor(textColor: Int): T {
        mPresenterShape.setDescriptionTextColor(textColor)
        return this as T
    }

    open fun setDescriptionTextSize(typedValue: Int, textSize: Float): T {
        mPresenterShape.setDescriptionTextSize(typedValue, textSize)
        return this as T
    }

    open fun setDescriptionTypeface(typeface: Typeface?): T {
        mPresenterShape.setDescriptionTypeface(typeface)
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