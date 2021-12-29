package com.example.flyingandroidclient

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withMatrix
import java.lang.Math.pow
import kotlin.math.pow

class CurvePlot @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var _a = 1f
    var a: Float
        get() = _a
        set(value) {
            _a = value
            invalidate()
        }

    private var _b = 1f
    var b: Float
        get() = _b
        set(value) {
            _b = value
            invalidate()
        }

    private var _xSpan = 1f
    var xSpan: Float
        get() = _xSpan
        set(value) {
            _xSpan = value
            invalidate()
        }

    private var _ySpan = 1f
    var ySpan: Float
        get() = _ySpan
        set(value) {
            _ySpan = value
            invalidate()
        }

    private var _xLabel = "abc"
    var xLabel: String
        get() = _xLabel
        set(value) {
            _xLabel = value
            invalidate()
        }

    private var _yLabel = "efg"
    var yLabel: String
        get() = _yLabel
        set(value) {
            _yLabel = value
            invalidate()
        }

    private var _xMinLabel = "0"
    var xMinLabel: String
        get() = _xMinLabel
        set(value) {
            _xMinLabel = value
            invalidate()
        }

    private var _yMinLabel = "0"
    var yMinLabel: String
        get() = _yMinLabel
        set(value) {
            _yMinLabel = value
            invalidate()
        }

    private var _xMaxLabel = "1"
    var xMaxLabel: String
        get() = _xMaxLabel
        set(value) {
            _xMaxLabel = value
            invalidate()
        }

    private var _yMaxLabel = "1"
    var yMaxLabel: String
        get() = _yMaxLabel
        set(value) {
            _yMaxLabel = value
            invalidate()
        }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CurvePlot,
            0, 0).apply {
            try {
                _a = getFloat(R.styleable.CurvePlot_a, 1f)
                _b = getFloat(R.styleable.CurvePlot_b, 1f)
                _xSpan = getFloat(R.styleable.CurvePlot_xSpan, 1f)
                _ySpan = getFloat(R.styleable.CurvePlot_ySpan, 1f)
                _xLabel = getString(R.styleable.CurvePlot_xLabel).toString()
                _yLabel = getString(R.styleable.CurvePlot_yLabel).toString()
                _xMaxLabel = getString(R.styleable.CurvePlot_xMaxLabel).toString()
                _yMaxLabel = getString(R.styleable.CurvePlot_yMaxLabel).toString()
                _xMinLabel = getString(R.styleable.CurvePlot_xMinLabel).toString()
                _yMinLabel = getString(R.styleable.CurvePlot_yMinLabel).toString()
            } finally {
                recycle()
            }
        }
    }

    private val labelPaint = Paint().apply {
        color = ResourcesCompat.getColor(resources, R.color.teal_200, null)
        style = Paint.Style.FILL
    }

    private val paint = Paint().apply {
        color = ResourcesCompat.getColor(resources, R.color.teal_200, null)
        style = Paint.Style.STROKE
    }

    private val paint2 = Paint().apply {
        color = Color.rgb(100, 100, 100)
    }

    private  val verticalMatrix = Matrix()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val heightF = height.toFloat()
        val widthF = width.toFloat()
        val bottom = heightF - heightF / 10f
        val left = widthF / 10f
        paint.strokeWidth = heightF / 120f
        labelPaint.textSize = heightF / 15f
        paint2.strokeWidth = heightF / 120f
        canvas.drawLine(left, bottom, widthF, bottom, paint2)
        canvas.drawLine(left, 0f, left, bottom, paint2)
        verticalMatrix.setRotate(-90f)
        verticalMatrix.postTranslate(0f , heightF)
        canvas.withMatrix(verticalMatrix) {
            labelPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(yMinLabel, heightF - bottom, left - widthF / 60f, labelPaint)
            labelPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(yLabel, bottom/2f + heightF - bottom, left - widthF / 60f, labelPaint)
            labelPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(yMaxLabel, heightF, left - widthF / 60f, labelPaint)
        }
        labelPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(xMinLabel, left, bottom + heightF / 15f, labelPaint)
        labelPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(xLabel, left + (widthF - left) / 2f, bottom + heightF / 15f, labelPaint)
        labelPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(xMaxLabel, widthF, bottom + heightF / 15f, labelPaint)
        var curX = left
        var curY = bottom
        val xStep = xSpan / 100f
        val xPixRange = (widthF - left) / xSpan
        val yPixRange = bottom / ySpan
        for(i in 1..100) {
            val x = xStep * i
            val y = a * x.pow(b)
            val newX = left + x * xPixRange
            val newY = bottom - y * yPixRange
            canvas.drawLine(curX, curY, newX, newY, paint)
            curX = newX
            curY = newY
        }
    }
}