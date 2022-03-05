package com.germainkevin.library.prototypes

import com.germainkevin.library.Presenter
import kotlinx.coroutines.CoroutineScope

/**
 * Implement this interface when you want to create your own animation to run on a [Presenter]
 *
 * By default, there are two animations that run inside a [Presenter]
 * (but you can change that, by implementing your own, when you implement this interface):
 *
 * One runs when it's being added by the [DecorView][android.view.ViewGroup], which is the
 * [FadeIn][com.germainkevin.library.prototype_impl.FadeIn] animation
 *
 * The other one runs, when it's being removed from the [DecorView][android.view.ViewGroup],
 * which is the [FadeOut][com.germainkevin.library.prototype_impl.FadeOut] animation
 *
 * They both implement this interface and are set
 * inside the [UIPresenter.set][com.germainkevin.library.UIPresenter.set] method, by default.
 *@author Kevin Germain
 * */
interface PresenterAnimation {
    /**
     * @param coroutineScope A lifecycle-aware coroutineScope to run your animation in, if you want
     * @param animationDuration The duration of the animation in milliseconds
     * @param presenter The Presenter is the [View][android.view.View] which presents your UI
     * and the [View][android.view.View] that your custom animation will animate.
     * @param afterAnim When called that means we can safely consider this animation to be done
     *
     * If you do not call [afterAnim] when the animation is done, no click event will be propagated
     * from the [presenter] to the [UIPresenter][com.germainkevin.library.UIPresenter],
     * making it impossible for this library and you, to remove the [presenter]
     * */
    fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, animationDuration: Long,
        afterAnim: () -> Unit
    )
}