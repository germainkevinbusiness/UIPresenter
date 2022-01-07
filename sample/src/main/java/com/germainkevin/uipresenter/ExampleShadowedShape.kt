package com.germainkevin.uipresenter

import android.graphics.Canvas
import android.graphics.Region
import android.os.Build
import com.germainkevin.library.UIPresenter
import com.germainkevin.library.prototypes.PresenterShape

/**
 * Just a dummy example of making your own shape for a [Presenter][com.germainkevin.library.Presenter]
 * This is a [PresenterShape] that has the shape of a shadowed Rectangle, with the same size as
 * the decorView
 * @author Kevin Germain
 * */
class ExampleShadowedShape : PresenterShape() {

    override fun shapeContains(x: Float, y: Float): Boolean {
        // Made to return true in this case, so that any click events on the [Presenter] be
        // considered as : Presenter.STATE_FOCAL_PRESSED
        return true
    }

    override fun buildSelfWith(builder: UIPresenter) {
        val decorView = builder.resourceFinder.getDecorView()

        hasShadowedWindow = builder.hasShadowedWindow

        if (hasShadowedWindow) {
            viewToPresentBounds.inset(-4f, -4f)
            decorView.getGlobalVisibleRect(decorViewCoordinates)
            shadowedWindow.set(decorViewCoordinates)
        }

        builder.viewToPresent!!.getGlobalVisibleRect(vTPCoordinates)
        viewToPresentBounds.set(vTPCoordinates)

        if (builder.hasShadowLayer) {
            builder.shadowLayer.apply {
                shapeBackgroundPaint.setShadowLayer(radius, dx, dy, shadowColor)
            }
        }
    }

    override fun onDrawInPresenterWith(canvas: Canvas?) {
        canvas?.apply {
            save()
            if (hasShadowedWindow) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) clipOutRect(viewToPresentBounds)
                else clipRect(viewToPresentBounds, Region.Op.DIFFERENCE)
                drawRect(shadowedWindow, shadowedWindowPaint)
            }
            restore()
        }
    }
}