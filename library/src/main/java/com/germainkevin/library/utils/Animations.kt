package com.germainkevin.library.utils

import android.view.ViewAnimationUtils
import androidx.core.view.isVisible
import com.germainkevin.library.presenter_view.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 * Creates a [ViewAnimationUtils.createCircularReveal] animation on a [Presenter]
 * */
fun Presenter.circularReveal(mDuration: Long) {
    val cx: Int = this.width / 2
    val cy: Int = this.height / 2
    val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
    val anim = ViewAnimationUtils.createCircularReveal(this, cx, cy, 0f, finalRadius)
    anim.duration = mDuration
    this.isVisible = true
    anim.start()
}

fun Presenter.rotationXByImpl(mDuration: Long) {
    animate().apply {
        duration = mDuration
        rotationXBy(360f)
    }.start()
}

fun Presenter.rotationYByImpl(mDuration: Long) {
    animate().apply {
        duration = mDuration
        rotationYBy(360f)
    }.start()
}

fun Presenter.fadeIn(fadeInDuration: Long) {
    animate().apply {
        duration = fadeInDuration
        alpha(1f)
    }.start()
}

fun CoroutineScope.fadeOut(presenter: Presenter?, fadeOutDuration: Long, afterAnim: () -> Unit) {
    launch {
        presenter?.let {
            it.animate().apply {
                duration = fadeOutDuration
                alpha(0f)
            }.start()
        }
        delay(fadeOutDuration)
        afterAnim()
    }
}