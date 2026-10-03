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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.runtime.collectAsState
import com.bluetalk.app.session.ConnectionRole
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluetalk.app.bluetooth.BluetoothConnectionState
import com.bluetalk.app.model.DeviceIdentity
import com.bluetalk.app.session.SessionState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    messages: List<String>,
    transferProgress: Float?,
    onRoleSelected: (ConnectionRole) -> Unit,
    onSelectNearbyDevice: (DeviceIdentity) -> Unit,
    onCreateHostSession: () -> Unit,
    onEndSession: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendFile: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.sessionState is SessionState.Active) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.sessionState.asDisplayText(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
                OutlinedButton(
                    onClick = onEndSession,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text("End Session", color = MaterialTheme.colorScheme.error, fontFamily = FontFamily.Monospace)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.startsWith("Me:")
                    val isSystem = msg.startsWith("System:")
                    val color = when {
                        isSystem -> MaterialTheme.colorScheme.tertiary
                        isMe -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.primary
                    }
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace,
                        color = color,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.GetContent()
            ) { uri ->
                uri?.let { onSendFile(it) }
            }

            if (transferProgress != null) {
                val progressPercent = (transferProgress * 100).toInt()
                val barLength = 20
                val filled = (transferProgress * barLength).toInt()
                val bar = "[" + "#".repeat(filled) + ".".repeat(barLength - filled) + "]"
                
                Text(
                    text = "Transferring $bar $progressPercent%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            var textState by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text("Type a message...", fontFamily = FontFamily.Monospace) 
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { 
                        if (textState.isNotBlank()) {
                            onSendMessage(textState)
                            textState = ""
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text("Send", fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text("File", fontFamily = FontFamily.Monospace)
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Bluetalk",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Offline. Nearby. Temporary.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bluetooth status", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
                Text(
                    text = uiState.bluetoothState.asDisplayText(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        Column {
            Text("Connection role", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val roles = listOf(ConnectionRole.Host, ConnectionRole.Join)
                roles.forEachIndexed { index, role ->
                    SegmentedButton(
                        selected = uiState.connectionRole == role,
                        onClick = { onRoleSelected(role) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = roles.size)
                    ) {
                        Text(role.name, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Column {
            Text("Session", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = uiState.sessionState.asDisplayText(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    if (
                        uiState.connectionRole == ConnectionRole.Join &&
                        uiState.sessionState !is SessionState.Active &&
                        (
                            uiState.bluetoothState == BluetoothConnectionState.Scanning ||
                                uiState.nearbyDevices.isNotEmpty()
                            )
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.bluetoothState == BluetoothConnectionState.Scanning) "Scanning..." else "Scan for Host",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nearby devices", style = MaterialTheme.typography.titleSmall, fontFamily = FontFamily.Monospace)
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            uiState.nearbyDevices.forEach { device ->
                                NearbyDeviceRow(
                                    device = device,
                                    selected = device == uiState.selectedDevice,
                                    onClick = { onSelectNearbyDevice(device) }
                                )
                            }
                        }
                    } else if (uiState.connectionRole == ConnectionRole.Host && uiState.sessionState is SessionState.NoSession) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCreateHostSession,
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        ) {
                            Text("Create Host Session", fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NearbyDeviceRow(
    device: DeviceIdentity,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = device.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = device.id,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
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


@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onRequestBluetoothPermissions: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val progress by viewModel.transferProgress.collectAsState()

    HomeScreen(
        uiState = uiState,
        messages = messages,
        transferProgress = progress,
        onRoleSelected = viewModel::selectConnectionRole,
        onSelectNearbyDevice = viewModel::selectNearbyDevice,
        onCreateHostSession = viewModel::createPrivateSession,
        onEndSession = viewModel::endSession,
        onSendMessage = viewModel::sendMessage,
        onSendFile = viewModel::sendFile
    )
}
