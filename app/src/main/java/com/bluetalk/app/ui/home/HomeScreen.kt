package com.bluetalk.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.bluetalk.app.model.DeviceIdentity
import com.bluetalk.app.session.ConnectionRole
import com.bluetalk.app.session.SessionState
import com.bluetalk.app.ui.components.StatusLine
import com.bluetalk.app.ui.theme.BluetalkTheme

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onRequestBluetoothPermissions: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()

    HomeScreen(
        uiState = uiState,
        onCreatePrivateSession = viewModel::createPrivateSession,
        onFindNearbyUsers = viewModel::findNearbyUsers,
        onSelectConnectionRole = viewModel::selectConnectionRole,
        onSelectNearbyDevice = { device ->
            viewModel.selectNearbyDevice(device)
            viewModel.joinSession(device)
        },
        onEndSession = viewModel::endSession,
        onRequestBluetoothPermissions = onRequestBluetoothPermissions,
        messages = messages,
        onSendMessage = viewModel::sendMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onCreatePrivateSession: () -> Unit,
    onFindNearbyUsers: () -> Unit,
    onSelectConnectionRole: (ConnectionRole) -> Unit,
    onSelectNearbyDevice: (DeviceIdentity) -> Unit,
    onEndSession: () -> Unit,
    onRequestBluetoothPermissions: () -> Unit,
    messages: List<String> = emptyList(),
    onSendMessage: (String) -> Unit = {},
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

            Text(
                text = "Connection role",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ConnectionRole.entries.forEachIndexed { index, role ->
                    SegmentedButton(
                        selected = uiState.connectionRole == role,
                        onClick = { onSelectConnectionRole(role) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ConnectionRole.entries.size,
                        ),
                        enabled = uiState.sessionState is SessionState.NoSession,
                    ) {
                        Text(role.asDisplayText())
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            StatusLine(
                label = "Session",
                value = uiState.sessionState.asDisplayText(),
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.connectionRole == ConnectionRole.Host) {
                Button(
                    onClick = onCreatePrivateSession,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.sessionState is SessionState.NoSession,
                ) {
                    Text("Create Host Session")
                }
            } else {
                OutlinedButton(
                    onClick = onFindNearbyUsers,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.bluetoothState == BluetoothConnectionState.Ready,
                ) {
                    Text(
                        text = if (uiState.bluetoothState == BluetoothConnectionState.Scanning) {
                            "Scanning..."
                        } else {
                            "Scan for Host"
                        },
                    )
                }
            }

            if (
                uiState.connectionRole == ConnectionRole.Join &&
                (
                    uiState.bluetoothState == BluetoothConnectionState.Scanning ||
                        uiState.nearbyDevices.isNotEmpty()
                    )
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
                                device = device,
                                selected = uiState.selectedDevice?.id == device.id,
                                onClick = { onSelectNearbyDevice(device) },
                            )
                        }
                    }
                }
            }


            if (uiState.sessionState is SessionState.Active) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Messages", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        Text(msg)
                    }
                }
                
                var textState by remember { mutableStateOf("") }
                
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") }
                    )
                    Button(
                        onClick = {
                            onSendMessage(textState)
                            textState = ""
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Send")
                    }
                }

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
    device: DeviceIdentity,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.small,
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            ) {
                Text(
                    text = device.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = device.id,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun ConnectionRole.asDisplayText(): String {
    return when (this) {
        ConnectionRole.Host -> "Host"
        ConnectionRole.Join -> "Join"
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
            onSelectConnectionRole = {},
            onSelectNearbyDevice = {},
            onEndSession = {},
            onRequestBluetoothPermissions = {},
        )
    }
}
