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
    private var mDescriptionTextPosition: PointF

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

    /**
     * Gets the exact coordinate on the screen, of the view to present
     * */
    private fun calculateVTPBounds(viewToPresent: View): Pair<PointF, PointF> {
        val rect = Rect()
        viewToPresent.getGlobalVisibleRect(rect)
        val viewToPresentLeftTopPosition = PointF(rect.left.toFloat(), rect.top.toFloat())
        val viewToPresentRightBottomPosition = PointF(rect.right.toFloat(), rect.bottom.toFloat())
        return Pair(viewToPresentLeftTopPosition, viewToPresentRightBottomPosition)
    }

    private fun calculatedTextSize(mDisplayMetrics: DisplayMetrics): Float {
        return TypedValue.applyDimension(mDefaultTextUnit, mDefaultTextSize, mDisplayMetrics)
    }

    private fun getTextHeight(text: String, paint: Paint): Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.height().toFloat()
    }

    override fun buildSelfWith(builder: PresentationBuilder<*>) {
        CoroutineScope(Dispatchers.Main).launch {
            // Set up the description text coordinates
            descriptionText?.let { description ->
                val mDecorView: ViewGroup = builder.resourceFinder.getDecorView()!!
                val viewToPresent: View = builder.mViewToPresent!!
                val mDisplayMetrics = mDecorView.resources.displayMetrics
                buildSelfJob = async {
                    mDescriptionTextPaint.textSize = calculatedTextSize(mDisplayMetrics)
                    val viewToPresentBounds = calculateVTPBounds(viewToPresent)
                    // We now have the exact coordinates of the view to present
                    mViewToPresentBounds.set(
                        viewToPresentBounds.first.x, // left
                        viewToPresentBounds.first.y, // top
                        viewToPresentBounds.second.x, // right
                        viewToPresentBounds.second.y // bottom
                    )
                    val desiredShapeWidthLeftToRight =
                        mViewToPresentBounds.right + (description.length * 3)
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
                            mViewToPresentBounds.left - (description.length * 3)
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
                            descriptionText,
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
            }
            buildSelfJob.await()
            buildSelfJob.join()
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
            Timber.d("BuildSelf job is incompleted")
            false
        }
    }

    override fun viewToPresentContains(x: Float, y: Float): Boolean {
        return mViewToPresentBounds.contains(x, y)
    }
}