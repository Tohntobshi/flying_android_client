package com.example.flyingandroidclient

import android.bluetooth.BluetoothDevice
import android.graphics.PointF
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


enum class Tabs {
    MAIN,
    TWEAKS,
    OPTIONS
}

class MainActivityViewModel: ViewModel() {
    private val connection = BluetoothConnection()

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

    private suspend fun onMessage (bytes: ByteArray, amount: Int): Unit {
        val text = bytes.toString()
        withContext(Dispatchers.Main) {
            // do stuff
            Log.i("myinfo", "received ${amount} bytes: ${text}")
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

        // Log.i("myinfo", "position ${coord.x} ${coord.y}")
        viewModelScope.launch {
            connection.send("position ${coord.x} ${coord.y}".toByteArray())
        }
    }

    override fun onCleared() {
        super.onCleared()
        connection.cancel()
    }


}