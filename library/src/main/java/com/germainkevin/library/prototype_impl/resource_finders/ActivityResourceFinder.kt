package com.germainkevin.library.prototype_impl.resource_finders

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.germainkevin.library.prototypes.ResourceFinder

/** Gives access to an [Activity]'s environment */
internal class ActivityResourceFinder(private val activity: Activity) : ResourceFinder {

    override fun getDecorView(): ViewGroup = activity.window?.decorView as ViewGroup

    override fun findViewById(id: Int): View? = activity.findViewById(id)

    override fun getContext(): Context = activity
}