package com.germainkevin.library.prototypes

import com.germainkevin.library.presenter_view.Presenter
import kotlinx.coroutines.CoroutineScope

/**
 * This animation runs in a [Presenter] after its state changes to [Presenter.STATE_CANVAS_DRAWN]
 * This is the animation that runs when the [Presenter] is being added to your UI's decorView
 *
 * Extend this interface to create your own reveal animation
 * Here's an example: [RotationXByAnimation][com.germainkevin.library.prototype_impl.RotationXByAnimation]
 * @author Kevin Germain
 * */
interface RevealAnimation {
    /**
     * @param coroutineScope A scope to run your animation in, if you want
     * @param revealAnimationDuration The duration of the animation
     * @param presenter The Presenter is the View which presents your UI.
     * @param afterAnim When called that means we can safely consider this animation to be done
     *
     * If you do not call [afterAnim] when the animation is done, no click event will be propagated from
     * the [presenter] to the [com.germainkevin.library.prototype_impl.PresentationBuilder], making
     * it impossible for this library and you to remove the [presenter]
     * */
    fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, revealAnimationDuration: Long,
        afterAnim: () -> Unit
    )
}

/**
 * This animation runs in a [Presenter] after its state changes to [Presenter.STATE_REMOVING]
 * This is the animation that runs when the [Presenter] is being removed from your UI's decorView
 * Here's an example: [FadeOutAnimation][com.germainkevin.library.prototype_impl.FadeOutAnimation]
 * @author Kevin Germain
 * */
interface RemoveAnimation {
    /**
     * @param coroutineScope A scope to run your animation in, if you want
     * @param removeAnimationDuration The duration of the animation
     * @param presenter The Presenter is the View which presents your UI.
     * @param afterAnim When called that means we can safely consider this animation to be done
     *
     * If you do not call [afterAnim] when the animation is done, the [presenter] will not
     * be removed from the decor view
     * */
    fun runAnimation(
        coroutineScope: CoroutineScope, presenter: Presenter, removeAnimationDuration: Long,
        afterAnim: () -> Unit
    )
}