package com.germainkevin.library.prototypes

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import com.germainkevin.library.prototype_impl.resource_finders.ActivityResourceFinder
import com.germainkevin.library.prototype_impl.resource_finders.FragmentResourceFinder
import androidx.lifecycle.LifecycleOwner

/**
 * Accesses a [LifecycleOwner]'s environment when implemented
 * @see [ActivityResourceFinder]
 * @see [FragmentResourceFinder]
 */
interface ResourceFinder {

    fun getDecorView(): ViewGroup

    fun findViewById(@IdRes id: Int): View?

    fun getContext(): Context
}