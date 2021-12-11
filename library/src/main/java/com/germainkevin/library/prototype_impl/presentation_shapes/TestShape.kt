package com.germainkevin.library.prototype_impl.presentation_shapes

import android.graphics.*
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import com.germainkevin.library.prototype_impl.PresentationBuilder
import com.germainkevin.library.prototypes.PresenterShape
import com.germainkevin.library.utils.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import timber.log.Timber
import kotlin.math.abs

class TestShape : PresenterShape {

    /**
     * The background to draw on the SquircleShape
     */
    private lateinit var mSquircleShapeRectF: RectF

    /**
     * The left,top,right and bottom position, of the
     * [view to present][PresentationBuilder.mViewToPresent]
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
     * The text description the view to present
     * */
    private var descriptionText: String? = null

    /**
     * Position of the [staticLayout] inside this [PresenterShape]
     */
    private lateinit var mStaticLayoutPosition: PointF

    /**
     * Layout to wrap the [SquircleShape.descriptionText]
     * so that the text can be laid out in multiline instead of the
     * default singleLine that the [Canvas.drawText]
     * method puts the text in by default
     */
    private lateinit var staticLayout: StaticLayout

    /**
     * [TypedValue] unit in which the [SquircleShape.descriptionText]
     * should be displayed.
     * Usually a text on android is displayed in the [TypedValue.COMPLEX_UNIT_SP] unit
     *
     */
    private var mDefaultTextUnit = TypedValue.COMPLEX_UNIT_SP

    /**
     * Desired text size to be displayed in a [TypedValue] unit
     */
    private var mDefaultTextSize = 18f

    /**
     * Defines whether to add a shadow layer or not to the [mSquircleShapePaint]
     * */
    private var isShadowLayerEnabled = true

    private var shadowLayerColor = Color.DKGRAY

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
    }

    init {
        setupPaints()
        setupFloats()
    }

    override lateinit var buildSelfJob: Deferred<Unit>

    override fun setHasShadowLayer(mBoolean: Boolean) {
        isShadowLayerEnabled = mBoolean
    }

    override fun setShadowLayerColor(shadowColor: Int) {
        shadowLayerColor = shadowColor
    }

    override fun setBackgroundColor(color: Int) {
        mSquircleShapePaint.color = color
    }

    override fun setDescriptionText(text: String) {
        descriptionText = text
    }

    override fun setDescriptionTextColor(textColor: Int) {
        mDescriptionTextPaint.color = textColor
    }

    override fun setDescriptionTextSize(typedValueUnit: Int, textSize: Float) {
        mDefaultTextUnit = if (typedValueUnit == 0) TypedValue.COMPLEX_UNIT_SP else typedValueUnit
        mDefaultTextSize = textSize
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
            if (isShadowLayerEnabled) setShadowLayer(mSquircleShapePaint, shadowLayerColor)
            descriptionText?.let {
                val mDecorView = builder.resourceFinder.getDecorView()!!
                val displayMetrics = mDecorView.resources.displayMetrics
                val viewToPresent = builder.mViewToPresent!!
                // Doing some calculations
                buildSelfJob = async {
                    mDescriptionTextPaint.textSize =
                        calculatedTextSize(displayMetrics, mDefaultTextUnit, mDefaultTextSize)
                    // Get the exact coordinates of the view to present
                    mViewToPresentBounds = viewToPresent.getBounds()

                    // Determine description text width
                    val descTextWidth = mDescriptionTextPaint.measureText(it).toInt()

                    Timber.d("DecorView Width: ${mDecorView.width}")
                    Timber.d("mViewToPresentBounds.left: ${mViewToPresentBounds.left}")
                    Timber.d("DecorView height: ${mDecorView.height}")
                    Timber.d("mViewToPresentBounds.bottom: ${mViewToPresentBounds.bottom}")

                    // Remaining Space between View to present's left position and the end of the
                    // decorView's width
                    val a = mDecorView.width - mViewToPresentBounds.left
                    // $a minus description text width
                    val b = a - descTextWidth

                    // Space between the vtp bottom and the bottom of the decor view
                    val c = mDecorView.height - mViewToPresentBounds.bottom

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
                    val d = staticLayoutWidth * 100 / mDecorView.width

                    Timber.d("Space between View to present and end edge: $a")
                    Timber.d("Space between the vtp bottom and the bottom of the decor view: $c")
                    Timber.d("Remaining space minus descriptionText width: $b")
                    Timber.d("descTextWidth: $descTextWidth")
                    Timber.d("staticLayoutWidth: $staticLayoutWidth")
                    Timber.d("staticLayoutWidth % of width: $d%")

                    // Determine DescriptionText position on screen & Build the squircle
                    val mDescriptionTextPosition = PointF()
                    // Verifies if the static layout width is more than 45% of the decor view width
                    val isWidthMoreThan45Percent: Boolean
                    if (d > 45) {
                        // Build Static layout as we are satisfied with its current width
                        staticLayout =
                            buildStaticLayout(it, mDescriptionTextPaint, staticLayoutWidth)
                        isWidthMoreThan45Percent = true
                    } else {
                        // let's make staticLayoutWidth 65% of decor view
                        // StaticLayoutWidth will go from right to left when
                        // this condition is considered
                        staticLayoutWidth = (65 * mDecorView.width / 100)
                        // then build it with new width
                        staticLayout =
                            buildStaticLayout(it, mDescriptionTextPaint, staticLayoutWidth)
                        isWidthMoreThan45Percent = false
                    }

                    // The position where the height ends on the screen
                    val staticLayoutHeightPosition =
                        mViewToPresentBounds.bottom + staticLayout.height

                    // Space between the height of the static layout & the end of the screen
                    val e = mDecorView.height - staticLayoutHeightPosition
                    // How much percentage of the decor view height does this distance represent
                    val eSpacePercentageOnScreen = e * 100 / mDecorView.height
                    // The static layout position on screen from the top of the vtp
                    // to its height when put at the top
                    // So this is the new top coordinate
                    val f = (mViewToPresentBounds.top - 16) - (staticLayout.height + 20)

                    if (isWidthMoreThan45Percent) {
                        Timber.d("Static layout height end in position: $staticLayoutHeightPosition")
                        Timber.d("Space between the height of the static layout & the end of the screen: $e")
                        Timber.d("eSpacePercentageOnScreen: $eSpacePercentageOnScreen")
                        // StaticLayout goes from up to down from the bottom of the view to present
                        if (eSpacePercentageOnScreen >= 15) {
                            mDescriptionTextPosition.x = mViewToPresentBounds.left + 16
                            mDescriptionTextPosition.y = mViewToPresentBounds.bottom + 16
                            mSquircleShapeRectF.set(
                                mDescriptionTextPosition.x - 16,
                                mDescriptionTextPosition.y - 16,
                                mDescriptionTextPosition.x + staticLayoutWidth.toFloat(),
                                mDescriptionTextPosition.y + (staticLayout.height + 20)
                            )
                            mStaticLayoutPosition =
                                PointF(mDescriptionTextPosition.x, mDescriptionTextPosition.y)
                        } else {
                            // StaticLayout goes down to up from the top of the view to present
                            Timber.d("f position on screen: $f")

                            mDescriptionTextPosition.x = mViewToPresentBounds.left - 16
                            mDescriptionTextPosition.y = mViewToPresentBounds.top - 16

                            mSquircleShapeRectF.set(
                                mDescriptionTextPosition.x - 16,
                                mDescriptionTextPosition.y,
                                mDescriptionTextPosition.x + staticLayoutWidth.toFloat(),
                                mDescriptionTextPosition.y - (staticLayout.height + 20)
                            )
                            mStaticLayoutPosition =
                                PointF(mDescriptionTextPosition.x, f)
                        }

                    } else {

                        if (eSpacePercentageOnScreen >= 15) {
                            // the position of the text based on those conditions
                            mDescriptionTextPosition.x = mViewToPresentBounds.right - 16
                            mDescriptionTextPosition.y = mViewToPresentBounds.bottom - 16

                            mSquircleShapeRectF.set(
                                mDescriptionTextPosition.x - staticLayoutWidth.toFloat(),
                                mDescriptionTextPosition.y - 16,
                                mDescriptionTextPosition.x + 16,
                                mDescriptionTextPosition.y + (staticLayout.height + 20)
                            )
                            mStaticLayoutPosition =
                                PointF(mSquircleShapeRectF.left + 16, mDescriptionTextPosition.y)
                        } else {
                            mDescriptionTextPosition.x = mViewToPresentBounds.right - 16
                            mDescriptionTextPosition.y = mViewToPresentBounds.top + 16

                            mSquircleShapeRectF.set(
                                mDescriptionTextPosition.x - staticLayoutWidth.toFloat(),
                                mDescriptionTextPosition.y - 16,
                                mDescriptionTextPosition.x + 16,
                                mDescriptionTextPosition.y + (staticLayout.height + 20)
                            )
                            mStaticLayoutPosition =
                                PointF(mSquircleShapeRectF.left + 16, f)
                        }
                    }
                }
                buildSelfJob.await()
                buildSelfJob.join()
            }
        }
    }

    override fun onDrawInPresenterWith(canvas: Canvas?) {
        canvas?.let { cv ->
            cv.save()
            cv.drawRoundRect(
                mSquircleShapeRectF,
                mDefaultSquircleRadius,
                mDefaultSquircleRadius,
                mSquircleShapePaint
            )
            cv.translate(mStaticLayoutPosition.x, mStaticLayoutPosition.y)
            staticLayout.draw(cv)
            cv.restore()
        }
    }
}