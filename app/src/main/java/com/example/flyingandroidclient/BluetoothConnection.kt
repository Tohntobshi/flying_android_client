package com.example.flyingandroidclient

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.NullPointerException
import java.nio.ByteBuffer
import java.util.UUID

val MAX_MESSAGE_SIZE = 5000

class BluetoothConnection {
    private val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var inStream: InputStream? = null
    private var outStream: OutputStream? = null
    private val bufferMesSize: ByteArray = ByteArray(4)
    private var socket: BluetoothSocket? = null

    fun getPairedDevices(): Set<BluetoothDevice> {
        return btAdapter?.bondedDevices ?: setOf()
    }

    fun startDiscovery() {
        btAdapter?.startDiscovery()
    }

    fun cancelDiscovery() {
        btAdapter?.cancelDiscovery()
    }

    // it will return on disconnect or failed connection
    suspend fun establish(device: BluetoothDevice, uuid: String, onRead: (data: ByteArray) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
                socket!!.connect()
                inStream = socket!!.inputStream
                outStream = socket!!.outputStream
                while (true) {
                    val numBytes = inStream!!.read(bufferMesSize)
                    if (numBytes != 4) throw IOException("invalid message header size")
                    val messageSize = ByteBuffer.wrap(bufferMesSize).getInt()
                    if (messageSize > MAX_MESSAGE_SIZE) throw IOException("message size is too big")
                    val bufferMesData = ByteArray(messageSize)
                    val numBytes2 = inStream!!.read(bufferMesData)
                    if (numBytes2 != messageSize) throw IOException("message size conflict")
//                    Log.i("myinfo", "got message size $messageSize")
                    withContext(Dispatchers.Main) {
                        onRead(bufferMesData)
                    }
                }
            } catch (e: IOException) {

            } catch (e: NullPointerException) {

            } finally {
                socket?.close()
            }
        }
    }

    fun send(data: ByteArray) {
        try {
            val sizeData = ByteArray(4)
            ByteBuffer.wrap(sizeData).putInt(data.size)
            outStream?.write(sizeData + data)
        } catch (e: IOException) {
        }
    }

    fun cancel() {
        try {
            socket?.close()
        } catch (e: IOException) {
        }
    }
}