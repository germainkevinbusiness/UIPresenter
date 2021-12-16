package com.germainkevin.library

import android.app.Activity
import android.app.Dialog
import android.view.View
import androidx.fragment.app.Fragment
import com.germainkevin.library.prototype_impl.PresentationBuilder
import com.germainkevin.library.prototype_impl.resource_finders.ActivityResourceFinder
import com.germainkevin.library.prototype_impl.resource_finders.FragmentResourceFinder
import com.germainkevin.library.prototypes.ResourceFinder

/**
 * [Builder class][PresentationBuilder] that sets all the needed information to display a
 * [Presenter][com.germainkevin.library.presenter_view.Presenter].
 * @author Kevin Germain
 * */
class UIPresenter : PresentationBuilder<UIPresenter> {

    constructor(activity: Activity) : super(resourceFinder = ActivityResourceFinder(activity))

    constructor(fragment: Fragment) : super(resourceFinder = FragmentResourceFinder(fragment))
}