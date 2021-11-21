package com.germainkevin.library.prototype_impl.resource_finders

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import com.germainkevin.library.prototypes.ResourceFinder

/**
 * Gives access to an [Activity]'s environment
 * */
class ActivityResourceFinder constructor(activity: Activity) : ResourceFinder {
    private var mActivity: Activity? = null

    init {
        mActivity = activity
    }

    override fun getDecorView(): ViewGroup? {
        mActivity?.let { return it.window.decorView as ViewGroup? } ?: return null
    }

    override fun findViewById(id: Int): View? {
        return mActivity?.let { return it.findViewById(id) } ?: return null
    }

    override fun getContext(): Context? {
        mActivity?.let { return it } ?: return null
    }

    override fun getResources(): Resources? {
        mActivity?.let { return it.resources } ?: return null
    }
}