package com.example.flyingandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.flyingandroidclient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val model: MainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding.viewModel = model
        binding.lifecycleOwner = this

        model.optionsVisibility.observe(this, Observer {
            Log.i("myinfo", "vis val $it")
        })

        binding.joystick.setPositionListener {
            Log.i("myinfo", "pos ${it.x} ${it.y}")
        }
    }
}