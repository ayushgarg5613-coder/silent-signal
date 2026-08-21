package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.StealthCalculatorScreen
import com.example.ui.screens.StealthSettingsScreen
import com.example.ui.theme.BeaconCyan
import com.example.ui.theme.SignalRed
import com.example.ui.theme.SilentSignalTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val requiredPermissions = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.RECORD_AUDIO
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    private fun hasRequiredPermissions(): Boolean = requiredPermissions.all { permission ->
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!hasRequiredPermissions()) {
            permissionLauncher.launch(requiredPermissions)
        }

        setContent {
            val authState by viewModel.authState.collectAsState()
            val stealthActive by viewModel.stealthModeActive.collectAsState()
            val stealthDarkMode by viewModel.stealthDarkModeActive.collectAsState()

            // Global brightness controller for Stealth Dark Privacy Mode
            LaunchedEffect(stealthDarkMode) {
                val layoutParams = window.attributes
                layoutParams.screenBrightness = if (stealthDarkMode) 0.01f else android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = layoutParams
            }

            LaunchedEffect(authState.isAuthenticated) {
                if (authState.isAuthenticated && stealthActive) {
                    viewModel.toggleStealthMode(false)
                }
            }

            SilentSignalTheme(darkTheme = stealthDarkMode) {
                if (!authState.isAuthenticated) {
                    AuthScreen(viewModel = viewModel)
                } else if (stealthActive) {
                    StealthCalculatorScreen(viewModel = viewModel)
                } else {
                    SilentSignalMainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SilentSignalMainApp(viewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val stealthDarkMode by viewModel.stealthDarkModeActive.collectAsState()

    val navItems = listOf(
        NavItem("Shield", Icons.Filled.Shield, Icons.Outlined.Shield),
        NavItem("Circle", Icons.Filled.People, Icons.Outlined.People),
        NavItem("History", Icons.Filled.History, Icons.Outlined.History),
        NavItem("Settings", Icons.Filled.Security, Icons.Outlined.Security)
    )

    val navBgColor = if (stealthDarkMode) Color(0xFF0F172A) else Color.White
    val navSelectedColor = if (stealthDarkMode) BeaconCyan else com.example.ui.theme.SleekNavy
    val navUnselectedColor = if (stealthDarkMode) Color(0xFF64748B) else com.example.ui.theme.SleekMutedText
    val indicatorColor = if (stealthDarkMode) Color(0xFF1E293B) else com.example.ui.theme.SleekCardBlue

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = navBgColor,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = if (isSelected) navSelectedColor else navUnselectedColor
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) navSelectedColor else navUnselectedColor
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = indicatorColor
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Global Stealth Dark Mode Emergency Privacy Banner & Quick Toggle Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (stealthDarkMode) Color(0xFF000000) else Color(0xFFF1F5F9))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (stealthDarkMode) SignalRed.copy(alpha = 0.2f) else BeaconCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (stealthDarkMode) Icons.Default.VisibilityOff else Icons.Default.DarkMode,
                            contentDescription = "Stealth Status",
                            tint = if (stealthDarkMode) SignalRed else com.example.ui.theme.SleekNavy,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (stealthDarkMode) "STEALTH DARK PRIVACY ACTIVE" else "SILENT SIGNAL SHIELD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (stealthDarkMode) SignalRed else com.example.ui.theme.SleekNavy,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (stealthDarkMode) "Display Dimmed (1%) • Confidential Redaction" else "Global Privacy & Emergency Protection",
                            fontSize = 9.sp,
                            color = if (stealthDarkMode) Color.Gray else com.example.ui.theme.SleekMutedText
                        )
                    }
                }

                // Global Stealth Toggle Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (stealthDarkMode) SignalRed else com.example.ui.theme.SleekNavy)
                        .clickable { viewModel.toggleStealthDarkMode(!stealthDarkMode) }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (stealthDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Stealth Dark",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (stealthDarkMode) "NORMAL MODE" else "STEALTH DARK",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> DashboardScreen(viewModel = viewModel)
                    1 -> ContactsScreen(viewModel = viewModel)
                    2 -> HistoryScreen(viewModel = viewModel)
                    3 -> StealthSettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

data class NavItem(
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
