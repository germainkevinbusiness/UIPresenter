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
class FragmentResourceFinder(private val fragment: Fragment) : ResourceFinder {

    override fun getDecorView(): ViewGroup? = fragment.activity?.window?.decorView as ViewGroup?

    override fun findViewById(id: Int): View? = fragment.activity?.findViewById(id)

    override fun getContext(): Context? = fragment.requireContext()

    override fun getResources(): Resources = fragment.resources
}