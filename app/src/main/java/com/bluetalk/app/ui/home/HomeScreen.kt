package com.bluetalk.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bluetalk.app.bluetooth.BluetoothConnectionState
import com.bluetalk.app.session.SessionState
import com.bluetalk.app.ui.components.StatusLine
import com.bluetalk.app.ui.theme.BluetalkTheme

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onRequestBluetoothPermissions: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreen(
        uiState = uiState,
        onCreatePrivateSession = viewModel::createPrivateSession,
        onFindNearbyUsers = viewModel::findNearbyUsers,
        onEndSession = viewModel::endSession,
        onRequestBluetoothPermissions = onRequestBluetoothPermissions,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onCreatePrivateSession: () -> Unit,
    onFindNearbyUsers: () -> Unit,
    onEndSession: () -> Unit,
    onRequestBluetoothPermissions: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "Bluetalk",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Offline. Nearby. Temporary.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            StatusLine(
                label = "Bluetooth status",
                value = uiState.bluetoothState.asDisplayText(),
            )

            if (uiState.bluetoothState == BluetoothConnectionState.PermissionRequired) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Bluetalk needs Bluetooth permission before it can find or connect nearby devices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onRequestBluetoothPermissions) {
                    Text("Grant Bluetooth Permission")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            StatusLine(
                label = "Session",
                value = uiState.sessionState.asDisplayText(),
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onCreatePrivateSession,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Create Private Session")
                }

                OutlinedButton(
                    onClick = onFindNearbyUsers,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.bluetoothState == BluetoothConnectionState.Ready,
                ) {
                    Text(
                        text = if (uiState.bluetoothState == BluetoothConnectionState.Scanning) {
                            "Scanning..."
                        } else {
                            "Find Nearby Users"
                        },
                    )
                }
            }

            if (
                uiState.bluetoothState == BluetoothConnectionState.Scanning ||
                uiState.nearbyDevices.isNotEmpty()
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Nearby devices",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.nearbyDevices.isEmpty()) {
                    Text(
                        text = "Scanning for nearby Bluetooth devices...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        uiState.nearbyDevices.forEach { device ->
                            NearbyDeviceRow(
                                name = device.displayName,
                                address = device.id,
                            )
                        }
                    }
                }
            }

            if (uiState.sessionState is SessionState.Active) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onEndSession) {
                    Text("End Session")
                }
            }
        }
    }
}

@Composable
private fun NearbyDeviceRow(
    name: String,
    address: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.small,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun BluetoothConnectionState.asDisplayText(): String {
    return when (this) {
        BluetoothConnectionState.Idle -> "Not connected"
        BluetoothConnectionState.PermissionRequired -> "Bluetooth permission required"
        BluetoothConnectionState.Ready -> "Ready"
        BluetoothConnectionState.Scanning -> "Scanning"
        BluetoothConnectionState.Listening -> "Listening"
        is BluetoothConnectionState.Connecting -> "Connecting to ${deviceName ?: "nearby device"}"
        is BluetoothConnectionState.Connected -> "Connected to ${deviceName ?: "nearby device"}"
        is BluetoothConnectionState.Error -> message
    }
}

private fun SessionState.asDisplayText(): String {
    return when (this) {
        SessionState.NoSession -> "No active session"
        is SessionState.Active -> "${session.name} (${session.members.size} member)"
        is SessionState.Ending -> "Ending session"
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    BluetalkTheme {
        HomeScreen(
            uiState = HomeUiState(),
            onCreatePrivateSession = {},
            onFindNearbyUsers = {},
            onEndSession = {},
            onRequestBluetoothPermissions = {},
        )
    }
}
