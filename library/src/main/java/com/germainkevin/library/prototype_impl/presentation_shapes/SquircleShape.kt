package com.germainkevin.library.prototype_impl.presentation_shapes

import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.germainkevin.library.prototype_impl.PresentationBuilder
import com.germainkevin.library.prototypes.PresenterShape
import com.germainkevin.library.utils.calculateVTPBounds
import com.germainkevin.library.utils.calculatedTextSize
import com.germainkevin.library.utils.mainThread
import com.germainkevin.library.utils.setShadowLayer
import kotlinx.coroutines.*
import timber.log.Timber

class SquircleShape : PresenterShape {

    private lateinit var buildSelfJob: Deferred<Unit>

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
    private lateinit var mStaticLayout: StaticLayout

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

    override fun buildSelfWith(builder: PresentationBuilder<*>) {
        mainThread {
            if (isShadowLayerEnabled) setShadowLayer(mSquircleShapePaint, shadowLayerColor)
            // Set up the description text coordinates
            descriptionText?.let { description ->
                val mDecorView: ViewGroup = builder.resourceFinder.getDecorView()!!
                val mDisplayMetrics = mDecorView.resources.displayMetrics
                val viewToPresent: View = builder.mViewToPresent!!
                buildSelfJob = async {
                    mDescriptionTextPaint.textSize =
                        calculatedTextSize(mDisplayMetrics, mDefaultTextUnit, mDefaultTextSize)
                    val viewToPresentBounds = calculateVTPBounds(viewToPresent)
                    // We now have the exact coordinates of the view to present
                    mViewToPresentBounds.set(
                        viewToPresentBounds.first.x, // left
                        viewToPresentBounds.first.y, // top
                        viewToPresentBounds.second.x, // right
                        viewToPresentBounds.second.y // bottom
                    )
                    val desiredShapeWidthLeftToRight =
                        mViewToPresentBounds.right + (description.length * 3.5).toInt()
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

                    Timber.d("rightSpaceAvailable: $rightSpaceAvailable")
                    Timber.d("bottomSpaceAvailable: $bottomSpaceAvailable")

                    if (rightSpaceAvailable >= rightMaxMarginDistance) {
                        finalLeftValue = mViewToPresentBounds.left
                        finalRightValue = desiredShapeWidthLeftToRight
                    } else {
                        val preferredLeftPosition =
                            mViewToPresentBounds.left - (description.length * 3.5).toInt()
                        finalLeftValue =
                            if (preferredLeftPosition <= mDecorView.width) mViewToPresentBounds.left
                            else preferredLeftPosition
                        finalRightValue = mViewToPresentBounds.right
                    }
                    if (bottomSpaceAvailable >= bottomMaxMarginDistance) {
                        finalTopValue = desiredShapeHeightTopToBottom
                        finalBottomValue = mViewToPresentBounds.bottom
                    } else {
                        finalTopValue = mViewToPresentBounds.top
                        finalBottomValue = mViewToPresentBounds.top - 250
                    }

                    // Set up the rounded rectangle coordinates
                    mSquircleShapeRectF.set(
                        finalLeftValue,
                        finalTopValue,
                        finalRightValue,
                        finalBottomValue
                    )
                    val mStaticLayoutWidth = mSquircleShapeRectF.width().toInt() - 16
                    mStaticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        StaticLayout.Builder.obtain(
                            description,
                            0,
                            description.length,
                            mDescriptionTextPaint,
                            mStaticLayoutWidth
                        )
                            .build()
                    } else {
                        StaticLayout(
                            description,
                            0,
                            mStaticLayoutWidth,
                            mDescriptionTextPaint,
                            description.length,
                            Layout.Alignment.ALIGN_CENTER,
                            1f,
                            1f,
                            false
                        )
                    }
                    mDescriptionTextPosition.x = mSquircleShapeRectF.left + 16
//                    mDescriptionTextPosition.y =
//                        mSquircleShapeRectF.centerY() - mStaticLayout.lineCount * textHeight / 2
                    mDescriptionTextPosition.y = mSquircleShapeRectF.bottom + 16
                }
                buildSelfJob.await()
                buildSelfJob.join()
            }
        }
    }

    override fun bindCanvasToDraw(canvas: Canvas?) {
        canvas?.let { cv ->
            cv.save()
            cv.drawRoundRect(
                mSquircleShapeRectF,
                mDefaultSquircleRadius,
                mDefaultSquircleRadius,
                mSquircleShapePaint
            )
            cv.translate(mDescriptionTextPosition.x, mDescriptionTextPosition.y)
            mStaticLayout.draw(cv)
            cv.restore()
        }
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
}