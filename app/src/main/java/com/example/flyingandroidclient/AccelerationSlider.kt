package com.example.flyingandroidclient

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.getFloatOrThrow
import kotlin.math.max
import kotlin.math.min


class AccelerationSlider @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val activeColor = ResourcesCompat.getColor(resources, R.color.teal_200, null)
    private val passiveColor = ResourcesCompat.getColor(resources, R.color.gray_600, null)
    private val paint = Paint().apply {
        color = ResourcesCompat.getColor(resources, R.color.teal_200, null)
        textAlign = Paint.Align.CENTER
    }

    private var _value = 0f
    var value: Float
        set(value) {
            _value = value
            invalidate()
        }
        get() = _value


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val length = height.toFloat()
        val thickness = width.toFloat()
        paint.textSize = length / 10f
        paint.strokeWidth = length / 22f

        val valueToShow = if (dragged) draggedValue else value
        paint.color = activeColor
        canvas?.drawText(String.format("%.0f", valueToShow * 100) + " %", thickness / 2f, length / 10f, paint)
        // val remainingLength = 0.9f * length
        for (index in 0..9) {
            paint.color = if (9 - index < valueToShow * 10) activeColor else passiveColor
            val y = index * (0.85f * length) / 10f + 0.20f * length
            canvas?.drawLine(0f, y, thickness, y, paint)
        }

    }

    private var draggedValue = 0f
    private var dragged = false
    private var dragStartTouchPosition = 0f
    private var dragStartValue = 0f

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action === MotionEvent.ACTION_MOVE && !dragged) return false
        if (e.action === MotionEvent.ACTION_UP) {
            dragged = false
            parent.requestDisallowInterceptTouchEvent(false)
            return true
        }
        val touchPosition = e.getY(0)
        val viewLength = height.toFloat()
        if (e.action === MotionEvent.ACTION_DOWN) {
            dragged = true
            dragStartTouchPosition = touchPosition
            dragStartValue = value
            parent.requestDisallowInterceptTouchEvent(true)
        }
        val touchPositionDelta = touchPosition - dragStartTouchPosition
        draggedValue = dragStartValue - POSITION_CHANGE_FOR_FULL_LENGTH * touchPositionDelta / viewLength
        if (draggedValue > 1f) draggedValue = 1f
        if (draggedValue < 0f) draggedValue = 0f
        invalidate()
        for (callback in callbacks) {
            callback(draggedValue)
        }
        return true
    }

    private val callbacks: MutableList<(Float) -> Unit> = mutableListOf()

    fun setValueChangedListener (callback: (Float) -> Unit) {
        callbacks.add(callback)
    }

    companion object {
        val POSITION_CHANGE_FOR_FULL_LENGTH = 0.5f
    }
}