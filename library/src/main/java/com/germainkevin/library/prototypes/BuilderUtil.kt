package com.germainkevin.library.prototypes

/**
 * To make it possible for the library to be usable in Java, I use this approach so that listening
 * to state changes in java, inside the [com.germainkevin.library.UIPresenter.mPresenter] be possible
 * */
interface BuilderUtil {
    fun removePresenterIfPresent()
}