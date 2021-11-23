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

        binding.sliderPitchProp.setValueChangedListener { model.controls.setPitchPropCoef(it) }
        binding.sliderPitchDer.setValueChangedListener { model.controls.setPitchDerCoef(it) }
        binding.sliderPitchInt.setValueChangedListener { model.controls.setPitchIntCoef(it) }
        binding.sliderRollProp.setValueChangedListener { model.controls.setRollPropCoef(it) }
        binding.sliderRollDer.setValueChangedListener { model.controls.setRollDerCoef(it) }
        binding.sliderRollInt.setValueChangedListener { model.controls.setRollIntCoef(it) }
        binding.sliderAccTrust.setValueChangedListener { model.controls.setAccTrust(it) }
        binding.sliderInclChRFilt.setValueChangedListener { model.controls.setInclineChangeRateFilteringCoef(it) }
        binding.sliderInclFilt.setValueChangedListener { model.controls.setInclineFilteringCoef(it) }
        binding.sliderBaseAccel.setValueChangedListener { model.controls.setBaseAcceleration(it) }
        binding.sliderHeightProp.setValueChangedListener { model.controls.setHeightPropCoef(it) }
        binding.sliderHeightDer.setValueChangedListener { model.controls.setHeightDerCoef(it) }
        binding.sliderHeightInt.setValueChangedListener { model.controls.setHeightIntCoef(it) }
        binding.sliderTurnOffInclineAngle.setValueChangedListener { model.controls.setTurnOffInclineAngle(it) }
        binding.sliderYawSpProp.setValueChangedListener { model.controls.setYawSpPropCoef(it) }
        binding.sliderYawSpDer.setValueChangedListener { model.controls.setYawSpDerCoef(it) }
        binding.sliderYawSpInt.setValueChangedListener { model.controls.setYawSpIntCoef(it) }
        binding.sliderYawSpFilt.setValueChangedListener { model.controls.setYawSpFilteringCoef(it) }
        binding.sliderImuLPFMode.setValueChangedListener { model.controls.setImuLPFMode(it.roundToInt()) }
        model.controls.startSendingInfo()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        model.controls.stopSendingInfo()
    }

}