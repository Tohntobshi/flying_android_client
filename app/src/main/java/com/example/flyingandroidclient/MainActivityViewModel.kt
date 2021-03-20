package com.example.flyingandroidclient

import android.bluetooth.BluetoothDevice
import android.graphics.PointF
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer


//@ExperimentalUnsignedTypes // just to make it clear that the experimental unsigned types are used
//fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

fun <T> MutableList<T>.addNotExceed(element: T, maxSize: Int) {
    this.add(element)
    if (this.size > maxSize) this.removeFirst()
}

enum class Tabs {
    MAIN,
    TWEAKS,
    OPTIONS
}

class MainActivityViewModel: ViewModel() {
//    init {
//        viewModelScope.launch {
//            while (true) {
//                delay(50)
//                val list = pitchErrors.value
//                list?.addNotExceed(Random.Default.nextDouble(-1.0, 1.0).toFloat(), 100)
//                pitchErrors.value = list
//            }
//        }
//    }

    private val connection = BluetoothConnection()

    val pitchErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val rollErrors = MutableLiveData<MutableList<Float>>(mutableListOf())

    val exampleValue = MutableLiveData<Float>(0f)
    fun setExampleValue(v: Float) {
        exampleValue.value = v
    }

    private val _isConnected = MutableLiveData<Boolean>(false)
    val isConnected: LiveData<Boolean>
        get() {
            return _isConnected
        }

    private val _btDevices: MutableLiveData<Map<String, BluetoothDevice>> = MutableLiveData<Map<String, BluetoothDevice>>(mapOf())
    val btDevices: LiveData<Map<String, BluetoothDevice>>
        get() {
            return _btDevices
        }

    val btDevicesList: LiveData<List<BluetoothDevice>>
        get() {
            return Transformations.map(btDevices) { it.toList().map { it.second } }
        }

    fun addBtDevice(device: BluetoothDevice) {
        _btDevices.value = _btDevices.value?.plus(Pair(device.address, device))
    }

    fun addBtDevice(device: Set<BluetoothDevice>) {
        val pairs = device.map { Pair(it.address, it) }
        _btDevices.value = _btDevices.value?.plus(pairs)
    }

    fun updateDeviceList() {
        if (isConnected.value!!) return
        addBtDevice(connection.getPairedDevices())
        connection.startDiscovery()
    }

    private val _currentTab = MutableLiveData<Tabs>(Tabs.MAIN)
    val currentTab: LiveData<Tabs>
        get() {
            return _currentTab
        }

    private val _serviceUUID = MutableLiveData<String>("848d828b-c486-44df-83fa-5413b06146e0")
    val serviceUUID: LiveData<String>
        get() {
            return _serviceUUID
        }

    private val _connectionStatus = MutableLiveData<String>("not connected")
    val connectionStatus: LiveData<String>
        get() {
            return _connectionStatus
        }

    fun changeServiceUUID(value: String) {
        // Log.i("myinfo", "value changed ${value}")
        _serviceUUID.value = value
    }

    fun changeCurrentTab(tab: Tabs) {
        _currentTab.value = tab
    }

    private suspend fun onMessage (data: ByteArray): Unit {
        if (data.size < 1) return
        if (data[0] == MessageTypes.INFO.ordinal.toByte() && data.size == 25) {
            val currentPitchError = ByteBuffer.wrap(data, 1, 4).float
            val currentRollError = ByteBuffer.wrap(data, 5, 4).float
            val pitchErrorChangeRate = ByteBuffer.wrap(data, 9, 4).float
            val rollErrorChangeRate = ByteBuffer.wrap(data, 13, 4).float
            val currentYawSpeedError = ByteBuffer.wrap(data, 17, 4).float
            val yawSpeedErrorChangeRate = ByteBuffer.wrap(data, 21, 4).float
            withContext(Dispatchers.Main) {
                val pitchErrorsList = pitchErrors.value
                pitchErrorsList?.addNotExceed(currentPitchError, 100)
                pitchErrors.value = pitchErrorsList
                val rollErrorsList = rollErrors.value
                rollErrorsList?.addNotExceed(currentRollError, 100)
                rollErrors.value = rollErrorsList
                // do stuff
                // Log.i("myinfo", "received perr ${currentPitchError} rerr ${currentRollError}")
            }
            return
        }

    }

    fun establishConnection(device: BluetoothDevice) {
//        btDevices.value?.forEach {
//            Log.i("myinfo", "addr ${it.key}")
//        }
        if (isConnected.value!!) connection.cancel()
        connection.cancelDiscovery()
        viewModelScope.launch {
            _isConnected.value = true
            _connectionStatus.value = "connected to ${device.address}"
            connection.establish(device, _serviceUUID.value ?: "", ::onMessage)
            _isConnected.value = false
            _connectionStatus.value = "not connected"
        }
    }

    fun setPosition(coord: PointF) {
//        Log.i("myinfo", "position ${coord.x} ${coord.y}")
        val data = ByteArray(10)
        data[0] = MessageTypes.CONTROLS.ordinal.toByte()
        data[1] = Controls.SET_PITCH_AND_ROLL.ordinal.toByte()
        ByteBuffer.wrap(data, 2, 4).putFloat(coord.x)
        ByteBuffer.wrap(data, 6, 4).putFloat(coord.y)
        connection.send(data)
    }

    override fun onCleared() {
        super.onCleared()
        connection.cancel()
    }


}