package com.example.flyingandroidclient

import android.animation.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class AccelerationSliderMode {
    UNSET,
    US_HEIGHT,
    REL_ACC,
    BAR_HOLD
}


class AccelerationSlider @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val color1 = ResourcesCompat.getColor(resources, R.color.teal_200, null)
    private val color2 = ResourcesCompat.getColor(resources, R.color.gray_600, null)
    private val colorFilt = PorterDuffColorFilter(ResourcesCompat.getColor(resources, R.color.teal_200, null), PorterDuff.Mode.SRC_IN)

    private val paint1 = Paint()
    private val paint2 = Paint()
    private val paint3 = Paint().apply {
        colorFilter = colorFilt
    }

    private var _desiredHeight = 0f
    var desiredHeight: Float
        set(value) {
            _desiredHeight = value
            invalidate()
        }
        get() = _desiredHeight

    private var _relativeAcc = 0f
    var relativeAcc: Float
        set(value) {
            _relativeAcc = value
            invalidate()
        }
        get() = _relativeAcc


    private var _mode = AccelerationSliderMode.UNSET
    var mode: AccelerationSliderMode
        set(value) {
            val nextModesTransitionState = when(value) {
                AccelerationSliderMode.UNSET -> 0f
                AccelerationSliderMode.US_HEIGHT -> 0f
                AccelerationSliderMode.REL_ACC -> 1f
                AccelerationSliderMode.BAR_HOLD -> 2f
            }
            if (_mode == AccelerationSliderMode.UNSET) {
                modesTransitionState = nextModesTransitionState
            } else {
                ValueAnimator.ofFloat(modesTransitionState, nextModesTransitionState).apply {
                    duration = 300
                    addUpdateListener {
                        modesTransitionState = it.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
            }
            _mode = value
        }
        get() = _mode

    private val upArrow = BitmapFactory.decodeResource(resources, R.drawable.up_arrow)
    private val downArrow = BitmapFactory.decodeResource(resources, R.drawable.down_arrow)
    private val lockImg = BitmapFactory.decodeResource(resources, R.drawable.lock)

    private val arrowUpPosition = Rect()
    private val arrowDownPosition = Rect()
    private val textBoundsTmp = Rect()
    private val lockImgPosition = Rect()

    private val relAccScreenRange: Float
        get() = height / 3f

    private var modesTransitionState = 0f

    override fun onDraw(canvas: Canvas?) {
        val topBottomArrowsMovement = 1f - modesTransitionState
        super.onDraw(canvas)
        val arrowSize: Int = (width * 0.1).toInt()
        arrowUpPosition.left = width / 2 - arrowSize / 2
        arrowUpPosition.top = ((height / 4) * topBottomArrowsMovement).toInt()
        arrowUpPosition.right = width / 2 + arrowSize /2
        arrowUpPosition.bottom = ((height / 4) * topBottomArrowsMovement).toInt() + arrowSize

        arrowDownPosition.left = width / 2 - arrowSize / 2
        arrowDownPosition.top = height - ((height / 4) * topBottomArrowsMovement).toInt() - arrowSize
        arrowDownPosition.right = width / 2 + arrowSize /2
        arrowDownPosition.bottom =height - ((height / 4) * topBottomArrowsMovement).toInt()

        canvas?.drawBitmap(upArrow, null, arrowUpPosition, paint1)
        canvas?.drawBitmap(downArrow, null, arrowDownPosition, paint1)


        if (modesTransitionState < 1f) { // draw us mode elements
            val shiftForElements = modesTransitionState * width / 2
            val alphaForElements = ((1f - max(min(modesTransitionState * 2, 1f), 0f)) * 255).toInt()

            val text = String.format("%.2f", if (draggedInUsHeightMode) draggedDesiredHeightValue else desiredHeight ) + " m"
            paint2.textSize = height / 10f
            paint2.color = color1
            paint2.alpha = alphaForElements
            paint2.getTextBounds(text, 0, text.length, textBoundsTmp)
            canvas?.drawText(text, width / 2f - textBoundsTmp.exactCenterX() - shiftForElements, height / 2f - textBoundsTmp.exactCenterY(), paint2)
        }
        if (modesTransitionState > 0f && modesTransitionState < 2f) { // draw rel acc mode elements
            val elementsAppearance = max(min((0.5f - abs(1f - modesTransitionState)), 0.5f), 0f) * 2f

            paint2.color = color2
            paint2.alpha = (elementsAppearance * 255).toInt()
            paint2.strokeWidth = height / 100f
            val lineWidth2 = height / 6
            for(n in 0..20) {
                val curY = height / 6f + n * (height * 4f) / (6f * 20f)
                canvas?.drawLine(width / 2f - lineWidth2 / 2f, curY, width / 2f + lineWidth2 / 2f, curY, paint2)
            }

            val lineWidth = height / 3
            val lineY = getRelAccValueScreenPos(if (draggedInRelativeAccMode) draggedRelativeAccValue else relativeAcc)
            paint2.color = color1
            paint2.strokeWidth = height / 20f * elementsAppearance
            paint2.alpha = (elementsAppearance * 255).toInt()
            canvas?.drawLine(width / 2f - lineWidth / 2f, lineY, width / 2f + lineWidth / 2f, lineY, paint2)
        }
        if (modesTransitionState > 1f) { // draw bar hold elements
            val elementAppearance = max(min((0.5f - abs(2f - modesTransitionState)), 0.5f), 0f) * 2f
            val imgSize = height / 3
            val shiftForElements = ((1f - elementAppearance) * width / 2).toInt()
            paint3.alpha = (elementAppearance * 255).toInt()
            lockImgPosition.left = width / 2 - imgSize / 2 + shiftForElements
            lockImgPosition.top = height / 2 - imgSize / 2
            lockImgPosition.right = width / 2 + imgSize / 2 + shiftForElements
            lockImgPosition.bottom = height / 2 + imgSize /2
            canvas?.drawBitmap(lockImg, null, lockImgPosition, paint3)
        }
    }

    private var draggedDesiredHeightValue = 0f
    private var draggedRelativeAccValue = 0f
    private var draggedInUsHeightMode = false
    private var draggedInRelativeAccMode = false
    private var draggedInBarHoldMode = false
    private var dragStartTouchPositionX = 0.0f
    private var dragStartTouchPositionY = 0.0f
    private var dragStartDesiredHeight = 0f

    override fun onTouchEvent(e: MotionEvent): Boolean {
        return when(mode) {
            AccelerationSliderMode.US_HEIGHT -> onTouchEventInUsHeightMode(e)
            AccelerationSliderMode.BAR_HOLD -> onTouchEventInBarHoldMode(e)
            AccelerationSliderMode.REL_ACC -> onTouchEventInRelAccMode(e)
            AccelerationSliderMode.UNSET -> false
        }
    }

    private fun onTouchEventInUsHeightMode(e: MotionEvent): Boolean {
        if ((e.action == MotionEvent.ACTION_MOVE || e.action == MotionEvent.ACTION_UP) && !draggedInUsHeightMode) return false
        if (e.action == MotionEvent.ACTION_UP) {
            draggedInUsHeightMode = false
            parent.requestDisallowInterceptTouchEvent(false)
        }
        val touchPositionX = e.getX(0)
        val touchPositionY = e.getY(0)
        if (e.action == MotionEvent.ACTION_DOWN) {
            draggedInUsHeightMode = true
            dragStartTouchPositionX = touchPositionX
            dragStartTouchPositionY = touchPositionY
            dragStartDesiredHeight = desiredHeight
            parent.requestDisallowInterceptTouchEvent(true)
        }
        val touchPositionDeltaX = touchPositionX - dragStartTouchPositionX
        val touchPositionDeltaY = touchPositionY - dragStartTouchPositionY
        draggedDesiredHeightValue = dragStartDesiredHeight - touchPositionDeltaY / height
        if (draggedDesiredHeightValue > 3f) draggedDesiredHeightValue = 3f
        if (draggedDesiredHeightValue < 0f) draggedDesiredHeightValue = 0f
        invalidate()
        for (callback in heightCallbacks) {
            callback(draggedDesiredHeightValue, e.action == MotionEvent.ACTION_UP)
        }
        val changeModeThreshold = width.toFloat() / 3
        if (-touchPositionDeltaX > changeModeThreshold) {
            draggedInUsHeightMode = false
            parent.requestDisallowInterceptTouchEvent(false)
            for (callback in modeCallbacks) {
                callback(AccelerationSliderMode.REL_ACC)
            }
        }
        return true
    }

    private fun updateRelAccValue(touchY: Float, isLast: Boolean) {
        val positionFromCenter = height / 2 - touchY
        draggedRelativeAccValue = max(min(positionFromCenter / relAccScreenRange, 1f), -1f)
        invalidate()
        for (callback in relAccCallbacks) {
            callback(draggedRelativeAccValue, isLast)
        }
    }

    private fun getRelAccValueScreenPos(relAccVal: Float): Float {
        return height / 2f - relAccVal * relAccScreenRange
    }

    private var relAccStartAnim: ValueAnimator? = null

    private fun onTouchEventInRelAccMode(e: MotionEvent): Boolean {
        if ((e.action == MotionEvent.ACTION_MOVE || e.action == MotionEvent.ACTION_UP) && !draggedInRelativeAccMode) return false
        if (e.action == MotionEvent.ACTION_UP) {
            draggedInRelativeAccMode = false
            parent.requestDisallowInterceptTouchEvent(false)
        }
        val touchPositionX = e.getX(0)
        val touchPositionY = e.getY(0)
        if (e.action == MotionEvent.ACTION_DOWN) {
            draggedInRelativeAccMode = true
            dragStartTouchPositionX = touchPositionX
            dragStartTouchPositionY = touchPositionY
            parent.requestDisallowInterceptTouchEvent(true)
            draggedRelativeAccValue = relativeAcc
        }
        val touchPositionDeltaX = touchPositionX - dragStartTouchPositionX
        val touchPositionDeltaY = touchPositionY - dragStartTouchPositionY

        if (e.action == MotionEvent.ACTION_DOWN) {
            relAccStartAnim = ValueAnimator.ofFloat(getRelAccValueScreenPos(draggedRelativeAccValue), touchPositionY).apply {
                duration = 100
                addUpdateListener {
                    val v = it.animatedValue as Float
                    updateRelAccValue(v, false)
                }
                start()
            }
        } else {
            relAccStartAnim?.cancel()
            updateRelAccValue(touchPositionY, e.action == MotionEvent.ACTION_UP)
        }


        val changeModeThreshold = width.toFloat() / 3
        if (touchPositionDeltaX > changeModeThreshold) {
            draggedInRelativeAccMode = false
            parent.requestDisallowInterceptTouchEvent(false)
            for (callback in modeCallbacks) {
                callback(AccelerationSliderMode.US_HEIGHT)
            }
        }
        if (-touchPositionDeltaX > changeModeThreshold) {
            draggedInRelativeAccMode = false
            parent.requestDisallowInterceptTouchEvent(false)
            for (callback in modeCallbacks) {
                callback(AccelerationSliderMode.BAR_HOLD)
            }
        }
        return true
    }

    private fun onTouchEventInBarHoldMode(e: MotionEvent): Boolean {
        if ((e.action == MotionEvent.ACTION_MOVE || e.action == MotionEvent.ACTION_UP) && !draggedInBarHoldMode) return false
        if (e.action == MotionEvent.ACTION_UP) {
            draggedInBarHoldMode = false
            parent.requestDisallowInterceptTouchEvent(false)
        }
        val touchPositionX = e.getX(0)
        val touchPositionY = e.getY(0)
        if (e.action == MotionEvent.ACTION_DOWN) {
            draggedInBarHoldMode = true
            dragStartTouchPositionX = touchPositionX
            dragStartTouchPositionY = touchPositionY
            parent.requestDisallowInterceptTouchEvent(true)
        }
        val touchPositionDeltaX = touchPositionX - dragStartTouchPositionX
        val touchPositionDeltaY = touchPositionY - dragStartTouchPositionY
        val changeModeThreshold = width.toFloat() / 3
        if (touchPositionDeltaX > changeModeThreshold) {
            draggedInBarHoldMode = false
            parent.requestDisallowInterceptTouchEvent(false)
            for (callback in modeCallbacks) {
                callback(AccelerationSliderMode.REL_ACC)
            }
        }
        return true
    }

    private val heightCallbacks: MutableList<(Float, Boolean) -> Unit> = mutableListOf()
    private val modeCallbacks: MutableList<(AccelerationSliderMode) -> Unit> = mutableListOf()
    private val relAccCallbacks: MutableList<(Float, Boolean) -> Unit> = mutableListOf()


    fun setHeightChangedListener (callback: (Float, Boolean) -> Unit) {
        heightCallbacks.add(callback)
    }
    fun setModeChangedListener (callback: (AccelerationSliderMode) -> Unit) {
        modeCallbacks.add(callback)
    }
    fun setRelativeAccelerationChangedListener (callback: (Float, Boolean) -> Unit) {
        relAccCallbacks.add(callback)
    }
}