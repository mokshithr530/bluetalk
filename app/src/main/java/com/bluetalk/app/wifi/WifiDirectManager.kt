package com.bluetalk.app.wifi

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log

class WifiDirectManager(private val context: Context) {
    private val manager: WifiP2pManager? = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    private val channel: WifiP2pManager.Channel? = manager?.initialize(context, Looper.getMainLooper(), null)

    private val _connectionInfo = MutableStateFlow<WifiP2pInfo?>(null)
    val connectionInfo: StateFlow<WifiP2pInfo?> = _connectionInfo.asStateFlow()

    private val _myMacAddress = MutableStateFlow<String?>(null)
    val myMacAddress: StateFlow<String?> = _myMacAddress.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val networkInfo = intent.getParcelableExtra<android.net.NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                    if (networkInfo?.isConnected == true) {
                        manager?.requestConnectionInfo(channel) { info ->
                            _connectionInfo.value = info
                            Log.d("WifiDirect", "Connected! Group Owner IP: ${info.groupOwnerAddress?.hostAddress}")
                        }
                    } else {
                        _connectionInfo.value = null
                    }
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    val device = intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                    device?.deviceAddress?.let { mac ->
                        _myMacAddress.value = mac
                    }
                }
            }
        }
    }

    fun startListening() {
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
        context.registerReceiver(receiver, intentFilter)
    }

    fun stopListening() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {}
    }

    @SuppressLint("MissingPermission")
    fun createGroup() {
        manager?.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("WifiDirect", "Group created successfully")
            }
            override fun onFailure(reason: Int) {
                Log.e("WifiDirect", "Group creation failed: $reason")
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun connectToMac(macAddress: String) {
        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                val config = WifiP2pConfig().apply {
                    deviceAddress = macAddress
                    wps.setup = android.net.wifi.WpsInfo.PBC
                }
                manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d("WifiDirect", "Connection initiated")
                    }
                    override fun onFailure(reason: Int) {
                        Log.e("WifiDirect", "Connection failed: $reason")
                    }
                })
            }
            override fun onFailure(reason: Int) {
                Log.e("WifiDirect", "Discover peers failed before connect: $reason")
                // Try connecting anyway
                val config = WifiP2pConfig().apply {
                    deviceAddress = macAddress
                    wps.setup = android.net.wifi.WpsInfo.PBC
                }
                manager?.connect(channel, config, null)
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        manager?.removeGroup(channel, null)
    }
}
