package com.germainkevin.library

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.germainkevin.library.Presenter.Companion.STATE_NOT_SHOWN
import com.germainkevin.library.Presenter.Companion.STATE_REMOVED
import com.germainkevin.library.Presenter.Companion.STATE_REMOVING
import com.germainkevin.library.Presenter.Companion.STATE_REVEALING
import com.germainkevin.library.prototype_impl.PresentationBuilder
import com.germainkevin.library.prototype_impl.resource_finders.ActivityResourceFinder
import com.germainkevin.library.prototype_impl.resource_finders.FragmentResourceFinder
import com.germainkevin.library.prototypes.ResourceFinder
import timber.log.Timber

/**
 * [Builder class][Class] whose purpose is to display a [Presenter] that helps
 * explain a [View]'s role in the UI.
 * @author Kevin Germain
 * */
class UIPresenter : PresentationBuilder<UIPresenter> {
    constructor(resourceFinder: ResourceFinder) : super(resourceFinder = resourceFinder)

    constructor(activity: Activity) : super(resourceFinder = ActivityResourceFinder(activity))

    constructor(fragment: Fragment) : super(resourceFinder = FragmentResourceFinder(fragment))
}