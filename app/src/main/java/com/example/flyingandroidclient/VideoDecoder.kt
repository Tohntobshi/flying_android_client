package com.example.flyingandroidclient

import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaCodecInfo
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.LinkedBlockingQueue

val IMAGE_WIDTH = 320
val IMAGE_HEIGHT = 240

class VideoDecoder (private val viewModel: MainActivityViewModel) {
    val lastDecodedFrame = MutableLiveData<Bitmap>()
    private val inputPackets = LinkedBlockingQueue<ByteArray>(100)
    private var pts = 0L
    fun putPacket(data: ByteArray) {
//        Log.i("myinfo", "received video packet of ${data.size} bytes")
        inputPackets.offer(data)
    }
    init {
        viewModel.viewModelScope.launch (Dispatchers.Default) {
            val codec = MediaCodec.createDecoderByType("video/mp4v-es")
            val inputFormat = MediaFormat()
            inputFormat.setInteger(MediaFormat.KEY_WIDTH, IMAGE_WIDTH)
            inputFormat.setInteger(MediaFormat.KEY_HEIGHT, IMAGE_HEIGHT)
            inputFormat.setString(MediaFormat.KEY_MIME, "video/mp4v-es")
            inputFormat.setLong(MediaFormat.KEY_DURATION, 66000)
            inputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) // COLOR_FormatYUV420Planar
            inputFormat.setInteger(MediaFormat.KEY_CAPTURE_RATE, 15)
            codec.configure(inputFormat, null, null, 0)
            codec.start()

            val outputBufferInfo = MediaCodec.BufferInfo()
            while (true) {
                val inputBufferId = codec.dequeueInputBuffer(-1)
                if (inputBufferId >= 0) {
                    val bytes = inputPackets.take()
                    val inputBuffer = codec.getInputBuffer(inputBufferId)
                    inputBuffer?.clear()
                    inputBuffer?.put(bytes)
                    codec.queueInputBuffer(inputBufferId, 0, bytes.size, pts, 0)
                    pts += 33000
                }
                val outputBufferId = codec.dequeueOutputBuffer(outputBufferInfo, 0)
                if (outputBufferId >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferId)
                    val bufferFormat = codec.getOutputFormat(outputBufferId)
//                    Log.i("myinfo", "received output buffer of ${outputBufferInfo.size} bytes")
                    if (outputBuffer != null) {
                        val bytes = ByteArray(outputBufferInfo.size)
                        outputBuffer.get(bytes)
                        val bitmap = yuv420SPImageToBitmapRotated(bytes, IMAGE_WIDTH, IMAGE_HEIGHT)
                        withContext(Dispatchers.Main) {
                            lastDecodedFrame.value = bitmap
                        }
                    }
                    codec.releaseOutputBuffer(outputBufferId, false)
                }
            }
            codec.stop()
            codec.release()
        }
    }
    private fun yuv420SPImageToBitmap(bytes: ByteArray, width: Int, height: Int): Bitmap {
        val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                var yElem = bytes[y * width + x].toUByte().toInt()
                // planar way
//                var uElem = bytes[width * height + (y / 2) * (width / 2) + x / 2].toUByte().toInt()
//                var vElem = bytes[width * height + width * height / 4 + (y / 2) * (width / 2) + x / 2].toUByte().toInt()
                val y2 = y / 2
                val x2 = (x / 2) * 2
                var uElem = bytes[width * height + y2 * width + x2].toUByte().toInt()
                var vElem = bytes[width * height + y2 * width + x2 + 1].toUByte().toInt()
                var blue = (1.164 * (yElem - 16) + 2.018 * (uElem - 128)).toInt()
                var green = (1.164 * (yElem - 16) - 0.813 * (vElem - 128) - 0.391 * (uElem - 128)).toInt()
                var red = (1.164 * (yElem - 16) + 1.596 * (vElem - 128)).toInt()
                red = if (red > 255) 255 else (if (red < 0) 0 else red)
                green = if (green > 255) 255 else (if (green < 0) 0 else green)
                blue = if (blue > 255) 255 else (if (blue < 0) 0 else blue)
                bm.setPixel(x, y, Color.argb(255, red, green, blue))
            }
        }
        return bm
    }
    private fun yuv420SPImageToBitmapRotated(bytes: ByteArray, width: Int, height: Int): Bitmap {
        val bm = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                var yElem = bytes[y * width + x].toUByte().toInt()
                val y2 = y / 2
                val x2 = (x / 2) * 2
                var uElem = bytes[width * height + y2 * width + x2].toUByte().toInt()
                var vElem = bytes[width * height + y2 * width + x2 + 1].toUByte().toInt()
                var blue = (1.164 * (yElem - 16) + 2.018 * (uElem - 128)).toInt()
                var green = (1.164 * (yElem - 16) - 0.813 * (vElem - 128) - 0.391 * (uElem - 128)).toInt()
                var red = (1.164 * (yElem - 16) + 1.596 * (vElem - 128)).toInt()
                red = if (red > 255) 255 else (if (red < 0) 0 else red)
                green = if (green > 255) 255 else (if (green < 0) 0 else green)
                blue = if (blue > 255) 255 else (if (blue < 0) 0 else blue)
                bm.setPixel(height - 1 - y, x, Color.argb(255, red, green, blue))
            }
        }
        return bm
    }

}