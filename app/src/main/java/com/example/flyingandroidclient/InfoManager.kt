package com.example.flyingandroidclient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import java.nio.ByteBuffer

class InfoManager (private val viewModel: MainActivityViewModel) {
    val videoDecoder = VideoDecoder(viewModel)

    val pitchErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val rollErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val pitchErrorChangeRates = MutableLiveData<MutableList<Float>>(mutableListOf())
    val rollErrorChangeRates = MutableLiveData<MutableList<Float>>(mutableListOf())
    val heightErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val heightErrorChangeRates = MutableLiveData<MutableList<Float>>(mutableListOf())
    val yawErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val yawErrorChangeRates = MutableLiveData<MutableList<Float>>(mutableListOf())

    val posXErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val posYErrors = MutableLiveData<MutableList<Float>>(mutableListOf())
    val posXErrorChangeRates = MutableLiveData<MutableList<Float>>(mutableListOf())
    val posYErrorChangeRates = MutableLiveData<MutableList<Float>>(mutableListOf())

    val posXError = MutableLiveData<Float>(0.0f)
    val posYError = MutableLiveData<Float>(0.0f)
    val posXErrorDer = MutableLiveData<Float>(0.0f)
    val posYErrorDer = MutableLiveData<Float>(0.0f)
    val posXErrorInt = MutableLiveData<Float>(0.0f)
    val posYErrorInt = MutableLiveData<Float>(0.0f)

    val positionValidity = MutableLiveData<Boolean>(false)
    val numSatellites = MutableLiveData<Int>(0)

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

    val pitchGyroNoise = Transformations.map(pitchErrorChangeRates) {
        if (it.size < 1) return@map 0f
        var min = it[0]
        var max = it[0]
        it.forEach {
            if (it < min) min = it
            if (it > max) max = it
        }
        max - min
    }

    val rollGyroNoise = Transformations.map(rollErrorChangeRates) {
        if (it.size < 1) return@map 0f
        var min = it[0]
        var max = it[0]
        it.forEach {
            if (it < min) min = it
            if (it > max) max = it
        }
        max - min
    }

    val yawGyroNoise = Transformations.map(yawErrorChangeRates) {
        if (it.size < 1) return@map 0f
        var min = it[0]
        var max = it[0]
        it.forEach {
            if (it < min) min = it
            if (it > max) max = it
        }
        max - min
    }

    private val pRNoise = PairMediatorLiveData(pitchGyroNoise, rollGyroNoise)
    private val pRYNoise = PairMediatorLiveData(pRNoise, yawGyroNoise)

    val gyroNoise = Transformations.map(pRYNoise) {
        it.first!!.first!! + it.first!!.second!! + it.second!!
    }

    val landingFlag = MutableLiveData<Boolean>(false)

    val batteryVoltage = MutableLiveData<Float>(0.0f)
    val batteryVoltageFormatted: LiveData<String> = Transformations.map(batteryVoltage) {
        String.format("%.2f", it)
    }

    private fun addToList(value: Float, mldlist: MutableLiveData<MutableList<Float>>) {
        val list = mldlist.value
        list?.addNotExceed(value, 100)
        mldlist.value = list
    }

    fun onMessage (data: ByteArray): Unit {
        if (data.size < 1) return
        if (data[0] == MessageTypes.SECONDARY_INFO.ordinal.toByte() && data.size == 97) {
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

            val currentVoltage = ByteBuffer.wrap(data, 69, 4).float

            val currentPosXError = ByteBuffer.wrap(data, 73, 4).float
            val currentPosYError  = ByteBuffer.wrap(data, 77, 4).float
            val posXErrorChangeRate  = ByteBuffer.wrap(data, 81, 4).float
            val posYErrorChangeRate = ByteBuffer.wrap(data, 85, 4).float
            val currentPosXErrInt = ByteBuffer.wrap(data, 89, 4).float
            val currentPosYErrInt = ByteBuffer.wrap(data, 93, 4).float

            addToList(currentPitchError, pitchErrors)
            addToList(currentRollError, rollErrors)

            addToList(pitchErrorChangeRate, pitchErrorChangeRates)
            addToList(rollErrorChangeRate, rollErrorChangeRates)

            addToList(currentHeightError, heightErrors)
            addToList(heightErrorChangeRate, heightErrorChangeRates)

            addToList(currentYawError, yawErrors)
            addToList(yawErrorChangeRate, yawErrorChangeRates)

            addToList(currentPosXError, posXErrors)
            addToList(currentPosYError, posYErrors)

            addToList(posXErrorChangeRate, posXErrorChangeRates)
            addToList(posYErrorChangeRate, posYErrorChangeRates)

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

            batteryVoltage.value = currentVoltage

            posXError.value = currentPosXError
            posYError.value = currentPosYError
            posXErrorDer.value = posXErrorChangeRate
            posYErrorDer.value = posYErrorChangeRate
            posXErrorInt.value = currentPosXErrInt
            posYErrorInt.value = currentPosYErrInt
            return
        }
        if (data[0] == MessageTypes.PRIMARY_INFO.ordinal.toByte() && data.size == 11) {
            landingFlag.value = data[1] != 0.toByte()
            batteryVoltage.value = ByteBuffer.wrap(data, 2, 4).float
            positionValidity.value = data[6] != 0.toByte()
            numSatellites.value = ByteBuffer.wrap(data, 7, 4).int
            return
        }
        if (data[0] == MessageTypes.VIDEO_FRAME.ordinal.toByte()) {
            val videoPacket = data.slice(1 until data.size).toByteArray()
            videoDecoder.putPacket(videoPacket)
            return
        }
    }
}