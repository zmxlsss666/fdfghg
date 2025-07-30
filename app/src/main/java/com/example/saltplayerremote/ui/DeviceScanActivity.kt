package com.example.saltplayerremote.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saltplayerremote.R
import com.example.saltplayerremote.databinding.ActivityDeviceScanBinding
import com.example.saltplayerremote.utils.NetworkUtils
import com.example.saltplayerremote.viewmodels.ScanViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class DeviceScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceScanBinding
    private val viewModel: ScanViewModel by viewModels()
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
        startScan()
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter { device ->
            saveDeviceIp(device.ipAddress)
            openPlayerControl(device.ipAddress)
        }
        
        binding.devicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DeviceScanActivity)
            adapter = deviceAdapter
        }
    }

    private fun setupListeners() {
        binding.scanButton.setOnClickListener {
            startScan()
        }
        
        binding.manualConnectButton.setOnClickListener {
            val ip = binding.manualIpInput.text.toString().trim()
            if (ip.isNotEmpty()) {
                saveDeviceIp(ip)
                openPlayerControl(ip)
            } else {
                Snackbar.make(binding.root, "Please enter a valid IP address", Snackbar.LENGTH_SHORT).show()
            }
        }
        
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.devices.observe(this) { devices ->
            deviceAdapter.submitList(devices)
            binding.emptyState.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.isScanning.observe(this) { isScanning ->
            binding.progressBar.visibility = if (isScanning) View.VISIBLE else View.GONE
            binding.scanButton.isEnabled = !isScanning
        }
        
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun startScan() {
        lifecycleScope.launch {
            viewModel.scanDevices()
        }
    }

    private fun saveDeviceIp(ip: String) {
        getSharedPreferences("SaltPlayerPrefs", Context.MODE_PRIVATE).edit()
            .putString("last_ip", ip)
            .apply()
    }

    private fun openPlayerControl(ip: String) {
        val intent = Intent(this, PlayerControlActivity::class.java).apply {
            putExtra("device_ip", ip)
        }
        startActivity(intent)
    }
}