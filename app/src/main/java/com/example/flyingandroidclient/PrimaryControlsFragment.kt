package com.example.flyingandroidclient

import android.graphics.PointF
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.flyingandroidclient.databinding.FragmentPrimaryControlsBinding

class PrimaryControlsFragment : Fragment() {
    lateinit var model: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentPrimaryControlsBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_primary_controls, container, false)

        model = ViewModelProvider(requireActivity()).get(
                MainActivityViewModel::class.java)

        binding.viewmodel = model
        binding.lifecycleOwner = this

        binding.joystick.setPositionListener { point: PointF, isLast: Boolean ->
            model.controls.move(point, isLast)
            // Log.i("myinfo", "x ${it.x} y ${it.y}")
        }
        binding.joystick.setDirectionListener { dir: Float, isLast: Boolean ->
            // Log.i("myinfo", "direction ${it}")
            model.controls.setDirection(dir, isLast)
        }
        binding.accelSlider.setHeightChangedListener() { value: Float, isLast: Boolean ->
            model.controls.setHeight(value, isLast)
        }
        binding.accelSlider.setModeChangedListener {
            model.onAccSliderModeChange(it)
        }
        binding.accelSlider.setRelativeAccelerationChangedListener {value: Float, isLast: Boolean ->
            model.controls.setRelativeAcceleration(value, isLast)
        }

        binding.holdSwitch.setOnCheckedChangeListener { _, isChecked ->
            model.controls.setHoldMode(if (isChecked) 1 else 0)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        model.controls.startSensorListening()
        model.controls.startSendingPrimaryInfo()
    }

    override fun onPause() {
        super.onPause()
        model.controls.stopSensorListening()
        model.controls.stopSendingPrimaryInfo()
    }
}