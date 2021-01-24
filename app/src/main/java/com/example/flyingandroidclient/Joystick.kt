package com.example.flyingandroidclient

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

val MAX_LENGTH = 0.4f

class Joystick @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action === MotionEvent.ACTION_MOVE && !dragged) return false
        val coordX = e.getX(0)
        val coordY = e.getY(0)
        val normX = (coordX / width) * 2 - 1
        val normY = -(coordY / width) * 2 + 1
        var newPosition = PointF(normX, normY)
        if (newPosition.length() > MAX_LENGTH) {
            newPosition.set((newPosition.x * MAX_LENGTH )/ newPosition.length(), (newPosition.y * MAX_LENGTH )/ newPosition.length())
        }
        if (e.action === MotionEvent.ACTION_DOWN) {
            dragged = true
        }
        if (e.action === MotionEvent.ACTION_UP) {
            dragged = false
            newPosition = PointF(0.0f,0.0f)
        }
        position = newPosition
        invalidate()
        val postionForCallback = PointF(newPosition.x * (1 / MAX_LENGTH), newPosition.y * (1 / MAX_LENGTH))
        for (callback in callbacks) {
            callback(postionForCallback)
        }
        return true
    }

    private val paint = Paint().apply {
        color = Color.rgb(54 , 237,237)
    }

    private var dragged = false

    private var position = PointF(0.0f,0.0f)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (width / 20).toFloat()
        canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (width / 2.2).toFloat(), paint)
        paint.style = Paint.Style.FILL
        val x = width.toFloat() / 2 + position.x * width.toFloat() / 2
        val y = height.toFloat() / 2 - position.y * width.toFloat() / 2
        canvas?.drawCircle(x, y, (width / 4).toFloat(), paint)
    }

    private val callbacks: MutableList<(PointF) -> Unit> = mutableListOf()

    fun setPositionListener (callback: (PointF) -> Unit) {
        callbacks.add(callback)
    }
}