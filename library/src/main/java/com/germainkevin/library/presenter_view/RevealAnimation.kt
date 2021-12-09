package com.germainkevin.library.presenter_view

/**
 * A set of animations we can apply to a [Presenter].
 * The animation is applied to a [Presenter] right
 * after its state changes to [Presenter.STATE_CANVAS_DRAWN]
 * */
enum class RevealAnimation {
    CIRCULAR_REVEAL, // This is the default animation
    FADE_IN,
    ROTATION_X,
    ROTATION_Y,
    NO_REVEAL_ANIMATION
}