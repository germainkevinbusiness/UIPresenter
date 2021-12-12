package com.germainkevin.library.prototype_impl

import android.view.ViewAnimationUtils
import androidx.core.view.isVisible
import com.germainkevin.library.presenter_view.Presenter
import com.germainkevin.library.prototypes.RemoveAnimation
import com.germainkevin.library.prototypes.RevealAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

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
 * When you don't want no reveal animation to launch
 * */
class NoRevealAnimation : RevealAnimation {
    override fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, revealAnimationDuration: Long,
        afterAnim: () -> Unit
    ) = afterAnim()
}

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