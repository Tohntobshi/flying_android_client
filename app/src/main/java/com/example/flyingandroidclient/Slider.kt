package com.example.flyingandroidclient

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat

val POSITION_CHANGE_FOR_FULL_HEIGHT = 3f

class Slider @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        // color = Color.rgb(54 , 237,237)
        color = ResourcesCompat.getColor(resources, R.color.teal_200, null)
        textAlign = Paint.Align.CENTER
    }


    private var position = 0f

    private fun getRelativePosition(position: Float): Float {
      return if (position >= 0f) (position % POSITION_CHANGE_FOR_FULL_HEIGHT) else (POSITION_CHANGE_FOR_FULL_HEIGHT - (-position % POSITION_CHANGE_FOR_FULL_HEIGHT))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.strokeWidth = width.toFloat() / 10f
        canvas?.drawLine(0f, 0f, 0f, height.toFloat(), paint)
        canvas?.drawLine(width.toFloat(), 0f, width.toFloat(), height.toFloat(), paint)
        val stepHeight = height.toFloat() / POSITION_CHANGE_FOR_FULL_HEIGHT
        for (index in 0..2) {
            val indicatorPosition = getRelativePosition(position + 0.5f * POSITION_CHANGE_FOR_FULL_HEIGHT + index.toFloat()) * stepHeight
            canvas?.drawLine(0f, indicatorPosition, width.toFloat() / 4f, indicatorPosition, paint)
            canvas?.drawLine(width.toFloat() * 3f / 4f, indicatorPosition, width.toFloat(), indicatorPosition, paint)
        }
        paint.style = Paint.Style.FILL
        paint.textSize = width.toFloat() / 2f
        canvas?.rotate(-90.0f)
        canvas?.drawText(String.format("%.02f", position), -height.toFloat() / 2f, width.toFloat() * 2f/3f, paint)
    }

    private var dragged = false
    private var dragStartCoordYPosition = 0f
    private var dragStartPosition = 0f

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action === MotionEvent.ACTION_MOVE && !dragged) return false
        val coordY = e.getY(0)
        if (e.action === MotionEvent.ACTION_DOWN) {
            dragged = true
            dragStartCoordYPosition = coordY
            dragStartPosition = position
        }
        if (e.action === MotionEvent.ACTION_UP) {
            dragged = false
        }
        val coordYDelta = coordY - dragStartCoordYPosition
        position = dragStartPosition + POSITION_CHANGE_FOR_FULL_HEIGHT * coordYDelta / height.toFloat()
        invalidate()
        return true
    }
}