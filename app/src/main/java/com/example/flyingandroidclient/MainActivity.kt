package com.example.flyingandroidclient

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.flyingandroidclient.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.os.PowerManager

import android.os.PowerManager.WakeLock

import android.R.attr.name





class MainActivity : AppCompatActivity() {
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: WakeLock
    private lateinit var model: MainActivityViewModel
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "myapp:mywakelocktag")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        model = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding.viewModel = model
        binding.lifecycleOwner = this

        model.currentTab.observe(this, Observer {
            changeTab(it)
        })

        binding.outerLayout.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            binding.innerLayout.layoutParams.width = v.height
            binding.innerLayout.layoutParams.height = v.width
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }


    private suspend fun updateViewAfterDelay() {
        delay(1)
        binding.outerLayout.requestLayout()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            updateViewAfterDelay()
        }
        wakeLock.acquire()
    }

    override fun onPause() {
        super.onPause()
        wakeLock.release()
    }

    override fun onBackPressed() {
        // do nothing
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