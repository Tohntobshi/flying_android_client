package com.example.flyingandroidclient

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.NullPointerException
import java.util.UUID

class BluetoothConnection {
    private val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var inStream: InputStream? = null
    private var outStream: OutputStream? = null
    private val buffer: ByteArray = ByteArray(1024)
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
    suspend fun establish(device: BluetoothDevice, uuid: String, onRead: suspend (data: ByteArray, amount: Int) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
                socket!!.connect()
                inStream = socket!!.inputStream
                outStream = socket!!.outputStream
                while (true) {
                    val numBytes = inStream!!.read(buffer)
                    onRead(buffer, numBytes)
                }
            } catch (e: IOException) {

            } catch (e: NullPointerException) {

            }
        }
    }

    suspend fun send(bytes: ByteArray) {
        withContext(Dispatchers.IO) {
            try {
                outStream?.write(bytes)
            } catch (e: IOException) {
            }
        }
    }

    fun cancel() {
        try {
            socket?.close()
        } catch (e: IOException) {
        }
    }
}