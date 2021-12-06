package com.germainkevin.library

import android.content.Context
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.Toast
import androidx.core.view.isVisible
import com.germainkevin.library.prototype_impl.PresentationBuilder
import kotlin.math.hypot

/**
 * Creates a [ViewAnimationUtils.createCircularReveal] animation on a [View]
 * */
fun Presenter.circularReveal(duration: Long?) {
    val cx: Int = this.width / 2
    val cy: Int = this.height / 2
    val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
    val anim = ViewAnimationUtils.createCircularReveal(this, cx, cy, 0f, finalRadius)
    duration?.let { anim.duration = it }
    this.isVisible = true
    anim.start()
}

fun createToast(context: Context, msg: String) =
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()