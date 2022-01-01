package com.germainkevin.library.prototype_impl.presentation_shapes

import android.graphics.*
import android.os.Build
import android.text.StaticLayout
import com.germainkevin.library.buildStaticLayout
import com.germainkevin.library.UIPresenter
import com.germainkevin.library.prototypes.PresenterShape

/**
 * This is a [PresenterShape] that has the shape of a Squircle
 * @author Kevin Germain
 * */
class SquircleShape : PresenterShape() {
    /** The background or first visible non-transparent layer of the [SquircleShape] */
    private var squircleBackground = RectF()

    /**
     * Is laid out on the [squircleBackground] and takes care of laying out the
     * [UIPresenter.descriptionText][com.germainkevin.library.UIPresenter.descriptionText]
     * in multiline
     */
    private lateinit var staticLayout: StaticLayout

    /** Position of the [staticLayout] inside this [squircleBackground] */
    private var staticLayoutPosition = PointF()

    override fun shapeContains(x: Float, y: Float): Boolean = squircleBackground.contains(x, y)

    override fun buildSelfWith(builder: UIPresenter) {
        val decorView = builder.resourceFinder.getDecorView()
        shapeBackgroundPaint.color = builder.backgroundColor!!
        descriptionTextPaint.color = builder.descriptionTextColor!!
        descriptionTextPaint.typeface = builder.typeface
        setDescriptionTextSize(
            builder.descriptionTextUnit, builder.descriptionTextSize,
            decorView.resources.displayMetrics
        )

        builder.viewToPresent!!.getGlobalVisibleRect(vTPCoordinates)
        viewToPresentBounds.set(vTPCoordinates)

        hasShadowedWindow = builder.hasShadowedWindow
        if (hasShadowedWindow) {
            viewToPresentBounds.inset(-4f, -4f)
            decorView.getGlobalVisibleRect(decorViewCoordinates)
            shadowedWindow.set(decorViewCoordinates)
        }
        if (builder.hasShadowLayer) {
            builder.shadowLayer.apply {
                shapeBackgroundPaint.setShadowLayer(radius, dx, dy, shadowColor)
            }
        }

        val decorViewWidth: Int
        val decorViewHeight: Int

        if (builder.isLandscapeMode) {
            decorViewWidth = decorView.height
            decorViewHeight = decorView.width
        } else {
            decorViewWidth = decorView.width
            decorViewHeight = decorView.height
        }

        val descTextWidth = descriptionTextPaint.measureText(builder.descriptionText).toInt()

        // How much space is left when you place the description text from end to start
        val textWidthRemainingSpace1 = viewToPresentBounds.left - descTextWidth

        val percentageFromStart = viewToPresentBounds.left * 100 / decorViewWidth

        // Remaining Space between View to present's left position and the end of the decorViewWidth
        val vtpStartToDecorEnd = decorViewWidth - viewToPresentBounds.left

        val textWidthRemainingSpace2 = vtpStartToDecorEnd - descTextWidth

        val horizontalMargin = 56
        val shouldLayoutFromStartToEnd: Boolean
        var staticLayoutWidth: Int = (vtpStartToDecorEnd - horizontalMargin).toInt()

        when {
            textWidthRemainingSpace1 <= 0 -> {
                when {
                    textWidthRemainingSpace2 <= 0 -> {
                        if (percentageFromStart <= 45
                            && staticLayoutWidth <= decorViewWidth - horizontalMargin
                        ) {
                            staticLayoutWidth -= horizontalMargin
                            shouldLayoutFromStartToEnd = true
                        } else if (percentageFromStart <= 45
                            && staticLayoutWidth > decorViewWidth - horizontalMargin
                        ) {
                            shouldLayoutFromStartToEnd = true
                        } else {
                            // The % of the decorViewWidth occupied by the staticLayoutWidth
                            val c = staticLayoutWidth * 100 / decorViewWidth
                            if (c <= 45) {
                                // let's make staticLayoutWidth 65% of decor view's width
                                staticLayoutWidth = (65 * decorViewWidth / 100)
                                shouldLayoutFromStartToEnd = false
                            } else {
                                staticLayoutWidth -= horizontalMargin
                                shouldLayoutFromStartToEnd = false
                            }
                        }
                    }
                    else -> {
                        staticLayoutWidth -= horizontalMargin
                        shouldLayoutFromStartToEnd = true
                    }
                }
            }
            textWidthRemainingSpace1 >= horizontalMargin -> {
                staticLayoutWidth = descTextWidth + 16
                shouldLayoutFromStartToEnd = false
            }
            else -> {
                staticLayoutWidth = descTextWidth + 16
                shouldLayoutFromStartToEnd = false
            }
        }
        // Build Static layout as we are satisfied with its current width
        staticLayout =
            buildStaticLayout(builder.descriptionText, descriptionTextPaint, staticLayoutWidth)

        // Registers the position of the text inside the StaticLayout, so that it can later be
        // drawn and positioned by the canvas
        val sLTextPosition = PointF()

        // Will give us the exact final vertical position of the StaticLayout when laid out
        val slHeightFinalPosition = viewToPresentBounds.bottom + staticLayout.height

        // Remaining distance between the end of the screen and the StaticLayout's final
        // bottom position or final Height position
        val v = decorView.height - slHeightFinalPosition
        // How much percentage of the decorViewHeight is still available when the StaticLayout is
        // laid out vertically from up to down
        val vPercentage = v * 100 / decorViewHeight

        if (shouldLayoutFromStartToEnd) {
            // HERE STATIC LAYOUT IS LAID OUT: UP TO DOWN, START TO END
            if (vPercentage >= 15) {
                sLTextPosition.x = viewToPresentBounds.left + 16
                sLTextPosition.y = viewToPresentBounds.bottom + 32
                squircleBackground.set(
                    sLTextPosition.x - 16,
                    sLTextPosition.y - 16,
                    sLTextPosition.x + staticLayoutWidth.toFloat(),
                    sLTextPosition.y + (staticLayout.height + 16)
                )
                staticLayoutPosition = PointF(sLTextPosition.x, sLTextPosition.y)
            } else {
                // HERE STATIC LAYOUT IS LAID OUT: DOWN TO UP, START TO END
                sLTextPosition.x = viewToPresentBounds.left + 16
                sLTextPosition.y = viewToPresentBounds.top - (staticLayout.height + 32)

                squircleBackground.set(
                    sLTextPosition.x - 16,
                    sLTextPosition.y - 16,
                    sLTextPosition.x + (staticLayoutWidth + 16).toFloat(),
                    sLTextPosition.y + staticLayout.height + 16
                )
                staticLayoutPosition = PointF(squircleBackground.left + 16, sLTextPosition.y)
            }
        } else {
            // HERE STATIC LAYOUT IS LAID OUT: UP TO DOWN, END TO START
            if (vPercentage >= 15) {
                // the position of the text based on those conditions
                sLTextPosition.x = viewToPresentBounds.right - 16
                sLTextPosition.y = viewToPresentBounds.bottom + 32

                squircleBackground.set(
                    viewToPresentBounds.right - (staticLayoutWidth.toFloat() + 32),
                    sLTextPosition.y - 16,
                    sLTextPosition.x + 16,
                    sLTextPosition.y + (staticLayout.height + 16)
                )
                staticLayoutPosition = PointF(squircleBackground.left + 16, sLTextPosition.y)
            } else {
                // HERE STATIC LAYOUT IS LAID OUT: DOWN TO UP, END TO START
                sLTextPosition.x = viewToPresentBounds.right
                sLTextPosition.y = viewToPresentBounds.top - (staticLayout.height + 32)

                squircleBackground.set(
                    sLTextPosition.x - (staticLayoutWidth + 32).toFloat(),
                    sLTextPosition.y - 16,
                    sLTextPosition.x,
                    sLTextPosition.y + staticLayout.height + 16
                )
                staticLayoutPosition = PointF(squircleBackground.left + 16, sLTextPosition.y)
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
            drawRoundRect(squircleBackground, 15f, 15f, shapeBackgroundPaint)
            translate(staticLayoutPosition.x, staticLayoutPosition.y)
            staticLayout.draw(canvas)
            restore()
        }
    }
}