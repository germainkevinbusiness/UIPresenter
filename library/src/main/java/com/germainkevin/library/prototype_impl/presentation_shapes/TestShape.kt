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
     * Position of the [SquircleShape.descriptionText] inside
     * the [SquircleShape.mSquircleShapeRectF]
     */
    private lateinit var mDescriptionTextPosition: PointF

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
        mDescriptionTextPosition = PointF()
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


                    // Thorough calculations
                    Timber.d("DecorView Width: ${mDecorView.width}")
                    Timber.d("mViewToPresentBounds.left: ${mViewToPresentBounds.left}")
                    // Remaining Space between View to present's left position and the end of the
                    // decorView's width
                    val a = mDecorView.width - mViewToPresentBounds.left
                    // $a minus description text width
                    val b = a - descTextWidth

                    Timber.d("Space between View to present and end edge: $a")
                    Timber.d(" Remaining space minus descriptionText width: $b")

                    val horizontalMargin = 56
                    val staticLayoutWidth: Int = when {
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

                    Timber.d("descTextWidth: $descTextWidth")
                    Timber.d("staticLayoutWidth: $staticLayoutWidth")
                    // Build Static layout
                    staticLayout = buildStaticLayout(it, mDescriptionTextPaint, staticLayoutWidth)

                    // Determine DescriptionText position on screen
                    mDescriptionTextPosition.x = mViewToPresentBounds.left + 16
                    mDescriptionTextPosition.y = mViewToPresentBounds.bottom + 16

                    // Build the squircle
                    val mSquircleWidth = staticLayoutWidth.toFloat()
                    mSquircleShapeRectF.set(
                        mDescriptionTextPosition.x - 16,
                        mDescriptionTextPosition.y - 16,
                        mDescriptionTextPosition.x + mSquircleWidth,
                        mDescriptionTextPosition.y + (staticLayout.height + 20)
                    )
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
            cv.translate(mDescriptionTextPosition.x, mDescriptionTextPosition.y)
            staticLayout.draw(cv)
            cv.restore()
        }
    }
}