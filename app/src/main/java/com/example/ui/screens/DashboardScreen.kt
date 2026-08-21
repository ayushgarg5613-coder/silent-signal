package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ml.MLContextAnalyzer
import com.example.ui.theme.BeaconCyan
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SignalRed
import com.example.ui.theme.SleekBlueButton
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekCardBlue
import com.example.ui.theme.SleekGrayCard
import com.example.ui.theme.SleekGreen
import com.example.ui.theme.SleekGreenText
import com.example.ui.theme.SleekHeaderNavy
import com.example.ui.theme.SleekMutedText
import com.example.ui.theme.SleekNavy
import com.example.ui.theme.SleekOnBg
import com.example.ui.theme.SleekPillBg
import com.example.ui.theme.SleekRed
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val settings by viewModel.safetySettings.collectAsState()
    val shakeIntensity by viewModel.liveShakeIntensity.collectAsState()
    val coords by viewModel.currentLocationCoords.collectAsState()
    val countdownSec by viewModel.activeCountdownSec.collectAsState()
    val countdownType by viewModel.activeCountdownTriggerType.collectAsState()
    val lastAlert by viewModel.lastDispatchedAlert.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val voiceListeningActive by viewModel.voiceListeningActive.collectAsState()
    val lastHeardVoicePhrase by viewModel.lastHeardVoicePhrase.collectAsState()

    val (lat, lng) = coords
    val googleMapsUrl = "https://maps.google.com/?q=%.6f,%.6f".format(lat, lng)

    // ML analysis sample calculation based on current sensor state
    val liveMlAnalysis = remember(shakeIntensity) {
        MLContextAnalyzer.analyzeContext(
            triggerType = "Live Sensor Baseline",
            movementIntensity = shakeIntensity
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Silent Signal",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekHeaderNavy,
                        letterSpacing = (-0.5).sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SleekGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ACTIVE SHIELD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SleekGreenText,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SleekCardBlue)
                        .clickable { viewModel.toggleStealthMode(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Stealth Disguise",
                        tint = SleekNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Central SOS Panic Trigger Section
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekGrayCard),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsing Ring & SOS Button
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.25f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1400, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(210.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        viewModel.secretTapDetector.registerTap()
                                    },
                                    onLongPress = {
                                        viewModel.triggerEmergencyAlert("SOS Long-Press", intensity = 5f)
                                    }
                                )
                            }
                    ) {
                        // Pulse ring
                        Box(
                            modifier = Modifier
                                .size(190.dp)
                                .scale(pulseScale)
                                .background(
                                    SleekRed.copy(alpha = 0.12f),
                                    CircleShape
                                )
                        )
                        // Outer border SOS Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(175.dp)
                                .clip(CircleShape)
                                .background(SleekRed)
                                .border(8.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = "SOS Flash",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "SOS",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "HOLD 3 SECONDS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.85f),
                                    letterSpacing = (-0.2).sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Emergency signals are armed and monitoring sensors.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekMutedText,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.triggerEmergencyAlert("Manual Emergency Alert", bypassCountdown = true)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekRed),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Instant Silent SOS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Active Stealth Triggers Grid Dashboard
            Column {
                Text(
                    text = "ACTIVE STEALTH TRIGGERS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SleekMutedText,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Shake Trigger Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SleekCardBlue),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .clickable {
                                viewModel.triggerEmergencyAlert("Shake Detected", intensity = settings.shakeThreshold + 2f)
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(SleekBlueButton, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Vibration, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    text = "Shake Detect",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekNavy
                                )
                                Text(
                                    text = "High Sensitivity",
                                    fontSize = 11.sp,
                                    color = SleekNavy.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Triple Click Power Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SleekGrayCard),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .border(1.dp, SleekBorder, RoundedCornerShape(24.dp))
                            .clickable {
                                viewModel.secretTapDetector.registerTap()
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(SleekMutedText, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    text = "Triple Tap",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekOnBg
                                )
                                Text(
                                    text = "Discreet SOS",
                                    fontSize = 11.sp,
                                    color = SleekMutedText
                                )
                            }
                        }
                    }
                }
            }

            // Trusted Contacts Summary Pill Row
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekPillBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Overlapping avatars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-10).dp)
                    ) {
                        val avatarColors = listOf(Color(0xFF3B5BA9), Color(0xFF006A6A), Color(0xFF7C5800))
                        contacts.take(3).forEachIndexed { index, contact ->
                            val color = avatarColors.getOrElse(index) { Color(0xFF3B5BA9) }
                            val initials = contact.name.take(2).uppercase()
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(2.dp, SleekPillBg, CircleShape)
                                    .background(color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        if (contacts.size > 3) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(2.dp, SleekPillBg, CircleShape)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${contacts.size - 3}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekMutedText
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${contacts.size} Trusted Contacts",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekOnBg
                        )
                        Text(
                            text = "Linked to Emergency SMS",
                            fontSize = 11.sp,
                            color = SleekMutedText
                        )
                    }
                }
            }

            // ML Context Risk Analysis Engine Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = BeaconCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ML Intent & Risk Engine",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (liveMlAnalysis.riskScore > 70) SignalRed.copy(alpha = 0.2f) else BeaconCyan.copy(alpha = 0.2f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${liveMlAnalysis.confidence} (${liveMlAnalysis.riskScore}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (liveMlAnalysis.riskScore > 70) SignalRed else BeaconCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { liveMlAnalysis.riskScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (liveMlAnalysis.riskScore > 70) SignalRed else BeaconCyan,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Classification: ${liveMlAnalysis.intentClassification}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = liveMlAnalysis.recommendation,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Quick Trigger Simulator Panel
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DISCREET TRIGGER TEST HUB",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Shake Simulator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = WarningAmber)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Shake Motion Detector",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Live Accel: ${"%.1f".format(shakeIntensity)} m/s² (Target: ${settings.shakeThreshold})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.triggerEmergencyAlert("Shake Detected", intensity = settings.shakeThreshold + 2f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Simulate Shake", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = BeaconCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Voice Monitor",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (voiceListeningActive) {
                                    "Listening for: \"${settings.voiceKeyword}\""
                                } else {
                                    "Enable RECORD_AUDIO permission and voice trigger in settings"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (voiceListeningActive) "LIVE" else "OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (voiceListeningActive) SafeGreen else SignalRed
                        )
                    }

                    if (lastHeardVoicePhrase != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Last heard: \"$lastHeardVoicePhrase\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Voice Keyword Simulator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = BeaconCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Voice Command Trigger",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Keyword: \"${settings.voiceKeyword}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.triggerEmergencyAlert("Voice Keyword (\"${settings.voiceKeyword}\")", intensity = 8f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Say Phrase", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Wearable BLE Disconnect Simulator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = SafeGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Wearable Device Panic",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Smart Watch / BLE Button Signal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.triggerEmergencyAlert("Wearable BLE Disconnect", intensity = 6f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("BLE Trigger", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Real-Time GPS Tracking Card
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
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SignalRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LIVE GPS COORDINATES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "%.5f, %.5f".format(lat, lng),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(googleMapsUrl))
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUrl))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open Live Location in Google Maps",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Recent Alert Dispatch Result Banner
            lastAlert?.let { alert ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (alert.status.contains("SENT")) SafeGreen.copy(alpha = 0.15f) else SignalRed.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (alert.status.contains("SENT")) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (alert.status.contains("SENT")) SafeGreen else SignalRed
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Alert Status: ${alert.status}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Trigger: ${alert.triggerType} • ${alert.recipientCount} Contacts Notified",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        // Emergency SOS Countdown Modal Overlay
        if (countdownSec != null) {
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = SignalRed,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "DISPATCHING SILENT ALERT",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Triggered by: ${countdownType ?: "Emergency Button"}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "$countdownSec",
                            color = SignalRed,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.cancelCountdown() },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("I AM SAFE — CANCEL ALERT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
