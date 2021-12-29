package com.example.flyingandroidclient

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withMatrix
import kotlin.math.*

class Joystick @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private fun updateJoystick(x: Float, y: Float, isLast: Boolean) {
        val xFromCenter = x - centerX
        val yFromCenter = centerY - y
        val newPosition = PointF(xFromCenter, yFromCenter)
        val newPosLength = newPosition.length()
        if (newPosLength > maxJoystickMovement) {
            newPosition.x = newPosition.x * maxJoystickMovement / newPosLength
            newPosition.y = newPosition.y * maxJoystickMovement / newPosLength
        }
        val normalizedPosition = PointF(newPosition.x / maxJoystickMovement, newPosition.y / maxJoystickMovement)
        position = normalizedPosition
        invalidate()
        for (callback in positionCallbacks) {
            callback(normalizedPosition, isLast)
        }
    }

    private fun getJoystickPositionAsScreenCoords(): PointF {
        val x = position.x * maxJoystickMovement + centerX
        val y = centerY - position.y * maxJoystickMovement
        return PointF(x, y)
    }

    private fun updateWheel(x: Float, y: Float, start: Boolean, stop: Boolean) {
        if (start) {
            dragStartDirection = direction
            dragStartTouchPosition = PointF(x, y)
            draggedDirection = direction
        } else {
            // TODO probably need to normalize vectors before further computation
            val x1 = dragStartTouchPosition.x - centerX
            val y1 = centerY - dragStartTouchPosition.y
            val x2 = x - centerX
            val y2 = centerY - y
            val angle = atan2(x1*y2-y1*x2,x1*x2+y1*y2) * 180f / PI.toFloat()
            draggedDirection = clampDegrees(dragStartDirection - angle)
            for (callback in directionCallbacks) {
                callback(draggedDirection, stop)
            }
            if (stop) {
                dragStartDirection = 0f
                draggedDirection = 0f
                dragStartTouchPosition = PointF(0f, 0f)
            }
        }
        invalidate()
    }

    private class PointEvaluator : TypeEvaluator<PointF> {
        override fun evaluate(fraction: Float, startValue: PointF, endValue: PointF): PointF {
            val x = startValue.x + (endValue.x - startValue.x) * fraction
            val y = startValue.y + (endValue.y - startValue.y) * fraction
            return PointF(x, y)
        }
    }

    private val pointEvaluator = PointEvaluator()

    private var joystickStartAnimation: ValueAnimator? = null
    private var joystickReleaseAnimation: ValueAnimator? = null

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val pointerIndex: Int = e.actionIndex
        val pointerId: Int = e.getPointerId(pointerIndex)
        val action = e.action and MotionEvent.ACTION_MASK
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            val lengthFromCenter = PointF(e.getX(pointerIndex) - centerX, centerY - e.getY(pointerIndex)).length()
            if(lengthFromCenter < 0.7f * radius && joystickPointerId == null) {
                joystickPointerId = pointerId
                joystickReleaseAnimation?.cancel()
                joystickStartAnimation = ValueAnimator.ofObject(pointEvaluator, getJoystickPositionAsScreenCoords(), PointF(e.getX(pointerIndex), e.getY(pointerIndex))).apply {
                    duration = 50
                    addUpdateListener {
                        val v = it.animatedValue as PointF
                        updateJoystick(v.x, v.y, false)
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            val v = (animation as ValueAnimator).animatedValue as PointF
                            updateJoystick(v.x, v.y, true)
                        }
                    })
                    start()
                }
                return true
            }
            if (lengthFromCenter >= 0.7f * radius && lengthFromCenter <= radius && wheelPointerId == null) {
                wheelPointerId = pointerId
                updateWheel(e.getX(pointerIndex), e.getY(pointerIndex), true, false)
                return true
            }
            return false

        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL) {
            if (pointerId == joystickPointerId) {
                joystickPointerId = null
                joystickStartAnimation?.cancel()
                joystickReleaseAnimation = ValueAnimator.ofObject(pointEvaluator, PointF(e.getX(pointerIndex), e.getY(pointerIndex)) , PointF(centerX, centerY)).apply {
                    duration = 100
                    addUpdateListener {
                        val v = it.animatedValue as PointF
                        updateJoystick(v.x, v.y, false)
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            val v = (animation as ValueAnimator).animatedValue as PointF
                            updateJoystick(v.x, v.y, true)
                        }
                    })
                    start()
                }
                return true
            }
            if (pointerId == wheelPointerId) {
                wheelPointerId = null
                updateWheel(e.getX(pointerIndex), e.getY(pointerIndex), false, true)
                return true
            }
        }
        if (e.action == MotionEvent.ACTION_MOVE) {
            for(index in 0 until e.pointerCount) {
                if (e.getPointerId(index) == wheelPointerId) updateWheel(e.getX(index), e.getY(index), false, false)
                if (e.getPointerId(index) == joystickPointerId) {
                    joystickStartAnimation?.cancel()
                    updateJoystick(e.getX(index), e.getY(index), false)
                }
            }
            return true
        }
        return false
    }

    private var joystickPointerId: Int? = null
    private var wheelPointerId: Int? = null

    private var draggedDirection = 0f
    private var dragStartTouchPosition = PointF(0f, 0f)
    private var dragStartDirection = 0f

    private var position = PointF(0.0f, 0.0f)
    private var _direction = 0f
    var direction: Float
        get() = _direction
        set(value) {
            _direction = value
            invalidate()
        }

    private fun clampDegrees(deg: Float): Float {
        val clamped = if (deg >= 0f) deg % 360f else 360f - (-deg % 360f)
        return if (clamped == 360f) 0f else clamped
    }

    private val radius: Float
        get() = if (width < height) width / 2f else height / 2f
    private val top: Float
        get() = if (width < height) (height - width) / 2f else 0f
    private val bottom: Float
        get() = if (width < height) height/2f + width/2f else height.toFloat()
    private val left: Float
        get() = if (width < height) 0f else (width - height) / 2f
    private val right: Float
        get() = if (width < height) width.toFloat() else height/2f + width/2f
    private val centerX: Float
        get() = left + radius
    private val centerY: Float
        get() = top + radius
    private val maxJoystickMovement: Float
        get() = radius * 2f / 5f

    private val paint = Paint().apply {
        color = ResourcesCompat.getColor(resources, R.color.teal_200, null)
        textAlign = Paint.Align.CENTER
    }

    private val boundariesTmp = RectF()

    private val rotationMatrix = Matrix()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val strokeWidth = radius / 7f
        paint.style = Paint.Style.STROKE

        paint.strokeWidth = strokeWidth / 2f
        paint.textSize = strokeWidth
        val directionToShow = if (wheelPointerId == null) direction else draggedDirection
        val directionToShowInt = directionToShow.roundToInt()
        boundariesTmp.left = left + strokeWidth / 2f
        boundariesTmp.top = top + strokeWidth / 2f
        boundariesTmp.right = right - strokeWidth / 2f
        boundariesTmp.bottom = bottom - strokeWidth / 2f

        rotationMatrix.setRotate(directionToShow, centerX, centerY)
        canvas?.withMatrix(rotationMatrix) {
            var i = 0
            while (i < 90) {
                val degToSkip = clampDegrees(4f * i + directionToShow)
                val deg = clampDegrees(4f * i)
                i += 1
                if (degToSkip > 255 && degToSkip < 285) continue
                canvas?.drawArc(boundariesTmp, deg, 1.5f, false, paint)

            }
        }


        paint.style = Paint.Style.FILL
        val x = centerX + position.x * maxJoystickMovement
        val y = centerY - position.y * maxJoystickMovement
        canvas?.drawCircle(x, y, radius / 2f, paint)
        canvas?.drawText("${if (directionToShowInt == 360) 0 else directionToShowInt}°", left + radius, top + strokeWidth, paint)
    }

    private val positionCallbacks: MutableList<(PointF, Boolean) -> Unit> = mutableListOf()
    private val directionCallbacks: MutableList<(Float, Boolean) -> Unit> = mutableListOf()

    fun setPositionListener(callback: (PointF, Boolean) -> Unit) {
        positionCallbacks.add(callback)
    }
    fun setDirectionListener(callback: (Float, Boolean) -> Unit) {
        directionCallbacks.add(callback)
    }
}