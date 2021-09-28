package com.example.flyingandroidclient

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import kotlin.random.Random


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
//                addToList(Random.Default.nextDouble(-40.0, 40.0).toFloat(), pitchErrors)
//                addToList(Random.Default.nextDouble(-40.0, 40.0).toFloat(), rollErrors)
//                addToList(Random.Default.nextDouble(-40.0, 40.0).toFloat(), pitchErrorChangeRates)
//                addToList(Random.Default.nextDouble(-40.0, 40.0).toFloat(), rollErrorChangeRates)
//            }
//        }
//    }

    private val connection = BluetoothConnection()
    val controls = ControlsManager(connection)

    val pitchErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val rollErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val pitchErrorChangeRates = MutableLiveData<MutableList<Float>>(mutableListOf())
    val rollErrorChangeRates = MutableLiveData<MutableList<Float>>(mutableListOf())
    val yawSpeedErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val yawSpeedErrorChangeRates = MutableLiveData<MutableList<Float>>(mutableListOf())

    private fun addToList(value: Float, mldlist: MutableLiveData<MutableList<Float>>) {
        val list = mldlist.value
        list?.addNotExceed(value, 100)
        mldlist.value = list
    }

    private val _isConnected = MutableLiveData<Boolean>(false)
    val isConnected: LiveData<Boolean>
        get() = _isConnected

    private val _btDevices: MutableLiveData<Map<String, BluetoothDevice>> = MutableLiveData<Map<String, BluetoothDevice>>(mapOf())
    val btDevices: LiveData<Map<String, BluetoothDevice>>
        get() = _btDevices

    val btDevicesList: LiveData<List<BluetoothDevice>>
        get() = Transformations.map(btDevices) { it.toList().map { it.second } }

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
        get() = _currentTab

    private val _serviceUUID = MutableLiveData<String>("848d828b-c486-44df-83fa-5413b06146e0")
    val serviceUUID: LiveData<String>
        get() = _serviceUUID

    private val _connectionStatus = MutableLiveData<String>("not connected")
    val connectionStatus: LiveData<String>
        get() = _connectionStatus

    fun changeServiceUUID(value: String) {
        // Log.i("myinfo", "value changed ${value}")
        _serviceUUID.value = value
    }

    fun changeCurrentTab(tab: Tabs) {
        _currentTab.value = tab
    }

    private suspend fun onMessage (data: ByteArray): Unit {
        if (data.size < 1) return
        if (data[0] == MessageTypes.ERRORS_INFO.ordinal.toByte() && data.size == 25) {
            val currentPitchError = ByteBuffer.wrap(data, 1, 4).float
            val currentRollError = ByteBuffer.wrap(data, 5, 4).float
            val pitchErrorChangeRate = ByteBuffer.wrap(data, 9, 4).float
            val rollErrorChangeRate = ByteBuffer.wrap(data, 13, 4).float
            val currentYawSpeedError = ByteBuffer.wrap(data, 17, 4).float
            val yawSpeedErrorChangeRate = ByteBuffer.wrap(data, 21, 4).float
            withContext(Dispatchers.Main) {
                addToList(currentPitchError, pitchErrors)
                addToList(currentRollError, rollErrors)
                addToList(pitchErrorChangeRate, pitchErrorChangeRates)
                addToList(rollErrorChangeRate, rollErrorChangeRates)
                addToList(currentYawSpeedError, yawSpeedErrors)
                addToList(yawSpeedErrorChangeRate, yawSpeedErrorChangeRates)
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

    override fun onCleared() {
        super.onCleared()
        connection.cancel()
    }


}