package com.example.flyingandroidclient

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.flyingandroidclient.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity() {
    private lateinit var model: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        model = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding.viewModel = model
        binding.lifecycleOwner = this

        model.currentTab.observe(this, Observer {
            changeTab(it)
        })

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    private fun changeTab(tab: Tabs) {
        val fragmentClass = when(tab) {
            Tabs.MAIN -> PrimaryContolsFragment::class.java
            Tabs.TWEAKS -> TweaksFragment::class.java
            Tabs.OPTIONS -> OptionsFragment::class.java
        }

        supportFragmentManager.commit {
            replace(R.id.contentFragment, fragmentClass, null)
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        model.addBtDevice(it)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}