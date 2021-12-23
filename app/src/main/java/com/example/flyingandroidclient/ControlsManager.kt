package com.example.flyingandroidclient

import android.animation.TypeEvaluator
import android.app.Application
import android.content.Context
import android.graphics.PointF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class ControlsManager(private val viewModel: MainActivityViewModel): SensorEventListener {
    private var sensorManager: SensorManager = viewModel.appl.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accSensor: Sensor? = null
    private var magSensor: Sensor? = null

    private val reduceSendRateBy: Int = 8
    private var reduceSendCounter: Int = 0
    private val sharedPref = viewModel.appl.getSharedPreferences("mysettings", Context.MODE_PRIVATE)

    private var lastControl: Controls = Controls.UNSET
    private var lastFloatVal1: Float = 0f
    private var lastFloatVal2: Float = 0f
    private var lastIntVal: Int = 0

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    init {
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    }

    fun startSensorListening() {
        if (accSensor != null && magSensor != null) {
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_UI)
            Log.i("myinfo", "sensor is present")
        }
        else {
            Log.i("myinfo", "no sensor")
        }
    }

    fun stopSensorListening() {
        if (accSensor != null && magSensor != null) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    private fun sendTwoFloatControl(control: Controls, value1: Float, value2: Float, noSkip: Boolean) {
        if (control == lastControl && lastFloatVal1 == value1 && lastFloatVal2 == value2) return
        if (reduceSendCounter == 0 || noSkip) {
            lastControl = control
            lastFloatVal1 = value1
            lastFloatVal2 = value2
            val data = ByteArray(10)
            data[0] = MessageTypes.CONTROLS.ordinal.toByte()
            data[1] = control.ordinal.toByte()
            ByteBuffer.wrap(data, 2, 4).putFloat(value1)
            ByteBuffer.wrap(data, 6, 4).putFloat(value2)
            viewModel.connection.send(data)
        }
        reduceSendCounter = if (reduceSendCounter >= reduceSendRateBy - 1) 0 else reduceSendCounter + 1
    }
    private fun sendOneFloatControl(control: Controls, value: Float, noSkip: Boolean) {
        if (control == lastControl && lastFloatVal1 == value) return
        if (reduceSendCounter == 0 || noSkip) {
            lastControl = control
            lastFloatVal1 = value
            val data = ByteArray(6)
            data[0] = MessageTypes.CONTROLS.ordinal.toByte()
            data[1] = control.ordinal.toByte()
            ByteBuffer.wrap(data, 2, 4).putFloat(value)
            viewModel.connection.send(data)
        }
        reduceSendCounter = if (reduceSendCounter >= reduceSendRateBy - 1) 0 else reduceSendCounter + 1
    }
    private fun sendOneIntControl(control: Controls, value: Int, noSkip: Boolean) {
        if (control == lastControl && lastIntVal == value) return
        if (reduceSendCounter == 0 || noSkip) {
            lastControl = control
            lastIntVal = value
            val data = ByteArray(6)
            data[0] = MessageTypes.CONTROLS.ordinal.toByte()
            data[1] = control.ordinal.toByte()
            ByteBuffer.wrap(data, 2, 4).putInt(value)
            viewModel.connection.send(data)
        }
        reduceSendCounter = if (reduceSendCounter >= reduceSendRateBy - 1) 0 else reduceSendCounter + 1
    }
    private fun sendControl(control: Controls) {
        val data = ByteArray(2)
        data[0] = MessageTypes.CONTROLS.ordinal.toByte()
        data[1] = control.ordinal.toByte()
        viewModel.connection.send(data)
    }



    fun move(coord: PointF, isLast: Boolean) {
        if (accSensor != null && magSensor != null) {
            val yawRad = orientationAngles[0] + PI / 2.0f;
            val x: Float = coord.x * cos(yawRad).toFloat() + coord.y * sin(yawRad).toFloat();
            val y: Float = -coord.x * sin(yawRad).toFloat() + coord.y * cos(yawRad).toFloat();
            sendTwoFloatControl(Controls.MOVE, x, y, isLast)
            return
        }
        sendTwoFloatControl(Controls.MOVE, coord.x, coord.y, isLast)
    }
    val pitchPropCoef = MutableLiveData<Float>(0f)
    fun setPitchPropCoef(value: Float, isLast: Boolean) {
        pitchPropCoef.value = value
        sendOneFloatControl(Controls.SET_PITCH_PROP_COEF, value, isLast)
    }
    val pitchDerCoef = MutableLiveData<Float>(0f)
    fun setPitchDerCoef(value: Float, isLast: Boolean) {
        pitchDerCoef.value = value
        sendOneFloatControl(Controls.SET_PITCH_DER_COEF, value, isLast)
    }
    val pitchIntCoef = MutableLiveData<Float>(0f)
    fun setPitchIntCoef(value: Float, isLast: Boolean) {
        pitchIntCoef.value = value
        sendOneFloatControl(Controls.SET_PITCH_INT_COEF, value, isLast)
    }
    val rollPropCoef = MutableLiveData<Float>(0f)
    fun setRollPropCoef(value: Float, isLast: Boolean) {
        rollPropCoef.value = value
        sendOneFloatControl(Controls.SET_ROLL_PROP_COEF, value, isLast)
    }
    val rollDerCoef = MutableLiveData<Float>(0f)
    fun setRollDerCoef(value: Float, isLast: Boolean) {
        rollDerCoef.value = value
        sendOneFloatControl(Controls.SET_ROLL_DER_COEF, value, isLast)
    }
    val rollIntCoef = MutableLiveData<Float>(0f)
    fun setRollIntCoef(value: Float, isLast: Boolean) {
        rollIntCoef.value = value
        sendOneFloatControl(Controls.SET_ROLL_INT_COEF, value, isLast)
    }
    val accTrust = MutableLiveData<Float>(0.1f)
    fun setAccTrust(value: Float, isLast: Boolean) {
        accTrust.value = value
        sendOneFloatControl(Controls.SET_ACC_TRUST, value, isLast)
    }
    val magTrust = MutableLiveData<Float>(0.1f)
    fun setMagTrust(value: Float, isLast: Boolean) {
        magTrust.value = value
        sendOneFloatControl(Controls.SET_MAG_TRUST, value, isLast)
    }
    val accFiltering = MutableLiveData<Float>(0.95f)
    fun setAccFiltering(value: Float, isLast: Boolean) {
        accFiltering.value = value
        sendOneFloatControl(Controls.SET_ACC_FILTERING, value, isLast)
    }
    val height = MutableLiveData<Float>(0f)
    fun setHeight(value: Float, isLast: Boolean) {
        height.value = value
        sendOneFloatControl(Controls.SET_HEIGHT, value, isLast)
    }
    val direction = MutableLiveData<Float>(0f)
    fun setDirection(value: Float, isLast: Boolean) {
        direction.value = value
        sendOneFloatControl(Controls.SET_DIRECTION, value, isLast)
//            Log.i("myinfo", "dir ${value} last ${isLast}")
    }
    val baseAcceleartion = MutableLiveData<Float>(0f);
    fun setBaseAcceleration(value: Float, isLast: Boolean) {
        baseAcceleartion.value = value
        sendOneFloatControl(Controls.SET_BASE_ACCELERATION, value, isLast)
    }
    val heightPropCoef = MutableLiveData<Float>(0f);
    fun setHeightPropCoef(value: Float, isLast: Boolean) {
        heightPropCoef.value = value
        sendOneFloatControl(Controls.SET_HEIGHT_PROP_COEF, value, isLast)
    }
    val heightDerCoef = MutableLiveData<Float>(0f);
    fun setHeightDerCoef(value: Float, isLast: Boolean) {
        heightDerCoef.value = value
        sendOneFloatControl(Controls.SET_HEIGHT_DER_COEF, value, isLast)
    }
    val heightIntCoef = MutableLiveData<Float>(0f);
    fun setHeightIntCoef(value: Float, isLast: Boolean) {
        heightIntCoef.value = value
        sendOneFloatControl(Controls.SET_HEIGHT_INT_COEF, value, isLast)
    }

    val yawPropCoef = MutableLiveData<Float>(0f);
    fun setYawPropCoef(value: Float, isLast: Boolean) {
        yawPropCoef.value = value
        sendOneFloatControl(Controls.SET_YAW_PROP_COEF, value, isLast)
    }
    val yawDerCoef = MutableLiveData<Float>(0f);
    fun setYawDerCoef(value: Float, isLast: Boolean) {
        yawDerCoef.value = value
        sendOneFloatControl(Controls.SET_YAW_DER_COEF, value, isLast)
    }
    val yawIntCoef = MutableLiveData<Float>(0f);
    fun setYawIntCoef(value: Float, isLast: Boolean) {
        yawIntCoef.value = value
        sendOneFloatControl(Controls.SET_YAW_INT_COEF, value, isLast)
    }
    val turnOffInclineAngle = MutableLiveData<Float>(30f);
    fun setTurnOffInclineAngle(value: Float, isLast: Boolean) {
        turnOffInclineAngle.value = value
        sendOneFloatControl(Controls.SET_TURN_OFF_INCLINE_ANGLE, value, isLast)
    }
    val imuLPFMode = MutableLiveData<Int>(3);
    fun setImuLPFMode(value: Int, isLast: Boolean) {
        imuLPFMode.value = value
        sendOneIntControl(Controls.SET_IMU_LPF_MODE, value, isLast)
    }
    fun resetTurnOffTrigger() {
        sendControl(Controls.RESET_TURN_OFF_TRIGGER)
    }
    fun startSendingPrimaryInfo() {
        sendControl(Controls.START_SENDING_PRIMARY_INFO)
    }
    fun stopSendingPrimaryInfo() {
        sendControl(Controls.STOP_SENDING_PRIMARY_INFO)
    }
    fun startSendingSecondaryInfo() {
        sendControl(Controls.START_SENDING_SECONDARY_INFO)
    }
    fun stopSendingSecondaryInfo() {
        sendControl(Controls.STOP_SENDING_SECONDARY_INFO)
    }
    fun resetLandingFlag() {
        sendControl(Controls.RESET_LANDING_FLAG)
    }
    fun switchToRelativeAcceleration() {
        sendControl(Controls.SWITCH_TO_RELATIVE_ACCELERATION)
    }
    val relativeAcceleration = MutableLiveData<Float>(0f);
    fun setRelativeAcceleration(value: Float, isLast: Boolean)
    {
        relativeAcceleration.value = value
        sendOneFloatControl(Controls.SET_RELATIVE_ACCELERATION, value, isLast)
    }
    fun calibrateESC() {
        sendControl(Controls.CALIBRATE_ESC)
    }
    fun calibrateGyro() {
        sendControl(Controls.CALIBRATE_GYRO)
    }
    fun calibrateAccelerometer() {
        sendControl(Controls.CALIBRATE_ACC)
    }
    fun calibrateMag() {
        sendControl(Controls.CALIBRATE_MAG)
    }
    val pitchAdjust = MutableLiveData<Float>(0f);
    fun setPitchAdjust(value: Float, isLast: Boolean) {
        pitchAdjust.value = value
        sendOneFloatControl(Controls.SET_PITCH_ADJUST, value, isLast)
    }
    val rollAdjust = MutableLiveData<Float>(0f);
    fun setRollAdjust(value: Float, isLast: Boolean) {
        rollAdjust.value = value
        sendOneFloatControl(Controls.SET_ROLL_ADJUST, value, isLast)
    }
    val usHeightFiltering = MutableLiveData<Float>(0.95f);
    fun setUsHeightFiltering(value: Float, isLast: Boolean) {
        usHeightFiltering.value = value
        sendOneFloatControl(Controls.SET_US_HEIGHT_FILTERING, value, isLast)
    }
    val usHeightDerFiltering = MutableLiveData<Float>(0.95f);
    fun setUsHeightDerFiltering(value: Float, isLast: Boolean) {
        usHeightDerFiltering.value = value
        sendOneFloatControl(Controls.SET_US_HEIGHT_DER_FILTERING, value, isLast)
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
            putFloat("MAG_TRUST", magTrust.value!!)
            putFloat("BASE_ACCELERATION", baseAcceleartion.value!!)
            putFloat("HEIGHT_PROP_COEF", heightPropCoef.value!!)
            putFloat("HEIGHT_DER_COEF", heightDerCoef.value!!)
            putFloat("HEIGHT_INT_COEF", heightIntCoef.value!!)
            putFloat("TURN_OFF_INCLINE_ANGLE", turnOffInclineAngle.value!!)
            putFloat("YAW_PROP_COEF", yawPropCoef.value!!)
            putFloat("YAW_DER_COEF", yawDerCoef.value!!)
            putFloat("YAW_INT_COEF", yawIntCoef.value!!)
            putInt("IMU_LPF_MODE", imuLPFMode.value!!)
            putFloat("PITCH_ADJUST", pitchAdjust.value!!)
            putFloat("ROLL_ADJUST", rollAdjust.value!!)
            putFloat("ACC_FILTERING", accFiltering.value!!)
            putFloat("US_HEIGHT_FILTERING", usHeightFiltering.value!!)
            putFloat("US_HEIGHT_DER_FILTERING", usHeightDerFiltering.value!!)
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
        accTrust.value = sharedPref.getFloat("ACC_TRUST", 0.1f)
        magTrust.value = sharedPref.getFloat("MAG_TRUST", 0.1f)
        baseAcceleartion.value = sharedPref.getFloat("BASE_ACCELERATION", 0.0f)
        heightPropCoef.value = sharedPref.getFloat("HEIGHT_PROP_COEF", 0.0f)
        heightDerCoef.value = sharedPref.getFloat("HEIGHT_DER_COEF", 0.0f)
        heightIntCoef.value = sharedPref.getFloat("HEIGHT_INT_COEF", 0.0f)
        turnOffInclineAngle.value = sharedPref.getFloat("TURN_OFF_INCLINE_ANGLE", 30.0f)
        yawPropCoef.value = sharedPref.getFloat("YAW_PROP_COEF", 0.0f)
        yawDerCoef.value = sharedPref.getFloat("YAW_DER_COEF", 0.0f)
        yawIntCoef.value = sharedPref.getFloat("YAW_INT_COEF", 0.0f)
        imuLPFMode.value = sharedPref.getInt("IMU_LPF_MODE", 3)
        pitchAdjust.value = sharedPref.getFloat("PITCH_ADJUST", 0.0f)
        rollAdjust.value = sharedPref.getFloat("ROLL_ADJUST", 0.0f)
        accFiltering.value = sharedPref.getFloat("ACC_FILTERING", 0.95f)
        usHeightFiltering.value = sharedPref.getFloat("US_HEIGHT_FILTERING", 0.95f)
        usHeightDerFiltering.value = sharedPref.getFloat("US_HEIGHT_DER_FILTERING", 0.95f)
    }
    fun sendCurrentSettings() {
        sendOneFloatControl(Controls.SET_PITCH_PROP_COEF, pitchPropCoef.value!!, true)
        sendOneFloatControl(Controls.SET_PITCH_DER_COEF, pitchDerCoef.value!!, true)
        sendOneFloatControl(Controls.SET_PITCH_INT_COEF, pitchIntCoef.value!!, true)
        sendOneFloatControl(Controls.SET_ROLL_PROP_COEF, rollPropCoef.value!!, true)
        sendOneFloatControl(Controls.SET_ROLL_DER_COEF, rollDerCoef.value!!, true)
        sendOneFloatControl(Controls.SET_ROLL_INT_COEF, rollIntCoef.value!!, true)
        sendOneFloatControl(Controls.SET_ACC_TRUST, accTrust.value!!, true)
        sendOneFloatControl(Controls.SET_MAG_TRUST, magTrust.value!!, true)
        sendOneFloatControl(Controls.SET_BASE_ACCELERATION, baseAcceleartion.value!!, true)
        sendOneFloatControl(Controls.SET_HEIGHT_PROP_COEF, heightPropCoef.value!!, true)
        sendOneFloatControl(Controls.SET_HEIGHT_DER_COEF, heightDerCoef.value!!, true)
        sendOneFloatControl(Controls.SET_HEIGHT_INT_COEF, heightIntCoef.value!!, true)
        sendOneFloatControl(Controls.SET_TURN_OFF_INCLINE_ANGLE, turnOffInclineAngle.value!!, true)
        sendOneFloatControl(Controls.SET_YAW_PROP_COEF, yawPropCoef.value!!, true)
        sendOneFloatControl(Controls.SET_YAW_DER_COEF, yawDerCoef.value!!, true)
        sendOneFloatControl(Controls.SET_YAW_INT_COEF, yawIntCoef.value!!, true)
        sendOneIntControl(Controls.SET_IMU_LPF_MODE, imuLPFMode.value!!, true)
        sendOneFloatControl(Controls.SET_PITCH_ADJUST, pitchAdjust.value!!, true)
        sendOneFloatControl(Controls.SET_ROLL_ADJUST, rollAdjust.value!!, true)
        sendOneFloatControl(Controls.SET_ACC_FILTERING, accFiltering.value!!, true)
        sendOneFloatControl(Controls.SET_US_HEIGHT_FILTERING, usHeightFiltering.value!!, true)
        sendOneFloatControl(Controls.SET_US_HEIGHT_DER_FILTERING, usHeightDerFiltering.value!!, true)
    }


}