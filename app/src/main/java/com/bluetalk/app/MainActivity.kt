package com.bluetalk.app

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluetalk.app.bluetooth.AndroidBluetoothController
import com.bluetalk.app.session.SessionManager
import com.bluetalk.app.ui.BluetalkApp
import com.bluetalk.app.ui.home.HomeViewModel
import com.bluetalk.app.ui.theme.BluetalkTheme

class MainActivity : ComponentActivity() {
    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        homeViewModel.refreshBluetoothAvailability()
    }

    private val homeViewModel: HomeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    bluetoothController = AndroidBluetoothController(applicationContext),
                    sessionManager = SessionManager(),
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BluetalkTheme {
                BluetalkApp(
                    homeViewModel = homeViewModel,
                    onRequestBluetoothPermissions = ::requestBluetoothPermissions,
                )
            }
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = homeViewModel.requiredBluetoothPermissions()

        if (permissions.isEmpty()) {
            homeViewModel.refreshBluetoothAvailability()
        } else {
            bluetoothPermissionLauncher.launch(permissions.toTypedArray())
        }
    }
}
