package com.germainkevin.library.prototypes

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes

/**
 * Created to be implemented by a class with an [android.app.Activity] or a [android.app.Fragment]
 * as parameter such as with
 * [ActivityResourceFinder][com.germainkevin.library.prototype_impl.resource_finders.ActivityResourceFinder]
 * and [FragmentResourceFinder][com.germainkevin.library.prototype_impl.resource_finders.FragmentResourceFinder]
 */
interface ResourceFinder {

    fun getDecorView(): ViewGroup

    fun findViewById(@IdRes id: Int): View?

    fun getContext(): Context
}