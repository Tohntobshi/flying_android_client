package com.example.flyingandroidclient

import android.graphics.PointF
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.nio.ByteBuffer

class ControlsManager(private val connection: BluetoothConnection) {
    fun setPosition(coord: PointF) {
//        Log.i("myinfo", "position ${coord.x} ${coord.y}")
        val data = ByteArray(10)
        data[0] = MessageTypes.CONTROLS.ordinal.toByte()
        data[1] = Controls.SET_PITCH_AND_ROLL.ordinal.toByte()
        ByteBuffer.wrap(data, 2, 4).putFloat(coord.x)
        ByteBuffer.wrap(data, 6, 4).putFloat(coord.y)
        connection.send(data)
    }
    private fun sendOneFloatControl(control: Controls, value: Float) {
        val data = ByteArray(6)
        data[0] = MessageTypes.CONTROLS.ordinal.toByte()
        data[1] = control.ordinal.toByte()
        ByteBuffer.wrap(data, 2, 4).putFloat(value)
        connection.send(data)
    }
    val pitchPropCoef = MutableLiveData<Float>(0f)
    fun setPitchPropCoef(value: Float) {
        pitchPropCoef.value = value
        sendOneFloatControl(Controls.SET_PITCH_PROP_COEF, value)
    }
    val pitchDerCoef = MutableLiveData<Float>(0f)
    fun setPitchDerCoef(value: Float) {
        pitchDerCoef.value = value
        sendOneFloatControl(Controls.SET_PITCH_DER_COEF, value)
    }
    val pitchIntCoef = MutableLiveData<Float>(0f)
    fun setPitchIntCoef(value: Float) {
        pitchIntCoef.value = value
        sendOneFloatControl(Controls.SET_PITCH_INT_COEF, value)
    }
    val rollPropCoef = MutableLiveData<Float>(0f)
    fun setRollPropCoef(value: Float) {
        rollPropCoef.value = value
        sendOneFloatControl(Controls.SET_ROLL_PROP_COEF, value)
    }
    val rollDerCoef = MutableLiveData<Float>(0f)
    fun setRollDerCoef(value: Float) {
        rollDerCoef.value = value
        sendOneFloatControl(Controls.SET_ROLL_DER_COEF, value)
    }
    val rollIntCoef = MutableLiveData<Float>(0f)
    fun setRollIntCoef(value: Float) {
        rollIntCoef.value = value
        sendOneFloatControl(Controls.SET_ROLL_INT_COEF, value)
    }
    val accTrust = MutableLiveData<Float>(0.1f)
    fun setAccTrust(value: Float) {
        accTrust.value = value
        sendOneFloatControl(Controls.SET_ACC_TRUST, value)
    }
    val inclineChangeRateFilteringCoef = MutableLiveData<Float>(0f)
    fun setInclineChangeRateFilteringCoef(value: Float) {
        inclineChangeRateFilteringCoef.value = value
        sendOneFloatControl(Controls.SET_INCL_CH_RATE_FILTERING_COEF, value)
    }
    val inclineFilteringCoef = MutableLiveData<Float>(0f)
    fun setInclineFilteringCoef(value: Float) {
        inclineFilteringCoef.value = value
        sendOneFloatControl(Controls.SET_INCL_FILTERING_COEF, value)
    }
    val acceleration = MutableLiveData<Float>(0f)
    fun setAcceleration(value: Float) {
        acceleration.value = value
        sendOneFloatControl(Controls.SET_ACCELERATION, value)
        // Log.i("myinfo", "acceleration ${value}")
    }
    val direction = MutableLiveData<Float>(0f)
    fun setDirection(value: Float) {
        direction.value = value
        sendOneFloatControl(Controls.SET_DIRECTION, value)
    }
}