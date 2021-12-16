package com.germainkevin.library.prototype_impl.presentation_shapes

import android.graphics.*
import android.os.Build
import android.text.StaticLayout
import com.germainkevin.library.buildStaticLayout
import com.germainkevin.library.PresentationBuilder
import com.germainkevin.library.prototypes.PresenterShape
import timber.log.Timber

/**
 * This is a [PresenterShape] that has the shape of a Squircle
 * @author Kevin Germain
 * */
class SquircleShape : PresenterShape() {
    /**
     * The first layer of the [SquircleShape], a rectangle that we will later on be the Squircle
     *
     * It's the background of the [SquircleShape]
     */
    private lateinit var mSquircleShapeRectF: RectF

    /**
     * The radius applied to the [mSquircleShapeRectF]
     */
    private var mSquircleRadius = 15f

    /**
     * Will hold the coordinates of the [PresentationBuilder.mViewToPresent] on the decorView
     * through the [android.view.View.getGlobalVisibleRect] method, which gives the accurate
     * positioning of a View on a screen
     * */
    private lateinit var vTPCoordinates: Rect

    /**
     * Will hold the coordinates of the decorView through
     * the [android.view.View.getGlobalVisibleRect] method, which gives the accurate
     * positioning of a View on a screen
     * */
    private lateinit var decorViewCoordinates: Rect

    /**
     * The left,top,right and bottom position, of the
     * [view to present][PresentationBuilder.mViewToPresent] in [Float]
     * This variable will hold the [vTPCoordinates] in [RectF]
     */
    private lateinit var mViewToPresentBounds: RectF

    /**
     * The [Paint] to use to draw the [mSquircleShapeRectF]
     */
    private lateinit var mSquircleShapePaint: Paint

    /**
     * Layout to wrap the [PresentationBuilder.mDescriptionText]
     * so that the text can be laid out in multiline instead of the
     * default singleLine that the [Canvas.drawText] method puts the text in, by default
     */
    private lateinit var staticLayout: StaticLayout

    /**
     * Position of the [staticLayout] inside this [mSquircleShapeRectF]
     */
    private lateinit var mStaticLayoutPosition: PointF

    private fun initPaint() {
        mSquircleShapePaint = Paint()
        mSquircleShapePaint.isAntiAlias = true
        mSquircleShapePaint.style = Paint.Style.FILL
    }

    private fun initFloats() {
        mViewToPresentBounds = RectF()
        mSquircleShapeRectF = RectF()
        mStaticLayoutPosition = PointF()
        vTPCoordinates = Rect()
        decorViewCoordinates = Rect()
    }

    init {
        initPaint()
        initFloats()
    }

    override fun setShapeBackgroundColor(color: Int) {
        mSquircleShapePaint.color = color
    }

    override fun shapeContains(x: Float, y: Float): Boolean {
        return mSquircleShapeRectF.contains(x, y)
    }

    override fun viewToPresentContains(x: Float, y: Float): Boolean {
        return mViewToPresentBounds.contains(x, y)
    }

    override fun buildSelfWith(builder: PresentationBuilder<*>) {
        builder.mDescriptionText?.let {
            setShapeBackgroundColor(builder.mBackgroundColor!!)
            val mDecorView = builder.resourceFinder.getDecorView()!!
            hasShadowedWindow = builder.mPresenterHasShadowedWindow
            if (hasShadowedWindow) {
                mDecorView.getGlobalVisibleRect(decorViewCoordinates)
                shadowedWindow.set(decorViewCoordinates) // takes the coordinates of the decorView
                setShadowedWindowColor(Color.parseColor("#80000000"))
            }
            if (builder.mHasShadowLayer) {
                mSquircleShapePaint.setShadowLayer(
                    builder.presenterShadowLayer.radius,
                    builder.presenterShadowLayer.dx,
                    builder.presenterShadowLayer.dy,
                    builder.presenterShadowLayer.shadowColor
                )
            }
            setDescriptionTextColor(builder.mDescriptionTextColor!!)
            setDescriptionTypeface(builder.mTypeface)
            setDescriptionTextSize(
                builder.mDescriptionTextUnit,
                builder.mDescriptionTextSize,
                mDecorView.resources.displayMetrics
            )
            // Get the exact coordinates of the view to present inside the decorView
            // So we can relatively place the StaticLayout & mSquircleShapeRectF next to it
            builder.mViewToPresent!!.getGlobalVisibleRect(vTPCoordinates)
            mViewToPresentBounds.set(vTPCoordinates)

            val descTextWidth = mDescriptionTextPaint.measureText(it).toInt()

            // How much space is left when you place the description text from end to start
            val textWidthRemainingSpace1 = mViewToPresentBounds.left - descTextWidth

            val percentageFromStart = mViewToPresentBounds.left * 100 / mDecorView.width
            Timber.d("percentageFromStart: $percentageFromStart%")

            // Remaining Space between View to present's left position and the end of the
            // decorView's width
            val vtpStartToDecorEnd = mDecorView.width - mViewToPresentBounds.left

            val textWidthRemainingSpace2 = vtpStartToDecorEnd - descTextWidth

            val horizontalMargin = 56
            val shouldLayoutFromStartToEnd: Boolean
            // set to that for now
            var staticLayoutWidth: Int = (vtpStartToDecorEnd - horizontalMargin).toInt()

            Timber.d("staticLayoutWidth: $staticLayoutWidth")
            Timber.d("mDecorView.width - horizontalMargin: ${mDecorView.width - horizontalMargin}")
            val difference = (mDecorView.width - horizontalMargin) - staticLayoutWidth
            Timber.d("difference : $difference")

            when {
                textWidthRemainingSpace1 <= 0 -> {
                    when {
                        textWidthRemainingSpace2 <= 0 -> {
                            if (percentageFromStart <= 45
                                && staticLayoutWidth <= mDecorView.width - horizontalMargin
                            ) {
                                staticLayoutWidth -= horizontalMargin
                                shouldLayoutFromStartToEnd = true
                            } else if (percentageFromStart <= 45
                                && staticLayoutWidth > mDecorView.width - horizontalMargin
                            ) {
                                shouldLayoutFromStartToEnd = true
                            } else {
                                // The percentage of the decor view's width occupied by
                                // the staticLayoutWidth
                                val c = staticLayoutWidth * 100 / mDecorView.width
                                if (c <= 45) {
                                    // let's make staticLayoutWidth 65% of decor view's width
                                    staticLayoutWidth = (65 * mDecorView.width / 100)
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
            staticLayout = buildStaticLayout(it, mDescriptionTextPaint, staticLayoutWidth)

            // Registers the position the text inside the StaticLayout should be placed
            // inside the canvas
            val sLTextPosition = PointF()

            // The amount of space in px that the StaticLayout needs to lay itself out
            // vertically
            val slHeightVertically = mViewToPresentBounds.bottom + staticLayout.height

            // Distance left between the end of the screen and the StaticLayout's final
            // bottom position or final Height position
            val e = mDecorView.height - slHeightVertically
            // How much percentage of the decor view height's still available if
            // we lay out the StaticLayout vertically from up to down
            val ePercentage = e * 100 / mDecorView.height

            if (shouldLayoutFromStartToEnd) {
                // StaticLayout goes: up to down, start to end

                // If the DecorView's Height has a remaining of 15% space available
                // still at its bottom even after the StaticLayout gets laid out
                // vertically from up to down, then execute the condition inside the
                // if statement
                if (ePercentage >= 15) {
                    sLTextPosition.x = mViewToPresentBounds.left + 16
                    sLTextPosition.y = mViewToPresentBounds.bottom + 32
                    mSquircleShapeRectF.set(
                        sLTextPosition.x - 16,
                        sLTextPosition.y - 16,
                        sLTextPosition.x + staticLayoutWidth.toFloat(),
                        sLTextPosition.y + (staticLayout.height + 16)
                    )
                    mStaticLayoutPosition = PointF(sLTextPosition.x, sLTextPosition.y)
                } else {
                    // StaticLayout goes: down to up, start to end
                    sLTextPosition.x = mViewToPresentBounds.left + 16
                    sLTextPosition.y = mViewToPresentBounds.top - (staticLayout.height + 32)

                    mSquircleShapeRectF.set(
                        sLTextPosition.x - 16,
                        sLTextPosition.y - 16,
                        sLTextPosition.x + (staticLayoutWidth + 16).toFloat(),
                        sLTextPosition.y + staticLayout.height + 16
                    )
                    mStaticLayoutPosition = PointF(mSquircleShapeRectF.left + 16, sLTextPosition.y)
                }
            } else {
                // StaticLayout goes: up to down, end to start
                if (ePercentage >= 15) {
                    // the position of the text based on those conditions
                    sLTextPosition.x = mViewToPresentBounds.right - 16
                    sLTextPosition.y = mViewToPresentBounds.bottom + 32

                    mSquircleShapeRectF.set(
                        mViewToPresentBounds.right - (staticLayoutWidth.toFloat() + 32),
                        sLTextPosition.y - 16,
                        sLTextPosition.x + 16,
                        sLTextPosition.y + (staticLayout.height + 16)
                    )
                    mStaticLayoutPosition = PointF(mSquircleShapeRectF.left + 16, sLTextPosition.y)
                } else {
                    // StaticLayout goes: down to up, end to start
                    sLTextPosition.x = mViewToPresentBounds.right
                    sLTextPosition.y = mViewToPresentBounds.top - (staticLayout.height + 32)

                    mSquircleShapeRectF.set(
                        sLTextPosition.x - (staticLayoutWidth + 32).toFloat(),
                        sLTextPosition.y - 16,
                        sLTextPosition.x,
                        sLTextPosition.y + staticLayout.height + 16
                    )
                    mStaticLayoutPosition = PointF(mSquircleShapeRectF.left + 16, sLTextPosition.y)
                }
            }
        }
    }

    override fun onDrawInPresenterWith(canvas: Canvas?) {
        canvas!!.save()
        // Draws the shadowed window first, if true
        if (hasShadowedWindow) {
            mViewToPresentBounds.inset(-4f, -4f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                canvas.clipOutRect(mViewToPresentBounds)
            } else {
                canvas.clipRect(mViewToPresentBounds, Region.Op.DIFFERENCE)
            }
            canvas.drawRect(shadowedWindow, shadowedWindowPaint)
        }
        // then draws the presenter shape
        canvas.drawRoundRect(
            mSquircleShapeRectF,
            mSquircleRadius,
            mSquircleRadius,
            mSquircleShapePaint
        )
        canvas.translate(mStaticLayoutPosition.x, mStaticLayoutPosition.y)
        staticLayout.draw(canvas)
        canvas.restore()
    }
}