package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BeaconCyan
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SignalRed
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MainViewModel

@Composable
fun StealthSettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val settings by viewModel.safetySettings.collectAsState()
    val permissionsMap by viewModel.permissionStatusMap.collectAsState()

    // Permission activity launchers
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            viewModel.refreshPermissions()
        }
    )

    var realPinState by remember(settings.realPin) { mutableStateOf(settings.realPin) }
    var fakePinState by remember(settings.fakePin) { mutableStateOf(settings.fakePin) }
    var customMsgState by remember(settings.customEmergencyMessage) { mutableStateOf(settings.customEmergencyMessage) }
    var voiceKeywordState by remember(settings.voiceKeyword) { mutableStateOf(settings.voiceKeyword) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "STEALTH & SECURITY SETTINGS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Disguise Mode, Dark Privacy, Sensitivity & Permissions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { viewModel.signOut() },
                colors = ButtonDefaults.buttonColors(containerColor = SignalRed)
            ) {
                Text(text = "Sign Out")
            }
        }

        // Stealth Dark Mode & Screen Privacy Card
        val stealthDarkModeActive by viewModel.stealthDarkModeActive.collectAsState()
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = BeaconCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Global Stealth Dark Mode",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Reduces screen brightness to 1% & hides sensitive UI data for privacy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = stealthDarkModeActive,
                        onCheckedChange = { viewModel.toggleStealthDarkMode(it) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Auto-Activate Stealth Dark on Emergency SOS",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = settings.autoStealthOnEmergency,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoStealthOnEmergency = it)) }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "When enabled, triggering any panic alert immediately dims your screen to minimum brightness and redacts confidential contact numbers & location logs.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Stealth Calculator Disguise Mode Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = BeaconCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Calculator Stealth Disguise",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Disguise app UI as an innocent calculator",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.stealthModeEnabled,
                        onCheckedChange = { viewModel.toggleStealthMode(it) }
                    )
                }

                if (settings.stealthModeEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = realPinState,
                        onValueChange = {
                            realPinState = it
                            if (it.length == 4) viewModel.updateSettings(settings.copy(realPin = it))
                        },
                        label = { Text("Real Unlock PIN (4 digits)") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = fakePinState,
                        onValueChange = {
                            fakePinState = it
                            if (it.length == 4) viewModel.updateSettings(settings.copy(fakePin = it))
                        },
                        label = { Text("Covert Fake PIN (Triggers Secret SOS)") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = SignalRed) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tip: Typing the Real PIN ($realPinState) unlocks the dashboard. Typing Covert PIN ($fakePinState) silently dispatches emergency alerts while looking like a normal reset!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Sensor & Trigger Sensitivity Settings
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TRIGGER SENSITIVITY & CONFIGURATION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Shake Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Vibration, contentDescription = null, tint = WarningAmber)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Shake Detection Trigger",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = settings.shakeEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(shakeEnabled = it)) }
                    )
                }

                if (settings.shakeEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Shake Sensitivity: ${"%.1f".format(settings.shakeThreshold)} m/s²",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Slider(
                        value = settings.shakeThreshold,
                        onValueChange = { viewModel.updateSettings(settings.copy(shakeThreshold = it)) },
                        valueRange = 8f..25f,
                        steps = 17
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Voice Command Keyword
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = BeaconCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Voice Phrase Trigger",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = settings.voiceCommandEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(voiceCommandEnabled = it)) }
                    )
                }

                if (settings.voiceCommandEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = voiceKeywordState,
                        onValueChange = {
                            voiceKeywordState = it
                            viewModel.updateSettings(settings.copy(voiceKeyword = it))
                        },
                        label = { Text("Emergency Voice Phrase") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Emergency Message Note
                OutlinedTextField(
                    value = customMsgState,
                    onValueChange = {
                        customMsgState = it
                        viewModel.updateSettings(settings.copy(customEmergencyMessage = it))
                    },
                    label = { Text("Custom Emergency SMS Body Note") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        }

        // Permissions Center Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SYSTEM PERMISSIONS STATUS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                PermissionRow("SMS Dispatch", Manifest.permission.SEND_SMS, permissionsMap[Manifest.permission.SEND_SMS] == true)
                PermissionRow("GPS Location", Manifest.permission.ACCESS_FINE_LOCATION, permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] == true)
                PermissionRow("Call Escalation", Manifest.permission.CALL_PHONE, permissionsMap[Manifest.permission.CALL_PHONE] == true)
                PermissionRow("Audio Recognizer", Manifest.permission.RECORD_AUDIO, permissionsMap[Manifest.permission.RECORD_AUDIO] == true)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.CALL_PHONE,
                                Manifest.permission.RECORD_AUDIO
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BeaconCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grant All Required Permissions", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun PermissionRow(label: String, permission: String, isGranted: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isGranted) SafeGreen else SignalRed,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (isGranted) "Granted" else "Needed",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isGranted) SafeGreen else SignalRed
        )
    }
}
