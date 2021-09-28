package com.example.flyingandroidclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flyingandroidclient.databinding.FragmentTabsBinding


class TabsFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentTabsBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_tabs, container, false)

        val model: MainActivityViewModel = ViewModelProvider(requireActivity()).get(
                MainActivityViewModel::class.java)

        binding.viewmodel = model
        binding.lifecycleOwner = this

        return binding.root
    }

}