package com.example.flyingandroidclient

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.flyingandroidclient.databinding.FragmentTweaksBinding
import kotlin.math.roundToInt

class TweaksFragment : Fragment() {
    lateinit var model: MainActivityViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FragmentTweaksBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_tweaks, container, false)

        model = ViewModelProvider(requireActivity()).get(
                MainActivityViewModel::class.java)

        binding.viewmodel = model
        binding.lifecycleOwner = this

        binding.sliderPitchProp.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setPitchPropCoef(value, isLast) }
        binding.sliderPitchDer.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setPitchDerCoef(value, isLast) }
        binding.sliderPitchInt.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setPitchIntCoef(value, isLast) }
        binding.sliderRollProp.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setRollPropCoef(value, isLast) }
        binding.sliderRollDer.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setRollDerCoef(value, isLast) }
        binding.sliderRollInt.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setRollIntCoef(value, isLast) }
        binding.sliderAccTrust.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setAccTrust(value, isLast) }
        binding.sliderMagTrust.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setMagTrust(value, isLast) }
        binding.sliderBaseAccel.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setBaseAcceleration(value, isLast) }
        binding.sliderHeightProp.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setHeightPropCoef(value, isLast) }
        binding.sliderHeightDer.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setHeightDerCoef(value, isLast) }
        binding.sliderHeightInt.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setHeightIntCoef(value, isLast) }
        binding.sliderTurnOffInclineAngle.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setTurnOffInclineAngle(value, isLast) }
        binding.sliderYawProp.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setYawPropCoef(value, isLast) }
        binding.sliderYawDer.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setYawDerCoef(value, isLast) }
        binding.sliderYawInt.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setYawIntCoef(value, isLast) }
        binding.sliderAccLPFMode.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setAccLPFMode(value.roundToInt(), isLast) }
        binding.sliderGyroLPFMode.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setGyroLPFMode(value.roundToInt(), isLast) }
        binding.sliderPitchAdjust.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setPitchAdjust(value, isLast) }
        binding.sliderRollAdjust.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setRollAdjust(value, isLast) }
        binding.sliderAccFiltering.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setAccFiltering(value, isLast) }
        binding.sliderUsHeightFilt.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setUsHeightFiltering(value, isLast) }
        binding.sliderUsHeightDerFilt.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setUsHeightDerFiltering(value, isLast) }
        binding.sliderPitchILimit.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setPitchIntLimit(value, isLast) }
        binding.sliderRollILimit.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setRollIntLimit(value, isLast) }
        binding.sliderYawILimit.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setYawIntLimit(value, isLast) }
        binding.sliderHeightILimit.setValueChangedListener { value: Float, isLast: Boolean -> model.controls.setHeightIntLimit(value, isLast) }
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        model.controls.startSendingSecondaryInfo()
    }

    override fun onPause() {
        super.onPause()
        model.controls.stopSendingSecondaryInfo()
    }

}