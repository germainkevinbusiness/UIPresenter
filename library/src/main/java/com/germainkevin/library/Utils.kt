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
fun circularReveal(mView: View, duration: Long?) {
    val cx: Int = mView.width / 2
    val cy: Int = mView.height / 2
    val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
    val anim = ViewAnimationUtils.createCircularReveal(mView, cx, cy, 0f, finalRadius)
    duration?.let { anim.duration = it }
    mView.isVisible = true
    anim.start()
}

fun createToast(context: Context, msg: String) =
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()