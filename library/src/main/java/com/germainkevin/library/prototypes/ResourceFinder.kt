package com.germainkevin.library.prototypes

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes

/**
 * Gives access to activities or fragments resources
 */
interface ResourceFinder {

    fun getDecorView(): ViewGroup?

    fun findViewById(@IdRes id: Int): View?

    fun getContext(): Context?

    fun getResources(): Resources?
}