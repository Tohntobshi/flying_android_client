package com.example.flyingandroidclient

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withMatrix

class VideoFrame@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint()
    private var _image: Bitmap? = null
    var image: Bitmap?
        get() = _image
        set(value) {
            _image = value
            if(value != null) {
                invalidate()
            }
        }
    private val drawPosition = Rect()
//    private val rotationMatrix = Matrix()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        _image?.let {
            drawPosition.left = 0
            drawPosition.top = 0
            drawPosition.right = width
            drawPosition.bottom = height
//            rotationMatrix.setRotate(90F, (width / 2).toFloat(), (height / 2).toFloat())
            canvas.drawBitmap(it, null, drawPosition, paint)
//            canvas?.withMatrix(rotationMatrix) {
//
//            }
        }
    }
}