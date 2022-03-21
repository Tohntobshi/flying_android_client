package com.example.flyingandroidclient

import android.util.Log
import java.nio.ByteBuffer

class VideoDecoder {
    fun putPacket(data: ByteBuffer) {
        Log.i("myinfo", "received video frame of ${data.capacity()} bytes")
    }
}