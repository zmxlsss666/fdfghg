package com.example.saltplayerremote.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saltplayerremote.models.Device
import com.example.saltplayerremote.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanViewModel : ViewModel() {

    private val _devices = MutableLiveData<List<Device>>(emptyList())
    val devices: LiveData<List<Device>> get() = _devices

    private val _isScanning = MutableLiveData<Boolean>(false)
    val isScanning: LiveData<Boolean> get() = _isScanning

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private var scanJob: Job? = null

    fun scanDevices() {
        scanJob?.cancel()
        _isScanning.value = true
        _devices.value = emptyList()
        _errorMessage.value = null

        scanJob = viewModelScope.launch {
            try {
                // 在后台线程执行扫描
                val foundDevices = withContext(Dispatchers.IO) {
                    NetworkUtils.scanLocalNetwork()
                }
                
                _devices.value = foundDevices.map { Device(ipAddress = it) }
                
                if (foundDevices.isEmpty()) {
                    _errorMessage.value = "No SaltPlayer devices found on the network"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Scan failed: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}
