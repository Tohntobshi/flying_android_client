package com.example.flyingandroidclient

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.flyingandroidclient.databinding.FragmentOptionsBinding

class OptionsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentOptionsBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_options, container, false)

        val model: MainActivityViewModel = ViewModelProvider(requireActivity()).get(
                MainActivityViewModel::class.java)

        model.updateDeviceList()

        binding.viewmodel = model
        binding.lifecycleOwner = this
        val adapter = DeviceListAdapter {
//            Log.i("myinfo", "clicked ${it.address}")
            model.establishConnection(it)
        }
        binding.deviceList.adapter = adapter

        model.btDevicesList.observe(this, Observer {
            adapter.update(it)
//            Log.i("myinfo", "list:")
//            it.forEach {
//                Log.i("myinfo", "${it.name} ${it.address}")
//            }
        })

        adapter

        binding.uuidInput.addTextChangedListener {
            model.changeServiceUUID(it.toString())
        }

        return binding.root
    }
}