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

        binding.slider.setValueChangedListener { model.setExampleValue(it) }
        binding.slider2.setValueChangedListener { model.setExampleValue(it) }

        return binding.root
    }

}