package com.wonhui.demo.langchain_ondevice_cloudllm

import android.app.Application
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow

class MyApp: Application() {

    companion object {
        val networkStatus = MutableStateFlow(false)
    }

    override fun onCreate() {
        super.onCreate()

        // Monitor connectivity status and connection metering
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        // Configure a network request
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)  // 데이터 사용 시 X, 와이파이 사용 시 O
            .build()

        // Configure a network callback
        val networkCallback = object : NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                networkStatus.value = true
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                networkStatus.value = unmetered
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                networkStatus.value = false
            }
        }

        // Register for network updates
        val connectivityManager = getSystemService(ConnectivityManager::class.java)  as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
}