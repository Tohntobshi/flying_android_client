package com.example.flyingandroidclient

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.flyingandroidclient.databinding.FragmentTweaksBinding

class TweaksFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FragmentTweaksBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_tweaks, container, false)

        val model: MainActivityViewModel = ViewModelProvider(requireActivity()).get(
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

        return binding.root
    }

}