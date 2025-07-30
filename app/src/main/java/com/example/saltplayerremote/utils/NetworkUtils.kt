package com.example.saltplayerremote.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NetworkUtils {

    suspend fun scanLocalNetwork(port: Int = 35373): List<String> = withContext(Dispatchers.IO) {
        val ipPrefix = getLocalIpAddress()?.substringBeforeLast('.') ?: return@withContext emptyList()
        val devices = mutableListOf<String>()

        // 扫描IP范围：192.168.x.1 - 192.168.x.255
        for (i in 1..255) {
            val host = "$ipPrefix.$i"
            if (isReachable(host, port)) {
                devices.add(host)
                Log.d("NetworkUtils", "Found device at $host")
            }
        }

        return@withContext devices
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            for (intf in Collections.list(interfaces)) {
                // 跳过无效接口
                if (intf.isLoopback || !intf.isUp) continue
                
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        if (sAddr != null && sAddr.indexOf(':') < 0) {
                            return sAddr
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("NetworkUtils", "Error getting local IP: ${ex.message}")
        }
        return null
    }

    private fun isReachable(host: String, port: Int, timeout: Int = 300): Boolean {
        return try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress(host, port), timeout)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
