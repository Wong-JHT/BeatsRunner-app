package com.beatrunner.ui.bluetooth

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beatrunner.viewmodel.BluetoothViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(
        onBack: () -> Unit,
        modifier: Modifier = Modifier,
        viewModel: BluetoothViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allGranted = permissions.values.all { it }
                if (allGranted) {
                    viewModel.startScanning()
                }
            }

    LaunchedEffect(Unit) {
        val permissionsToRequest =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                    )
                } else {
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                }
        permissionLauncher.launch(permissionsToRequest)
    }

    DisposableEffect(Unit) { onDispose { viewModel.stopScanning() } }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Bluetooth Devices") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.startScanning() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Scan")
                            }
                        }
                )
            }
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding, modifier = modifier.fillMaxSize()) {
            // Group 1: Known Devices
            if (uiState.knownDevices.isNotEmpty()) {
                item { SectionHeader("Known Devices") }
                items(uiState.knownDevices) { deviceUiModel ->
                    BluetoothDeviceItem(
                            name = deviceUiModel.device.name ?: "Unknown Device",
                            address = deviceUiModel.device.address,
                            status =
                                    when {
                                        deviceUiModel.isConnected -> "Connected"
                                        deviceUiModel.isAvailable -> "Available"
                                        else -> "Not Found"
                                    },
                            isConnected = deviceUiModel.isConnected,
                            onConnect = { viewModel.connectToDevice(deviceUiModel.device) },
                            onDisconnect = { viewModel.disconnect() }
                    )
                }
            }

            // Group 2: Available Devices
            if (uiState.availableDevices.isNotEmpty()) {
                item { SectionHeader("New Available Devices") }
                items(uiState.availableDevices) { deviceUiModel ->
                    BluetoothDeviceItem(
                            name = deviceUiModel.device.name ?: "Unknown Device",
                            address = deviceUiModel.device.address,
                            status = "Not Connected",
                            isConnected = false,
                            onConnect = { viewModel.connectToDevice(deviceUiModel.device) },
                            onDisconnect = {
                                viewModel.disconnect()
                            } // Should not happen for available devices
                    )
                }
            }

            if (uiState.knownDevices.isEmpty() && uiState.availableDevices.isEmpty()) {
                item {
                    Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        Text(
                                "No devices found. scanning...",
                                style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun BluetoothDeviceItem(
        name: String,
        address: String,
        status: String,
        isConnected: Boolean,
        onConnect: () -> Unit,
        onDisconnect: () -> Unit
) {
    ListItem(
            headlineContent = { Text(name) },
            supportingContent = { Text("$address\n$status") },
            trailingContent = {
                if (!isConnected) {
                    Button(onClick = onConnect) { Text("Connect") }
                } else {
                    TextButton(onClick = onDisconnect) { Text("Disconnect") }
                }
            }
    )
    HorizontalDivider()
}
