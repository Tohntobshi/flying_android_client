package com.example.flyingandroidclient

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
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

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
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
    val controls = ControlsManager(connection, application)

    val pitchErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val rollErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val pitchErrorChangeRates = MutableLiveData<MutableList<Float>>(mutableListOf())
    val rollErrorChangeRates = MutableLiveData<MutableList<Float>>(mutableListOf())
    val heightErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val heightErrorChangeRates = MutableLiveData<MutableList<Float>>(mutableListOf())
    val yawErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val yawErrorChangeRates = MutableLiveData<MutableList<Float>>(mutableListOf())

    val frontLeft = MutableLiveData<Int>(0)
    val frontRight = MutableLiveData<Int>(0)
    val backLeft = MutableLiveData<Int>(0)
    val backRight = MutableLiveData<Int>(0)
    val heightErr = MutableLiveData<Float>(0.0f)
    val pitchErr = MutableLiveData<Float>(0.0f)
    val rollErr = MutableLiveData<Float>(0.0f)
    val yawErr = MutableLiveData<Float>(0.0f)
    val heightErrDer = MutableLiveData<Float>(0.0f)
    val pitchErrDer = MutableLiveData<Float>(0.0f)
    val rollErrDer = MutableLiveData<Float>(0.0f)
    val yawErrDer = MutableLiveData<Float>(0.0f)
    val heightErrInt = MutableLiveData<Float>(0.0f)
    val pitchErrInt = MutableLiveData<Float>(0.0f)
    val rollErrInt = MutableLiveData<Float>(0.0f)
    val yawErrInt = MutableLiveData<Float>(0.0f)
    val pidLoopFreq = MutableLiveData<Float>(0.0f)

    val pitchPropInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.pitchPropCoef, pitchErr)) {
        (it.first!! * it.second!!).toInt()
    }
    val pitchDerInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.pitchDerCoef, pitchErrDer)) {
        (it.first!! * it.second!!).toInt()
    }
    val pitchIntInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.pitchIntCoef, pitchErrInt)) {
        (it.first!! * it.second!!).toInt()
    }
    val rollPropInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.rollPropCoef, rollErr)) {
        (it.first!! * it.second!!).toInt()
    }
    val rollDerInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.rollDerCoef, rollErrDer)) {
        (it.first!! * it.second!!).toInt()
    }
    val rollIntInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.rollIntCoef, rollErrInt)) {
        (it.first!! * it.second!!).toInt()
    }
    val heightPropInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.heightPropCoef, heightErr)) {
        (it.first!! * it.second!! * 1000).toInt()
    }
    val heightDerInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.heightDerCoef, heightErrDer)) {
        (it.first!! * it.second!! * 1000).toInt()
    }
    val heightIntInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.heightIntCoef, heightErrInt)) {
        (it.first!! * it.second!! * 1000).toInt()
    }

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
        if (data[0] == MessageTypes.ERRORS_INFO.ordinal.toByte() && data.size == (1 + 17 * 4)) {
            val currentPitchError = ByteBuffer.wrap(data, 1, 4).float
            val currentRollError = ByteBuffer.wrap(data, 5, 4).float

            val pitchErrorChangeRate = ByteBuffer.wrap(data, 9, 4).float
            val rollErrorChangeRate = ByteBuffer.wrap(data, 13, 4).float

            val currentHeightError = ByteBuffer.wrap(data, 17, 4).float
            val heightErrorChangeRate = ByteBuffer.wrap(data, 21, 4).float

            val currentYawError = ByteBuffer.wrap(data, 25, 4).float
            val yawErrorChangeRate = ByteBuffer.wrap(data, 29, 4).float

            val frontLeftVal = ByteBuffer.wrap(data, 33, 4).int
            val frontRightVal = ByteBuffer.wrap(data, 37, 4).int
            val backLeftVal = ByteBuffer.wrap(data, 41, 4).int
            val backRightVal = ByteBuffer.wrap(data, 45, 4).int

            val pidLoopFreq_ = ByteBuffer.wrap(data, 49, 4).float

            val currentPitchErrInt = ByteBuffer.wrap(data, 53, 4).float
            val currentRollErrInt = ByteBuffer.wrap(data, 57, 4).float
            val currentYawErrInt = ByteBuffer.wrap(data, 61, 4).float
            val currentHeightErrInt = ByteBuffer.wrap(data, 65, 4).float

            withContext(Dispatchers.Main) {
                addToList(currentPitchError, pitchErrors)
                addToList(currentRollError, rollErrors)

                addToList(pitchErrorChangeRate, pitchErrorChangeRates)
                addToList(rollErrorChangeRate, rollErrorChangeRates)

                addToList(currentHeightError, heightErrors)
                addToList(heightErrorChangeRate, heightErrorChangeRates)

                addToList(currentYawError, yawErrors)
                addToList(yawErrorChangeRate, yawErrorChangeRates)
                frontLeft.value = frontLeftVal
                frontRight.value = frontRightVal
                backLeft.value = backLeftVal
                backRight.value = backRightVal
                pidLoopFreq.value = pidLoopFreq_

                pitchErr.value = currentPitchError
                rollErr.value = currentRollError
                yawErr.value = currentYawError
                heightErr.value = currentHeightError

                pitchErrDer.value = pitchErrorChangeRate
                rollErrDer.value = rollErrorChangeRate
                yawErrDer.value = yawErrorChangeRate
                heightErrDer.value = heightErrorChangeRate

                pitchErrInt.value = currentPitchErrInt
                rollErrInt.value = currentRollErrInt
                yawErrInt.value = currentYawErrInt
                heightErrInt.value = currentHeightErrInt


                // do stuff
                // Log.i("myinfo", "height err ${currentHeightError} ${controls.heightPropCoef.value}")
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