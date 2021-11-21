package com.germainkevin.library.prototype_impl.resource_finders

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.germainkevin.library.prototypes.ResourceFinder

/**
 * Gives access to an [Fragment]'s environment
 * */
class FragmentResourceFinder constructor(fragment: Fragment) : ResourceFinder {
    private var mFragment: Fragment? = null

    init {
        mFragment = fragment
    }

    override fun getDecorView(): ViewGroup? {
        mFragment?.let { return it.activity?.window?.decorView as ViewGroup? } ?: return null
    }

    override fun findViewById(id: Int): View? {
        mFragment?.let { return it.activity?.findViewById(id) } ?: return null
    }

    override fun getContext(): Context? {
        mFragment?.let { return it.requireContext() } ?: return null
    }

    override fun getResources(): Resources? {
        mFragment?.let { return it.resources } ?: return null
    }
}