package com.example.flyingandroidclient

import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.flyingandroidclient.databinding.FragmentPrimaryContolsBinding
import com.example.flyingandroidclient.databinding.FragmentTabsBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PrimaryContolsFragment : Fragment() {
    lateinit var model: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentPrimaryContolsBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_primary_contols, container, false)

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
        binding.accelSlider.setValueChangedListener { value: Float, isLast: Boolean ->
            model.controls.setAcceleration(value, isLast)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        model.controls.startSensorListening()
    }

    override fun onPause() {
        super.onPause()
        model.controls.stopSensorListening()
    }
}