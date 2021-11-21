package com.germainkevin.library.prototype_impl.presentation_shapes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.germainkevin.library.prototype_impl.PresentationBuilder
import com.germainkevin.library.prototypes.PresenterShape

class TestShape(override var descriptionText: String?) : PresenterShape {

    private lateinit var mSquircleShapePaint: Paint

    private fun setupPaints() {
        mSquircleShapePaint = Paint()
        mSquircleShapePaint.isAntiAlias = true
        mSquircleShapePaint.style = Paint.Style.FILL
    }

    init {
        setupPaints()
    }

    override fun setBackgroundColor(color: Int) {
        mSquircleShapePaint.color = color
    }

    override fun setTextColor(textColor: Int) {
        TODO("Not yet implemented")
    }

    override fun setTextSize(typedValueUnit: Int, textSize: Float) {
        TODO("Not yet implemented")
    }

    override fun setTypeface(typeface: Typeface?) {
        TODO("Not yet implemented")
    }

    override fun shapeContains(x: Float, y: Float): Boolean {
        TODO("Not yet implemented")
    }

    override fun viewToPresentContains(x: Float, y: Float): Boolean {
        TODO("Not yet implemented")
    }

    override fun buildSelfWith(builder: PresentationBuilder<*>) {
        TODO("Not yet implemented")
    }

    override fun bindCanvasToDraw(canvas: Canvas?) {
        TODO("Not yet implemented")
    }
}