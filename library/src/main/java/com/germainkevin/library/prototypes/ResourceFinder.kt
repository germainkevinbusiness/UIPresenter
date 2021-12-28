package com.germainkevin.library.prototypes

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes

/**
 * Accesses a [androidx.lifecycle.LifecycleOwner]'s environment when implemented
 * @see [com.germainkevin.library.prototype_impl.resource_finders.ActivityResourceFinder]
 * @see [com.germainkevin.library.prototype_impl.resource_finders.FragmentResourceFinder]
 */
interface ResourceFinder {

    fun getDecorView(): ViewGroup

    fun findViewById(@IdRes id: Int): View?

    fun getContext(): Context
}