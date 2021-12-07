package com.germainkevin.library.prototype_impl.resource_finders

import android.app.Dialog
import android.content.Context

/**
 * Gives access to a [Dialog]'s environment
 * */
class DialogResourceFinder(private val dialog: Dialog) :
    ActivityResourceFinder(dialog.ownerActivity) {
    override fun getContext(): Context? = dialog.context
}