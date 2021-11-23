package com.example.flyingandroidclient

import android.app.Application
import android.content.Context
import android.graphics.PointF
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import java.nio.ByteBuffer
import kotlin.math.roundToInt

class ControlsManager(private val connection: BluetoothConnection, private val application: Application) {
    private val sharedPref = application.getSharedPreferences("mysettings", Context.MODE_PRIVATE)
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
    private fun sendOneIntControl(control: Controls, value: Int) {
        val data = ByteArray(6)
        data[0] = MessageTypes.CONTROLS.ordinal.toByte()
        data[1] = control.ordinal.toByte()
        ByteBuffer.wrap(data, 2, 4).putInt(value)
        connection.send(data)
    }
    private fun sendControl(control: Controls) {
        val data = ByteArray(2)
        data[0] = MessageTypes.CONTROLS.ordinal.toByte()
        data[1] = control.ordinal.toByte()
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
    val baseAcceleartion = MutableLiveData<Float>(0f);
    fun setBaseAcceleration(value: Float) {
        baseAcceleartion.value = value
        sendOneFloatControl(Controls.SET_BASE_ACCELERATION, value)
    }
    val heightPropCoef = MutableLiveData<Float>(0f);
    fun setHeightPropCoef(value: Float) {
        heightPropCoef.value = value
        sendOneFloatControl(Controls.SET_HEIGHT_PROP_COEF, value)
    }
    val heightDerCoef = MutableLiveData<Float>(0f);
    fun setHeightDerCoef(value: Float) {
        heightDerCoef.value = value
        sendOneFloatControl(Controls.SET_HEIGHT_DER_COEF, value)
    }
    val heightIntCoef = MutableLiveData<Float>(0f);
    fun setHeightIntCoef(value: Float) {
        heightIntCoef.value = value
        sendOneFloatControl(Controls.SET_HEIGHT_INT_COEF, value)
    }

    val yawSpPropCoef = MutableLiveData<Float>(0f);
    fun setYawSpPropCoef(value: Float) {
        yawSpPropCoef.value = value
        sendOneFloatControl(Controls.SET_YAW_SP_PROP_COEF, value)
    }
    val yawSpDerCoef = MutableLiveData<Float>(0f);
    fun setYawSpDerCoef(value: Float) {
        yawSpDerCoef.value = value
        sendOneFloatControl(Controls.SET_YAW_SP_DER_COEF, value)
    }
    val yawSpIntCoef = MutableLiveData<Float>(0f);
    fun setYawSpIntCoef(value: Float) {
        yawSpIntCoef.value = value
        sendOneFloatControl(Controls.SET_YAW_SP_INT_COEF, value)
    }
    val yawSpFilteringCoef = MutableLiveData<Float>(0f)
    fun setYawSpFilteringCoef(value: Float) {
        yawSpFilteringCoef.value = value
        sendOneFloatControl(Controls.SET_YAW_SP_FILTERING_COEF, value)
    }

    val turnOffInclineAngle = MutableLiveData<Float>(30f);
    fun setTurnOffInclineAngle(value: Float) {
        turnOffInclineAngle.value = value
        sendOneFloatControl(Controls.SET_TURN_OFF_INCLINE_ANGLE, value)
    }
    val imuLPFMode = MutableLiveData<Int>(3);
    fun setImuLPFMode(value: Int) {
        imuLPFMode.value = value
        sendOneIntControl(Controls.SET_IMU_LPF_MODE, value)
    }
    fun resetTurnOffTrigger() {
        sendControl(Controls.RESET_TURN_OFF_TRIGGER)
    }
    fun startSendingInfo() {
        sendControl(Controls.START_SENDING_INFO)
    }
    fun stopSendingInfo() {
        sendControl(Controls.STOP_SENDING_INFO)
    }
    fun saveCurrentSettings() {
        with (sharedPref.edit()) {
            putFloat("PITCH_PROP_COEF", pitchPropCoef.value!!)
            putFloat("PITCH_DER_COEF", pitchDerCoef.value!!)
            putFloat("PITCH_INT_COEF", pitchIntCoef.value!!)
            putFloat("ROLL_PROP_COEF", rollPropCoef.value!!)
            putFloat("ROLL_DER_COEF", rollDerCoef.value!!)
            putFloat("ROLL_INT_COEF", rollIntCoef.value!!)
            putFloat("ACC_TRUST", accTrust.value!!)
            putFloat("INCL_CH_RATE_FILTERING_COEF", inclineChangeRateFilteringCoef.value!!)
            putFloat("INCL_FILTERING_COEF", inclineFilteringCoef.value!!)
            putFloat("BASE_ACCELERATION", baseAcceleartion.value!!)
            putFloat("HEIGHT_PROP_COEF", heightPropCoef.value!!)
            putFloat("HEIGHT_DER_COEF", heightDerCoef.value!!)
            putFloat("HEIGHT_INT_COEF", heightIntCoef.value!!)
            putFloat("TURN_OFF_INCLINE_ANGLE", turnOffInclineAngle.value!!)
            putFloat("YAW_SP_FILTERING_COEF", yawSpFilteringCoef.value!!)
            putFloat("YAW_SP_PROP_COEF", yawSpPropCoef.value!!)
            putFloat("YAW_SP_DER_COEF", yawSpDerCoef.value!!)
            putFloat("YAW_SP_INT_COEF", yawSpIntCoef.value!!)
            putInt("IMU_LPF_MODE", imuLPFMode.value!!)
            apply()
        }
    }
    fun restoreSettings() {
        pitchPropCoef.value = sharedPref.getFloat("PITCH_PROP_COEF", 0.0f)
        pitchDerCoef.value = sharedPref.getFloat("PITCH_DER_COEF", 0.0f)
        pitchIntCoef.value = sharedPref.getFloat("PITCH_INT_COEF", 0.0f)
        rollPropCoef.value = sharedPref.getFloat("ROLL_PROP_COEF", 0.0f)
        rollDerCoef.value = sharedPref.getFloat("ROLL_DER_COEF", 0.0f)
        rollIntCoef.value = sharedPref.getFloat("ROLL_INT_COEF", 0.0f)
        accTrust.value = sharedPref.getFloat("ACC_TRUST", 0.0f)
        inclineChangeRateFilteringCoef.value = sharedPref.getFloat("INCL_CH_RATE_FILTERING_COEF", 0.0f)
        inclineFilteringCoef.value = sharedPref.getFloat("INCL_FILTERING_COEF", 0.0f)
        baseAcceleartion.value = sharedPref.getFloat("BASE_ACCELERATION", 0.0f)
        heightPropCoef.value = sharedPref.getFloat("HEIGHT_PROP_COEF", 0.0f)
        heightDerCoef.value = sharedPref.getFloat("HEIGHT_DER_COEF", 0.0f)
        heightIntCoef.value = sharedPref.getFloat("HEIGHT_INT_COEF", 0.0f)
        turnOffInclineAngle.value = sharedPref.getFloat("TURN_OFF_INCLINE_ANGLE", 30.0f)
        yawSpFilteringCoef.value = sharedPref.getFloat("YAW_SP_FILTERING_COEF", 0.0f)
        yawSpPropCoef.value = sharedPref.getFloat("YAW_SP_PROP_COEF", 0.0f)
        yawSpDerCoef.value = sharedPref.getFloat("YAW_SP_DER_COEF", 0.0f)
        yawSpIntCoef.value = sharedPref.getFloat("YAW_SP_INT_COEF", 0.0f)
        imuLPFMode.value = sharedPref.getInt("IMU_LPF_MODE", 3)
    }
    fun sendCurrentSettings() {
        sendOneFloatControl(Controls.SET_PITCH_PROP_COEF, pitchPropCoef.value!!)
        sendOneFloatControl(Controls.SET_PITCH_DER_COEF, pitchDerCoef.value!!)
        sendOneFloatControl(Controls.SET_PITCH_INT_COEF, pitchIntCoef.value!!)
        sendOneFloatControl(Controls.SET_ROLL_PROP_COEF, rollPropCoef.value!!)
        sendOneFloatControl(Controls.SET_ROLL_DER_COEF, rollDerCoef.value!!)
        sendOneFloatControl(Controls.SET_ROLL_INT_COEF, rollIntCoef.value!!)
        sendOneFloatControl(Controls.SET_ACC_TRUST, accTrust.value!!)
        sendOneFloatControl(Controls.SET_INCL_CH_RATE_FILTERING_COEF, inclineChangeRateFilteringCoef.value!!)
        sendOneFloatControl(Controls.SET_INCL_FILTERING_COEF, inclineFilteringCoef.value!!)
        sendOneFloatControl(Controls.SET_BASE_ACCELERATION, baseAcceleartion.value!!)
        sendOneFloatControl(Controls.SET_HEIGHT_PROP_COEF, heightPropCoef.value!!)
        sendOneFloatControl(Controls.SET_HEIGHT_DER_COEF, heightDerCoef.value!!)
        sendOneFloatControl(Controls.SET_HEIGHT_INT_COEF, heightIntCoef.value!!)
        sendOneFloatControl(Controls.SET_TURN_OFF_INCLINE_ANGLE, turnOffInclineAngle.value!!)
        sendOneFloatControl(Controls.SET_YAW_SP_FILTERING_COEF, yawSpFilteringCoef.value!!)
        sendOneFloatControl(Controls.SET_YAW_SP_PROP_COEF, yawSpPropCoef.value!!)
        sendOneFloatControl(Controls.SET_YAW_SP_DER_COEF, yawSpDerCoef.value!!)
        sendOneFloatControl(Controls.SET_YAW_SP_INT_COEF, yawSpIntCoef.value!!)
        sendOneIntControl(Controls.SET_IMU_LPF_MODE, imuLPFMode.value!!)
    }
}