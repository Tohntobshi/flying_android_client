package com.example.flyingandroidclient

import android.bluetooth.BluetoothDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class DeviceListAdapter(private val onClick: (device: BluetoothDevice) -> Unit): RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {
    private var dataset: List<BluetoothDevice> = listOf()

    fun update(data: List<BluetoothDevice>) {
        val diffRes = DiffUtil.calculateDiff(DiffCallback(data, dataset))
        dataset = data
        diffRes.dispatchUpdatesTo(this)
//        notifyDataSetChanged()
    }

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val name: TextView
        val address: TextView
        init {
            name = view.findViewById(R.id.device_name)
            address = view.findViewById(R.id.device_address)
        }
    }

    class DiffCallback(private val newList: List<BluetoothDevice>, private val oldList: List<BluetoothDevice>): DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList.getOrNull(oldItemPosition)
            val newItem = newList.getOrNull(newItemPosition)
            return return oldItem?.address == newItem?.address
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList.getOrNull(oldItemPosition)
            val newItem = newList.getOrNull(newItemPosition)
            return oldItem?.address == newItem?.address && oldItem?.name == newItem?.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.device_list_element, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = dataset[position]
        holder.name.text = device.name ?: "no name"
        holder.address.text = device.address ?: "no address"
        holder.view.setOnClickListener {
            onClick(device)
        }
    }

    override fun getItemCount(): Int {
        // Log.i("myinfo", "getitemcount ${dataset.size}")
        return dataset.size
    }

}