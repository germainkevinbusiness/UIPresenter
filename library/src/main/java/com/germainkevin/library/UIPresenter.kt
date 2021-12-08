package com.germainkevin.library

import android.app.Activity
import android.app.Dialog
import android.view.View
import androidx.fragment.app.Fragment
import com.germainkevin.library.prototype_impl.PresentationBuilder
import com.germainkevin.library.prototype_impl.resource_finders.ActivityResourceFinder
import com.germainkevin.library.prototype_impl.resource_finders.DialogResourceFinder
import com.germainkevin.library.prototype_impl.resource_finders.FragmentResourceFinder
import com.germainkevin.library.prototypes.ResourceFinder

/**
 * [Builder class][Class] whose purpose is to display a [Presenter] that helps
 * explain a [View]'s role in the UI.
 * @author Kevin Germain
 * */
class UIPresenter : PresentationBuilder<UIPresenter> {
    constructor(resourceFinder: ResourceFinder) : super(resourceFinder = resourceFinder)

    constructor(activity: Activity) : super(resourceFinder = ActivityResourceFinder(activity))

    constructor(dialog: Dialog) : super(resourceFinder = DialogResourceFinder(dialog))

    constructor(fragment: Fragment) : super(resourceFinder = FragmentResourceFinder(fragment))
}