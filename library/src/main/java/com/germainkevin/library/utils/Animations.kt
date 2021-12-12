package com.germainkevin.library.utils

import android.view.ViewAnimationUtils
import androidx.core.view.isVisible
import com.germainkevin.library.presenter_view.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 * This animation runs in a [Presenter] after its state changes to [Presenter.STATE_CANVAS_DRAWN]
 * This is the animation that runs when the [Presenter] is being added to your UI's decorView
 *
 * Extend this interface to create your own reveal animation
 * */
interface RevealAnimation {
    fun runAnimation(
        coroutineScope: CoroutineScope, // A scope to run your animation in, if you want
        presenter: Presenter, // The presenter the reveal animation will run on
        revealAnimationDuration: Long, // The duration of the animation
        afterAnim: () -> Unit // When called that means we can safely consider this animation to be done
    )
}

/**
 * This animation runs in a [Presenter] after its state changes to [Presenter.STATE_REMOVING]
 * This is the animation that runs when the [Presenter] is being removed from your UI's decorView
 * */
interface RemoveAnimation {
    fun runAnimation(
        coroutineScope: CoroutineScope,
        presenter: Presenter,
        removeAnimationDuration: Long,
        afterAnim: () -> Unit
    )
}

// This one doesn't need to be launched on a coroutine and waited for end result
// It goes straight to visible before the animation even starts
class CircularRevealAnimation : RevealAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope,
        presenter: Presenter,
        revealAnimationDuration: Long,
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

class RotationXByAnimation : RevealAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope,
        presenter: Presenter,
        revealAnimationDuration: Long,
        afterAnim: () -> Unit
    ) {
        presenter.animate().apply {
            duration = revealAnimationDuration
            rotationXBy(360f)
        }.start()
        afterAnim()
    }
}

class RotationYByAnimation : RevealAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope,
        presenter: Presenter,
        revealAnimationDuration: Long,
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

class NoRevealAnimation : RevealAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope,
        presenter: Presenter,
        revealAnimationDuration: Long,
        afterAnim: () -> Unit
    ) = afterAnim()
}

class FadeOutAnimation : RemoveAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope,
        presenter: Presenter,
        removeAnimationDuration: Long,
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