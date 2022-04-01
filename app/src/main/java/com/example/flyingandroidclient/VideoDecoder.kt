package com.example.flyingandroidclient

import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaCodecInfo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import java.util.concurrent.LinkedBlockingQueue

val IMAGE_WIDTH = 320
val IMAGE_HEIGHT = 240

class VideoDecoder (private val viewModel: MainActivityViewModel) {
    val lastDecodedFrame = MutableLiveData<Bitmap>()
    private val inputPackets = LinkedBlockingQueue<ByteArray>(30)
    fun putPacket(data: ByteArray) {
        inputPackets.offer(data)
    }
    fun startDecodingToBitmap() {
        viewModel.viewModelScope.launch (Dispatchers.Default) {
            val codec = MediaCodec.createDecoderByType("video/mp4v-es")
            val inputFormat = MediaFormat()
            inputFormat.setInteger(MediaFormat.KEY_WIDTH, IMAGE_WIDTH)
            inputFormat.setInteger(MediaFormat.KEY_HEIGHT, IMAGE_HEIGHT)
            inputFormat.setString(MediaFormat.KEY_MIME, "video/mp4v-es")
            inputFormat.setLong(MediaFormat.KEY_DURATION, 66000)
            inputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
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
                    codec.queueInputBuffer(inputBufferId, 0, bytes.size, 0, 0)
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
        }
    }
    private fun yuvToRgb(yElem: Int, uElem: Int, vElem: Int): Triple<Int, Int, Int> {
        var red = (1.164 * (yElem - 16) + 1.596 * (vElem - 128)).toInt()
        var green = (1.164 * (yElem - 16) - 0.813 * (vElem - 128) - 0.391 * (uElem - 128)).toInt()
        var blue = (1.164 * (yElem - 16) + 2.018 * (uElem - 128)).toInt()

        red = if (red > 255) 255 else (if (red < 0) 0 else red)
        green = if (green > 255) 255 else (if (green < 0) 0 else green)
        blue = if (blue > 255) 255 else (if (blue < 0) 0 else blue)
        return Triple(red, green, blue)
    }
    private fun yuv420SPImageToBitmap(bytes: ByteArray, width: Int, height: Int): Bitmap {
        val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                var yElem = bytes[y * width + x].toUByte().toInt()
                // planar way
//                var uElem = bytes[width * height + (y / 2) * (width / 2) + x / 2].toUByte().toInt()
//                var vElem = bytes[width * height + width * height / 4 + (y / 2) * (width / 2) + x / 2].toUByte().toInt()
                // semi planar way
                var uElem = bytes[width * height + (y / 2) * width + ((x / 2) * 2)].toUByte().toInt()
                var vElem = bytes[width * height + (y / 2) * width + ((x / 2) * 2) + 1].toUByte().toInt()
                val rgb = yuvToRgb(yElem, uElem, vElem)
                bm.setPixel(x, y, Color.argb(255, rgb.first, rgb.second, rgb.third))
            }
        }
        return bm
    }
    private fun yuv420SPImageToBitmapRotated(bytes: ByteArray, width: Int, height: Int): Bitmap {
        val bm = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                var yElem = bytes[y * width + x].toUByte().toInt()
                var uElem = bytes[width * height + (y / 2) * width + ((x / 2) * 2)].toUByte().toInt()
                var vElem = bytes[width * height + (y / 2) * width + ((x / 2) * 2) + 1].toUByte().toInt()
                val rgb = yuvToRgb(yElem, uElem, vElem)
                bm.setPixel(height - 1 - y, x, Color.argb(255, rgb.first, rgb.second, rgb.third))
            }
        }
        return bm
    }

}