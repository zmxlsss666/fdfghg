package com.example.saltplayerremote.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.saltplayerremote.databinding.ItemDeviceBinding
import com.example.saltplayerremote.models.Device

class DeviceAdapter(
    private val onDeviceClick: (Device) -> Unit
) : ListAdapter<Device, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding, onDeviceClick)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = getItem(position)
        holder.bind(device)
    }

    class DeviceViewHolder(
        private val binding: ItemDeviceBinding,
        private val onDeviceClick: (Device) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: Device) {
            binding.apply {
                deviceName.text = device.name
                deviceIp.text = device.ipAddress
                
                // 设置点击监听器
                root.setOnClickListener { onDeviceClick(device) }
                connectButton.setOnClickListener { onDeviceClick(device) }
            }
        }
    }

    private class DeviceDiffCallback : DiffUtil.ItemCallback<Device>() {
        override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
            // 使用IP地址作为唯一标识
            return oldItem.ipAddress == newItem.ipAddress
        }

        override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
            // 比较所有属性是否相同
            return oldItem == newItem
        }
    }
}