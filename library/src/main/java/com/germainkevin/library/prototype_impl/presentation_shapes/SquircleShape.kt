package com.germainkevin.library.prototype_impl.presentation_shapes

import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.germainkevin.library.prototype_impl.PresentationBuilder
import com.germainkevin.library.prototypes.PresenterShape
import kotlinx.coroutines.*
import timber.log.Timber

class SquircleShape(override var descriptionText: String?) : PresenterShape {

//    private var calculateVTPBoundsJob: Deferred<Unit>? = null

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
     * Position of the [SquircleShape.descriptionText] inside
     * the [SquircleShape.mSquircleShapeRectF]
     */
    private var mDescriptionTextPosition: PointF

    /**
     * Layout to wrap the [SquircleShape.descriptionText]
     * so that the text can be laid out in multiline instead of the
     * default singleLine that the [Canvas.drawText]
     * method puts the text in by default
     */
    private var mStaticLayout: StaticLayout? = null

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

    private fun setupPaints() {
        mSquircleShapePaint = Paint()
        mSquircleShapePaint.isAntiAlias = true
        mSquircleShapePaint.style = Paint.Style.FILL
        mDescriptionTextPaint = TextPaint()
        mDescriptionTextPaint.isAntiAlias = true
    }

    private fun setShadowLayer() {
        mSquircleShapePaint.setShadowLayer(10f, 5f, 5f, Color.DKGRAY)
    }

    private fun setupRectFs() {
        mViewToPresentBounds = RectF()
        mSquircleShapeRectF = RectF()
    }

    init {
        setupPaints()
        setShadowLayer()
        setupRectFs()
        mDescriptionTextPosition = PointF()
    }


    override fun setBackgroundColor(color: Int) {
        mSquircleShapePaint.color = color
    }

    override fun setTextColor(textColor: Int) {
        mDescriptionTextPaint.color = textColor
    }

    override fun setTextSize(typedValueUnit: Int, textSize: Float) {
        mDefaultTextUnit = if (typedValueUnit == 0) TypedValue.COMPLEX_UNIT_SP else typedValueUnit
        mDefaultTextSize = textSize
    }

    override fun setTypeface(typeface: Typeface?) {
        mDescriptionTextPaint.typeface = typeface
    }

    /**
     * Gets the exact coordinate on the screen, of the view to present
     * */
    private fun calculateVTPBounds(rect: Rect, viewToPresent: View): Pair<PointF, PointF> {
        viewToPresent.getGlobalVisibleRect(rect)
        val viewToPresentLeftTopPosition = PointF(rect.left.toFloat(), rect.top.toFloat())
        val viewToPresentRightBottomPosition = PointF(rect.right.toFloat(), rect.bottom.toFloat())
        return Pair(viewToPresentLeftTopPosition, viewToPresentRightBottomPosition)
    }

    private fun calculatedTextSize(mDisplayMetrics: DisplayMetrics): Float {
        return TypedValue.applyDimension(mDefaultTextUnit, mDefaultTextSize, mDisplayMetrics)
    }

    override fun buildSelfWith(builder: PresentationBuilder<*>) {
        val mDecorView: ViewGroup = builder.resourceFinder.getDecorView()!!
        val viewToPresent: View = builder.mViewToPresent!!
        val mDisplayMetrics = mDecorView.resources.displayMetrics
        mDescriptionTextPaint.textSize = calculatedTextSize(mDisplayMetrics)
        val rect = Rect()
        val viewToPresentBounds = calculateVTPBounds(rect = rect, viewToPresent = viewToPresent)
        mViewToPresentBounds.set(
            viewToPresentBounds.first.x, // left
            viewToPresentBounds.first.y, // top
            viewToPresentBounds.second.x, // right
            viewToPresentBounds.second.y // bottom
        )
        val desiredShapeWidthLeftToRight = mViewToPresentBounds.right + 110
        val desiredShapeHeightTopToBottom = mViewToPresentBounds.bottom + 250
        val finalLeftValue: Float
        val finalRightValue: Float
        val finalTopValue: Float
        val finalBottomValue: Float

        val rightMaxMarginDistance = 44f
        // 56, the usual height of bottom bars *2 + rightMaxMarginDistance - 10
        val bottomMaxMarginDistance = 154f
        val rightSpaceAvailable = mDecorView.width - desiredShapeWidthLeftToRight
        val bottomSpaceAvailable = mDecorView.height - desiredShapeHeightTopToBottom

        if (rightSpaceAvailable >= rightMaxMarginDistance) {
            finalLeftValue = mViewToPresentBounds.left
            finalRightValue = desiredShapeWidthLeftToRight
        } else {
            finalLeftValue = mViewToPresentBounds.left - 110
            finalRightValue = mViewToPresentBounds.right
        }
        if (bottomSpaceAvailable >= bottomMaxMarginDistance) {
            finalTopValue = desiredShapeHeightTopToBottom
            finalBottomValue = mViewToPresentBounds.bottom
        } else {
            finalTopValue = mViewToPresentBounds.top
            finalBottomValue = mViewToPresentBounds.top - 250
        }
        mSquircleShapeRectF.set(finalLeftValue, finalTopValue, finalRightValue, finalBottomValue)
    }

    override fun bindCanvasToDraw(canvas: Canvas?) {
        canvas?.let { cv ->
            cv.drawRoundRect(
                mSquircleShapeRectF,
                mDefaultSquircleRadius,
                mDefaultSquircleRadius,
                mSquircleShapePaint
            )
        }
    }

    override fun shapeContains(x: Float, y: Float): Boolean = mSquircleShapeRectF.contains(x, y)

    override fun viewToPresentContains(x: Float, y: Float): Boolean {
        return mViewToPresentBounds.contains(x, y)
    }
}