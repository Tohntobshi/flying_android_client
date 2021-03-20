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
import kotlin.math.max
import kotlin.math.min


class Slider @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        color = ResourcesCompat.getColor(resources, R.color.teal_200, null)
        textAlign = Paint.Align.CENTER
    }

    private var _position = 0f
    var position: Float
        set(value) {
            _position = value
            invalidate()
        }
        get() {
            return _position
        }
    private var _positionChangeMultiplier = 1f
    var positionChangeMultiplier: Float
        set(value) {
            _positionChangeMultiplier = value
            invalidate()
        }
        get() {
            return _positionChangeMultiplier
        }
    private var _format = "%.02f"
    private var _fractionDigits = 2
    var fractionDigits: Int
        set(value) {
            _fractionDigits = max(min(value, 5), 0)
            _format = "%.0${_fractionDigits}f"
            invalidate()
        }
        get() {
            return fractionDigits
        }
    private var isVertical = true

    init {
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.Slider,
                0, 0).apply {
            try {
                fractionDigits = getInteger(R.styleable.Slider_fractionDigits, 2)
                isVertical = getInteger(R.styleable.Slider_orientation, 0) == 0
                _positionChangeMultiplier = getFloat(R.styleable.Slider_positionChangeMultiplier, 1f)
            } finally {
                recycle()
            }
        }
    }


    private fun getRelativePosition(position: Float): Float {
      return if (position >= 0f) (position % POSITION_CHANGE_FOR_FULL_LENGTH) else (POSITION_CHANGE_FOR_FULL_LENGTH - (-position % POSITION_CHANGE_FOR_FULL_LENGTH))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val length = if (isVertical) height.toFloat() else width.toFloat()
        val thickness = if (isVertical) width.toFloat() else height.toFloat()
        paint.textSize = thickness / 2f
        paint.strokeWidth = thickness / 10f

        val positionToShow = if (dragged) draggedPosition else position
        val stepHeight = length / POSITION_CHANGE_FOR_FULL_LENGTH

        if (isVertical) {
            canvas?.drawLine(0f, 0f, 0f, length, paint)
            canvas?.drawLine(thickness, 0f, thickness, length, paint)
            for (index in 0..2) {
                val indicatorPosition = getRelativePosition(positionToShow / positionChangeMultiplier + 0.5f * POSITION_CHANGE_FOR_FULL_LENGTH + index.toFloat()) * stepHeight
                canvas?.drawLine(0f, indicatorPosition, thickness / 4f, indicatorPosition, paint)
                canvas?.drawLine(thickness * 3f / 4f, indicatorPosition, thickness, indicatorPosition, paint)
            }
            canvas?.rotate(-90.0f)
            canvas?.drawText(String.format(_format, positionToShow), -length / 2f, thickness * 2f/3f, paint)
        } else {
            canvas?.drawLine(0f, 0f, length, 0f,  paint)
            canvas?.drawLine(0f, thickness, length, thickness, paint)
            for (index in 0..2) {
                val indicatorPosition = getRelativePosition(-positionToShow / positionChangeMultiplier + 0.5f * POSITION_CHANGE_FOR_FULL_LENGTH + index.toFloat()) * stepHeight
                canvas?.drawLine(indicatorPosition, 0f, indicatorPosition, thickness / 4f, paint)
                canvas?.drawLine(indicatorPosition, thickness * 3f / 4f, indicatorPosition, thickness, paint)
            }
            canvas?.drawText(String.format(_format, positionToShow), length / 2f, thickness * 2f/3f, paint)
        }

    }

    private var draggedPosition = 0f
    private var dragged = false
    private var dragStartTouchPosition = 0f
    private var dragStartPosition = 0f

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action === MotionEvent.ACTION_MOVE && !dragged) return false
        if (e.action === MotionEvent.ACTION_UP) {
            dragged = false
            return true
        }
        val touchPosition = if (isVertical) e.getY(0) else e.getX(0)
        val viewLength = if (isVertical) height.toFloat() else width.toFloat()
        if (e.action === MotionEvent.ACTION_DOWN) {
            dragged = true
            dragStartTouchPosition = touchPosition
            dragStartPosition = position
        }
        val touchPositionDelta = (touchPosition - dragStartTouchPosition) * (if (isVertical) 1f else -1f)
        draggedPosition = dragStartPosition + positionChangeMultiplier * POSITION_CHANGE_FOR_FULL_LENGTH * touchPositionDelta / viewLength
        invalidate()
        for (callback in callbacks) {
            callback(draggedPosition)
        }
        return true
    }

    private val callbacks: MutableList<(Float) -> Unit> = mutableListOf()

    fun setValueChangedListener (callback: (Float) -> Unit) {
        callbacks.add(callback)
    }

    companion object {
        private val POSITION_CHANGE_FOR_FULL_LENGTH = 3f
    }
}