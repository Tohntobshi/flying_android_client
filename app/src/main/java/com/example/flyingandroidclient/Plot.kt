package com.example.flyingandroidclient

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat

/**
 * TODO: document your custom view class.
 */
class Plot @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var _plotData: MutableList<Float> = mutableListOf()

    private var valueRange = 90f

    private val paint = Paint().apply {
        color = ResourcesCompat.getColor(resources, R.color.teal_200, null)
        textAlign = Paint.Align.CENTER
        style = Paint.Style.STROKE
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
        val vertHalfRange = height.toFloat() / 2
        val horStep = width.toFloat() / 100
        val initX = width.toFloat()
        var i = 0
        var lastX = 0f
        var lastY = 0f
        for(value in plotData.reversed()) {
            val x = initX - horStep * i
            val y = vertHalfRange - (value / valueRange) * vertHalfRange
            if (i != 0) canvas.drawLine(lastX, lastY, x, y, paint)
            i++
            lastX = x
            lastY = y
        }
    }
}