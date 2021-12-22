package com.germainkevin.library.prototype_impl

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.OvershootInterpolator
import androidx.core.view.isVisible
import com.germainkevin.library.Presenter
import com.germainkevin.library.prototypes.PresenterAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 *  This animation doesn't need to be launched on a coroutine
 *  and delayed for "animationDuration". Because it's visible before
 *  the animation even starts and doing so would make the animation glitch
 */
class CircularReveal : PresenterAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, animationDuration: Long,
        afterAnim: () -> Unit
    ) {
        val cx: Int = presenter.width / 2
        val cy: Int = presenter.height / 2
        val endRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(presenter, cx, cy, 0f, endRadius)
        anim.duration = animationDuration
        presenter.isVisible = true
        anim.start()
        afterAnim()
    }
}

/**
 * Unless you specify your own [PresenterAnimation], this is the default [PresenterAnimation]
 * that runs when the [Presenter] is being added by the [DecorView][android.view.ViewGroup]
 * */
class FadeIn : PresenterAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, animationDuration: Long,
        afterAnim: () -> Unit
    ) {
        coroutineScope.launch {
            val alphaAnimation = ObjectAnimator.ofFloat(presenter, View.ALPHA, 0.0f, 1.1f)
            alphaAnimation.duration = animationDuration
            val animatorSet = AnimatorSet()
            animatorSet.play(alphaAnimation)
            animatorSet.start()
            delay(animationDuration)
            afterAnim()
        }
    }
}

/** Fades in and scales [View.SCALE_X] & [View.SCALE_Y] for the animationDuration delay */
class FadeInAndScale : PresenterAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, animationDuration: Long,
        afterAnim: () -> Unit
    ) {
        coroutineScope.launch {
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f, 1f)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f, 1f)
            val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
            val objectAnimator =
                ObjectAnimator.ofPropertyValuesHolder(presenter, scaleX, scaleY, alpha)
            objectAnimator.interpolator = OvershootInterpolator()
            objectAnimator.duration = animationDuration
            objectAnimator.start()
            delay(animationDuration)
            afterAnim()
        }
    }
}

/**
 * A fade out animation when the [Presenter] is being removed from
 * the [DecorView][android.view.ViewGroup]
 * */
class FadeOut : PresenterAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, animationDuration: Long,
        afterAnim: () -> Unit
    ) {
        coroutineScope.launch {
            presenter.animate().apply {
                duration = animationDuration
                alpha(0f)
            }.start()
            delay(animationDuration)
            afterAnim()
        }
    }
}

/** When you don't want an animation to run on the [Presenter] */
class NoAnimation : PresenterAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, animationDuration: Long,
        afterAnim: () -> Unit
    ) = afterAnim()
}

/**
 * This animation will cause the [Presenter]'s <code>rotationY</code> property to
 * be animated by 360f, when it's being added to the [DecorView][android.view.ViewGroup].
 * */
class HorizontalRotation : PresenterAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, animationDuration: Long,
        afterAnim: () -> Unit
    ) {
        coroutineScope.launch {
            presenter.animate().apply {
                duration = animationDuration
                rotationYBy(360f)
            }.start()
            delay(animationDuration)
            afterAnim()
        }
    }
}

/**
 * This animation will cause the [Presenter]'s <code>rotationX</code> property to
 * be animated by 360f, when it's being added to the [DecorView][android.view.ViewGroup].
 * */
class VerticalRotation : PresenterAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, animationDuration: Long,
        afterAnim: () -> Unit
    ) {
        presenter.animate().apply {
            duration = animationDuration
            rotationXBy(360f)
        }.start()
        afterAnim()
    }
}