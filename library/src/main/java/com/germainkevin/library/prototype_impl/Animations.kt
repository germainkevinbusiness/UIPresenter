package com.germainkevin.library.prototype_impl

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.OvershootInterpolator
import androidx.core.view.isVisible
import com.germainkevin.library.Presenter
import com.germainkevin.library.prototypes.RemoveAnimation
import com.germainkevin.library.prototypes.RevealAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 * This is the default [RevealAnimation] that runs when the [Presenter] is being added by a
 * [DecorView][android.view.ViewGroup]
 * */
class CircularRevealAnimation : RevealAnimation {
    /**
     *  This animation doesn't need to be launched on a coroutine
     *  and delayed for [revealAnimationDuration]. Because it's visible before
     *  the animation even starts and doing so would make the animation glitch
     */
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, revealAnimationDuration: Long,
        afterAnim: () -> Unit
    ) {
        val cx: Int = presenter.width / 2
        val cy: Int = presenter.height / 2
        val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(presenter, cx, cy, 0f, finalRadius)
        anim.duration = revealAnimationDuration
        presenter.isVisible = true
        anim.start()
        afterAnim()
    }
}

/**
 * This animation will animate the visibility of the [Presenter] from invisible to visible
 * according to the revealAnimationDuration defined
 * */
class FadeInAnimation : RevealAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, revealAnimationDuration: Long,
        afterAnim: () -> Unit
    ) {
        coroutineScope.launch {
            val alphaAnimation = ObjectAnimator.ofFloat(presenter, View.ALPHA, 0.0f, 1.1f)
            alphaAnimation.duration = revealAnimationDuration
            val animatorSet = AnimatorSet()
            animatorSet.play(alphaAnimation)
            animatorSet.start()
            delay(revealAnimationDuration)
            afterAnim()
        }
    }
}

/**
 * Fades in and scales [View.SCALE_X] & [View.SCALE_Y] for the revealAnimationDuration delay
 * */
class FadeInAndScale : RevealAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope,
        presenter: Presenter,
        revealAnimationDuration: Long,
        afterAnim: () -> Unit
    ) {
        coroutineScope.launch {
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f, 1f)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f, 1f)
            val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
            val objectAnimator =
                ObjectAnimator.ofPropertyValuesHolder(presenter, scaleX, scaleY, alpha)
            objectAnimator.interpolator = OvershootInterpolator()
            objectAnimator.duration = revealAnimationDuration
            objectAnimator.start()
            delay(revealAnimationDuration)
            afterAnim()
        }
    }
}

/**
 * This animation will cause the [Presenter]'s <code>rotationX</code> property to
 * be animated by 360f, when it's being added to the [DecorView][android.view.ViewGroup].
 * */
class RotationXByAnimation : RevealAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, revealAnimationDuration: Long,
        afterAnim: () -> Unit
    ) {
        presenter.animate().apply {
            duration = revealAnimationDuration
            rotationXBy(360f)
        }.start()
        afterAnim()
    }
}

/**
 * This animation will cause the [Presenter]'s <code>rotationY</code> property to
 * be animated by 360f, when it's being added to the [DecorView][android.view.ViewGroup].
 * */
class RotationYByAnimation : RevealAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, revealAnimationDuration: Long,
        afterAnim: () -> Unit
    ) {
        coroutineScope.launch {
            presenter.animate().apply {
                duration = revealAnimationDuration
                rotationYBy(360f)
            }.start()
            delay(revealAnimationDuration)
            afterAnim()
        }
    }
}

/**
 * When you don't want no reveal animation to run when your [Presenter] is being
 * added by the [DecorView][android.view.ViewGroup]
 * */
class NoRevealAnimation : RevealAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, revealAnimationDuration: Long,
        afterAnim: () -> Unit
    ) = afterAnim()
}

/**
 * A fade out animation when the [Presenter] is being removed from
 * the [DecorView][android.view.ViewGroup]
 * */
class FadeOutAnimation : RemoveAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, removeAnimationDuration: Long,
        afterAnim: () -> Unit
    ) {
        coroutineScope.launch {
            presenter.animate().apply {
                duration = removeAnimationDuration
                alpha(0f)
            }.start()
            delay(removeAnimationDuration)
            afterAnim()
        }
    }
}

/**
 * When you don't want no remove animation to run when a [Presenter] is being removed
 * from the [DecorView][android.view.ViewGroup]
 * */
class NoRemoveAnimation : RemoveAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, removeAnimationDuration: Long,
        afterAnim: () -> Unit
    ) = afterAnim()
}