package com.example.flyingandroidclient

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.getFloatOrThrow

/**
 * TODO: document your custom view class.
 */
class Plot @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var _plotData: MutableList<Float> = mutableListOf()

    private var _valueRange = 90f
    var valueRange: Float
        get() = _valueRange
        set(value) {
            _valueRange = value
            invalidate()
        }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Plot,
            0, 0).apply {
            try {
                _valueRange = getFloat(R.styleable.Plot_valueRange, 90f)
            } finally {
                recycle()
            }
        }
    }

    private val paint = Paint().apply {
        color = ResourcesCompat.getColor(resources, R.color.teal_200, null)
        textAlign = Paint.Align.CENTER
        style = Paint.Style.STROKE
    }

    private val paint2 = Paint().apply {
        color = Color.rgb(100, 100, 100)
    }

    var plotData: MutableList<Float>
        get() = _plotData
        set(value) {
            _plotData = value
            invalidate()
        }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.strokeWidth = height.toFloat() / 60f
        paint2.strokeWidth = height.toFloat() / 120f
        val vertHalfRange = height.toFloat() / 2
        val horStep = width.toFloat() / 100
        val initX = width.toFloat()
        var i = 0
        var lastX = 0f
        var lastY = 0f
        canvas.drawLine(0.0f, vertHalfRange, width.toFloat(), vertHalfRange, paint2)
        for(value in plotData.reversed()) {
            val x = initX - horStep * i
            val y = vertHalfRange - (value / _valueRange) * vertHalfRange
            if (i != 0) canvas.drawLine(lastX, lastY, x, y, paint)
            i++
            lastX = x
            lastY = y
        }
    }
}