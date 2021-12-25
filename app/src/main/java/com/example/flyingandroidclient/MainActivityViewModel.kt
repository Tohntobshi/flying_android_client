package com.example.flyingandroidclient

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import kotlinx.coroutines.launch


enum class Tabs {
    MAIN,
    TWEAKS,
    OPTIONS
}

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    val appl: Application = application
    val connection = BluetoothConnection()
    val controls = ControlsManager(this)
    val info = InfoManager()
    val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator


    val pitchPropInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.pitchPropCoef, info.pitchErr)) {
        (it.first!! * it.second!!).toInt()
    }
    val pitchDerInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.pitchDerCoef, info.pitchErrDer)) {
        (it.first!! * it.second!!).toInt()
    }
    val pitchIntInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.pitchIntCoef, info.pitchErrInt)) {
        (it.first!! * it.second!!).toInt()
    }
    val rollPropInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.rollPropCoef, info.rollErr)) {
        (it.first!! * it.second!!).toInt()
    }
    val rollDerInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.rollDerCoef, info.rollErrDer)) {
        (it.first!! * it.second!!).toInt()
    }
    val rollIntInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.rollIntCoef, info.rollErrInt)) {
        (it.first!! * it.second!!).toInt()
    }
    val heightPropInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.heightPropCoef, info.heightErr)) {
        (it.first!! * it.second!! * 1000).toInt()
    }
    val heightDerInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.heightDerCoef, info.heightErrDer)) {
        (it.first!! * it.second!! * 1000).toInt()
    }
    val heightIntInfluence: LiveData<Int> = Transformations.map(PairMediatorLiveData(controls.heightIntCoef, info.heightErrInt)) {
        (it.first!! * it.second!! * 1000).toInt()
    }

    fun onAccSliderModeChange(value: AccelerationSliderMode) {
        // only switch to relative acc mode now
        if (value == AccelerationSliderMode.REL_ACC) {
            controls.setRelativeAcceleration(0f, true)
            controls.switchToRelativeAcceleration()
            accSliderMode.value = value
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            }
        }
    }

    val distinctLandingFlag = Transformations.distinctUntilChanged(info.landingFlag).apply {
        observeForever(object: Observer<Boolean> {
            override fun onChanged(t: Boolean?) {
                if (t != null && t == true)
                onLanding()
            }
        })
    }


    fun onLanding() {
        controls.setHeight(0f, true)
        controls.resetLandingFlag()
        accSliderMode.value = AccelerationSliderMode.US_HEIGHT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        }
    }

    val accSliderMode = MutableLiveData<AccelerationSliderMode>(AccelerationSliderMode.US_HEIGHT)

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

    fun establishConnection(device: BluetoothDevice) {
//        btDevices.value?.forEach {
//            Log.i("myinfo", "addr ${it.key}")
//        }
        if (isConnected.value!!) connection.cancel()
        connection.cancelDiscovery()
        viewModelScope.launch {
            _isConnected.value = true
            _connectionStatus.value = "connected to ${device.address}"
            connection.establish(device, _serviceUUID.value ?: "", info::onMessage)
            _isConnected.value = false
            _connectionStatus.value = "not connected"
        }
    }

    override fun onCleared() {
        super.onCleared()
        connection.cancel()
    }
}