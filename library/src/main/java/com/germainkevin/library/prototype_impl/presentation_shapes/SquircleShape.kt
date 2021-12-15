package com.germainkevin.library.prototype_impl.presentation_shapes

import android.graphics.*
import android.text.StaticLayout
import android.text.TextPaint
import android.util.DisplayMetrics
import android.util.TypedValue
import com.germainkevin.library.buildStaticLayout
import com.germainkevin.library.mainThread
import com.germainkevin.library.prototype_impl.PresentationBuilder
import com.germainkevin.library.prototypes.PresenterShape
import kotlinx.coroutines.*
import timber.log.Timber

class SquircleShape : PresenterShape {
    /**
     * The background to draw on the SquircleShape
     */
    private lateinit var mSquircleShapeRectF: RectF

    /**
     * Will hold the coordinates of the [PresentationBuilder.mViewToPresent] on the decorView
     * through the [android.view.View.getGlobalVisibleRect] method
     * */
    private lateinit var vTPCoordinates: Rect

    /**
     * The left,top,right and bottom position, of the
     * [view to present][PresentationBuilder.mViewToPresent] in [Float] type
     * This variable will hold the [vTPCoordinates] in [RectF]
     */
    private lateinit var mViewToPresentBounds: RectF

    /**
     * The radius applied to the [SquircleShape.mSquircleShapeRectF]
     */
    private var mDefaultSquircleRadius = 15f

    /**
     * The [Paint] to use to draw the [SquircleShape.mSquircleShapeRectF]
     */
    private lateinit var mSquircleShapePaint: Paint

    /**
     * The [Paint] to use to draw the Description text inside the
     * [SquircleShape.mSquircleShapeRectF]
     */
    private lateinit var mDescriptionTextPaint: TextPaint

    /**
     * Position of the [staticLayout] inside this [PresenterShape]
     */
    private lateinit var mStaticLayoutPosition: PointF

    /**
     * Layout to wrap the [PresentationBuilder.mDescriptionText]
     * so that the text can be laid out in multiline instead of the
     * default singleLine that the [Canvas.drawText]
     * method puts the text in by default
     */
    private lateinit var staticLayout: StaticLayout


    private fun setupPaints() {
        mSquircleShapePaint = Paint()
        mSquircleShapePaint.isAntiAlias = true
        mSquircleShapePaint.style = Paint.Style.FILL
        mDescriptionTextPaint = TextPaint()
        mDescriptionTextPaint.isAntiAlias = true
    }

    private fun setupFloats() {
        mViewToPresentBounds = RectF()
        mSquircleShapeRectF = RectF()
        mStaticLayoutPosition = PointF()
        vTPCoordinates = Rect()
    }

    init {
        setupPaints()
        setupFloats()
    }

    override lateinit var buildSelfJob: Deferred<Unit>

    override fun setBackgroundColor(color: Int) {
        mSquircleShapePaint.color = color
    }

    override fun setDescriptionTextColor(textColor: Int) {
        mDescriptionTextPaint.color = textColor
    }

    override fun setDescriptionTextSize(
        typedValueUnit: Int,
        textSize: Float,
        displayMetrics: DisplayMetrics
    ) {
        mDescriptionTextPaint.textSize =
            TypedValue.applyDimension(typedValueUnit, textSize, displayMetrics)
    }

    override fun setDescriptionTypeface(typeface: Typeface?) {
        mDescriptionTextPaint.typeface = typeface
    }

    override fun shapeContains(x: Float, y: Float): Boolean {
        return if (buildSelfJob.isCompleted) {
            mSquircleShapeRectF.contains(x, y)
        } else {
            Timber.d("BuildSelf job is incomplete")
            false
        }
    }

    override fun viewToPresentContains(x: Float, y: Float): Boolean {
        return mViewToPresentBounds.contains(x, y)
    }

    override fun buildSelfWith(builder: PresentationBuilder<*>) {
        mainThread {
            builder.mDescriptionText?.let {
                val mDecorView = builder.resourceFinder.getDecorView()!!
                setBackgroundColor(builder.mBackgroundColor!!)
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
                buildSelfJob = async {
                    // Get the exact coordinates of the view to present
                    builder.mViewToPresent!!.getGlobalVisibleRect(vTPCoordinates)
                    mViewToPresentBounds.set(vTPCoordinates)

                    val descTextWidth = mDescriptionTextPaint.measureText(it).toInt()

                    // Remaining Space between View to present's left position and the end of the
                    // decorView's width
                    val a = mDecorView.width - mViewToPresentBounds.left
                    // $a minus description text width
                    val b = a - descTextWidth

                    val horizontalMargin = 56
                    var staticLayoutWidth: Int = when {
                        // The description text's width is larger than the available space
                        // for it to be laid out horizontally
                        b <= 0 -> (a - horizontalMargin).toInt()
                        // The description text's width is large enough to be laid out
                        b >= horizontalMargin -> descTextWidth + 16
                        // The description text's width is larger than the horizontalMargin
                        // but not larger than the remaining space it can be laid out in, inside
                        // the decorView's width
                        else -> descTextWidth - horizontalMargin
                    }

                    // The percentage of the decor view's width occupied by the staticLayoutWidth
                    val c = staticLayoutWidth * 100 / mDecorView.width

                    // Registers the position of the text inside the StaticLayout
                    val sLTextPosition = PointF()
                    // Verifies if the static layout width is more than 45% of the decor view width
                    val isWidthMoreThan45Percent: Boolean
                    if (c > 45) {
                        // Build Static layout as we are satisfied with its current width
                        staticLayout =
                            buildStaticLayout(it, mDescriptionTextPaint, staticLayoutWidth)
                        isWidthMoreThan45Percent = true
                    } else {
                        // let's make staticLayoutWidth 65% of decor view's width
                        staticLayoutWidth = (65 * mDecorView.width / 100)
                        // then build it with new width
                        staticLayout =
                            buildStaticLayout(it, mDescriptionTextPaint, staticLayoutWidth)
                        isWidthMoreThan45Percent = false
                    }

                    // StaticLayout remaining available space between the
                    // view to preent's bottom and the end of the screen
                    val decorViewHeightAvailableSpace =
                        mViewToPresentBounds.bottom + staticLayout.height

                    // Space between the height of the static layout & the end of the screen
                    val e = mDecorView.height - decorViewHeightAvailableSpace
                    // How much percentage of the decor view height available
                    val eSpacePercentageOnScreen = e * 100 / mDecorView.height

                    if (isWidthMoreThan45Percent) {
                        // StaticLayout goes: up to down, start to end
                        if (eSpacePercentageOnScreen >= 15) {
                            sLTextPosition.x = mViewToPresentBounds.left + 16
                            sLTextPosition.y = mViewToPresentBounds.bottom + 16
                            mSquircleShapeRectF.set(
                                sLTextPosition.x - 16,
                                sLTextPosition.y - 16,
                                sLTextPosition.x + staticLayoutWidth.toFloat(),
                                sLTextPosition.y + (staticLayout.height + 16)
                            )
                            mStaticLayoutPosition =
                                PointF(sLTextPosition.x, sLTextPosition.y)
                        } else {
                            // StaticLayout goes: down to up, start to end
                            sLTextPosition.x = mViewToPresentBounds.left + 16
                            sLTextPosition.y =
                                mViewToPresentBounds.top - (staticLayout.height + 32)

                            mSquircleShapeRectF.set(
                                sLTextPosition.x - 16,
                                sLTextPosition.y - 16,
                                sLTextPosition.x + (staticLayoutWidth + 16).toFloat(),
                                sLTextPosition.y + staticLayout.height + 16
                            )

                            mStaticLayoutPosition = PointF(
                                mSquircleShapeRectF.left + 16, sLTextPosition.y
                            )
                        }

                    } else {
                        // StaticLayout goes: up to down, end to start
                        if (eSpacePercentageOnScreen >= 15) {
                            // the position of the text based on those conditions
                            sLTextPosition.x = mViewToPresentBounds.right - 16
                            sLTextPosition.y = mViewToPresentBounds.bottom - 16

                            mSquircleShapeRectF.set(
                                sLTextPosition.x - staticLayoutWidth.toFloat(),
                                sLTextPosition.y - 16,
                                sLTextPosition.x + 16,
                                sLTextPosition.y + (staticLayout.height + 16)
                            )
                            mStaticLayoutPosition =
                                PointF(mSquircleShapeRectF.left + 16, sLTextPosition.y)
                        } else {
                            // StaticLayout goes: down to up, end to start
                            sLTextPosition.x = mViewToPresentBounds.right
                            sLTextPosition.y =
                                mViewToPresentBounds.top - (staticLayout.height + 32)

                            mSquircleShapeRectF.set(
                                sLTextPosition.x - (staticLayoutWidth + 16).toFloat(),
                                sLTextPosition.y - 16,
                                sLTextPosition.x,
                                sLTextPosition.y + staticLayout.height + 16
                            )

                            mStaticLayoutPosition = PointF(
                                mSquircleShapeRectF.left + 16, sLTextPosition.y
                            )
                        }
                    }
                }
                buildSelfJob.await()
                buildSelfJob.join()
            }
        }
    }

    override fun onDrawInPresenterWith(canvas: Canvas?) {
        canvas!!.save()
        canvas.drawRoundRect(
            mSquircleShapeRectF,
            mDefaultSquircleRadius,
            mDefaultSquircleRadius,
            mSquircleShapePaint
        )
        canvas.translate(mStaticLayoutPosition.x, mStaticLayoutPosition.y)
        staticLayout.draw(canvas)
        canvas.restore()
    }
}